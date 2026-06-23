package com.citrus.orchard.service;

import com.citrus.orchard.dto.AddMemberDTO;
import com.citrus.orchard.dto.CreateOrchardDTO;
import com.citrus.orchard.dto.UpdateOrchardDTO;
import com.citrus.orchard.vo.CreateOrchardVO;
import com.citrus.orchard.vo.OrchardDetailVO;
import com.citrus.orchard.vo.OrchardListVO;

import java.util.List;

/**
 * 果园服务接口
 */
public interface OrchardService {

    /**
     * 创建果园
     * @param dto 创建果园参数
     * @param userId 创建者用户ID
     * @return 创建结果
     */
    CreateOrchardVO createOrchard(CreateOrchardDTO dto, Long userId);

    /**
     * 查询果园列表
     * @param userId 用户ID
     * @param isAdmin 是否是管理员
     * @return 果园列表
     */
    List<OrchardListVO> listOrchards(Long userId, boolean isAdmin);

    /**
     * 查询果园详情
     * @param orchardId 果园ID
     * @param userId 当前用户ID
     * @param isAdmin 是否是管理员
     * @return 果园详情
     */
    OrchardDetailVO getOrchardDetail(Long orchardId, Long userId, boolean isAdmin);

    /**
     * 添加果园成员
     * @param orchardId 果园ID
     * @param dto 添加成员参数
     * @param operatorId 操作者用户ID
     */
    void addMember(Long orchardId, AddMemberDTO dto, Long operatorId);

    /**
     * 移除果园成员
     * @param orchardId 果园ID
     * @param targetUserId 被移除用户ID
     * @param operatorId 操作者用户ID
     */
    void removeMember(Long orchardId, Long targetUserId, Long operatorId);

    /**
     * 更新果园信息
     * @param orchardId 果园ID
     * @param dto 更新果园参数
     * @param operatorId 操作者用户ID
     */
    void updateOrchard(Long orchardId, UpdateOrchardDTO dto, Long operatorId);

    /**
     * 删除果园（软删除）
     * @param orchardId 果园ID
     * @param operatorId 操作者用户ID
     */
    void deleteOrchard(Long orchardId, Long operatorId);
}
