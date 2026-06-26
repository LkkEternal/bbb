package com.citrus.orchard.task;

import com.citrus.orchard.service.DeviceSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 设备同步定时任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceSyncTask {

    private final DeviceSyncService deviceSyncService;

    /**
     * 每5分钟同步一次设备状态
     */
    @Scheduled(fixedRate = 300000) // 5分钟 = 300000毫秒
    public void syncDeviceStatus() {
        try {
            deviceSyncService.syncAllDeviceStatus();
        } catch (Exception e) {
            log.error("定时同步设备状态失败: {}", e.getMessage(), e);
        }
    }
}
