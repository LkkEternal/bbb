package com.citrus.orchard.vo;

import lombok.Data;

/**
 * 创建果园响应
 */
@Data
public class CreateOrchardVO {

    /**
     * 果园ID
     */
    private Long id;

    /**
     * 果园名称
     */
    private String name;

    /**
     * 详细地址
     */
    private String formattedAddress;

    /**
     * 经纬度
     */
    private String location;
}
