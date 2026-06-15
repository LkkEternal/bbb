package com.citrus.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.citrus.annotation.OperationLog;
import com.citrus.auth.entity.Role;
import com.citrus.auth.entity.User;
import com.citrus.auth.mapper.UserMapper;
import com.citrus.auth.mapper.UserRoleMapper;
import com.citrus.common.constant.Constants;
import com.citrus.common.exception.BizException;
import com.citrus.common.result.PageResult;
import com.citrus.common.result.ResultCode;
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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;

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

    @Override
    @OperationLog(module = "user", action = "update", description = "修改密码")
    public void changePassword(Long userId, ChangePasswordDTO dto) {
        // 1. 查询用户
        User user = userMapper.selectById(userId);
        if (user == null || Boolean.TRUE.equals(user.getDeleted())) {
            throw new BizException(ResultCode.NOT_FOUND);
        }

        // 2. 验证原密码
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BizException(ResultCode.OLD_PASSWORD_ERROR);
        }

        // 3. 验证新密码不能与原密码相同
        if (passwordEncoder.matches(dto.getNewPassword(), user.getPassword())) {
            throw new BizException(ResultCode.NEW_PASSWORD_SAME_AS_OLD);
        }

        // 4. 验证新密码与确认密码一致
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new BizException(ResultCode.PASSWORD_NOT_MATCH);
        }

        // 5. 更新密码
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userMapper.updateById(user);

        // 6. 使当前Token失效
        String tokenKey = Constants.REDIS_USER_TOKEN + userId;
        redisTemplate.delete(tokenKey);

        log.info("密码修改成功，userId={}", userId);
    }

    @Override
    public NotificationSettingsVO updateNotificationSettings(Long userId, NotificationSettingsDTO dto) {
        // 1. 查询用户
        User user = userMapper.selectById(userId);
        if (user == null || Boolean.TRUE.equals(user.getDeleted())) {
            throw new BizException(ResultCode.NOT_FOUND);
        }

        // 2. 更新通知设置（只更新非null字段，支持部分更新）
        if (dto.getSmsEnabled() != null) {
            user.setSmsEnabled(dto.getSmsEnabled());
        }
        if (dto.getPushEnabled() != null) {
            user.setPushEnabled(dto.getPushEnabled());
        }
        userMapper.updateById(user);

        log.info("通知设置更新成功，userId={}, smsEnabled={}, pushEnabled={}", 
                userId, user.getSmsEnabled(), user.getPushEnabled());

        // 3. 返回更新后的设置
        return NotificationSettingsVO.builder()
                .smsEnabled(user.getSmsEnabled())
                .pushEnabled(user.getPushEnabled())
                .build();
    }

    // TODO: 实现分配角色逻辑 - 先删除旧关联，再批量插入新角色关联

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
     * 
     * @param user 用户实体对象
     * @param roles 用户角色列表
     * @return 脱敏后的用户资料视图对象
     */
    private UserProfileVO convertToProfileVO(User user, List<Role> roles) {
        // 声明角色视图列表变量，初始化为null
        List<RoleVO> roleVOList = null;
        
        // 判断角色列表是否存在且不为空
        if (roles != null && !roles.isEmpty()) {
            // 使用Java Stream将Role实体列表转换为RoleVO视图列表
            roleVOList = roles.stream()
                    // 映射每个Role实体到RoleVO对象
                    .map(role -> RoleVO.builder()
                            .id(role.getId())          // 设置角色ID
                            .code(role.getCode())      // 设置角色编码（如ROLE_FARMER）
                            .name(role.getName())      // 设置角色名称（如农户）
                            .build())
                    // 收集转换后的列表
                    .collect(Collectors.toList());
        }

        // 使用Builder模式构建UserProfileVO对象并返回
        return UserProfileVO.builder()
                .id(user.getId())                          // 用户ID
                .phone(desensitizePhone(user.getPhone()))  // 脱敏后的手机号（如138****8000）
                .username(user.getUsername())              // 用户名
                .avatarUrl(user.getAvatarUrl())            // 头像URL
                .roles(roleVOList)                         // 用户角色列表
                .smsEnabled(user.getSmsEnabled())          // 是否开启短信通知
                .pushEnabled(user.getPushEnabled())        // 是否开启推送通知
                .createdAt(user.getCreatedAt())            // 用户创建时间
                .lastLoginIp(user.getLastLoginIp())        // 最后登录IP
                .build();                                  // 构建并返回UserProfileVO对象
    }

    /**
     * 手机号脱敏方法：将手机号中间4位替换为星号
     * 脱敏规则：13800138000 -> 138****8000
     * 
     * @param phone 原始手机号
     * @return 脱敏后的手机号
     */
    private String desensitizePhone(String phone) {
        // 边界检查：手机号为空或长度不足7位时直接返回原值
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        // 截取前3位 + "****" + 截取后4位
        // substring(0, 3) 获取前3位（如138）
        // substring(phone.length() - 4) 获取最后4位（如8000）
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}
