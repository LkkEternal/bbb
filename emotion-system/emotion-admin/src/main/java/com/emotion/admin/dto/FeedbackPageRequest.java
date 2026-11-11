package com.emotion.admin.dto;

import lombok.Data;

@Data
public class FeedbackPageRequest {

    private Integer page = 1;
    private Integer size = 20;
    private Integer status;
    private String correctLabel;
}
