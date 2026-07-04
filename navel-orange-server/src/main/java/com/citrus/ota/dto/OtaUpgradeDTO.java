package com.citrus.ota.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "OTA 升级请求")
public class OtaUpgradeDTO {

    @NotNull(message = "固件包 ID 不能为空")
    @Schema(description = "固件包 ID")
    private Long packageId;
}
