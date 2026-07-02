package com.citrus.auth.service.impl;

import com.citrus.auth.dto.LoginDTO;
import com.citrus.auth.dto.RefreshTokenDTO;
import com.citrus.auth.dto.RegisterDTO;
import com.citrus.auth.dto.SmsLoginDTO;
import com.citrus.auth.dto.SmsSendDTO;
import com.citrus.auth.dto.InitProfileDTO;
import com.citrus.auth.dto.WechatBindDTO;
import com.citrus.auth.dto.WechatLoginDTO;
import com.citrus.auth.dto.WechatPhoneLoginDTO;
import com.citrus.auth.entity.RefreshToken;
import com.citrus.auth.util.WechatPhoneUtil;
import com.citrus.auth.entity.User;
import com.citrus.auth.entity.UserRole;
import com.citrus.auth.mapper.RefreshTokenMapper;
import com.citrus.auth.mapper.UserMapper;
import com.citrus.auth.mapper.UserRoleMapper;
import com.citrus.auth.service.AuthService;
import com.citrus.auth.vo.LoginVO;
import com.citrus.auth.vo.RefreshTokenVO;
import com.citrus.auth.vo.SmsSendVO;
import com.citrus.common.constant.Constants;
import com.citrus.common.exception.BizException;
import com.citrus.common.result.ResultCode;
import com.citrus.util.JwtUtil;
import com.citrus.util.SecurityUtil;
import com.citrus.util.SmsCodeUtil;
import com.citrus.annotation.OperationLog;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final RefreshTokenMapper refreshTokenMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;
    private final SecurityUtil securityUtil;
    private final SmsCodeUtil smsCodeUtil;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${wechat.appid:}")
    private String wechatAppId;

    @Value("${wechat.secret:}")
    private String wechatSecret;

    @Override
    @Transactional
    @OperationLog(module = "auth", action = "register", description = "用户注册")
    public LoginVO register(RegisterDTO dto) {
        // 1. 校验验证码（从Redis中验证）
        if (!smsCodeUtil.verifyCode(redisTemplate, "register", dto.getPhone(), dto.getSmsCode())) {
            throw new BizException(ResultCode.SMS_CODE_ERROR);
        }

        // 2. 检查手机号是否已注册
        User existUser = userMapper.selectByPhone(dto.getPhone());
        if (existUser != null) {
            throw new BizException(ResultCode.PHONE_ALREADY_REGISTERED);
        }

        // 3. 创建用户
        User user = new User();
        user.setPhone(dto.getPhone());
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setStatus(Constants.USER_ENABLED);
        user.setSmsEnabled(false);
        user.setPushEnabled(true);
        user.setDeleted(false);
        // TODO: 获取客户端IP地址并设置 user.setLastLoginIp(ip)，可参考 OperationLogAspect.getClientIp()
        userMapper.insert(user);

        // 4. 分配默认角色（ROLE_FARMER）
        UserRole userRole = new UserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(1); // ROLE_FARMER 的 ID 为 1
        userRoleMapper.insert(userRole);

        // 5. 生成Token
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername());
        String refreshTokenId = generateAndSaveRefreshToken(user.getId());

        // 6. 存储AccessToken到Redis（用于退出登录时失效）
        saveAccessTokenToRedis(user.getId(), accessToken);

        // 7. 返回响应
        return LoginVO.builder()
                .id(user.getId())
                .phone(user.getPhone())
                .username(user.getUsername())
                .accessToken(accessToken)
                .refreshTokenId(refreshTokenId)
                .expiresIn(jwtUtil.getAccessTokenExpireSeconds())
                .roles(userRoleMapper.selectRolesByUserId(user.getId()))
                .build();
    }

    @Override
    @OperationLog(module = "auth", action = "login", description = "密码登录")
    public LoginVO loginByPassword(LoginDTO dto) {
        // 1. 查询用户
        User user = userMapper.selectByPhone(dto.getPhone());
        if (user == null) {
            throw new BizException(ResultCode.PHONE_NOT_REGISTERED);
        }

        // 2. 检查用户状态
        if (user.getStatus() == Constants.USER_DISABLED) {
            throw new BizException(ResultCode.ACCOUNT_DISABLED);
        }

        // 3. 验证密码
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BizException(ResultCode.PASSWORD_ERROR);
        }

        // 4. 生成Token
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername());
        String refreshTokenId = generateAndSaveRefreshToken(user.getId());

        // 5. 存储AccessToken到Redis（用于退出登录时失效）
        saveAccessTokenToRedis(user.getId(), accessToken);

        // TODO: 获取客户端IP地址，更新 user.setLastLoginIp(ip) 并保存到数据库

        // 6. 返回响应
        return LoginVO.builder()
                .id(user.getId())
                .phone(user.getPhone())
                .username(user.getUsername())
                .avatarUrl(user.getAvatarUrl())
                .accessToken(accessToken)
                .refreshTokenId(refreshTokenId)
                .expiresIn(jwtUtil.getAccessTokenExpireSeconds())
                .roles(userRoleMapper.selectRolesByUserId(user.getId()))
                .build();
    }

    @Override
    @OperationLog(module = "auth", action = "login", description = "短信验证码登录")
    public LoginVO loginBySms(SmsLoginDTO dto) {
        // 1. 校验验证码（从Redis中验证）
        if (!smsCodeUtil.verifyCode(redisTemplate, "login", dto.getPhone(), dto.getSmsCode())) {
            throw new BizException(ResultCode.SMS_CODE_ERROR);
        }

        // 2. 查询用户
        User user = userMapper.selectByPhone(dto.getPhone());
        if (user == null) {
            throw new BizException(ResultCode.PHONE_NOT_REGISTERED);
        }

        // 3. 检查用户状态
        if (user.getStatus() == Constants.USER_DISABLED) {
            throw new BizException(ResultCode.ACCOUNT_DISABLED);
        }

        // 4. 生成Token
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername());
        String refreshTokenId = generateAndSaveRefreshToken(user.getId());

        // 5. 存储AccessToken到Redis（用于退出登录时失效）
        saveAccessTokenToRedis(user.getId(), accessToken);

        // TODO: 获取客户端IP地址，更新 user.setLastLoginIp(ip) 并保存到数据库

        // 6. 返回响应
        return LoginVO.builder()
                .id(user.getId())
                .phone(user.getPhone())
                .username(user.getUsername())
                .avatarUrl(user.getAvatarUrl())
                .accessToken(accessToken)
                .refreshTokenId(refreshTokenId)
                .expiresIn(jwtUtil.getAccessTokenExpireSeconds())
                .roles(userRoleMapper.selectRolesByUserId(user.getId()))
                .build();
    }

    @Override
    public RefreshTokenVO refreshToken(RefreshTokenDTO dto) {
        // 1. 查询RefreshToken
        RefreshToken refreshToken = refreshTokenMapper.selectByTokenId(dto.getRefreshTokenId());
        if (refreshToken == null) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }

        // 2. 验证是否已吊销
        if (refreshToken.getRevoked()) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }

        // 3. 验证是否过期
        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }

        // 4. 查询用户
        User user = userMapper.selectById(refreshToken.getUserId());
        if (user == null) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }

        // 5. 检查用户状态
        if (user.getStatus() == Constants.USER_DISABLED) {
            throw new BizException(ResultCode.ACCOUNT_DISABLED);
        }

        // 6. 生成新的AccessToken
        String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername());

        // 7. 返回响应（RefreshTokenId不变）
        return RefreshTokenVO.builder()
                .accessToken(newAccessToken)
                .refreshTokenId(refreshToken.getTokenId())
                .expiresIn(jwtUtil.getAccessTokenExpireSeconds())
                .build();
    }

    @Override
    @OperationLog(module = "auth", action = "logout", description = "退出登录")
    public void logout(RefreshTokenDTO dto) {
        // 1. 查询 RefreshToken
        RefreshToken refreshToken = refreshTokenMapper.selectByTokenId(dto.getRefreshTokenId());
        if (refreshToken == null) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }

        // 2. 验证是否已吊销（防止重复退出）
        if (refreshToken.getRevoked()) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }

        // 3. 标记为已吊销
        refreshToken.setRevoked(true);
        refreshTokenMapper.updateById(refreshToken);

        // 4. 从 Redis 中删除 RefreshToken
        String refreshTokenKey = Constants.REDIS_REFRESH_TOKEN + dto.getRefreshTokenId();
        redisTemplate.delete(refreshTokenKey);

        // 5. 从 Redis 中删除 AccessToken（使当前登录失效）
        Long userId = refreshToken.getUserId();
        if (userId != null) {
            String accessTokenKey = Constants.REDIS_USER_TOKEN + userId;
            redisTemplate.delete(accessTokenKey);
        }

        // 6. 更新用户表的 lastLoginAt 字段
        if (userId != null) {
            User user = userMapper.selectById(userId);
            if (user != null) {
                user.setLastLoginAt(LocalDateTime.now());
                userMapper.updateById(user);
            }
        }

        log.info("用户退出登录成功: userId={}, refreshTokenId={}", userId, dto.getRefreshTokenId());
    }

    @Override
    public SmsSendVO sendSmsCode(SmsSendDTO dto) {
        // 1. 检查发送频率（60秒内只能发送一次）
        if (smsCodeUtil.isRateLimited(redisTemplate, dto.getPhone())) {
            throw new BizException(ResultCode.SMS_SEND_FREQUENT);
        }

        // 2. 检查每日发送上限（每天最多10条）
        if (smsCodeUtil.isDailyLimitReached(redisTemplate, dto.getPhone())) {
            throw new BizException(ResultCode.SMS_DAILY_LIMIT_EXCEEDED);
        }

        // 3. 根据场景校验手机号
        validatePhoneByScene(dto.getPhone(), dto.getScene());

        // 4. 生成验证码
        String code = smsCodeUtil.generateCode(Constants.SMS_CODE_LENGTH);

        // 5. 存储到Redis（5分钟过期）
        smsCodeUtil.saveCode(redisTemplate, dto.getScene(), dto.getPhone(),
                code, Constants.SMS_CODE_EXPIRE_MINUTES);

        // 6. 设置发送频率限制（60秒）
        smsCodeUtil.setRateLimit(redisTemplate, dto.getPhone());

        // 7. 增加每日发送次数
        smsCodeUtil.incrementDailyCount(redisTemplate, dto.getPhone());

        // 8. 发送验证码（通过阿里云短信）
        smsCodeUtil.sendCode(dto.getPhone(), code, Constants.SMS_CODE_EXPIRE_MINUTES);

        // 9. 返回响应
        return SmsSendVO.builder()
                .expireSeconds(Constants.SMS_CODE_EXPIRE_MINUTES * 60)
                .build();
    }

    @Override
    @OperationLog(module = "auth", action = "login", description = "微信小程序登录")
    public LoginVO loginByWechat(WechatLoginDTO dto) {
        // 1. 调用微信API获取openid
        String openid = getWechatOpenid(dto.getCode());

        // 2. 根据openid查找用户
        User user = userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getWechatOpenid, openid)
        );

        // 3. 已绑定 → 正常登录
        if (user != null) {
            if (user.getStatus() == Constants.USER_DISABLED) {
                throw new BizException(ResultCode.ACCOUNT_DISABLED);
            }

            String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername());
            String refreshTokenId = generateAndSaveRefreshToken(user.getId());
            saveAccessTokenToRedis(user.getId(), accessToken);

            return LoginVO.builder()
                    .id(user.getId())
                    .phone(user.getPhone())
                    .username(user.getUsername())
                    .avatarUrl(user.getAvatarUrl())
                    .accessToken(accessToken)
                    .refreshTokenId(refreshTokenId)
                    .expiresIn(jwtUtil.getAccessTokenExpireSeconds())
                    .roles(userRoleMapper.selectRolesByUserId(user.getId()))
                    .isNewUser(false)
                    .build();
        }

        // 4. 未绑定 → 返回openid让前端引导绑定/注册
        log.info("微信登录未绑定用户: openid={}", openid);
        return LoginVO.builder()
                .wechatOpenid(openid)
                .needsBind(true)
                .isNewUser(true)
                .build();
    }

    @Override
    @Transactional
    @OperationLog(module = "auth", action = "register", description = "微信新用户注册")
    public LoginVO wechatRegister(WechatLoginDTO dto) {
        String openid = getWechatOpenid(dto.getCode());

        // 防止重复注册
        User exist = userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getWechatOpenid, openid)
        );
        if (exist != null) {
            throw new BizException(ResultCode.WECHAT_ALREADY_BOUND);
        }

        // 创建新用户
        User user = new User();
        user.setWechatOpenid(openid);
        user.setUsername("微信用户" + openid.substring(0, 6));
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setStatus(Constants.USER_ENABLED);
        user.setSmsEnabled(false);
        user.setPushEnabled(true);
        user.setDeleted(false);
        userMapper.insert(user);

        UserRole userRole = new UserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(1);
        userRoleMapper.insert(userRole);

        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername());
        String refreshTokenId = generateAndSaveRefreshToken(user.getId());
        saveAccessTokenToRedis(user.getId(), accessToken);

        log.info("微信新用户注册成功: userId={}, openid={}", user.getId(), openid);

        return LoginVO.builder()
                .id(user.getId())
                .phone(user.getPhone())
                .username(user.getUsername())
                .avatarUrl(user.getAvatarUrl())
                .accessToken(accessToken)
                .refreshTokenId(refreshTokenId)
                .expiresIn(jwtUtil.getAccessTokenExpireSeconds())
                .roles(userRoleMapper.selectRolesByUserId(user.getId()))
                .isNewUser(true)
                .build();
    }

    @Override
    @Transactional
    @OperationLog(module = "auth", action = "bind", description = "微信绑定已有账号")
    public LoginVO bindWechat(WechatBindDTO dto) {
        // 1. 校验验证码（scene=bind_wechat）
        if (!smsCodeUtil.verifyCode(redisTemplate, "bind_wechat", dto.getPhone(), dto.getSmsCode())) {
            throw new BizException(ResultCode.SMS_CODE_ERROR);
        }

        // 2. 按手机号查找已有用户
        User user = userMapper.selectByPhone(dto.getPhone());
        if (user == null) {
            throw new BizException(ResultCode.PHONE_NOT_REGISTERED);
        }

        // 3. 检查用户状态
        if (user.getStatus() == Constants.USER_DISABLED) {
            throw new BizException(ResultCode.ACCOUNT_DISABLED);
        }

        // 4. 检查该openid是否已被其他账号绑定
        User boundUser = userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getWechatOpenid, dto.getOpenid())
        );
        if (boundUser != null && !boundUser.getId().equals(user.getId())) {
            throw new BizException(ResultCode.WECHAT_ALREADY_BOUND);
        }

        // 5. 绑定微信openid到已有用户
        user.setWechatOpenid(dto.getOpenid());
        userMapper.updateById(user);

        // 6. 生成Token
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername());
        String refreshTokenId = generateAndSaveRefreshToken(user.getId());
        saveAccessTokenToRedis(user.getId(), accessToken);

        log.info("微信绑定成功: userId={}, openid={}", user.getId(), dto.getOpenid());

        return LoginVO.builder()
                .id(user.getId())
                .phone(user.getPhone())
                .username(user.getUsername())
                .avatarUrl(user.getAvatarUrl())
                .accessToken(accessToken)
                .refreshTokenId(refreshTokenId)
                .expiresIn(jwtUtil.getAccessTokenExpireSeconds())
                .roles(userRoleMapper.selectRolesByUserId(user.getId()))
                .isNewUser(false)
                .build();
    }

    @Override
    @Transactional
    @OperationLog(module = "auth", action = "login", description = "微信手机号一键登录")
    public LoginVO loginByWechatPhone(WechatPhoneLoginDTO dto) {
        // 1. 调用微信API获取openid + session_key
        JsonNode wxResult = getWechatSession(dto.getCode());
        String openid = wxResult.get("openid").asText();
        String sessionKey = wxResult.get("session_key").asText();

        // 2. 解密手机号
        String phone = WechatPhoneUtil.decryptPhoneNumber(sessionKey, dto.getEncryptedData(), dto.getIv());
        log.info("微信手机号登录: openid={}, phone={}", openid, phone);

        // 3. 按手机号查找用户
        User user = userMapper.selectByPhone(phone);

        if (user != null) {
            // 已有账号 → 更新openid（如果尚未绑定）
            if (user.getWechatOpenid() == null || user.getWechatOpenid().isEmpty()) {
                user.setWechatOpenid(openid);
                userMapper.updateById(user);
                log.info("微信绑定已有账号: userId={}, phone={}, openid={}", user.getId(), phone, openid);
            }

            if (user.getStatus() == Constants.USER_DISABLED) {
                throw new BizException(ResultCode.ACCOUNT_DISABLED);
            }

            String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername());
            String refreshTokenId = generateAndSaveRefreshToken(user.getId());
            saveAccessTokenToRedis(user.getId(), accessToken);

            return LoginVO.builder()
                    .id(user.getId())
                    .phone(user.getPhone())
                    .username(user.getUsername())
                    .avatarUrl(user.getAvatarUrl())
                    .accessToken(accessToken)
                    .refreshTokenId(refreshTokenId)
                    .expiresIn(jwtUtil.getAccessTokenExpireSeconds())
                    .roles(userRoleMapper.selectRolesByUserId(user.getId()))
                    .isNewUser(false)
                    .build();
        }

        // 4. 手机号未注册 → 自动创建新用户
        user = new User();
        user.setPhone(phone);
        user.setWechatOpenid(openid);
        user.setUsername("微信用户" + phone.substring(phone.length() - 4));
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setStatus(Constants.USER_ENABLED);
        user.setSmsEnabled(false);
        user.setPushEnabled(true);
        user.setDeleted(false);
        userMapper.insert(user);

        UserRole userRole = new UserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(1);
        userRoleMapper.insert(userRole);

        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername());
        String refreshTokenId = generateAndSaveRefreshToken(user.getId());
        saveAccessTokenToRedis(user.getId(), accessToken);

        log.info("微信手机号新用户注册成功: userId={}, phone={}, openid={}", user.getId(), phone, openid);

        return LoginVO.builder()
                .id(user.getId())
                .phone(user.getPhone())
                .username(user.getUsername())
                .avatarUrl(user.getAvatarUrl())
                .accessToken(accessToken)
                .refreshTokenId(refreshTokenId)
                .expiresIn(jwtUtil.getAccessTokenExpireSeconds())
                .roles(userRoleMapper.selectRolesByUserId(user.getId()))
                .isNewUser(true)
                .build();
    }

    @Override
    @Transactional
    @OperationLog(module = "auth", action = "init-profile", description = "首次设置用户名和密码")
    public void initProfile(InitProfileDTO dto) {
        Long userId = securityUtil.getCurrentUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }

        // 仅允许尚未设置用户名的微信用户修改
        if (user.getUsername() != null && !user.getUsername().startsWith("微信用户")) {
            throw new BizException(ResultCode.UNPROCESSABLE, "用户名已设置，不可修改");
        }

        // 检查用户名是否已被占用
        User exist = userMapper.selectByUsername(dto.getUsername());
        if (exist != null && !exist.getId().equals(userId)) {
            throw new BizException(ResultCode.UNPROCESSABLE, "用户名已被占用");
        }

        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        userMapper.updateById(user);

        log.info("用户首次设置信息成功: userId={}, username={}", userId, dto.getUsername());
    }

    /**
     * 调用微信API获取openid + session_key
     */
    private JsonNode getWechatSession(String code) {
        if (wechatAppId == null || wechatAppId.isEmpty() || wechatSecret == null || wechatSecret.isEmpty()) {
            log.error("微信小程序配置未设置: appid={}, secret={}", wechatAppId, wechatSecret);
            throw new BizException(ResultCode.WECHAT_CONFIG_ERROR);
        }

        String url = String.format("%s?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                Constants.WECHAT_LOGIN_URL, wechatAppId, wechatSecret, code);

        try {
            String response = restTemplate.getForObject(url, String.class);
            JsonNode jsonNode = objectMapper.readTree(response);

            if (jsonNode.has("errcode") && jsonNode.get("errcode").asInt() != 0) {
                String errMsg = jsonNode.has("errmsg") ? jsonNode.get("errmsg").asText() : "未知错误";
                log.error("微信登录失败: errcode={}, errmsg={}", jsonNode.get("errcode").asInt(), errMsg);
                throw new BizException(ResultCode.WECHAT_LOGIN_FAILED);
            }

            if (!jsonNode.has("openid") || !jsonNode.has("session_key")) {
                log.error("微信登录响应缺少openid或session_key: {}", response);
                throw new BizException(ResultCode.WECHAT_LOGIN_FAILED);
            }

            return jsonNode;
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用微信API异常: {}", e.getMessage(), e);
            throw new BizException(ResultCode.WECHAT_LOGIN_FAILED);
        }
    }

    /**
     * 调用微信API获取openid
     */
    private String getWechatOpenid(String code) {
        if (wechatAppId == null || wechatAppId.isEmpty() || wechatSecret == null || wechatSecret.isEmpty()) {
            log.error("微信小程序配置未设置: appid={}, secret={}", wechatAppId, wechatSecret);
            throw new BizException(ResultCode.WECHAT_CONFIG_ERROR);
        }

        String url = String.format("%s?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                Constants.WECHAT_LOGIN_URL, wechatAppId, wechatSecret, code);

        try {
            String response = restTemplate.getForObject(url, String.class);
            JsonNode jsonNode = objectMapper.readTree(response);

            if (jsonNode.has("errcode") && jsonNode.get("errcode").asInt() != 0) {
                String errMsg = jsonNode.has("errmsg") ? jsonNode.get("errmsg").asText() : "未知错误";
                log.error("微信登录失败: errcode={}, errmsg={}", jsonNode.get("errcode").asInt(), errMsg);
                throw new BizException(ResultCode.WECHAT_LOGIN_FAILED);
            }

            if (!jsonNode.has("openid")) {
                log.error("微信登录响应缺少openid: {}", response);
                throw new BizException(ResultCode.WECHAT_LOGIN_FAILED);
            }

            return jsonNode.get("openid").asText();
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用微信API异常: {}", e.getMessage(), e);
            throw new BizException(ResultCode.WECHAT_LOGIN_FAILED);
        }
    }

    /**
     * 根据场景校验手机号
     * - 注册场景：手机号不能已注册
     * - 登录/重置密码场景：手机号必须已注册
     */
    private void validatePhoneByScene(String phone, String scene) {
        User existUser = userMapper.selectByPhone(phone);

        if ("register".equals(scene)) {
            // 注册场景：手机号不能已注册
            if (existUser != null) {
                throw new BizException(ResultCode.PHONE_ALREADY_REGISTERED);
            }
        } else if ("login".equals(scene) || "reset_password".equals(scene) || "bind_wechat".equals(scene)) {
            // 登录/重置密码场景：手机号必须已注册
            if (existUser == null) {
                throw new BizException(ResultCode.PHONE_NOT_REGISTERED);
            }
        }
    }

    /**
     * 生成并保存RefreshToken
     */
    private String generateAndSaveRefreshToken(Long userId) {
        String tokenId = "rt_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        String tokenHash = UUID.randomUUID().toString(); // 简化处理，实际应使用SHA256

        // 保存到数据库
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setTokenId(tokenId);
        refreshToken.setUserId(userId);
        refreshToken.setTokenHash(tokenHash);
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(Constants.REFRESH_TOKEN_EXPIRE_DAYS));
        refreshToken.setRevoked(false);
        refreshTokenMapper.insert(refreshToken);

        // 保存到Redis（用于快速校验）
        String redisKey = Constants.REDIS_REFRESH_TOKEN + tokenId;
        redisTemplate.opsForValue().set(redisKey, String.valueOf(userId),
                Constants.REFRESH_TOKEN_EXPIRE_DAYS, TimeUnit.DAYS);

        return tokenId;
    }

    /**
     * 保存AccessToken到Redis
     * 用于退出登录时使AccessToken失效
     */
    private void saveAccessTokenToRedis(Long userId, String accessToken) {
        String redisKey = Constants.REDIS_USER_TOKEN + userId;
        redisTemplate.opsForValue().set(redisKey, accessToken,
                Constants.ACCESS_TOKEN_EXPIRE_HOURS, TimeUnit.HOURS);
    }
}
