package com.caoim.pushserver.controller;

import com.caoim.pushserver.common.Result;
import com.caoim.pushserver.dto.DeviceRequest;
import com.caoim.pushserver.dto.NotifyRequest;
import com.caoim.pushserver.service.DeviceService;
import com.caoim.pushserver.service.NotifyService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InternalPushController {

    private final DeviceService deviceService;
    private final NotifyService notifyService;

    public InternalPushController(DeviceService deviceService, NotifyService notifyService) {
        this.deviceService = deviceService;
        this.notifyService = notifyService;
    }

    @PostMapping("/internal/device")
    public Result<Void> register(@Valid @RequestBody DeviceRequest request) {
        deviceService.register(request);
        return Result.success();
    }

    @DeleteMapping("/internal/device")
    public Result<Void> disable(@RequestParam Long userId, @RequestParam String deviceId) {
        deviceService.disable(userId, deviceId);
        return Result.success();
    }

    @PostMapping("/internal/notify")
    public Result<Void> notify(@Valid @RequestBody NotifyRequest request) {
        notifyService.notify(request);
        return Result.success();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Void> badRequest(IllegalArgumentException exception) {
        return Result.error(400, exception.getMessage());
    }
}
