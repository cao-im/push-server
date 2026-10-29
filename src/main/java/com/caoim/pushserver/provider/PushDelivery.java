package com.caoim.pushserver.provider;

import com.caoim.pushserver.config.PushProperties;
import com.caoim.pushserver.dto.NotifyRequest;
import com.caoim.pushserver.entity.PushDevice;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class PushDelivery {

    private static final Logger log = LoggerFactory.getLogger(PushDelivery.class);

    private final PushProperties properties;
    private final ObjectMapper objectMapper;
    private final UmengSender umengSender;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(2))
            .build();

    public PushDelivery(PushProperties properties, ObjectMapper objectMapper, UmengSender umengSender) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.umengSender = umengSender;
    }

    /**
     * @return logged、sent 或抛出异常表示失败
     */
    public String deliver(PushDevice device, NotifyRequest request) throws Exception {
        if ("umeng".equalsIgnoreCase(device.getProvider()) && "umeng".equalsIgnoreCase(properties.getDelivery())) {
            umengSender.send(device, request);
            return "sent";
        }
        if ("webhook".equalsIgnoreCase(properties.getDelivery()) && hasWebhook()) {
            postWebhook(device, request);
            return "sent";
        }
        log.info("离线通知已记录: userId={}, provider={}, platform={}, bizType={}, title={}",
                device.getUserId(), device.getProvider(), device.getPlatform(), request.getBizType(), request.getTitle());
        return "logged";
    }

    private boolean hasWebhook() {
        return properties.getWebhookUrl() != null && !properties.getWebhookUrl().isBlank();
    }

    private void postWebhook(PushDevice device, NotifyRequest request) throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("userId", device.getUserId());
        payload.put("platform", device.getPlatform());
        payload.put("provider", device.getProvider());
        payload.put("token", device.getToken());
        payload.put("deviceId", device.getDeviceId());
        payload.put("bizType", request.getBizType());
        payload.put("bizId", request.getBizId() == null ? "" : request.getBizId());
        payload.put("title", request.getTitle());
        payload.put("body", request.getBody());
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(properties.getWebhookUrl().trim()))
                .timeout(Duration.ofSeconds(3))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                .build();
        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("webhook 返回 " + response.statusCode());
        }
    }
}
