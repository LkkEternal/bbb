package com.emotion.admin.dto;

import lombok.Data;

import java.util.List;

/**
 * 预警摘要响应（首页「全部预警」卡回显）
 */
@Data
public class AlertSummaryResponse {

    private Long total;

    private List<AlertMessageDTO> recent;
}
