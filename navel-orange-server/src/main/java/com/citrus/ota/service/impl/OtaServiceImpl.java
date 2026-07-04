package com.citrus.ota.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.citrus.common.exception.BizException;
import com.citrus.common.iotda.HuaweiIotdaClient;
import com.citrus.common.result.ResultCode;
import com.citrus.ota.dto.FirmwarePackageDTO;
import com.citrus.ota.entity.FirmwarePackage;
import com.citrus.ota.mapper.FirmwarePackageMapper;
import com.citrus.ota.service.OtaService;
import com.citrus.orchard.entity.Device;
import com.citrus.orchard.mapper.DeviceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtaServiceImpl extends ServiceImpl<FirmwarePackageMapper, FirmwarePackage> implements OtaService {

    private final DeviceMapper deviceMapper;
    private final HuaweiIotdaClient huaweiIotdaClient;

    @Override
    @Transactional
    public FirmwarePackage createPackage(FirmwarePackageDTO dto) {
        FirmwarePackage pkg = new FirmwarePackage();
        pkg.setFileName(dto.getFileName());
        pkg.setVersion(dto.getVersion());
        pkg.setFileSize(dto.getFileSize());
        pkg.setFileUrl(dto.getFileUrl());
        pkg.setMd5(dto.getMd5());
        pkg.setDescription(dto.getDescription());
        pkg.setStatus(dto.getStatus() != null ? dto.getStatus() : 0);
        save(pkg);
        log.info("Firmware package created: id={}, version={}", pkg.getId(), pkg.getVersion());
        return pkg;
    }

    @Override
    public Page<FirmwarePackage> listPackages(int page, int size) {
        return page(new Page<>(page, size),
                new LambdaQueryWrapper<FirmwarePackage>()
                        .orderByDesc(FirmwarePackage::getCreatedAt));
    }

    @Override
    @Transactional
    public Map<String, Object> upgradeDevice(Long deviceId, Long packageId) {
        // 查询固件包
        FirmwarePackage pkg = getById(packageId);
        if (pkg == null) {
            throw new BizException(ResultCode.NOT_FOUND, "固件包不存在");
        }

        // 查询本地设备，获取 IoTDA deviceId
        Device device = deviceMapper.selectById(deviceId);
        if (device == null) {
            throw new BizException(ResultCode.DEVICE_NOT_FOUND);
        }
        String iotdaDeviceId = device.getDeviceCode();

        // 构建命令参数
        Map<String, Object> paras = new LinkedHashMap<>();
        paras.put("url", pkg.getFileUrl());
        paras.put("version", pkg.getVersion());
        if (pkg.getMd5() != null) {
            paras.put("md5", pkg.getMd5());
        }
        paras.put("file_size", pkg.getFileSize());

        // 通过 IoTDA 下发同步命令
        log.info("Sending OTA upgrade command: device={}, packageId={}, version={}",
                iotdaDeviceId, packageId, pkg.getVersion());
        HuaweiIotdaClient.CommandResponse response = huaweiIotdaClient.sendCommand(
                iotdaDeviceId, "DiseaseMonitor", "firmware_upgrade", paras);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("commandId", response.getCommandId());
        result.put("resultCode", response.getResultCode());
        result.put("response", response.getResponse());
        result.put("responseName", response.getResponseName());
        log.info("OTA command result: commandId={}, resultCode={}",
                response.getCommandId(), response.getResultCode());
        return result;
    }
}
