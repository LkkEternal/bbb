package com.emotion.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "map.amap")
public class AmapProperties {
    private String apiKey;
    private String apiHost;
}
