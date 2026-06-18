package com.citrus.orchard.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.citrus.orchard.entity.IotdaDevice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * IoTDA设备 Mapper
 */
@Mapper
public interface IotdaDeviceMapper extends BaseMapper<IotdaDevice> {

    /**
     * 根据IoTDA设备ID查询
     */
    @Select("SELECT * FROM iotda_device WHERE device_id = #{deviceId}")
    IotdaDevice selectByDeviceId(@Param("deviceId") String deviceId);
}
