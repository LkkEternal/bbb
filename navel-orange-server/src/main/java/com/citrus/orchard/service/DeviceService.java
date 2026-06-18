package com.citrus.orchard.service;

import com.citrus.orchard.dto.RegisterDeviceDTO;
import com.citrus.orchard.vo.DeviceListVO;
import com.citrus.orchard.vo.IotdaDeviceVO;
import com.citrus.orchard.vo.RegisterDeviceVO;

import java.util.List;

/**
 * 设备服务接口
 */
public interface DeviceService {

    /**
     * 注册设备
     * @param dto 注册设备参数
     * @param operatorId 操作者用户ID
     * @return 注册结果（包含API Key）
     */
    RegisterDeviceVO registerDevice(RegisterDeviceDTO dto, Long operatorId);

    /**
     * 获取果园设备列表
     * @param orchardId 果园ID
     * @param userId 当前用户ID
     * @param isAdmin 是否是管理员
     * @return 设备列表
     */
    List<DeviceListVO> listDevices(Long orchardId, Long userId, boolean isAdmin);

    /**
     * 根据设备ID获取设备详情
     * @param deviceId 设备ID
     * @return 设备详情
     */
    DeviceListVO getDeviceById(Long deviceId);

    /**
     * 获取IoTDA平台上的所有设备
     * @return IoTDA设备列表
     */
    List<IotdaDeviceVO> listIotdaDevices();
}
