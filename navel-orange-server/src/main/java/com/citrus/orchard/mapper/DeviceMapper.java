package com.citrus.orchard.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.citrus.orchard.entity.Device;
import com.citrus.orchard.vo.DeviceListVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 设备 Mapper
 */
@Mapper
public interface DeviceMapper extends BaseMapper<Device> {

    /**
     * 查询果园下的设备列表
     */
    @Select("SELECT d.id, d.device_code, d.name, d.type, d.model, d.location_desc, " +
            "d.status, d.last_heartbeat, d.created_at " +
            "FROM device d " +
            "WHERE d.orchard_id = #{orchardId} " +
            "ORDER BY d.created_at DESC")
    List<DeviceListVO> selectDevicesByOrchardId(@Param("orchardId") Long orchardId);

    /**
     * 统计果园设备数量
     */
    @Select("SELECT COUNT(*) FROM device WHERE orchard_id = #{orchardId}")
    int countByOrchardId(@Param("orchardId") Long orchardId);

    /**
     * 根据设备编码查询设备
     */
    @Select("SELECT * FROM device WHERE device_code = #{deviceCode}")
    Device selectByDeviceCode(@Param("deviceCode") String deviceCode);

    /**
     * 根据API Key查询设备
     */
    @Select("SELECT * FROM device WHERE api_key = #{apiKey}")
    Device selectByApiKey(@Param("apiKey") String apiKey);
}
