package com.citrus.ota.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.citrus.common.exception.BizException;
import com.citrus.common.iotda.HuaweiIotdaClient;
import com.citrus.common.result.ResultCode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper;

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

        // 构建消息体内容：{"cmd":"ota_update","url":"..."}
        Map<String, Object> content = new LinkedHashMap<>();
        content.put("cmd", "ota_update");
        content.put("url", pkg.getFileUrl());

        // IoTDA 消息 API 要求：message.content 是字符串，需序列化
        String contentJson;
        try {
            contentJson = objectMapper.writeValueAsString(content);
        } catch (Exception e) {
            throw new BizException(ResultCode.INTERNAL_ERROR, "序列化消息内容失败");
        }
        Map<String, Object> messageBody = new LinkedHashMap<>();
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("content", contentJson);
        message.put("ttl", 3600);
        messageBody.put("message", message);

        // 通过 IoTDA 下发消息
        log.info("Sending OTA message: device={}, packageId={}, version={}",
                iotdaDeviceId, packageId, pkg.getVersion());
        String messageId = huaweiIotdaClient.sendMessage(iotdaDeviceId, messageBody);
        if (messageId == null) {
            throw new BizException(ResultCode.INTERNAL_ERROR, "IoTDA消息下发失败");
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("messageId", messageId);
        log.info("OTA message sent: messageId={}", messageId);
        return result;
    }
}
