package com.citrus.collector.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "设备病害识别上报请求参数")
public class DeviceDetectionDTO {

    @Schema(description = "设备编码（可选，从API Key自动解析）", example = "maixcam_01")
    private String device;

    @Schema(description = "病害识别结果列表")
    private List<DiseaseItem> diseases;

    @Schema(description = "GPS坐标")
    private GpsData gps;

    @Data
    @Schema(description = "单条病害识别结果")
    public static class DiseaseItem {

        @Schema(description = "病害序号", example = "4")
        private Integer id;

        @Schema(description = "病害名称", example = "S4_Red_nose_fruit")
        private String name;

        @Schema(description = "置信度", example = "0.94")
        private BigDecimal conf;

        @Schema(description = "检测数量", example = "1")
        private Integer cnt;

        @Schema(description = "图片文件名", example = "citrus_1781954601.jpg")
        private String img;

        @Schema(description = "时间戳（秒）", example = "1781954601")
        private Long ts;
    }

    @Data
    @Schema(description = "GPS坐标")
    public static class GpsData {

        @Schema(description = "纬度", example = "22.543097")
        private BigDecimal lat;

        @Schema(description = "经度", example = "114.057861")
        private BigDecimal lng;

        @Schema(description = "海拔（米）", example = "10")
        private BigDecimal alt;
    }
}
