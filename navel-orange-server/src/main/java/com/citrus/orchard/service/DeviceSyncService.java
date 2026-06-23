package com.citrus.orchard.service;

/**
 * 设备同步服务接口
 */
public interface DeviceSyncService {

    /**
     * 同步所有已注册设备的状态
     */
    void syncAllDeviceStatus();

    /**
     * 同步单个设备的状态
     * @param deviceId 本地设备ID
     */
    void syncDeviceStatus(Long deviceId);

    /**
     * 根据设备编码同步设备状态
     * @param deviceCode 设备编码（IoTDA的device_id）
     */
    void syncDeviceStatusByCode(String deviceCode);
}
