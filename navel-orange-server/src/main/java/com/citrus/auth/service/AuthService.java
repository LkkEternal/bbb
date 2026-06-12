package com.citrus.auth.service;

import com.citrus.auth.dto.LoginDTO;
import com.citrus.auth.dto.RefreshTokenDTO;
import com.citrus.auth.dto.RegisterDTO;
import com.citrus.auth.dto.SmsLoginDTO;
import com.citrus.auth.dto.SmsSendDTO;
import com.citrus.auth.vo.LoginVO;
import com.citrus.auth.vo.RefreshTokenVO;
import com.citrus.auth.vo.SmsSendVO;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 用户注册
     */
    LoginVO register(RegisterDTO dto);

    /**
     * 密码登录
     */
    LoginVO loginByPassword(LoginDTO dto);

    /**
     * 短信验证码登录
     */
    LoginVO loginBySms(SmsLoginDTO dto);

    /**
     * 刷新Token
     */
    RefreshTokenVO refreshToken(RefreshTokenDTO dto);

    /**
     * 退出登录
     */
    void logout(RefreshTokenDTO dto);

    /**
     * 发送短信验证码
     */
    SmsSendVO sendSmsCode(SmsSendDTO dto);
}
