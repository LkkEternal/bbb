package com.citrus.orchard.vo;

import lombok.Data;

/**
 * 注册设备响应VO
 */
@Data
public class RegisterDeviceVO {

    /**
     * 设备ID
     */
    private Long id;

    /**
     * 设备编码
     */
    private String deviceCode;

    /**
     * 设备API密钥（明文，仅注册时返回一次）
     */
    private String apiKey;

    /**
     * 设备名称
     */
    private String name;
}
