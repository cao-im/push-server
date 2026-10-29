package com.caoim.pushserver.provider;

import com.caoim.pushserver.config.PushProperties;
import com.caoim.pushserver.dto.NotifyRequest;
import com.caoim.pushserver.entity.PushDevice;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class UmengSender {

    private static final String SEND_URL = "https://msgapi.umeng.com/api/send";

    private final PushProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    public UmengSender(PushProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public void send(PushDevice device, NotifyRequest request) throws Exception {
        PushProperties.Umeng umeng = properties.getUmeng();
        boolean ios = "ios".equalsIgnoreCase(device.getPlatform());
        String appKey = ios ? umeng.getIosAppKey() : umeng.getAndroidAppKey();
        String secret = ios ? umeng.getIosMasterSecret() : umeng.getAndroidMasterSecret();
        if (appKey == null || appKey.isBlank() || secret == null || secret.isBlank()) {
            throw new IllegalStateException(ios ? "未配置友盟 iOS AppKey 或 Master Secret" : "未配置友盟 Android AppKey 或 Master Secret");
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("appkey", appKey.trim());
        body.put("timestamp", String.valueOf(System.currentTimeMillis()));
        body.put("type", "unicast");
        body.put("device_tokens", device.getToken());
        body.put("production_mode", umeng.isProductionMode() ? "true" : "false");
        body.put("description", request.getBizType());
        body.put("category", 1);
        body.put("payload", ios ? iosPayload(request) : androidPayload(request));
        if (!ios) {
            body.put("channel_properties", channelProperties(umeng));
        }

        String json = objectMapper.writeValueAsString(body);
        String sign = md5("POST" + SEND_URL + json + secret.trim());
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(SEND_URL + "?sign=" + sign))
                .timeout(Duration.ofSeconds(5))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        String responseBody = response.body() == null ? "" : response.body();
        if (response.statusCode() < 200 || response.statusCode() >= 300 || !responseBody.contains("\"SUCCESS\"")) {
            throw new IllegalStateException("友盟返回 " + response.statusCode() + " " + clip(responseBody));
        }
    }

    private Map<String, Object> androidPayload(NotifyRequest request) {
        Map<String, Object> notification = new LinkedHashMap<>();
        notification.put("title", request.getTitle());
        notification.put("text", request.getBody());
        notification.put("after_open", "go_app");
        Map<String, Object> extra = new LinkedHashMap<>();
        extra.put("bizType", request.getBizType());
        extra.put("bizId", request.getBizId() == null ? "" : request.getBizId());
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("display_type", "notification");
        payload.put("body", notification);
        payload.put("extra", extra);
        return payload;
    }

    private Map<String, Object> iosPayload(NotifyRequest request) {
        Map<String, Object> alert = new LinkedHashMap<>();
        alert.put("title", request.getTitle());
        alert.put("body", request.getBody());
        Map<String, Object> aps = new LinkedHashMap<>();
        aps.put("alert", alert);
        aps.put("sound", "default");
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("aps", aps);
        payload.put("bizType", request.getBizType());
        payload.put("bizId", request.getBizId() == null ? "" : request.getBizId());
        return payload;
    }

    private Map<String, Object> channelProperties(PushProperties.Umeng umeng) {
        Map<String, Object> channel = new LinkedHashMap<>();
        channel.put("channel_activity", umeng.getChannelActivity());
        channel.put("main_activity", umeng.getMainActivity());
        channel.put("huawei_channel_importance", "NORMAL");
        putIfPresent(channel, "huawei_channel_category", umeng.getHuaweiCategory());
        putIfPresent(channel, "vivo_category", umeng.getVivoCategory());
        return channel;
    }

    private static void putIfPresent(Map<String, Object> target, String key, String value) {
        if (value != null && !value.isBlank()) {
            target.put(key, value.trim());
        }
    }

    private static String md5(String text) throws Exception {
        byte[] digest = MessageDigest.getInstance("MD5").digest(text.getBytes(StandardCharsets.UTF_8));
        StringBuilder builder = new StringBuilder(digest.length * 2);
        for (byte value : digest) {
            builder.append(String.format("%02x", value));
        }
        return builder.toString();
    }

    private static String clip(String value) {
        String text = value.replaceAll("\\s+", " ").trim();
        return text.length() <= 180 ? text : text.substring(0, 180);
    }
}
