package com.citrus.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.citrus.auth.entity.Role;
import com.citrus.auth.entity.User;
import com.citrus.auth.mapper.UserMapper;
import com.citrus.auth.mapper.UserRoleMapper;
import com.citrus.common.constant.Constants;
import com.citrus.common.exception.BizException;
import com.citrus.common.result.PageResult;
import com.citrus.common.result.ResultCode;
import com.citrus.orchard.mapper.OrchardMapper;
import com.citrus.user.dto.UserQueryDTO;
import com.citrus.user.service.UserService;
import com.citrus.user.vo.RoleVO;
import com.citrus.user.vo.UserDetailVO;
import com.citrus.user.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
}
