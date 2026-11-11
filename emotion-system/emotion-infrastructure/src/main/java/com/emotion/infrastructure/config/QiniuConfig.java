package com.emotion.infrastructure.config;

import com.qiniu.common.Zone;
import com.qiniu.util.Auth;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

@Data
@org.springframework.context.annotation.Configuration
@ConfigurationProperties(prefix = "qiniu")
public class QiniuConfig {

    private String accessKey;
    private String secretKey;
    private String bucket;
    private String domain;
    private String zone;

    @Bean
    public Auth qiniuAuth() {
        return Auth.create(accessKey, secretKey);
    }

    @Bean(name = "qiniuConfiguration")
    public com.qiniu.storage.Configuration qiniuConfiguration() {
        Zone zone;
        switch (this.zone) {
            case "z0": zone = Zone.zone0(); break;
            case "z1": zone = Zone.zone1(); break;
            case "z2": zone = Zone.zone2(); break;
            case "na0": zone = Zone.zoneNa0(); break;
            case "as0": zone = Zone.zoneAs0(); break;
            default: zone = Zone.zone2(); break;
        }
        return new com.qiniu.storage.Configuration(zone);
    }
}
