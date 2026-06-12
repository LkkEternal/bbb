package com.citrus.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.citrus.auth.entity.Role;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色表 Mapper
 */
@Mapper
public interface RoleMapper extends BaseMapper<Role> {
}
