package com.emotion.user.service.impl;

import com.emotion.common.exception.BizException;
import com.emotion.common.response.ErrorCode;
import com.emotion.infrastructure.client.AmapClient;
import com.emotion.infrastructure.security.JwtUtil;
import com.emotion.user.dto.*;
import com.emotion.user.entity.RefreshToken;
import com.emotion.user.entity.User;
import com.emotion.user.mapper.RefreshTokenMapper;
import com.emotion.user.mapper.UserMapper;
import com.emotion.user.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HexFormat;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务实现类
 * 负责用户认证的核心业务逻辑：
 * - 用户注册
 * - 用户登录（包含密码验证和防暴力破解）
 * - Token 刷新
 * - 退出登录
 * - 地址解析（高德地图）
 *
 * 安全机制：
 * - 密码使用BCrypt加密存储
 * - 登录失败5次后锁定账号30分钟
 * - JWT Token包含用户ID、用户名、角色
 * - 刷新令牌使用SHA-256哈希存储
 *
 * @author Emotion Team
 * @since 2025-07-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;                 // 用户数据访问对象
    private final RefreshTokenMapper refreshTokenMapper; // 刷新令牌数据访问对象
    private final JwtUtil jwtUtil;                       // JWT工具类
    private final PasswordEncoder passwordEncoder;       // 密码加密器
    private final StringRedisTemplate redisTemplate;     // Redis模板
    private final AmapClient amapClient;                 // 高德地图客户端

    /** Redis 刷新令牌键前缀 */
    private static final String REFRESH_TOKEN_KEY_PREFIX = "refresh:";
    /** 最大登录失败次数（触发账号锁定） */
    private static final int MAX_LOGIN_FAIL_COUNT = 5;
    /** 账号锁定时间（分钟） */
    private static final int LOCK_MINUTES = 30;

    /**
     * 用户注册
     *
     * 流程说明：
     * 1. 验证用户名和邮箱是否已被注册
     * 2. 加密密码（BCrypt）
     * 3. 解析地址获取城市ID、经纬度
     * 4. 生成默认昵称（用户名后4位）
     * 5. 设置默认角色（1=普通用户）
     * 6. 设置默认状态（1=启用）
     * 7. 清除登录失败计数
     * 8. 保存用户信息
     * 9. 设置认证上下文
     *
     * @param request 注册请求，包含用户名、邮箱、密码、昵称、地址
     * @return 注册响应，包含用户ID、用户名、邮箱、昵称
     * @throws BizException 如果用户名或邮箱已存在、地址解析失败
     */
    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String username = request.getUsername();
        String email = request.getEmail();

        // 1. 验证用户名是否已存在
        if (userMapper.selectByUsernameOrEmail(username) != null) {
            throw new BizException(ErrorCode.USERNAME_EXISTS.getCode(), "手机号已注册");
        }
        // 2. 验证邮箱是否已存在
        if (userMapper.selectByUsernameOrEmail(email) != null) {
            throw new BizException(ErrorCode.EMAIL_EXISTS.getCode(), "邮箱已注册");
        }

        // 3. 创建用户对象
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname() != null && !request.getNickname().isBlank()
                ? request.getNickname()
                : "用户_" + username.substring(Math.max(0, username.length() - 4)));

        // 4. 解析地址（自动获取城市ID、经纬度）
        try {
            resolveAddress(user, request.getAddress());
        } catch (Exception e) {
            log.warn("地址解析失败，回退存储原始地址: {}", e.getMessage());
            user.setCityName(request.getAddress());
        }

        // 5. 设置默认值
        user.setRole(1);  // 默认为普通用户
        user.setStatus(1); // 默认启用
        user.setLoginFailCount(0); // 重置登录失败计数

        // 6. 保存用户
        userMapper.insert(user);

        // 7. 设置认证上下文
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getId(), null, Collections.emptyList()));

        // 8. 构建响应
        RegisterResponse response = new RegisterResponse();
        response.setUserId(user.getId());
        response.setUsername(username);
        response.setEmail(email);
        response.setNickname(user.getNickname());
        return response;
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        User user = userMapper.selectByUsernameOrEmail(request.getAccount());
        if (user == null) {
            throw new BizException(ErrorCode.PASSWORD_ERROR.getCode(), "账号或密码错误");
        }

        if (user.getStatus() == 0) {
            throw new BizException(ErrorCode.ACCOUNT_DISABLED.getCode(), "账号已被禁用");
        }

        if (user.getLockUntil() != null && user.getLockUntil().isAfter(LocalDateTime.now())) {
            throw new BizException(ErrorCode.ACCOUNT_LOCKED.getCode(), "账号已被锁定，请稍后再试");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            int failCount = user.getLoginFailCount() + 1;
            user.setLoginFailCount(failCount);
            if (failCount >= MAX_LOGIN_FAIL_COUNT) {
                user.setLockUntil(LocalDateTime.now().plusMinutes(LOCK_MINUTES));
                userMapper.updateById(user);
                throw new BizException(ErrorCode.ACCOUNT_LOCKED.getCode(), "登录失败次数过多，账号已锁定30分钟");
            }
            userMapper.updateById(user);
            throw new BizException(ErrorCode.PASSWORD_ERROR.getCode(), "账号或密码错误");
        }

        user.setLoginFailCount(0);
        user.setLockUntil(null);
        user.setLastLoginAt(LocalDateTime.now());
        user.setLastLoginIp(getClientIp(httpRequest));
        userMapper.updateById(user);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getId(), null, Collections.emptyList()));

        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername(), user.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        String tokenId = UUID.randomUUID().toString().replace("-", "");
        String tokenHash = sha256(refreshToken);

        RefreshToken rt = new RefreshToken();
        rt.setTokenId(tokenId);
        rt.setUserId(user.getId());
        rt.setTokenHash(tokenHash);
        rt.setDeviceInfo(httpRequest.getHeader("User-Agent"));
        rt.setExpiresAt(LocalDateTime.now().plusDays(20));
        rt.setRevoked(0);
        rt.setCreatedAt(LocalDateTime.now());
        refreshTokenMapper.insert(rt);

        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_KEY_PREFIX + tokenId,
                String.valueOf(user.getId()),
                20, TimeUnit.DAYS
        );

        LoginResponse response = new LoginResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(tokenId);
        response.setExpiresIn((int) (jwtUtil.getAccessTokenExpiration() / 1000));

        UserDTO userDTO = new UserDTO();
        userDTO.setUserId(user.getId());
        userDTO.setUsername(user.getUsername());
        userDTO.setEmail(user.getEmail());
        userDTO.setNickname(user.getNickname());
        userDTO.setAvatarUrl(user.getAvatarUrl());
        userDTO.setRole(user.getRole());
        userDTO.setStatus(user.getStatus());
        userDTO.setLocationId(user.getLocationId());
        userDTO.setCreatedAt(user.getCreatedAt());
        response.setUser(userDTO);

        return response;
    }

    @Override
    @Transactional
    public LoginResponse refresh(RefreshTokenRequest request) {
        String tokenId = request.getRefreshToken();

        String userIdStr = redisTemplate.opsForValue().get(REFRESH_TOKEN_KEY_PREFIX + tokenId);
        if (userIdStr == null) {
            RefreshToken rt = refreshTokenMapper.selectValidByTokenId(tokenId, LocalDateTime.now());
            if (rt == null) {
                throw new BizException(ErrorCode.UNAUTHORIZED.getCode(), "Refresh Token无效或已过期");
            }
            userIdStr = String.valueOf(rt.getUserId());
        }

        Long userId = Long.parseLong(userIdStr);
        User user = userMapper.selectById(userId);
        if (user == null || user.getStatus() == 0) {
            throw new BizException(ErrorCode.ACCOUNT_DISABLED.getCode(), "账号已被禁用");
        }

        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername(), user.getRole());

        LoginResponse response = new LoginResponse();
        response.setAccessToken(accessToken);
        response.setExpiresIn((int) (jwtUtil.getAccessTokenExpiration() / 1000));
        return response;
    }

    @Override
    @Transactional
    public void logout(HttpServletRequest request) {
        String token = resolveToken(request);
        if (token != null && jwtUtil.validateToken(token)) {
            Long userId = jwtUtil.getUserIdFromToken(token);
            refreshTokenMapper.revokeAllByUserId(userId);

            Set<String> keys = redisTemplate.keys(REFRESH_TOKEN_KEY_PREFIX + "*");
            if (keys != null) {
                for (String key : keys) {
                    String val = redisTemplate.opsForValue().get(key);
                    if (String.valueOf(userId).equals(val)) {
                        redisTemplate.delete(key);
                    }
                }
            }
        }
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private void resolveAddress(User user, String address) {
        if (address == null || address.isBlank()) {
            throw new BizException(ErrorCode.ADDRESS_RESOLVE_FAILED);
        }
        try {
            AmapClient.GeoResult geoResult = amapClient.geo(address);
            if (geoResult == null || geoResult.getGeocodes() == null || geoResult.getGeocodes().isEmpty()) {
                throw new BizException(ErrorCode.ADDRESS_RESOLVE_FAILED);
            }
            AmapClient.Geocode geo = geoResult.getGeocodes().get(0);
            user.setCityName(address);
            user.setLocationId(geo.getAdcode());
            if (geo.getLocation() != null && geo.getLocation().contains(",")) {
                String[] parts = geo.getLocation().split(",");
                user.setLongitude(new BigDecimal(parts[0]));
                user.setLatitude(new BigDecimal(parts[1]));
            }
            log.info("地址解析成功: {} -> adcode={}, location={}",
                    address, geo.getAdcode(), geo.getLocation());
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("地址解析异常: address={}", address, e);
            throw new BizException(ErrorCode.ADDRESS_RESOLVE_FAILED);
        }
    }
}
