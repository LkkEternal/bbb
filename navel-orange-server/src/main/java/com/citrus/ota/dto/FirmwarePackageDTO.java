package com.citrus.ota.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "新增固件包请求")
public class FirmwarePackageDTO {

    @NotBlank(message = "文件名不能为空")
    @Schema(description = "文件名，如 test_01.bin")
    private String fileName;

    @NotBlank(message = "版本号不能为空")
    @Schema(description = "版本号，如 v1.0.1")
    private String version;

    @NotNull(message = "文件大小不能为空")
    @Schema(description = "文件大小（字节）")
    private Long fileSize;

    @NotBlank(message = "文件 URL 不能为空")
    @Schema(description = "OBS 下载 URL")
    private String fileUrl;

    @Schema(description = "MD5 校验值")
    private String md5;

    @Schema(description = "版本更新说明")
    private String description;

    @Schema(description = "状态: 0=待发布 1=已发布 2=已废弃", example = "0")
    private Integer status;
}
