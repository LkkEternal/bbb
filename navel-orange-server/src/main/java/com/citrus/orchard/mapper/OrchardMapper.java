package com.citrus.orchard.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.citrus.orchard.entity.Orchard;
import com.citrus.orchard.vo.OrchardDetailVO;
import com.citrus.orchard.vo.OrchardListVO;
import com.citrus.orchard.vo.OrchardMemberVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 果园 Mapper
 */
@Mapper
public interface OrchardMapper extends BaseMapper<Orchard> {

    /**
     * 根据用户ID查询果园数量
     * 包括作为园主(owner)或成员的果园
     */
    @Select("SELECT COUNT(DISTINCT o.id) FROM orchard o " +
            "LEFT JOIN orchard_user ou ON o.id = ou.orchard_id " +
            "WHERE (o.owner_id = #{userId} OR ou.user_id = #{userId}) " +
            "AND o.deleted = false")
    int countByUserId(@Param("userId") Long userId);

    /**
     * 根据园主ID查询创建的果园数量（只统计作为园主的）
     */
    @Select("SELECT COUNT(*) FROM orchard WHERE owner_id = #{userId} AND deleted = false")
    long countByOwnerId(@Param("userId") Long userId);

    /**
     * 查询普通用户的果园列表（只查询关联的果园）
     */
    @Select("SELECT o.id, o.name, o.formatted_address, ST_AsText(o.location) as location, o.area_mu, " +
            "o.tree_age, o.variety, ou.role_type, " +
            "0 as device_count, " +
            "o.created_at " +
            "FROM orchard o " +
            "INNER JOIN orchard_user ou ON o.id = ou.orchard_id AND ou.user_id = #{userId} " +
            "WHERE o.deleted = false " +
            "ORDER BY o.created_at DESC")
    List<OrchardListVO> selectOrchardListByUserId(@Param("userId") Long userId);

    /**
     * 查询所有果园列表（管理员使用）
     */
    @Select("SELECT o.id, o.name, o.formatted_address, ST_AsText(o.location) as location, o.area_mu, " +
            "o.tree_age, o.variety, 'owner' as role_type, " +
            "0 as device_count, " +
            "o.created_at " +
            "FROM orchard o " +
            "WHERE o.deleted = false " +
            "ORDER BY o.created_at DESC")
    List<OrchardListVO> selectAllOrchardList();

    /**
     * 查询果园详情
     */
    @Select("SELECT o.id, o.name, o.formatted_address, ST_AsText(o.location) as location, " +
            "o.area_mu, o.tree_age, o.variety, o.created_at, o.updated_at " +
            "FROM orchard o " +
            "WHERE o.id = #{orchardId} AND o.deleted = false")
    OrchardDetailVO selectOrchardDetail(@Param("orchardId") Long orchardId);

    /**
     * 查询果园成员列表
     */
    @Select("SELECT ou.user_id as userId, u.username, ou.role_type as roleType, ou.created_at as joinedAt " +
            "FROM orchard_user ou " +
            "INNER JOIN sys_user u ON ou.user_id = u.id " +
            "WHERE ou.orchard_id = #{orchardId} " +
            "ORDER BY ou.created_at ASC")
    List<OrchardMemberVO> selectOrchardMembers(@Param("orchardId") Long orchardId);
}
