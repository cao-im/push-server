package com.caoim.pushserver.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.caoim.pushserver.dao.PushDeviceMapper;
import com.caoim.pushserver.dto.DeviceRequest;
import com.caoim.pushserver.entity.PushDevice;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class DeviceService {

    private static final Set<String> PLATFORMS = Set.of("android", "ios");
    private static final Set<String> PROVIDERS = Set.of(
            "apns", "hms", "mipush", "oppo", "vivo", "honor", "fcm", "jpush", "umeng");

    private final PushDeviceMapper deviceMapper;

    public DeviceService(PushDeviceMapper deviceMapper) {
        this.deviceMapper = deviceMapper;
    }

    public void register(DeviceRequest request) {
        String platform = request.getPlatform().trim().toLowerCase();
        String provider = request.getProvider().trim().toLowerCase();
        String token = request.getToken().trim();
        String deviceId = request.getDeviceId().trim();
        if (!PLATFORMS.contains(platform)) {
            throw new IllegalArgumentException("platform 只能是 android 或 ios");
        }
        if (!PROVIDERS.contains(provider)) {
            throw new IllegalArgumentException("不支持的推送通道: " + provider);
        }
        if (token.length() > 512 || deviceId.length() > 128 || deviceId.isEmpty()) {
            throw new IllegalArgumentException("token 或 deviceId 长度不合法");
        }

        PushDevice existing = deviceMapper.selectOne(new LambdaQueryWrapper<PushDevice>()
                .eq(PushDevice::getUserId, request.getUserId())
                .eq(PushDevice::getDeviceId, deviceId)
                .last("LIMIT 1"));
        LocalDateTime now = LocalDateTime.now();
        if (existing == null) {
            PushDevice created = new PushDevice();
            created.setUserId(request.getUserId());
            created.setPlatform(platform);
            created.setProvider(provider);
            created.setToken(token);
            created.setDeviceId(deviceId);
            created.setEnabled(1);
            created.setCreateTime(now);
            created.setUpdateTime(now);
            deviceMapper.insert(created);
            return;
        }
        existing.setPlatform(platform);
        existing.setProvider(provider);
        existing.setToken(token);
        existing.setEnabled(1);
        existing.setUpdateTime(now);
        deviceMapper.updateById(existing);
    }

    public void disable(Long userId, String deviceId) {
        if (userId == null || deviceId == null || deviceId.isBlank()) {
            throw new IllegalArgumentException("缺少 userId 或 deviceId");
        }
        PushDevice existing = deviceMapper.selectOne(new LambdaQueryWrapper<PushDevice>()
                .eq(PushDevice::getUserId, userId)
                .eq(PushDevice::getDeviceId, deviceId.trim())
                .last("LIMIT 1"));
        if (existing == null) {
            return;
        }
        existing.setEnabled(0);
        existing.setUpdateTime(LocalDateTime.now());
        deviceMapper.updateById(existing);
    }

    public List<PushDevice> listEnabled(Long userId) {
        return deviceMapper.selectList(new LambdaQueryWrapper<PushDevice>()
                .eq(PushDevice::getUserId, userId)
                .eq(PushDevice::getEnabled, 1));
    }
}
