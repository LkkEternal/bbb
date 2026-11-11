package com.emotion.chat.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "zhipu")
public class ZhipuProperties {

    private String apiKey;

    private String baseUrl = "https://open.bigmodel.cn/api/paas/v4";

    private String model = "glm-4-flash";
}
