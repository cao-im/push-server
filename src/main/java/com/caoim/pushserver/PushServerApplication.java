package com.caoim.pushserver;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.caoim.pushserver.dao")
public class PushServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(PushServerApplication.class, args);
    }
}
