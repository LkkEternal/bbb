package com.citrus.collector.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "病害识别记录列表项")
public class DetectionListVO {

    @Schema(description = "记录ID", example = "80001")
    private Long id;

    @Schema(description = "设备编码", example = "maixcam_01")
    private String deviceCode;

    @Schema(description = "设备名称", example = "1号监测点")
    private String deviceName;

    @Schema(description = "果园ID", example = "2001")
    private Long orchardId;

    @Schema(description = "果园名称", example = "张家果园")
    private String orchardName;

    @Schema(description = "识别图片URL")
    private String imageUrl;

    @Schema(description = "病害编码", example = "S4")
    private String topClassCode;

    @Schema(description = "病害名称", example = "红鼻果")
    private String topClassName;

    @Schema(description = "置信度", example = "0.94")
    private BigDecimal topProbability;

    @Schema(description = "所有病害概率")
    private String allProbabilities;

    @Schema(description = "设备识别时间", example = "2026-06-19 18:30:01")
    private String deviceTimestamp;

    @Schema(description = "服务端接收时间", example = "2026-06-19 18:30:05")
    private String createdAt;

    @Schema(description = "是否已处理", example = "false")
    private Boolean processed;
}
