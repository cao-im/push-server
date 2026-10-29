package com.caoim.pushserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("push_device")
public class PushDevice {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String platform;
    private String provider;
    private String token;
    private String deviceId;
    private Integer enabled;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
