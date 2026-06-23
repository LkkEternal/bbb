package com.citrus.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.citrus.auth.entity.Role;
import com.citrus.auth.entity.User;
import com.citrus.auth.mapper.UserMapper;
import com.citrus.auth.mapper.UserRoleMapper;
import com.citrus.auth.entity.UserRole;
import com.citrus.common.constant.Constants;
import com.citrus.common.exception.BizException;
import com.citrus.common.result.PageResult;
import com.citrus.common.result.ResultCode;
import com.citrus.common.util.HuaweiObsClient;
import com.citrus.orchard.mapper.OrchardMapper;
import com.citrus.user.dto.ChangePasswordDTO;
import com.citrus.user.dto.NotificationSettingsDTO;
import com.citrus.user.dto.UserQueryDTO;
import com.citrus.user.service.UserService;
import com.citrus.user.vo.NotificationSettingsVO;
import com.citrus.user.vo.RoleVO;
import com.citrus.user.vo.UserDetailVO;
import com.citrus.user.vo.UserProfileVO;
import com.citrus.user.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final OrchardMapper orchardMapper;
    private final HuaweiObsClient huaweiObsClient;

    /**
     * 允许的头像文件格式
     */
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png");

    /**
     * 头像文件最大大小：2MB
     */
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024;

    @Override
    public PageResult<UserVO> getUserList(UserQueryDTO queryDTO) {
        // 1. 构建查询条件
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("deleted", false);

        // 2. 关键词搜索（手机号或用户名）
        String keyword = queryDTO.getKeyword();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like("phone", keyword)
                    .or()
                    .like("username", keyword)
            );
        }

        // 3. 按创建时间降序排序
        wrapper.orderByDesc("created_at");

        // 4. 分页查询
        Page<User> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());
        Page<User> userPage = userMapper.selectPage(page, wrapper);

        // 5. 批量查询用户角色
        List<Long> userIds = userPage.getRecords().stream()
                .map(User::getId)
                .collect(Collectors.toList());

        Map<Long, List<Role>> userRolesMap = userIds.stream()
                .collect(Collectors.toMap(
                        userId -> userId,
                        userId -> userRoleMapper.selectRolesByUserId(userId)
                ));

        // 6. 组装返回数据
        List<UserVO> userVOList = userPage.getRecords().stream()
                .map(user -> convertToVO(user, userRolesMap.get(user.getId())))
                .collect(Collectors.toList());

        // 7. 返回分页结果
        return PageResult.of(userVOList, userPage.getTotal(),
                queryDTO.getPage(), queryDTO.getSize());
    }

    @Override
    public UserDetailVO getUserById(Long id) {
        // 1. 查询用户
        User user = userMapper.selectById(id);
        if (user == null || Boolean.TRUE.equals(user.getDeleted())) {
            throw new BizException(ResultCode.NOT_FOUND);
        }

        // 2. 查询用户角色
        List<Role> roles = userRoleMapper.selectRolesByUserId(id);

        // 3. 查询关联果园数量
        int orchardCount = orchardMapper.countByUserId(id);

        // 4. 转换为UserDetailVO
        return convertToDetailVO(user, roles, orchardCount);
    }

    @Override
    public void updateUserStatus(Long id, Integer status) {
        // 1. 校验状态值
        if (status != Constants.USER_ENABLED && status != Constants.USER_DISABLED) {
            throw new BizException(ResultCode.BAD_REQUEST);
        }

        // 2. 查询用户
        User user = userMapper.selectById(id);
        if (user == null || Boolean.TRUE.equals(user.getDeleted())) {
            throw new BizException(ResultCode.NOT_FOUND);
        }

        // 3. 更新用户状态
        user.setStatus(status);
        userMapper.updateById(user);

        log.info("更新用户状态成功，userId={}, status={}", id, status);
    }

    @Override
    public UserProfileVO getUserProfile(Long userId) {
        // 1. 查询用户
        User user = userMapper.selectById(userId);
        if (user == null || Boolean.TRUE.equals(user.getDeleted())) {
            throw new BizException(ResultCode.NOT_FOUND);
        }

        // 2. 查询用户角色
        List<Role> roles = userRoleMapper.selectRolesByUserId(userId);

        // 3. 转换为UserProfileVO（phone脱敏）
        return convertToProfileVO(user, roles);
    }

    /**
     * 转换为 UserVO
     */
    private UserVO convertToVO(User user, List<Role> roles) {
        List<RoleVO> roleVOList = null;
        if (roles != null && !roles.isEmpty()) {
            roleVOList = roles.stream()
                    .map(role -> RoleVO.builder()
                            .id(role.getId())
                            .code(role.getCode())
                            .name(role.getName())
                            .build())
                    .collect(Collectors.toList());
        }

        return UserVO.builder()
                .id(user.getId())
                .phone(user.getPhone())
                .username(user.getUsername())
                .avatarUrl(user.getAvatarUrl())
                .roles(roleVOList)
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .lastLoginIp(user.getLastLoginIp())
                .build();
    }

    /**
     * 转换为 UserDetailVO
     */
    private UserDetailVO convertToDetailVO(User user, List<Role> roles, int orchardCount) {
        List<RoleVO> roleVOList = null;
        if (roles != null && !roles.isEmpty()) {
            roleVOList = roles.stream()
                    .map(role -> RoleVO.builder()
                            .id(role.getId())
                            .code(role.getCode())
                            .name(role.getName())
                            .build())
                    .collect(Collectors.toList());
        }

        return UserDetailVO.builder()
                .id(user.getId())
                .phone(user.getPhone())
                .username(user.getUsername())
                .avatarUrl(user.getAvatarUrl())
                .wechatOpenId(user.getWechatOpenid())
                .roles(roleVOList)
                .status(user.getStatus())
                .smsEnabled(user.getSmsEnabled())
                .pushEnabled(user.getPushEnabled())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .lastLoginIp(user.getLastLoginIp())
                .orchardCount(orchardCount)
                .build();
    }

    /**
     * 转换为 UserProfileVO（phone脱敏）
     */
    private UserProfileVO convertToProfileVO(User user, List<Role> roles) {
        List<RoleVO> roleVOList = null;
        if (roles != null && !roles.isEmpty()) {
            roleVOList = roles.stream()
                    .map(role -> RoleVO.builder()
                            .id(role.getId())
                            .code(role.getCode())
                            .name(role.getName())
                            .build())
                    .collect(Collectors.toList());
        }

        return UserProfileVO.builder()
                .id(user.getId())
                .phone(desensitizePhone(user.getPhone()))
                .username(user.getUsername())
                .avatarUrl(user.getAvatarUrl())
                .roles(roleVOList)
                .smsEnabled(user.getSmsEnabled())
                .pushEnabled(user.getPushEnabled())
                .createdAt(user.getCreatedAt())
                .lastLoginIp(user.getLastLoginIp())
                .build();
    }

    @Override
    public void changePassword(Long userId, ChangePasswordDTO dto) {
        // 1. 查询用户
        User user = userMapper.selectById(userId);
        if (user == null || Boolean.TRUE.equals(user.getDeleted())) {
            throw new BizException(ResultCode.NOT_FOUND);
        }

        // 2. 校验原密码是否正确
        if (!org.springframework.security.crypto.bcrypt.BCrypt.checkpw(dto.getOldPassword(), user.getPassword())) {
            throw new BizException(ResultCode.OLD_PASSWORD_ERROR);
        }

        // 3. 校验新密码不能与原密码相同
        if (dto.getOldPassword().equals(dto.getNewPassword())) {
            throw new BizException(ResultCode.NEW_PASSWORD_SAME_AS_OLD);
        }

        // 4. 校验两次输入的密码是否一致
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new BizException(ResultCode.PASSWORD_NOT_MATCH);
        }

        // 5. 更新密码
        String encodedPassword = org.springframework.security.crypto.bcrypt.BCrypt.hashpw(dto.getNewPassword(),
                org.springframework.security.crypto.bcrypt.BCrypt.gensalt());
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getId, userId)
                     .set(User::getPassword, encodedPassword);
        userMapper.update(null, updateWrapper);

        log.info("用户密码修改成功: userId={}", userId);
    }

    @Override
    public NotificationSettingsVO updateNotificationSettings(Long userId, NotificationSettingsDTO dto) {
        // 1. 查询用户
        User user = userMapper.selectById(userId);
        if (user == null || Boolean.TRUE.equals(user.getDeleted())) {
            throw new BizException(ResultCode.NOT_FOUND);
        }

        // 2. 更新通知设置
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getId, userId);

        if (dto.getSmsEnabled() != null) {
            updateWrapper.set(User::getSmsEnabled, dto.getSmsEnabled());
        }
        if (dto.getPushEnabled() != null) {
            updateWrapper.set(User::getPushEnabled, dto.getPushEnabled());
        }

        userMapper.update(null, updateWrapper);
        log.info("用户通知设置更新成功: userId={}", userId);

        // 3. 返回更新后的设置
        User updatedUser = userMapper.selectById(userId);
        return NotificationSettingsVO.builder()
                .smsEnabled(updatedUser.getSmsEnabled())
                .pushEnabled(updatedUser.getPushEnabled())
                .build();
    }

    @Override
    public String uploadAvatar(MultipartFile file, Long userId) {
        // 1. 校验文件是否为空
        if (file == null || file.isEmpty()) {
            throw new BizException(ResultCode.BAD_REQUEST, "请选择要上传的文件");
        }

        // 2. 校验文件格式
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new BizException(ResultCode.FILE_FORMAT_NOT_SUPPORTED);
        }

        // 3. 校验文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BizException(ResultCode.FILE_TOO_LARGE);
        }

        // 4. 查询用户旧头像并删除（忽略异常）
        User user = userMapper.selectById(userId);
        if (user == null || Boolean.TRUE.equals(user.getDeleted())) {
            throw new BizException(ResultCode.NOT_FOUND);
        }

        if (StringUtils.hasText(user.getAvatarUrl())) {
            try {
                String oldObjectName = huaweiObsClient.getObjectName(user.getAvatarUrl());
                huaweiObsClient.delete(oldObjectName);
                log.info("旧头像删除成功: {}", oldObjectName);
            } catch (Exception e) {
                log.warn("删除旧头像失败，继续上传新头像: {}", e.getMessage());
            }
        }

        // 5. 上传新头像到OBS
        String newAvatarUrl = huaweiObsClient.upload(file, "avatars");
        log.info("新头像上传成功: {}", newAvatarUrl);

        // 6. 更新数据库
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getId, userId)
                     .set(User::getAvatarUrl, newAvatarUrl);
        userMapper.update(null, updateWrapper);
        log.info("用户头像URL更新成功: userId={}", userId);

        return newAvatarUrl;
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    /**
     * 手机号脱敏：13800138000 -> 138****8000
     */
    private String desensitizePhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    @Override
    public void assignRoles(Long userId, List<Long> roleIds) {
        // 先删除现有角色
        userRoleMapper.delete(new LambdaQueryWrapper<UserRole>()
                .eq(UserRole::getUserId, userId));
        // 再添加新角色
        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                UserRole ur = new UserRole();
                ur.setUserId(userId);
                ur.setRoleId(roleId.intValue());
                userRoleMapper.insert(ur);
            }
        }
        log.info("用户角色已分配: userId={}, roles={}", userId, roleIds);
    }
}
