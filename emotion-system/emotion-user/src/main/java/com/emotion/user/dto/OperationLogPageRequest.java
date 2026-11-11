package com.emotion.user.dto;

import lombok.Data;

@Data
public class OperationLogPageRequest {
    private Integer page = 1;
    private Integer size = 20;
    private String action;
    private String module;
    private String startDate;
    private String endDate;
}
