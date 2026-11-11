package com.emotion.user.service;

import com.emotion.user.dto.*;
import com.emotion.infrastructure.entity.OperationLogEntity;

import java.util.List;

public interface UserService {

    UserPageResponse getUserList(UserPageRequest request);

    UserDetailResponse getUserDetail(Long userId);

    void updateUserStatus(Long userId, Integer status);

    OperationLogPageResponse getOperationLogs(OperationLogPageRequest request);

    UserDTO getCurrentUser(Long userId);

    void changePassword(Long userId, ChangePasswordRequest request);

    String uploadAvatar(Long userId, org.springframework.web.multipart.MultipartFile file);

    UserDTO updateNotificationSettings(Long userId, NotificationSettingsRequest request);
}
