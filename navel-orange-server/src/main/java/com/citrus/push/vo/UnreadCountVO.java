package com.citrus.push.vo;

import lombok.Data;

import java.util.Map;

@Data
public class UnreadCountVO {

    private Long total;
    private Map<String, Long> byLevel;
}
