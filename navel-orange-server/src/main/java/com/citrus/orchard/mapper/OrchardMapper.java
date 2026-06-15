package com.citrus.orchard.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 果园 Mapper
 */
@Mapper
public interface OrchardMapper {

    /**
     * 根据用户ID查询果园数量
     * 包括作为园主(owner)或成员的果园
     */
    @Select("SELECT COUNT(DISTINCT o.id) FROM orchard o " +
            "LEFT JOIN orchard_user ou ON o.id = ou.orchard_id " +
            "WHERE (o.owner_id = #{userId} OR ou.user_id = #{userId}) " +
            "AND o.deleted = false")
    int countByUserId(@Param("userId") Long userId);
}
