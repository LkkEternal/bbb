package com.citrus.operationLog.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 操作日志响应VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationLogVO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 操作用户ID
     */
    private Long userId;

    /**
     * 操作用户名
     */
    private String username;

    /**
     * 操作模块
     */
    private String module;

    /**
     * 操作类型
     */
    private String action;

    /**
     * 资源类型
     */
    private String resourceType;

    /**
     * 资源ID
     */
    private String resourceId;

    /**
     * 操作描述
     */
    private String description;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 客户端标识
     */
    private String userAgent;

    /**
     * 操作时间
     */
    private LocalDateTime createdAt;
}
