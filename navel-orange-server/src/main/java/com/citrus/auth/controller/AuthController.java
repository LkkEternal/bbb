package com.citrus.auth.controller;

import com.citrus.auth.dto.LoginDTO;
import com.citrus.auth.dto.RefreshTokenDTO;
import com.citrus.auth.dto.RegisterDTO;
import com.citrus.auth.dto.SmsLoginDTO;
import com.citrus.auth.dto.InitProfileDTO;
import com.citrus.auth.dto.SmsSendDTO;
import com.citrus.auth.dto.WechatBindDTO;
import com.citrus.auth.dto.WechatLoginDTO;
import com.citrus.auth.dto.WechatPhoneLoginDTO;
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

    /**
     * 微信小程序登录
     */
    @Operation(summary = "微信小程序登录", description = "code换取openid，已绑定直接登录，未绑定返回needsBind引导")
    @PostMapping("/login/wechat")
    public Result<LoginVO> loginByWechat(@Valid @RequestBody WechatLoginDTO dto) {
        LoginVO loginVO = authService.loginByWechat(dto);
        return Result.success("登录成功", loginVO);
    }

    /**
     * 微信新用户注册
     */
    @Operation(summary = "微信新用户注册", description = "使用微信openid创建新账号（跳过绑定）")
    @PostMapping("/wechat/register")
    public Result<LoginVO> wechatRegister(@Valid @RequestBody WechatLoginDTO dto) {
        LoginVO loginVO = authService.wechatRegister(dto);
        return Result.success("注册成功", loginVO);
    }

    /**
     * 微信绑定已有账号
     */
    @Operation(summary = "微信绑定已有账号", description = "手机号+验证码验证后，将微信openid绑定到已有账号")
    @PostMapping("/wechat/bind")
    public Result<LoginVO> bindWechat(@Valid @RequestBody WechatBindDTO dto) {
        LoginVO loginVO = authService.bindWechat(dto);
        return Result.success("绑定成功", loginVO);
    }

    /**
     * 微信手机号一键登录
     */
    @Operation(summary = "微信手机号一键登录", description = "wx.getPhoneNumber授权后解密手机号，已注册直接登录，未注册自动创建")
    @PostMapping("/login/wechat/phone")
    public Result<LoginVO> loginByWechatPhone(@Valid @RequestBody WechatPhoneLoginDTO dto) {
        LoginVO loginVO = authService.loginByWechatPhone(dto);
        return Result.success("登录成功", loginVO);
    }

    /**
     * 首次设置用户名和密码
     */
    @Operation(summary = "首次设置用户名和密码", description = "微信新用户首次设置用户名和密码（限username以微信用户开头）")
    @PutMapping("/user/init-profile")
    public Result<Void> initProfile(@Valid @RequestBody InitProfileDTO dto) {
        authService.initProfile(dto);
        return Result.success();
    }
}
