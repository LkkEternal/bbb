package com.citrus.user.service;

import com.citrus.common.result.PageResult;
import com.citrus.user.dto.ChangePasswordDTO;
import com.citrus.user.dto.NotificationSettingsDTO;
import com.citrus.user.dto.UpdateProfileDTO;
import com.citrus.user.dto.UserQueryDTO;

import java.util.List;import com.citrus.user.vo.NotificationSettingsVO;
import com.citrus.user.vo.UserDetailVO;
import com.citrus.user.vo.UserProfileVO;
import com.citrus.user.vo.UserVO;
import org.springframework.web.multipart.MultipartFile;

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

    /**
     * 查看个人信息（登录用户）
     *
     * @param userId 当前登录用户ID
     * @return 个人信息
     */
    UserProfileVO getUserProfile(Long userId);

    /**
     * 修改密码（登录用户）
     *
     * @param userId 当前登录用户ID
     * @param dto    修改密码请求参数
     */
    void changePassword(Long userId, ChangePasswordDTO dto);

    /**
     * 更新通知设置（登录用户）
     *
     * @param userId 当前登录用户ID
     * @param dto    通知设置请求参数
     * @return 更新后的通知设置
     */
    NotificationSettingsVO updateNotificationSettings(Long userId, NotificationSettingsDTO dto);

    /**
     * 更新个人信息（登录用户）
     *
     * @param userId 当前登录用户ID
     * @param dto    个人信息更新参数
     */
    void updateProfile(Long userId, UpdateProfileDTO dto);

    /**
     * 上传并更新用户头像
     *
     * @param file   头像图片文件（jpg/jpeg/png，≤2MB）
     * @param userId 当前登录用户ID
     * @return 新头像的访问URL
     */
    String uploadAvatar(MultipartFile file, Long userId);

    /**
     * 分配用户角色
     */
    void assignRoles(Long userId, List<Long> roleIds);
}
