package com.caoim.pushserver.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeviceRequest {

    @NotNull
    private Long userId;

    @NotBlank
    private String platform;

    @NotBlank
    private String provider;

    @NotBlank
    private String token;

    @NotBlank
    private String deviceId;
}
