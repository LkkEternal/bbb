package com.emotion.user.dto;

import lombok.Data;

@Data
public class LoginResponse {

    private String accessToken;

    private String refreshToken;

    private Integer expiresIn;

    private UserDTO user;
}
