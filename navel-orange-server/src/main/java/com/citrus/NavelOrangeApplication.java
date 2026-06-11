package com.citrus;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 脐橙病虫害监测系统 - 启动类
 */
@SpringBootApplication
@EnableScheduling
@MapperScan("com.citrus.**.mapper")
public class NavelOrangeApplication {

    public static void main(String[] args) {
        SpringApplication.run(NavelOrangeApplication.class, args);
    }
}
