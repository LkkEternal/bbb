package com.citrus.auth.controller;

import com.citrus.auth.dto.LoginDTO;
import com.citrus.auth.dto.RefreshTokenDTO;
import com.citrus.auth.dto.RegisterDTO;
import com.citrus.auth.dto.SmsLoginDTO;
import com.citrus.auth.dto.SmsSendDTO;
import com.citrus.auth.service.AuthService;
import com.citrus.auth.vo.LoginVO;
import com.citrus.auth.vo.RefreshTokenVO;
import com.citrus.auth.vo.SmsSendVO;
import com.citrus.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@Tag(name = "用户认证", description = "注册、登录、Token管理")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 手机号注册
     */
    @Operation(summary = "手机号注册", description = "手机号+验证码+密码注册新用户")
    @PostMapping("/register")
    public Result<LoginVO> register(@Valid @RequestBody RegisterDTO dto) {
        LoginVO loginVO = authService.register(dto);
        return Result.success("注册成功", loginVO);
    }

    /**
     * 密码登录
     */
    @Operation(summary = "密码登录", description = "手机号+密码登录")
    @PostMapping("/login/password")
    public Result<LoginVO> loginByPassword(@Valid @RequestBody LoginDTO dto) {
        LoginVO loginVO = authService.loginByPassword(dto);
        return Result.success("登录成功", loginVO);
    }

    /**
     * 短信验证码登录
     */
    @Operation(summary = "短信验证码登录", description = "手机号+验证码登录")
    @PostMapping("/login/sms")
    public Result<LoginVO> loginBySms(@Valid @RequestBody SmsLoginDTO dto) {
        LoginVO loginVO = authService.loginBySms(dto);
        return Result.success("登录成功", loginVO);
    }

    /**
     * 刷新Token
     */
    @Operation(summary = "刷新Token", description = "使用refreshToken刷新accessToken")
    @PostMapping("/refresh")
    public Result<RefreshTokenVO> refreshToken(@Valid @RequestBody RefreshTokenDTO dto) {
        RefreshTokenVO vo = authService.refreshToken(dto);
        return Result.success("刷新成功", vo);
    }

    /**
     * 退出登录
     */
    @Operation(summary = "退出登录", description = "使refreshToken失效")
    @PostMapping("/logout")
    public Result<Void> logout(@Valid @RequestBody RefreshTokenDTO dto) {
        authService.logout(dto);
        return Result.success("退出成功", null);
    }

    /**
     * 发送验证码
     */
    @Operation(summary = "发送验证码", description = "发送短信验证码")
    @PostMapping("/sms/send")
    public Result<SmsSendVO> sendSmsCode(@Valid @RequestBody SmsSendDTO dto) {
        SmsSendVO vo = authService.sendSmsCode(dto);
        return Result.success("验证码已发送", vo);
    }
}
