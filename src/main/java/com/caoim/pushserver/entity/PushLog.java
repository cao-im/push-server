package com.caoim.pushserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("push_log")
public class PushLog {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String bizType;
    private String bizId;
    private String title;
    private String body;
    private String provider;
    private String status;
    private String detail;
    private LocalDateTime createTime;
}
