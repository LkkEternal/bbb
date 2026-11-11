package com.emotion.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "weather.qweather")
public class QweatherProperties {

    private String apiKey;
    private String apiHost;
}
