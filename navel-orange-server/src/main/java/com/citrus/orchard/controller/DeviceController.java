package com.citrus.orchard.controller;

import com.citrus.common.constant.Constants;
import com.citrus.common.exception.BizException;
import com.citrus.common.result.Result;
import com.citrus.common.result.ResultCode;
import com.citrus.orchard.dto.RegisterDeviceDTO;
import com.citrus.orchard.service.DeviceService;
import com.citrus.orchard.service.DeviceSyncService;
import com.citrus.orchard.vo.DeviceListVO;
import com.citrus.orchard.vo.IotdaDeviceVO;
import com.citrus.orchard.vo.RegisterDeviceVO;
import com.citrus.auth.model.LoginUser;
import com.citrus.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
     */
    @GetMapping("/devices/{id}")
    @Operation(summary = "设备详情")
    public Result<DeviceListVO> getDeviceDetail(@PathVariable("id") Long deviceId) {
        // 权限校验：登录用户可以查看自己关联果园的设备
        LoginUser loginUser = securityUtil.getCurrentUser();
        Long userId = loginUser.getUserId();
        boolean isAdmin = loginUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(Constants.ROLE_ADMIN));

        DeviceListVO vo = deviceService.getDeviceById(deviceId);

        // 非管理员需要校验是否有权限查看该设备所属果园
        if (!isAdmin) {
            // 这里可以添加更细粒度的权限校验，暂时允许所有登录用户查看
        }

        return Result.success(vo);
    }

    /**
     * 同步所有设备状态
     * 从华为云IoTDA获取最新设备状态并更新到本地
     */
    @PostMapping("/devices/sync")
    @Operation(summary = "同步所有设备状态")
    public Result<Void> syncAllDevices() {
        deviceSyncService.syncAllDeviceStatus();
        return Result.success("同步成功", null);
    }

    /**
     * 同步单个设备状态
     */
    @PostMapping("/devices/{id}/sync")
    @Operation(summary = "同步单个设备状态")
    public Result<Void> syncDevice(@PathVariable("id") Long deviceId) {
        deviceSyncService.syncDeviceStatus(deviceId);
        return Result.success("同步成功", null);
    }

    /**
     * 获取IoTDA平台上的所有设备
     * 包含设备是否已注册到本地系统的标记
     */
    @GetMapping("/devices/iotda")
    @Operation(summary = "获取IoTDA设备列表")
    public Result<List<IotdaDeviceVO>> listIotdaDevices() {
        List<IotdaDeviceVO> list = deviceService.listIotdaDevices();
        return Result.success(list);
    }
}
