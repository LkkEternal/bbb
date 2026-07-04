package com.citrus.ota.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.citrus.common.result.Result;
import com.citrus.common.util.HuaweiObsClient;
import com.citrus.ota.dto.FirmwarePackageDTO;
import com.citrus.ota.dto.OtaUpgradeDTO;
import com.citrus.ota.entity.FirmwarePackage;
import com.citrus.ota.service.OtaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "OTA 固件升级")
@RestController
@RequestMapping("/api/ota")
@RequiredArgsConstructor
public class OtaController {

    private final OtaService otaService;
    private final HuaweiObsClient huaweiObsClient;

    // ── 固件包管理 ─────────────────────────────────────

    @PostMapping("/packages")
    @Operation(summary = "新增固件包")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<FirmwarePackage> createPackage(@Valid @RequestBody FirmwarePackageDTO dto) {
        return Result.success(otaService.createPackage(dto));
    }

    @GetMapping("/packages")
    @Operation(summary = "固件包列表（分页）")
    @PreAuthorize("hasAnyRole('ADMIN', 'FARMER')")
    public Result<Page<FirmwarePackage>> listPackages(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size) {
        return Result.success(otaService.listPackages(page, size));
    }

    // ── OBS 固件文件扫描 ────────────────────────────────

    @GetMapping("/obs-files")
    @Operation(summary = "扫描 OBS 桶中的固件文件")
    public Result<List<HuaweiObsClient.ObsFileInfo>> listObsFiles(
            @RequestParam(defaultValue = "") String prefix,
            @RequestParam(defaultValue = "100") int maxKeys) {
        return Result.success(huaweiObsClient.listObjects(prefix, maxKeys));
    }

    // ── OTA 升级 ────────────────────────────────────────

    @PostMapping("/devices/{deviceId}/upgrade")
    @Operation(summary = "向指定设备下发 OTA 升级命令")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Object>> upgradeDevice(
            @PathVariable Long deviceId,
            @Valid @RequestBody OtaUpgradeDTO dto) {
        return Result.success(otaService.upgradeDevice(deviceId, dto.getPackageId()));
    }
}
