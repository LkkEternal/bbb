package com.emotion.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.emotion.user.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM sys_user WHERE (username = #{account} OR email = #{account}) AND deleted = 0 LIMIT 1")
    User selectByUsernameOrEmail(@Param("account") String account);

    @Select("<script>" +
            "SELECT * FROM sys_user WHERE deleted = 0 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (username LIKE CONCAT('%',#{keyword},'%') OR nickname LIKE CONCAT('%',#{keyword},'%'))" +
            "</if>" +
            "ORDER BY created_at DESC" +
            "</script>")
    IPage<User> selectUserPage(Page<User> page, @Param("keyword") String keyword);

    @Select("SELECT COUNT(*) FROM sys_user WHERE deleted = 0 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (username LIKE CONCAT('%',#{keyword},'%') OR nickname LIKE CONCAT('%',#{keyword},'%'))" +
            "</if>")
    long countUsers(@Param("keyword") String keyword);
}
