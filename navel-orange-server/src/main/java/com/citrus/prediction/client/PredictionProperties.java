package com.citrus.prediction.client;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 风险预测服务配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "prediction")
public class PredictionProperties {

    /** Python 预测服务地址（复用 RAG 服务端口） */
    private String baseUrl = "http://localhost:8001";

    /** HTTP 调用超时（毫秒） */
    private int timeout = 30000;

    /** 连接超时（毫秒） */
    private int connectTimeout = 10000;

    /** 检测上报触发阈值（近1小时内上报次数超过此值触发预测） */
    private int burstThreshold = 10;

    /** 检测上报触发窗口（分钟） */
    private int burstWindowMinutes = 60;

    /** 高风险果园预测间隔（分钟，低于此间隔不重复触发） */
    private int highRiskCooldownMinutes = 360;
}
