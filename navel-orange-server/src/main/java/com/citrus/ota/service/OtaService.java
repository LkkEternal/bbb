package com.citrus.ota.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.citrus.ota.dto.FirmwarePackageDTO;
import com.citrus.ota.entity.FirmwarePackage;

import java.util.Map;

public interface OtaService {

    /** 新增固件包 */
    FirmwarePackage createPackage(FirmwarePackageDTO dto);

    /** 固件包列表（分页） */
    Page<FirmwarePackage> listPackages(int page, int size);

    /** 向指定设备下发 OTA 升级命令 */
    Map<String, Object> upgradeDevice(Long deviceId, Long packageId);
}
