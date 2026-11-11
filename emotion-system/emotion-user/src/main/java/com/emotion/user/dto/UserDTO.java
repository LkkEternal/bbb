package com.emotion.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserDTO {

    private Long userId;

    private String username;

    private String email;

    private String nickname;

    private String avatarUrl;

    private Integer role;

    private Integer status;

    private String locationId;

    private Integer pushEnabled;

    private Integer emailEnabled;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
