package com.emotion.infrastructure.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("sys_operation_log")
public class OperationLogEntity implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String module;

    private String action;

    private String resourceType;

    private String resourceId;

    private String description;

    private String detail;

    private String ipAddress;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime operationTime;
}
