package com.citrus.common.amap;

import com.citrus.common.exception.BizException;
import com.citrus.common.result.ResultCode;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

/**
 * 高德地图工具类
 * 支持地理编码（地址转经纬度）和逆地理编码（经纬度转地址）
 */
@Slf4j
@Component
public class AmapClient {

    @Value("${map.amap.api-key:}")
    private String apiKey;

    @Value("${map.amap.api-host:https://restapi.amap.com}")
    private String apiHost;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public AmapClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 逆地理编码（经纬度转地址）
     * @param longitude 经度
     * @param latitude 纬度
     * @return 地址信息
     */
    public GeocodeResult regeo(double longitude, double latitude) {
        String location = longitude + "," + latitude;
        String path = "/v3/geocode/regeo?location=" + location + "&key=" + apiKey;
        return doGet(path, GeocodeResult.class);
    }

    /**
     * 逆地理编码（经纬度字符串转地址）
     * @param location 经纬度，格式："经度,纬度"（如：116.470293,39.996171）
     * @return 地址信息
     */
    public GeocodeResult regeo(String location) {
        String path = "/v3/geocode/regeo?location=" + location + "&key=" + apiKey;
        return doGet(path, GeocodeResult.class);
    }

    /**
     * 地理编码（地址转经纬度）
     * @param address 中文地址
     * @param city 城市名（可选，用于限定搜索范围）
     * @return 坐标信息
     */
    public GeoResult geo(String address, String city) {
        StringBuilder path = new StringBuilder("/v3/geocode/geo?address=" + address + "&key=" + apiKey);
        if (city != null && !city.isEmpty()) {
            path.append("&city=").append(city);
        }
        return doGet(path.toString(), GeoResult.class);
    }

    /**
     * 地理编码（地址转经纬度）
     * @param address 中文地址
     * @return 坐标信息
     */
    public GeoResult geo(String address) {
        return geo(address, null);
    }

    /**
     * 根据IP定位
     * @param ip IP地址
     * @return 定位信息
     */
    public IpResult ip(String ip) {
        String path = "/v3/ip?ip=" + ip + "&key=" + apiKey;
        return doGet(path, IpResult.class);
    }

    /**
     * 获取格式化地址（简化方法）
     * @param longitude 经度
     * @param latitude 纬度
     * @return 格式化地址字符串
     */
    private <T> T doGet(String path, Class<T> responseType) {
        try {
            String url = apiHost + path;
            log.info("请求高德地图API: {}", url);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            String body = response.body();

            log.debug("API响应: {}", body);

            T result = objectMapper.readValue(body, responseType);

            // 检查状态码
            if (result instanceof AmapResponse) {
                String status = ((AmapResponse) result).getStatus();
                if (!"1".equals(status)) {
                    String info = ((AmapResponse) result).getInfo();
                    log.error("高德地图API错误: {}", info);
                    throw new BizException(ResultCode.INTERNAL_ERROR, "高德地图API错误: " + info);
                }
            }

            return result;
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("高德地图API请求异常", e);
            throw new BizException(ResultCode.INTERNAL_ERROR, "高德地图API请求异常: " + e.getMessage());
        }
    }

    // ==================== 响应数据结构 ====================

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AmapResponse {
        private String status;
        private String info;
        private String infocode;
    }

    /**
     * 逆地理编码响应
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GeocodeResult extends AmapResponse {
        private RegeoCode regeocode;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RegeoCode {
        @JsonProperty("formatted_address")
        private String formattedAddress;

        private AddressComponent addressComponent;

        private List<Poi> pois;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AddressComponent {
        private String country;
        private String province;
        private String city;
        private String district;
        private String township;

        @JsonProperty("adcode")
        private String adcode;

        @JsonProperty("citycode")
        private String citycode;

        private StreetNumber streetNumber;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StreetNumber {
        private String street;
        private String number;
        private String location;
    }

    @Data
    public static class Geocode {
        @JsonProperty("formatted_address")
        private String formattedAddress;

        private String country;
        private String province;
        private String city;
        private String citycode;
        private String district;
        private String township;
        private String adcode;
        private String street;
        private String number;
        private String location;
        private String level;
    }

    /**
     * 地理编码响应
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GeoResult extends AmapResponse {
        private String count;
        private List<Geocode> geocodes;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Geocode {
        @JsonProperty("formatted_address")
        private String formattedAddress;

        private String country;
        private String province;
        private String city;
        private String district;
        private String adcode;
        private String location;
        private String level;
    }

    /**
     * IP定位响应
     */
    @Data
    public static class AddressComponent {
        private String country;
        private String province;
        private String city;
        private String citycode;
        private String district;
        private String adcode;
        private String township;
        private String towncode;
        private StreetNumber streetNumber;
    }
}
