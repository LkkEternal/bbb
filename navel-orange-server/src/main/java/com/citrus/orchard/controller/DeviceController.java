package com.citrus.orchard.controller;

import com.citrus.common.constant.Constants;
import com.citrus.common.exception.BizException;
import com.citrus.common.result.Result;
import com.citrus.common.result.ResultCode;
import com.citrus.orchard.dto.RegisterDeviceDTO;
import com.citrus.orchard.dto.SendCommandDTO;
import com.citrus.orchard.dto.UpdateDeviceDTO;
import com.citrus.orchard.entity.Device;
import com.citrus.orchard.entity.OrchardUser;
import com.citrus.orchard.mapper.DeviceMapper;
import com.citrus.orchard.mapper.OrchardUserMapper;
import com.citrus.orchard.service.DeviceService;
import com.citrus.orchard.service.DeviceSyncService;
import com.citrus.orchard.vo.DeviceListVO;
import com.citrus.orchard.vo.DeviceStatusVO;
import com.citrus.orchard.vo.IotdaDeviceVO;
import com.citrus.orchard.vo.RegisterDeviceVO;
import com.citrus.auth.model.LoginUser;
import com.citrus.common.constant.Constants;
import com.citrus.common.iotda.HuaweiIotdaClient;
import com.citrus.util.SecurityUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 设备管理控制器
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "设备管理", description = "设备注册、查询等接口")
public class DeviceController {

    private final DeviceService deviceService;
    private final DeviceSyncService deviceSyncService;
    private final HuaweiIotdaClient huaweiIotdaClient;
    private final DeviceMapper deviceMapper;
    private final OrchardUserMapper orchardUserMapper;
    private final SecurityUtil securityUtil;

    /**
     * 注册设备
     * 果园的园主和管理员可以注册设备
     */
    @PostMapping("/devices")
    @Operation(summary = "注册设备")
    public Result<RegisterDeviceVO> registerDevice(@Valid @RequestBody RegisterDeviceDTO dto) {
        LoginUser loginUser = securityUtil.getCurrentUser();
        Long userId = loginUser.getUserId();

        // 系统管理员不能注册设备
        boolean isAdmin = loginUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(Constants.ROLE_ADMIN));
        if (isAdmin) {
            throw new BizException(ResultCode.FORBIDDEN, "系统管理员不能注册设备");
        }

        RegisterDeviceVO vo = deviceService.registerDevice(dto, userId);
        return Result.success("注册成功", vo);
    }

    /**
     * 获取果园设备列表
     */
    @GetMapping("/orchards/{id}/devices")
    @Operation(summary = "果园设备列表")
    public Result<List<DeviceListVO>> listDevices(@PathVariable("id") Long orchardId) {
        LoginUser loginUser = securityUtil.getCurrentUser();
        Long userId = loginUser.getUserId();
        boolean isAdmin = loginUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(Constants.ROLE_ADMIN));

        List<DeviceListVO> list = deviceService.listDevices(orchardId, userId, isAdmin);
        return Result.success(list);
    }

    /**
     * 获取设备详情
     * 果园关联用户可查看
     */
    @GetMapping("/devices/{id}")
    @Operation(summary = "设备详情")
    public Result<DeviceListVO> getDeviceDetail(@PathVariable("id") Long deviceId) {
        // 权限校验：果园关联用户可以查看自己果园的设备
        LoginUser loginUser = securityUtil.getCurrentUser();
        Long userId = loginUser.getUserId();
        boolean isSysAdmin = loginUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(Constants.ROLE_ADMIN));

        DeviceListVO vo = deviceService.getDeviceById(deviceId);

        // 非系统管理员需要校验是否有权限查看该设备所属果园
        if (!isSysAdmin) {
            Device device = deviceMapper.selectById(deviceId);
            if (device == null) {
                throw new BizException(ResultCode.DEVICE_NOT_FOUND);
            }
            OrchardUser orchardUser = orchardUserMapper.selectOne(
                    new LambdaQueryWrapper<OrchardUser>()
                            .eq(OrchardUser::getOrchardId, device.getOrchardId())
                            .eq(OrchardUser::getUserId, userId)
            );
            if (orchardUser == null) {
                throw new BizException(ResultCode.ORCHARD_NO_PERMISSION);
            }
        }

        return Result.success(vo);
    }

    /**
     * 更新设备
     * 果园园主和管理员可操作
     */
    @PutMapping("/devices/{id}")
    @Operation(summary = "更新设备")
    public Result<Void> updateDevice(
            @PathVariable("id") Long deviceId,
            @Valid @RequestBody UpdateDeviceDTO dto) {
        LoginUser loginUser = securityUtil.getCurrentUser();
        Long userId = loginUser.getUserId();

        // 系统管理员不能更新设备
        boolean isSysAdmin = loginUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(Constants.ROLE_ADMIN));
        if (isSysAdmin) {
            throw new BizException(ResultCode.FORBIDDEN, "系统管理员不能更新设备");
        }

        deviceService.updateDevice(deviceId, dto, userId);
        return Result.success("更新成功", null);
    }

    /**
     * 获取设备状态（包含IoTDA状态）
     * 果园关联用户可查看
     */
    @GetMapping("/devices/{id}/status")
    @Operation(summary = "设备状态")
    public Result<DeviceStatusVO> getDeviceStatus(@PathVariable("id") Long deviceId) {
        // 权限校验：果园关联用户可以查看自己果园的设备状态
        LoginUser loginUser = securityUtil.getCurrentUser();
        Long userId = loginUser.getUserId();
        boolean isSysAdmin = loginUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(Constants.ROLE_ADMIN));

        // 非系统管理员需要校验是否有权限查看该设备所属果园
        if (!isSysAdmin) {
            Device device = deviceMapper.selectById(deviceId);
            if (device == null) {
                throw new BizException(ResultCode.DEVICE_NOT_FOUND);
            }
            OrchardUser orchardUser = orchardUserMapper.selectOne(
                    new LambdaQueryWrapper<OrchardUser>()
                            .eq(OrchardUser::getOrchardId, device.getOrchardId())
                            .eq(OrchardUser::getUserId, userId)
            );
            if (orchardUser == null) {
                throw new BizException(ResultCode.ORCHARD_NO_PERMISSION);
            }
        }

        DeviceStatusVO vo = deviceService.getDeviceStatus(deviceId);
        return Result.success(vo);
    }

    /**
     * 同步所有设备状态
     * 从华为云IoTDA获取最新设备状态并更新到本地
     * 系统管理员、果园园主和管理员可操作
     */
    @PostMapping("/devices/sync")
    @Operation(summary = "同步所有设备状态")
    public Result<Void> syncAllDevices() {
        LoginUser loginUser = securityUtil.getCurrentUser();
        boolean isSysAdmin = loginUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(Constants.ROLE_ADMIN));

        // 系统管理员直接允许同步
        if (!isSysAdmin) {
            // 非系统管理员需要检查是否是果园园主或管理员
            Long userId = loginUser.getUserId();
            long orchardCount = orchardUserMapper.selectCount(
                    new LambdaQueryWrapper<OrchardUser>()
                            .eq(OrchardUser::getUserId, userId)
                            .in(OrchardUser::getRoleType,
                                    Constants.ORCHARD_ROLE_OWNER,
                                    Constants.ORCHARD_ROLE_MANAGER)
            );
            if (orchardCount == 0) {
                throw new BizException(ResultCode.ORCHARD_NO_PERMISSION, "只有系统管理员或果园园主、管理员可以同步设备状态");
            }
        }

        deviceSyncService.syncAllDeviceStatus();
        return Result.success("同步成功", null);
    }

    /**
     * 同步单个设备状态
     * 仅园主和果园管理员可操作（只能同步自己果园的设备）
     */
    @PostMapping("/devices/{id}/sync")
    @Operation(summary = "同步单个设备状态")
    public Result<Void> syncDevice(@PathVariable("id") Long deviceId) {
        // 系统管理员不能访问
        LoginUser loginUser = securityUtil.getCurrentUser();
        Long userId = loginUser.getUserId();
        boolean isSysAdmin = loginUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(Constants.ROLE_ADMIN));
        if (isSysAdmin) {
            throw new BizException(ResultCode.FORBIDDEN, "系统管理员不能同步设备状态");
        }

        // 查询设备是否存在
        Device device = deviceMapper.selectById(deviceId);
        if (device == null) {
            throw new BizException(ResultCode.DEVICE_NOT_FOUND);
        }

        // 检查用户是否是该设备所属果园的园主或管理员
        OrchardUser orchardUser = orchardUserMapper.selectOne(
                new LambdaQueryWrapper<OrchardUser>()
                        .eq(OrchardUser::getOrchardId, device.getOrchardId())
                        .eq(OrchardUser::getUserId, userId)
                        .in(OrchardUser::getRoleType,
                                Constants.ORCHARD_ROLE_OWNER,
                                Constants.ORCHARD_ROLE_MANAGER)
        );
        if (orchardUser == null) {
            throw new BizException(ResultCode.ORCHARD_NO_PERMISSION, "只有果园园主或管理员可以同步设备状态");
        }

        deviceSyncService.syncDeviceStatus(deviceId);
        return Result.success("同步成功", null);
    }

    /**
     * 获取IoTDA平台上的所有设备
     * 包含设备是否已注册到本地系统的标记
     * 系统管理员和果园园主、管理员可访问
     */
    @GetMapping("/devices/iotda")
    @Operation(summary = "获取IoTDA设备列表")
    public Result<List<IotdaDeviceVO>> listIotdaDevices() {
        // 检查用户是否有权限
        LoginUser loginUser = securityUtil.getCurrentUser();
        Long userId = loginUser.getUserId();
        boolean isSysAdmin = loginUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(Constants.ROLE_ADMIN));

        // 系统管理员可以直接访问
        if (isSysAdmin) {
            List<IotdaDeviceVO> list = deviceService.listIotdaDevices();
            return Result.success(list);
        }

        // 非系统管理员需要是果园园主或管理员
        long orchardCount = orchardUserMapper.selectCount(
                new LambdaQueryWrapper<OrchardUser>()
                        .eq(OrchardUser::getUserId, userId)
                        .in(OrchardUser::getRoleType, Constants.ORCHARD_ROLE_OWNER, Constants.ORCHARD_ROLE_MANAGER)
        );
        if (orchardCount == 0) {
            throw new BizException(ResultCode.ORCHARD_NO_PERMISSION, "只有系统管理员或果园园主、管理员可以访问IoTDA设备列表");
        }

        List<IotdaDeviceVO> list = deviceService.listIotdaDevices();
        return Result.success(list);
    }

    /**
     * 注销设备（删除）
     * 只有果园园主可以操作
     */
    @DeleteMapping("/devices/{id}")
    @Operation(summary = "注销设备")
    public Result<Void> deleteDevice(@PathVariable("id") Long deviceId) {
        LoginUser loginUser = securityUtil.getCurrentUser();
        Long userId = loginUser.getUserId();

        // 系统管理员不能注销设备
        boolean isSysAdmin = loginUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(Constants.ROLE_ADMIN));
        if (isSysAdmin) {
            throw new BizException(ResultCode.FORBIDDEN, "系统管理员不能注销设备");
        }

        deviceService.deleteDevice(deviceId, userId);
        return Result.success("注销成功", null);
    }

    /**
     * 下发设备命令（如云台控制）
     * 仅果园园主和管理员可操作
     */
    @PostMapping("/devices/{id}/command")
    @Operation(summary = "下发设备命令")
    public Result<Map<String, Object>> sendCommand(
            @PathVariable("id") Long deviceId,
            @Valid @RequestBody SendCommandDTO dto) {
        // 权限校验
        LoginUser loginUser = securityUtil.getCurrentUser();
        Long userId = loginUser.getUserId();
        boolean isSysAdmin = loginUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(Constants.ROLE_ADMIN));
        if (isSysAdmin) {
            throw new BizException(ResultCode.FORBIDDEN, "系统管理员不能下发设备命令");
        }

        // 查询设备
        Device device = deviceMapper.selectById(deviceId);
        if (device == null) {
            throw new BizException(ResultCode.DEVICE_NOT_FOUND);
        }

        // 校验果园权限
        OrchardUser orchardUser = orchardUserMapper.selectOne(
                new LambdaQueryWrapper<OrchardUser>()
                        .eq(OrchardUser::getOrchardId, device.getOrchardId())
                        .eq(OrchardUser::getUserId, userId)
                        .in(OrchardUser::getRoleType,
                                Constants.ORCHARD_ROLE_OWNER,
                                Constants.ORCHARD_ROLE_MANAGER)
        );
        if (orchardUser == null) {
            throw new BizException(ResultCode.ORCHARD_NO_PERMISSION, "只有果园园主或管理员可以下发命令");
        }

        // 下发命令
        String iotdaDeviceId = device.getDeviceCode();
        HuaweiIotdaClient.CommandResponse response = huaweiIotdaClient.sendCommand(
                iotdaDeviceId, "DiseaseMonitor",
                dto.getCommandName(),
                dto.getParas() != null ? dto.getParas() : Map.of());

        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("commandId", response.getCommandId());
        result.put("resultCode", response.getResultCode());
        result.put("response", response.getResponse());
        result.put("responseName", response.getResponseName());
        return Result.success(result);
    }
}
