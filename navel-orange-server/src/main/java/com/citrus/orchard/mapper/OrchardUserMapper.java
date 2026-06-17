package com.citrus.orchard.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.citrus.orchard.entity.OrchardUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 果园用户关联 Mapper
 */
@Mapper
public interface OrchardUserMapper extends BaseMapper<OrchardUser> {
}
