package com.emotion.app;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan(basePackages = "com.emotion")
@MapperScan({"com.emotion.infrastructure.mapper", "com.emotion.user.mapper", "com.emotion.weather.mapper", "com.emotion.detection.mapper", "com.emotion.admin.mapper", "com.emotion.chat.mapper"})
@EnableAsync
@EnableScheduling
public class EmotionApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmotionApplication.class, args);
    }
}
