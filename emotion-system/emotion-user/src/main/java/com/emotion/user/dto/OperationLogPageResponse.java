package com.emotion.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OperationLogPageResponse {
    private Long total;
    private Integer page;
    private Integer size;
    private Integer pages;
    private List<OperationLogDTO> records;

    @Data
    public static class OperationLogDTO {
        private Long id;
        private Long userId;
        private String module;
        private String action;
        private String description;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime operationTime;
        private String ipAddress;
        private String detail;
    }
}
