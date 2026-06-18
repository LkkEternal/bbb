package com.citrus.orchard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.citrus.annotation.OperationLog;
import com.citrus.common.constant.Constants;
import com.citrus.common.exception.BizException;
import com.citrus.common.iotda.HuaweiIotdaClient;
import com.citrus.common.result.ResultCode;
import com.citrus.orchard.dto.RegisterDeviceDTO;
import com.citrus.orchard.dto.UpdateDeviceDTO;
import com.citrus.orchard.entity.Device;
import com.citrus.orchard.entity.IotdaDevice;
import com.citrus.orchard.entity.Orchard;
import com.citrus.orchard.entity.OrchardUser;
import com.citrus.orchard.mapper.DeviceMapper;
import com.citrus.orchard.mapper.IotdaDeviceMapper;
import com.citrus.orchard.mapper.OrchardMapper;
import com.citrus.orchard.mapper.OrchardUserMapper;
import com.citrus.orchard.service.DeviceService;
import com.citrus.orchard.vo.DeviceListVO;
import com.citrus.orchard.vo.DeviceStatusVO;
import com.citrus.orchard.vo.IotdaDeviceVO;
import com.citrus.orchard.vo.RegisterDeviceVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
    private final IotdaDeviceMapper iotdaDeviceMapper;
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
    @Transactional
    public List<IotdaDeviceVO> listIotdaDevices() {
        // 1. 获取本地已注册设备的device_code集合
        List<Device> localDevices = deviceMapper.selectList(
                new LambdaQueryWrapper<Device>().select(Device::getId, Device::getDeviceCode)
        );
        Set<String> registeredDeviceCodes = localDevices.stream()
                .map(Device::getDeviceCode)
                .collect(Collectors.toSet());
        log.info("本地已注册设备数量: {}", registeredDeviceCodes.size());

        // 2. 从IoTDA获取所有设备并同步到iotda_device表
        List<IotdaDeviceVO> result = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        try {
            // 分页获取所有设备
            String marker = null;
            boolean hasMore = true;
            int pageNum = 1;
            
            while (hasMore) {
                log.info("开始获取IoTDA设备列表，第{}页, marker={}", pageNum, marker);
                HuaweiIotdaClient.DeviceListResponse response = iotdaClient.listDevices(null, 50, marker);
                
                if (response != null && response.getDevices() != null && !response.getDevices().isEmpty()) {
                    log.info("第{}页获取到{}个设备", pageNum, response.getDevices().size());
                    for (HuaweiIotdaClient.DeviceDetail device : response.getDevices()) {
                        // 同步到iotda_device表
                        syncIotdaDeviceToLocal(device, now);
                        
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
                        pageNum++;
                        // 避免超过IoTDA请求频率限制（10次/秒），等待200ms
                        Thread.sleep(200);
                    } else {
                        hasMore = false;
                    }
                } else {
                    log.info("IoTDA返回空数据，停止分页");
                    hasMore = false;
                }
            }
            
            log.info("从IoTDA获取设备列表成功，共{}个设备", result.size());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取IoTDA设备列表被中断");
            throw new BizException(ResultCode.INTERNAL_ERROR, "获取IoTDA设备列表被中断");
        } catch (Exception e) {
            log.error("从IoTDA获取设备列表失败: {}", e.getMessage(), e);
            throw new BizException(ResultCode.INTERNAL_ERROR, "获取IoTDA设备列表失败: " + e.getMessage());
        }
        
        return result;
    }

    @Override
    @Transactional
    @OperationLog(module = "device", action = "update", description = "更新设备")
    public void updateDevice(Long deviceId, UpdateDeviceDTO dto, Long operatorId) {
        // 1. 查询设备是否存在
        Device device = deviceMapper.selectById(deviceId);
        if (device == null) {
            throw new BizException(ResultCode.DEVICE_NOT_FOUND);
        }

        // 2. 验证操作者权限（必须是该设备所属果园的园主或管理员）
        OrchardUser operator = orchardUserMapper.selectOne(
                new LambdaQueryWrapper<OrchardUser>()
                        .eq(OrchardUser::getOrchardId, device.getOrchardId())
                        .eq(OrchardUser::getUserId, operatorId)
        );
        if (operator == null ||
                (!operator.getRoleType().equals(Constants.ORCHARD_ROLE_OWNER) &&
                        !operator.getRoleType().equals(Constants.ORCHARD_ROLE_MANAGER))) {
            throw new BizException(ResultCode.ORCHARD_NO_PERMISSION);
        }

        // 3. 更新设备信息（只更新非空字段）
        if (dto.getName() != null && !dto.getName().isEmpty()) {
            device.setName(dto.getName());
        }
        if (dto.getModel() != null) {
            device.setModel(dto.getModel());
        }
        if (dto.getLocationDesc() != null) {
            device.setLocationDesc(dto.getLocationDesc());
        }

        // 4. 保存更新
        deviceMapper.updateById(device);
        log.info("设备[{}]更新成功，操作者: {}", deviceId, operatorId);
    }

    @Override
    public DeviceStatusVO getDeviceStatus(Long deviceId) {
        // 1. 查询本地设备
        Device device = deviceMapper.selectById(deviceId);
        if (device == null) {
            throw new BizException(ResultCode.DEVICE_NOT_FOUND);
        }

        // 2. 构建响应
        DeviceStatusVO vo = new DeviceStatusVO();
        vo.setDeviceCode(device.getDeviceCode());
        vo.setName(device.getName());
        vo.setType(device.getType());
        vo.setModel(device.getModel());
        vo.setLocationDesc(device.getLocationDesc());
        vo.setStatus(device.getStatus());
        vo.setLastHeartbeat(device.getLastHeartbeat());
        vo.setCreatedAt(device.getCreatedAt());

        // 3. 从IoTDA获取设备状态
        try {
            HuaweiIotdaClient.DeviceDetail iotdaDevice = iotdaClient.getDevice(device.getDeviceCode());
            if (iotdaDevice != null) {
                vo.setIotdaStatus(iotdaDevice.getStatus());
            }
        } catch (Exception e) {
            log.warn("从IoTDA获取设备状态失败: {}", e.getMessage());
        }

        return vo;
    }

    @Override
    @Transactional
    @OperationLog(module = "device", action = "delete", description = "注销设备")
    public void deleteDevice(Long deviceId, Long operatorId) {
        // 1. 查询设备是否存在
        Device device = deviceMapper.selectById(deviceId);
        if (device == null) {
            throw new BizException(ResultCode.DEVICE_NOT_FOUND);
        }

        // 2. 验证操作者权限（必须是该设备所属果园的园主）
        OrchardUser operator = orchardUserMapper.selectOne(
                new LambdaQueryWrapper<OrchardUser>()
                        .eq(OrchardUser::getOrchardId, device.getOrchardId())
                        .eq(OrchardUser::getUserId, operatorId)
        );
        if (operator == null || !operator.getRoleType().equals(Constants.ORCHARD_ROLE_OWNER)) {
            throw new BizException(ResultCode.ORCHARD_NO_PERMISSION, "只有果园园主可以注销设备");
        }

        // 3. 清除iotda_device表中的关联
        IotdaDevice iotdaDevice = iotdaDeviceMapper.selectOne(
                new LambdaQueryWrapper<IotdaDevice>()
                        .eq(IotdaDevice::getLocalDeviceId, deviceId)
        );
        if (iotdaDevice != null) {
            iotdaDevice.setLocalDeviceId(null);
            iotdaDeviceMapper.updateById(iotdaDevice);
        }

        // 4. 删除设备
        deviceMapper.deleteById(deviceId);
        log.info("设备[{}]注销成功，操作者: {}", deviceId, operatorId);
    }

    /**
     * 同步IoTDA设备信息到本地iotda_device表
     */
    private void syncIotdaDeviceToLocal(HuaweiIotdaClient.DeviceDetail device, LocalDateTime syncTime) {
        try {
            IotdaDevice iotdaDevice = iotdaDeviceMapper.selectByDeviceId(device.getDeviceId());
            if (iotdaDevice == null) {
                // 新增
                iotdaDevice = new IotdaDevice();
                iotdaDevice.setDeviceId(device.getDeviceId());
                iotdaDevice.setDeviceName(device.getDeviceName());
                iotdaDevice.setProductId(device.getProductId());
                iotdaDevice.setProductName(device.getProductName());
                iotdaDevice.setStatus(device.getStatus());
                iotdaDevice.setDescription(device.getDescription());
                iotdaDevice.setFwVersion(device.getFwVersion());
                iotdaDevice.setSwVersion(device.getSwVersion());
                iotdaDevice.setSyncTime(syncTime);
                // 查找关联的本地设备
                Device localDevice = deviceMapper.selectByDeviceCode(device.getDeviceId());
                if (localDevice != null) {
                    iotdaDevice.setLocalDeviceId(localDevice.getId());
                }
                iotdaDeviceMapper.insert(iotdaDevice);
            } else {
                // 更新
                iotdaDevice.setDeviceName(device.getDeviceName());
                iotdaDevice.setProductName(device.getProductName());
                iotdaDevice.setStatus(device.getStatus());
                iotdaDevice.setDescription(device.getDescription());
                iotdaDevice.setFwVersion(device.getFwVersion());
                iotdaDevice.setSwVersion(device.getSwVersion());
                iotdaDevice.setSyncTime(syncTime);
                // 查找关联的本地设备
                Device localDevice = deviceMapper.selectByDeviceCode(device.getDeviceId());
                if (localDevice != null) {
                    iotdaDevice.setLocalDeviceId(localDevice.getId());
                }
                iotdaDeviceMapper.updateById(iotdaDevice);
            }
        } catch (Exception e) {
            log.error("同步IoTDA设备[{}]到本地失败: {}", device.getDeviceId(), e.getMessage());
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
