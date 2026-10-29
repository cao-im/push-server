package com.caoim.pushserver.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NotifyRequest {

    @NotNull
    private Long userId;

    @NotBlank
    private String bizType;

    private String bizId;

    @NotBlank
    private String title;

    @NotBlank
    private String body;
}
