package com.citrus.push.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.citrus.push.entity.AlertMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 预警消息Mapper接口
 *
 * 继承MyBatis-Plus的BaseMapper，提供基础的CRUD操作
 * 对应数据库表：alert_message
 */
@Mapper
public interface AlertMessageMapper extends BaseMapper<AlertMessage> {
    // 基础CRUD操作已由BaseMapper提供
    // 如需自定义SQL查询，可在此添加方法并在对应的XML文件中实现
}
