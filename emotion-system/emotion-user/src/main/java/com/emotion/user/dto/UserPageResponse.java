package com.emotion.user.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserPageResponse {
    private Long total;
    private Integer page;
    private Integer size;
    private Integer pages;
    private List<UserDTO> records;
}
