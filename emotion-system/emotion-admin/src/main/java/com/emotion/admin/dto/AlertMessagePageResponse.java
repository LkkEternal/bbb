package com.emotion.admin.dto;

import lombok.Data;

import java.util.List;

/**
 * 预警消息分页响应
 */
@Data
public class AlertMessagePageResponse {

    private Long total;
    private Integer page;
    private Integer size;
    private Integer pages;
    private List<AlertMessageDTO> records;
}
