package com.citrus.orchard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.citrus.common.constant.Constants;
import com.citrus.common.exception.BizException;
import com.citrus.common.iotda.HuaweiIotdaClient;
import com.citrus.common.result.ResultCode;
import com.citrus.orchard.entity.Device;
import com.citrus.orchard.mapper.DeviceMapper;
import com.citrus.orchard.service.DeviceSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 设备同步服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceSyncServiceImpl implements DeviceSyncService {

    private final DeviceMapper deviceMapper;
    private final HuaweiIotdaClient iotdaClient;

    @Override
    @Transactional
    public void syncAllDeviceStatus() {
        log.info("开始同步所有设备状态...");

        // 1. 查询所有本地设备
        List<Device> devices = deviceMapper.selectList(
                new LambdaQueryWrapper<Device>().select(Device::getId, Device::getDeviceCode, Device::getStatus)
        );

        if (devices.isEmpty()) {
            log.info("没有需要同步的设备");
            return;
        }

        int successCount = 0;
        int failCount = 0;

        // 2. 遍历设备，同步状态
        for (Device device : devices) {
            try {
                syncDeviceStatusInternal(device);
                successCount++;
            } catch (Exception e) {
                log.error("同步设备[{}]状态失败: {}", device.getDeviceCode(), e.getMessage());
                failCount++;
            }
        }

        log.info("设备状态同步完成: 成功={}, 失败={}, 总数={}", successCount, failCount, devices.size());
    }

    @Override
    @Transactional
    public void syncDeviceStatus(Long deviceId) {
        Device device = deviceMapper.selectById(deviceId);
        if (device == null) {
            throw new BizException(ResultCode.DEVICE_NOT_FOUND);
        }
        syncDeviceStatusInternal(device);
    }

    @Override
    @Transactional
    public void syncDeviceStatusByCode(String deviceCode) {
        Device device = deviceMapper.selectByDeviceCode(deviceCode);
        if (device == null) {
            log.warn("设备编码不存在: {}", deviceCode);
            return;
        }
        syncDeviceStatusInternal(device);
    }

    /**
     * 同步单个设备状态的内部实现
     */
    private void syncDeviceStatusInternal(Device device) {
        try {
            // 1. 调用IoTDA获取设备信息
            HuaweiIotdaClient.DeviceDetail iotdaDevice = iotdaClient.getDevice(device.getDeviceCode());

            if (iotdaDevice == null) {
                log.warn("IoTDA中未找到设备: {}", device.getDeviceCode());
                return;
            }

            // 2. 转换IoTDA状态为本地状态
            int newStatus = convertIotdaStatus(iotdaDevice.getStatus());

            // 3. 只有状态变化时才更新数据库
            if (device.getStatus() != newStatus) {
                device.setStatus(newStatus);
                deviceMapper.updateById(device);
                log.info("设备[{}]状态更新: {} -> {}", device.getDeviceCode(), device.getStatus(), newStatus);
            }
        } catch (Exception e) {
            log.error("同步设备[{}]状态失败: {}", device.getDeviceCode(), e.getMessage());
            throw e;
        }
    }

    /**
     * 转换IoTDA设备状态为本地状态
     * IoTDA状态: ONLINE, OFFLINE, INACTIVE
     * 本地状态: 0=离线, 1=在线, 2=故障
     */
    private int convertIotdaStatus(String iotdaStatus) {
        if (iotdaStatus == null) {
            return Constants.DEVICE_OFFLINE;
        }
        switch (iotdaStatus) {
            case "ONLINE":
                return Constants.DEVICE_ONLINE;
            case "OFFLINE":
                return Constants.DEVICE_OFFLINE;
            case "INACTIVE":
                return Constants.DEVICE_OFFLINE;
            default:
                return Constants.DEVICE_OFFLINE;
        }
    }
}
