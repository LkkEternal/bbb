package com.citrus.orchard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.citrus.annotation.OperationLog;
import com.citrus.common.constant.Constants;
import com.citrus.common.exception.BizException;
import com.citrus.common.iotda.HuaweiIotdaClient;
import com.citrus.common.result.ResultCode;
import com.citrus.orchard.dto.DeviceCommandDTO;
import com.citrus.orchard.dto.RegisterDeviceDTO;
import com.citrus.orchard.entity.Device;
import com.citrus.orchard.entity.Orchard;
import com.citrus.orchard.entity.OrchardUser;
import com.citrus.orchard.mapper.DeviceMapper;
import com.citrus.orchard.mapper.OrchardMapper;
import com.citrus.orchard.mapper.OrchardUserMapper;
import com.citrus.orchard.service.DeviceService;
import com.citrus.orchard.vo.DeviceListVO;
import com.citrus.orchard.vo.DeviceRealtimeDataVO;
import com.citrus.orchard.vo.IotdaDeviceVO;
import com.citrus.orchard.vo.RegisterDeviceVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 设备服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {

    private final DeviceMapper deviceMapper;
    private final OrchardMapper orchardMapper;
    private final OrchardUserMapper orchardUserMapper;
    private final HuaweiIotdaClient iotdaClient;

    @Override
    @Transactional
    @OperationLog(module = "device", action = "create", description = "注册设备")
    public RegisterDeviceVO registerDevice(RegisterDeviceDTO dto, Long operatorId) {
        // 1. 验证果园是否存在
        Orchard orchard = orchardMapper.selectById(dto.getOrchardId());
        if (orchard == null) {
            throw new BizException(ResultCode.ORCHARD_NOT_FOUND);
        }

        // 2. 验证操作者权限（必须是园主或管理员）
        OrchardUser operator = orchardUserMapper.selectOne(
                new LambdaQueryWrapper<OrchardUser>()
                        .eq(OrchardUser::getOrchardId, dto.getOrchardId())
                        .eq(OrchardUser::getUserId, operatorId)
        );
        if (operator == null ||
                (!operator.getRoleType().equals(Constants.ORCHARD_ROLE_OWNER) &&
                        !operator.getRoleType().equals(Constants.ORCHARD_ROLE_MANAGER))) {
            throw new BizException(ResultCode.ORCHARD_NO_PERMISSION);
        }

        // 3. 检查设备编码是否已存在
        Device existingDevice = deviceMapper.selectByDeviceCode(dto.getDeviceCode());
        if (existingDevice != null) {
            throw new BizException(ResultCode.DEVICE_CODE_EXISTS);
        }

        // 4. 验证设备类型是否合法
        if (!dto.getType().equals("camera") && !dto.getType().equals("sensor")) {
            throw new BizException(ResultCode.BAD_REQUEST, "设备类型只能是camera或sensor");
        }

        // 5. 从IoTDA获取设备信息（如果配置了IoTDA）
        String deviceName = dto.getName();
        String iotdaStatus = null;
        try {
            HuaweiIotdaClient.DeviceDetail iotdaDevice = iotdaClient.getDevice(dto.getDeviceCode());
            if (iotdaDevice != null) {
                // 如果用户未指定名称，则使用IoTDA的设备名称
                if (deviceName == null || deviceName.isEmpty()) {
                    deviceName = iotdaDevice.getDeviceName();
                }
                iotdaStatus = iotdaDevice.getStatus();
                log.info("从IoTDA获取设备信息成功: deviceName={}, status={}", deviceName, iotdaStatus);
            }
        } catch (Exception e) {
            log.warn("从IoTDA获取设备信息失败，将使用本地默认值: {}", e.getMessage());
        }

        // 6. 生成API Key
        String rawApiKey = generateApiKey();
        String hashedApiKey = sha256(rawApiKey);

        // 7. 转换IoTDA状态为本地状态
        int localStatus = Constants.DEVICE_OFFLINE;
        if (iotdaStatus != null) {
            if ("ONLINE".equals(iotdaStatus)) {
                localStatus = Constants.DEVICE_ONLINE;
            }
        }

        // 8. 创建设备
        Device device = new Device();
        device.setDeviceCode(dto.getDeviceCode());
        device.setOrchardId(dto.getOrchardId());
        device.setName(deviceName != null ? deviceName : dto.getDeviceCode());
        device.setType(dto.getType());
        device.setModel(dto.getModel());
        device.setApiKey(hashedApiKey);
        device.setLocationDesc(dto.getLocationDesc());
        device.setStatus(localStatus);
        deviceMapper.insert(device);

        log.info("果园[{}]注册设备[{}]成功，设备ID: {}", dto.getOrchardId(), dto.getDeviceCode(), device.getId());

        // 9. 返回响应（包含明文API Key，仅此一次返回）
        RegisterDeviceVO vo = new RegisterDeviceVO();
        vo.setId(device.getId());
        vo.setDeviceCode(device.getDeviceCode());
        vo.setApiKey(rawApiKey);
        vo.setName(device.getName());
        return vo;
    }

    @Override
    public List<DeviceListVO> listDevices(Long orchardId, Long userId, boolean isAdmin) {
        // 1. 验证果园是否存在
        Orchard orchard = orchardMapper.selectById(orchardId);
        if (orchard == null) {
            throw new BizException(ResultCode.ORCHARD_NOT_FOUND);
        }

        // 2. 权限校验：管理员可以查看所有果园，普通用户只能查看关联的果园
        if (!isAdmin) {
            OrchardUser orchardUser = orchardUserMapper.selectOne(
                    new LambdaQueryWrapper<OrchardUser>()
                            .eq(OrchardUser::getOrchardId, orchardId)
                            .eq(OrchardUser::getUserId, userId)
            );
            if (orchardUser == null) {
                throw new BizException(ResultCode.ORCHARD_NO_PERMISSION);
            }
        }

        // 3. 查询设备列表
        return deviceMapper.selectDevicesByOrchardId(orchardId);
    }

    @Override
    public DeviceListVO getDeviceById(Long deviceId) {
        Device device = deviceMapper.selectById(deviceId);
        if (device == null) {
            throw new BizException(ResultCode.DEVICE_NOT_FOUND);
        }

        DeviceListVO vo = new DeviceListVO();
        vo.setId(device.getId());
        vo.setDeviceCode(device.getDeviceCode());
        vo.setName(device.getName());
        vo.setType(device.getType());
        vo.setModel(device.getModel());
        vo.setLocationDesc(device.getLocationDesc());
        vo.setStatus(device.getStatus());
        vo.setLastHeartbeat(device.getLastHeartbeat());
        vo.setCreatedAt(device.getCreatedAt());
        return vo;
    }

    @Override
    public List<IotdaDeviceVO> listIotdaDevices() {
        // 1. 获取本地已注册设备的device_code集合
        List<Device> localDevices = deviceMapper.selectList(
                new LambdaQueryWrapper<Device>().select(Device::getDeviceCode)
        );
        Set<String> registeredDeviceCodes = localDevices.stream()
                .map(Device::getDeviceCode)
                .collect(Collectors.toSet());

        // 2. 从IoTDA获取所有设备
        List<IotdaDeviceVO> result = new ArrayList<>();
        try {
            // 分页获取所有设备
            String marker = null;
            boolean hasMore = true;
            
            while (hasMore) {
                HuaweiIotdaClient.DeviceListResponse response = iotdaClient.listDevices(null, 50, marker);
                
                if (response != null && response.getDevices() != null) {
                    for (HuaweiIotdaClient.DeviceDetail device : response.getDevices()) {
                        IotdaDeviceVO vo = new IotdaDeviceVO();
                        vo.setDeviceId(device.getDeviceId());
                        vo.setDeviceName(device.getDeviceName());
                        vo.setProductId(device.getProductId());
                        vo.setProductName(device.getProductName());
                        vo.setStatus(device.getStatus());
                        vo.setDescription(device.getDescription());
                        vo.setFwVersion(device.getFwVersion());
                        vo.setSwVersion(device.getSwVersion());
                        // 判断是否已注册到本地系统
                        vo.setRegistered(registeredDeviceCodes.contains(device.getDeviceId()));
                        result.add(vo);
                    }
                    
                    // 检查是否有更多数据
                    if (response.getPage() != null && response.getPage().getMarker() != null) {
                        marker = response.getPage().getMarker();
                        hasMore = true;
                    } else {
                        hasMore = false;
                    }
                } else {
                    hasMore = false;
                }
            }
            
            log.info("从IoTDA获取设备列表成功，共{}个设备", result.size());
        } catch (Exception e) {
            log.error("从IoTDA获取设备列表失败: {}", e.getMessage());
            throw new BizException(ResultCode.INTERNAL_ERROR, "获取IoTDA设备列表失败: " + e.getMessage());
        }
        
        return result;
    }

    @Override
    public DeviceRealtimeDataVO getDeviceRealtimeData(Long deviceId) {
        // 1. 查询本地设备
        Device device = deviceMapper.selectById(deviceId);
        if (device == null) {
            throw new BizException(ResultCode.DEVICE_NOT_FOUND);
        }

        // 2. 从IoTDA获取设备影子数据
        DeviceRealtimeDataVO vo = new DeviceRealtimeDataVO();
        vo.setDeviceId(device.getDeviceCode());
        vo.setDeviceName(device.getName());

        try {
            // 获取设备详情（包含状态）
            HuaweiIotdaClient.DeviceDetail deviceDetail = iotdaClient.getDevice(device.getDeviceCode());
            if (deviceDetail != null) {
                vo.setStatus(deviceDetail.getStatus());
            }

            // 获取设备影子数据（最后一次上报的属性）
            HuaweiIotdaClient.DeviceShadowResponse shadow = iotdaClient.getDeviceShadow(device.getDeviceCode());
            if (shadow != null && shadow.getShadow() != null) {
                for (HuaweiIotdaClient.ShadowItem item : shadow.getShadow()) {
                    if (item.getReported() != null) {
                        vo.setProperties(item.getReported().getProperties());
                        vo.setReportTime(item.getReported().getEventTime());
                        break; // 取第一个服务的数据
                    }
                }
            }

            log.info("获取设备[{}]实时数据成功", device.getDeviceCode());
        } catch (Exception e) {
            log.warn("获取设备[{}]实时数据失败: {}", device.getDeviceCode(), e.getMessage());
            // 不抛出异常，返回基本信息
        }

        return vo;
    }

    @Override
    public String sendDeviceCommand(Long deviceId, DeviceCommandDTO dto) {
        // 1. 查询本地设备
        Device device = deviceMapper.selectById(deviceId);
        if (device == null) {
            throw new BizException(ResultCode.DEVICE_NOT_FOUND);
        }

        // 2. 调用IoTDA下发命令
        try {
            HuaweiIotdaClient.CommandResponse response = iotdaClient.sendCommand(
                    device.getDeviceCode(),
                    dto.getServiceId(),
                    dto.getCommandName(),
                    dto.getParas()
            );

            if (response != null && response.getCommandId() != null) {
                log.info("向设备[{}]下发命令成功，命令ID: {}", device.getDeviceCode(), response.getCommandId());
                return response.getCommandId();
            } else {
                throw new BizException(ResultCode.INTERNAL_ERROR, "命令下发失败");
            }
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("向设备[{}]下发命令失败: {}", device.getDeviceCode(), e.getMessage());
            throw new BizException(ResultCode.INTERNAL_ERROR, "命令下发失败: " + e.getMessage());
        }
    }

    /**
     * 生成API Key
     * 格式：ak_xxxxxxxxxxxxxxxxxxxx
     */
    private String generateApiKey() {
        return "ak_" + UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * SHA256哈希
     */
    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256算法不可用", e);
        }
    }
}
