package com.citrus.user.service;

import com.citrus.common.result.PageResult;
import com.citrus.user.dto.UserQueryDTO;
import com.citrus.user.vo.UserDetailVO;
import com.citrus.user.vo.UserVO;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 分页查询用户列表（管理员）
     *
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    PageResult<UserVO> getUserList(UserQueryDTO queryDTO);

    /**
     * 根据ID查询用户详情（管理员）
     *
     * @param id 用户ID
     * @return 用户详情
     */
    UserDetailVO getUserById(Long id);

    /**
     * 更新用户状态（管理员）
     *
     * @param id     用户ID
     * @param status 状态：0=禁用, 1=启用
     */
    void updateUserStatus(Long id, Integer status);
}
