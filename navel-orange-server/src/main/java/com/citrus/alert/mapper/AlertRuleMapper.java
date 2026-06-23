package com.citrus.alert.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.citrus.alert.entity.AlertRule;
import org.apache.ibatis.annotations.Mapper;

/**
 * 预警规则Mapper接口
 *
 * 继承MyBatis-Plus的BaseMapper，提供基础的CRUD操作
 * 对应数据库表：alert_rule
 */
@Mapper
public interface AlertRuleMapper extends BaseMapper<AlertRule> {
    // 基础CRUD操作已由BaseMapper提供
}
