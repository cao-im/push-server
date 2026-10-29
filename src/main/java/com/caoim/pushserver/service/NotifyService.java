package com.caoim.pushserver.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.caoim.pushserver.dao.PushLogMapper;
import com.caoim.pushserver.dto.NotifyRequest;
import com.caoim.pushserver.entity.PushDevice;
import com.caoim.pushserver.entity.PushLog;
import com.caoim.pushserver.provider.PushDelivery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotifyService {

    private static final Logger log = LoggerFactory.getLogger(NotifyService.class);

    private final DeviceService deviceService;
    private final PushLogMapper logMapper;
    private final PushDelivery delivery;

    public NotifyService(DeviceService deviceService, PushLogMapper logMapper, PushDelivery delivery) {
        this.deviceService = deviceService;
        this.logMapper = logMapper;
        this.delivery = delivery;
    }

    public void notify(NotifyRequest request) {
        request.setTitle(clip(request.getTitle(), 40));
        request.setBody(clip(request.getBody(), 80));
        request.setBizType(clip(request.getBizType(), 32));
        request.setBizId(request.getBizId() == null ? "" : clip(request.getBizId(), 64));

        if (alreadySent(request)) {
            write(request, "", "skipped", "两分钟内已通知");
            return;
        }

        List<PushDevice> devices = deviceService.listEnabled(request.getUserId());
        if (devices.isEmpty()) {
            write(request, "", "skipped", "没有可用设备");
            return;
        }
        for (PushDevice device : devices) {
            try {
                String status = delivery.deliver(device, request);
                write(request, device.getProvider(), status, "");
            } catch (Exception e) {
                log.warn("离线通知投递失败: userId={}, provider={}, {}", request.getUserId(), device.getProvider(), e.toString());
                write(request, device.getProvider(), "failed", clip(e.getMessage() == null ? e.toString() : e.getMessage(), 200));
            }
        }
    }

    private boolean alreadySent(NotifyRequest request) {
        if (request.getBizId().isEmpty()) {
            return false;
        }
        Long count = logMapper.selectCount(new LambdaQueryWrapper<PushLog>()
                .eq(PushLog::getUserId, request.getUserId())
                .eq(PushLog::getBizType, request.getBizType())
                .eq(PushLog::getBizId, request.getBizId())
                .in(PushLog::getStatus, "logged", "sent")
                .ge(PushLog::getCreateTime, LocalDateTime.now().minusMinutes(2)));
        return count != null && count > 0;
    }

    private void write(NotifyRequest request, String provider, String status, String detail) {
        PushLog row = new PushLog();
        row.setUserId(request.getUserId());
        row.setBizType(request.getBizType());
        row.setBizId(request.getBizId());
        row.setTitle(request.getTitle());
        row.setBody(request.getBody());
        row.setProvider(provider == null ? "" : provider);
        row.setStatus(status);
        row.setDetail(detail == null ? "" : detail);
        row.setCreateTime(LocalDateTime.now());
        logMapper.insert(row);
    }

    private static String clip(String value, int max) {
        if (value == null) {
            return "";
        }
        String text = value.trim().replaceAll("\\s+", " ");
        if (text.length() <= max) {
            return text;
        }
        return text.substring(0, max);
    }
}
