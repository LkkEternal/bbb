package com.citrus.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Refresh Token 实体类
 */
@Data
@TableName("sys_refresh_token")
public class RefreshToken {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String tokenId;

    private Long userId;

    private String tokenHash;

    private String deviceInfo;

    private LocalDateTime expiresAt;

    private Boolean revoked;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
