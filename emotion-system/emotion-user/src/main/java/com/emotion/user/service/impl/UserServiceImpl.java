package com.emotion.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.emotion.common.exception.BizException;
import com.emotion.common.response.ErrorCode;
import com.emotion.infrastructure.entity.OperationLogEntity;
import com.emotion.infrastructure.mapper.OperationLogMapper;
import com.emotion.infrastructure.service.QiniuService;
import com.emotion.user.dto.*;
import com.emotion.user.entity.User;
import com.emotion.user.mapper.UserMapper;
import com.emotion.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final OperationLogMapper operationLogMapper;
    private final PasswordEncoder passwordEncoder;
    private final QiniuService qiniuService;
    private final StringRedisTemplate redisTemplate;

    @Override
    public UserPageResponse getUserList(UserPageRequest request) {
        Page<User> page = new Page<>(request.getPage(), request.getSize());
        IPage<User> userPage = userMapper.selectUserPage(page, request.getKeyword());

        UserPageResponse response = new UserPageResponse();
        response.setTotal(userPage.getTotal());
        response.setPage(request.getPage());
        response.setSize(request.getSize());
        response.setPages((int) userPage.getPages());
        response.setRecords(userPage.getRecords().stream().map(this::toUserDTO).collect(Collectors.toList()));
        return response;
    }

    @Override
    public UserDetailResponse getUserDetail(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ErrorCode.USER_NOT_FOUND.getCode(), "用户不存在");
        }
        return toUserDetailResponse(user);
    }

    @Override
    public void updateUserStatus(Long userId, Integer status) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ErrorCode.USER_NOT_FOUND.getCode(), "用户不存在");
        }
        user.setStatus(status);
        userMapper.updateById(user);
    }

    @Override
    public OperationLogPageResponse getOperationLogs(OperationLogPageRequest request) {
        Page<OperationLogEntity> page = new Page<>(request.getPage(), request.getSize());
        LambdaQueryWrapper<OperationLogEntity> wrapper = new LambdaQueryWrapper<>();

        if (request.getAction() != null && !request.getAction().isEmpty()) {
            wrapper.eq(OperationLogEntity::getAction, request.getAction());
        }
        if (request.getModule() != null && !request.getModule().isEmpty()) {
            wrapper.eq(OperationLogEntity::getModule, request.getModule());
        }
        if (request.getStartDate() != null && !request.getStartDate().isEmpty()) {
            wrapper.ge(OperationLogEntity::getOperationTime,
                    LocalDateTime.of(LocalDate.parse(request.getStartDate()), LocalTime.MIN));
        }
        if (request.getEndDate() != null && !request.getEndDate().isEmpty()) {
            wrapper.le(OperationLogEntity::getOperationTime,
                    LocalDateTime.of(LocalDate.parse(request.getEndDate()), LocalTime.MAX));
        }

        wrapper.orderByDesc(OperationLogEntity::getOperationTime);
        IPage<OperationLogEntity> logPage = operationLogMapper.selectPage(page, wrapper);

        OperationLogPageResponse response = new OperationLogPageResponse();
        response.setTotal(logPage.getTotal());
        response.setPage(request.getPage());
        response.setSize(request.getSize());
        response.setPages((int) logPage.getPages());
        response.setRecords(logPage.getRecords().stream()
                .map(this::toOperationLogDTO)
                .collect(Collectors.toList()));
        return response;
    }

    @Override
    public UserDTO getCurrentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ErrorCode.USER_NOT_FOUND.getCode(), "用户不存在");
        }
        return toUserDTO(user);
    }

    @Override
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ErrorCode.USER_NOT_FOUND.getCode(), "用户不存在");
        }
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BizException(ErrorCode.OLD_PASSWORD_ERROR.getCode(), "原密码错误");
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BizException(ErrorCode.NEW_PASSWORD_SAME.getCode(), "新密码不能与原密码相同");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateById(user);
    }

    @Override
    public String uploadAvatar(Long userId, MultipartFile file) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ErrorCode.USER_NOT_FOUND.getCode(), "用户不存在");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.matches("image/(jpeg|jpg|png)")) {
            throw new BizException(ErrorCode.FILE_FORMAT_ERROR.getCode(), "文件格式不支持，仅支持jpg/jpeg/png");
        }
        if (file.getSize() > 2 * 1024 * 1024) {
            throw new BizException(ErrorCode.FILE_TOO_LARGE.getCode(), "文件大小超出限制，最大2MB");
        }

        String activeKey = "avatar:active:" + userId;
        String historyKey = "avatar:history:" + userId;

        String oldUrl = redisTemplate.opsForValue().get(activeKey);
        if (oldUrl != null && !oldUrl.isEmpty()) {
            redisTemplate.opsForSet().add(historyKey, oldUrl);
        }

        String newUrl;
        try {
            newUrl = qiniuService.uploadAvatar(file);
        } catch (Exception e) {
            throw new BizException(ErrorCode.FILE_UPLOAD_FAILED.getCode(), "文件上传失败");
        }

        redisTemplate.opsForValue().set(activeKey, newUrl);
        user.setAvatarUrl(newUrl);
        userMapper.updateById(user);

        return newUrl;
    }

    @Override
    public UserDTO updateNotificationSettings(Long userId, NotificationSettingsRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ErrorCode.USER_NOT_FOUND.getCode(), "用户不存在");
        }
        if (request.getEmailEnabled() != null) {
            user.setEmailEnabled(request.getEmailEnabled() ? 1 : 0);
        }
        if (request.getPushEnabled() != null) {
            user.setPushEnabled(request.getPushEnabled() ? 1 : 0);
        }
        userMapper.updateById(user);
        return toUserDTO(user);
    }

    private UserDTO toUserDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setUserId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setNickname(user.getNickname());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setRole(user.getRole());
        dto.setStatus(user.getStatus());
        dto.setLocationId(user.getLocationId());
        dto.setPushEnabled(user.getPushEnabled());
        dto.setEmailEnabled(user.getEmailEnabled());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }

    private UserDetailResponse toUserDetailResponse(User user) {
        UserDetailResponse resp = new UserDetailResponse();
        resp.setUserId(user.getId());
        resp.setUsername(user.getUsername());
        resp.setEmail(user.getEmail());
        resp.setNickname(user.getNickname());
        resp.setAvatarUrl(user.getAvatarUrl());
        resp.setRole(user.getRole());
        resp.setStatus(user.getStatus());
        resp.setCreatedAt(user.getCreatedAt());
        resp.setUpdatedAt(user.getUpdatedAt());
        resp.setLastLoginAt(user.getLastLoginAt());
        resp.setLastLoginIp(user.getLastLoginIp());
        return resp;
    }

    private OperationLogPageResponse.OperationLogDTO toOperationLogDTO(OperationLogEntity entity) {
        OperationLogPageResponse.OperationLogDTO dto = new OperationLogPageResponse.OperationLogDTO();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUserId());
        dto.setModule(entity.getModule());
        dto.setAction(entity.getAction());
        dto.setDescription(entity.getDescription());
        dto.setOperationTime(entity.getOperationTime());
        dto.setIpAddress(entity.getIpAddress());
        dto.setDetail(entity.getDetail());
        return dto;
    }
}
