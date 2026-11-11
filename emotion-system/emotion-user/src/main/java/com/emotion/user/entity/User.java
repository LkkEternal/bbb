package com.emotion.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.emotion.common.base.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class User extends BaseEntity {

    private String username;

    private String email;

    private String password;

    private String nickname;

    private String avatarUrl;

    private String cityName;

    private String locationId;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private Integer role;

    private Integer status;

    private Integer loginFailCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lockUntil;

    private String lastLoginIp;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginAt;

    @TableLogic
    private Integer deleted;

    /** 是否接收预警消息：1=接收 0=不接收 */
    private Integer pushEnabled;

    /** 是否开启邮箱推送：1=开启 0=关闭 */
    private Integer emailEnabled;
}
