package com.citrus.user.service;

import com.citrus.common.result.PageResult;
import com.citrus.user.dto.UserQueryDTO;
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
}
