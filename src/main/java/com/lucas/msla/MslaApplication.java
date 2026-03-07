package com.lucas.msla;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class MslaApplication {

    public static void main(String[] args) {
        SpringApplication.run(MslaApplication.class, args);
    }
}