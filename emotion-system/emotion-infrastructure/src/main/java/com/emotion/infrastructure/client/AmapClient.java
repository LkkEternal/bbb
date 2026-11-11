package com.emotion.infrastructure.client;

import com.emotion.common.exception.BizException;
import com.emotion.common.response.ErrorCode;
import com.emotion.infrastructure.config.AmapProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

@Slf4j
@Component
public class AmapClient {

    private final AmapProperties amapProperties;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public AmapClient(AmapProperties amapProperties) {
        this.amapProperties = amapProperties;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    // ==================== 地理编码 ====================

    public GeoResult geo(String address, String city) {
        String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8);
        StringBuilder path = new StringBuilder("/v3/geocode/geo?address=" + encodedAddress + "&key=" + amapProperties.getApiKey());
        if (city != null && !city.isEmpty()) {
            String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8);
            path.append("&city=").append(encodedCity);
        }
        return doGet(path.toString(), GeoResult.class);
    }

    public GeoResult geo(String address) {
        return geo(address, null);
    }

    // ==================== 逆地理编码 ====================

    public GeocodeResult regeo(double longitude, double latitude) {
        String location = longitude + "," + latitude;
        String path = "/v3/geocode/regeo?location=" + location + "&key=" + amapProperties.getApiKey();
        return doGet(path, GeocodeResult.class);
    }

    public GeocodeResult regeo(String location) {
        String path = "/v3/geocode/regeo?location=" + location + "&key=" + amapProperties.getApiKey();
        return doGet(path, GeocodeResult.class);
    }

    // ==================== 天气查询 ====================

    public WeatherLiveResult getLiveWeather(String city) {
        String path = "/v3/weather/weatherInfo?city=" + city + "&key=" + amapProperties.getApiKey() + "&extensions=base";
        String url = amapProperties.getApiHost() + path;
        try {
            log.info("请求高德地图API: {}", url);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            String body = response.body();
            log.debug("API响应: {}", body);

            com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(body);
            WeatherLiveResult result = new WeatherLiveResult();
            result.setStatus(root.has("status") ? root.get("status").asText() : null);
            result.setInfo(root.has("info") ? root.get("info").asText() : null);
            result.setInfocode(root.has("infocode") ? root.get("infocode").asText() : null);

            if ("1".equals(result.getStatus()) && root.has("lives") && root.get("lives").isArray()) {
                com.fasterxml.jackson.databind.JsonNode livesNode = root.get("lives");
                for (com.fasterxml.jackson.databind.JsonNode item : livesNode) {
                    if (item.isObject() && item.size() > 0) {
                        WeatherLive live = objectMapper.treeToValue(item, WeatherLive.class);
                        if (result.getLives() == null) {
                            result.setLives(new java.util.ArrayList<>());
                        }
                        result.getLives().add(live);
                    }
                }
            }
            return result;
        } catch (Exception e) {
            log.error("天气查询失败: city={}", city, e);
            throw new BizException(ErrorCode.WEATHER_API_ERROR, "天气查询失败: " + e.getMessage());
        }
    }

    public WeatherForecastResult getForecastWeather(String city) {
        String path = "/v3/weather/weatherInfo?city=" + city + "&key=" + amapProperties.getApiKey() + "&extensions=all";
        return doGet(path, WeatherForecastResult.class);
    }

    // ==================== IP定位 ====================

    public IpResult ip(String ip) {
        String path = "/v3/ip?ip=" + ip + "&key=" + amapProperties.getApiKey();
        return doGet(path, IpResult.class);
    }

    // ==================== 通用请求方法 ====================

    private <T> T doGet(String path, Class<T> responseType) {
        try {
            String url = amapProperties.getApiHost() + path;
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

            if (result instanceof AmapResponse) {
                String status = ((AmapResponse) result).getStatus();
                if (!"1".equals(status)) {
                    String info = ((AmapResponse) result).getInfo();
                    log.error("高德地图API错误: {}", info);
                    throw new BizException(ErrorCode.WEATHER_API_ERROR, "高德地图API错误: " + info);
                }
            }

            return result;
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("高德地图API请求异常", e);
            throw new BizException(ErrorCode.WEATHER_API_ERROR, "高德地图API请求异常: " + e.getMessage());
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
        private Object district;
        private String adcode;
        private String location;
        private String level;

        public String getDistrictAsString() {
            if (district == null) return null;
            if (district instanceof String) return (String) district;
            if (district instanceof List) {
                List<?> list = (List<?>) district;
                return list.isEmpty() ? null : list.get(0).toString();
            }
            return district.toString();
        }
    }

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
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WeatherLiveResult extends AmapResponse {
        private List<WeatherLive> lives;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WeatherLive {
        private String province;
        private String city;
        @JsonProperty("adcode")
        private String adcode;
        @JsonProperty("weather")
        private String weather;
        private String temperature;
        @JsonProperty("winddirection")
        private String windDirection;
        @JsonProperty("windpower")
        private String windPower;
        private String humidity;
        @JsonProperty("reporttime")
        private String reportTime;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WeatherForecastResult extends AmapResponse {
        private List<WeatherForecast> forecasts;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WeatherForecast {
        private String province;
        private String city;
        @JsonProperty("adcode")
        private String adcode;
        @JsonProperty("reportTime")
        private String reportTime;
        private List<ForecastCast> casts;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ForecastCast {
        private String date;
        private String week;
        @JsonProperty("dayWeather")
        private String dayWeather;
        @JsonProperty("nightWeather")
        private String nightWeather;
        @JsonProperty("dayTemp")
        private String dayTemp;
        @JsonProperty("nightTemp")
        private String nightTemp;
        @JsonProperty("dayHumidity")
        private String dayHumidity;
        @JsonProperty("nightHumidity")
        private String nightHumidity;
        @JsonProperty("dayWindDir")
        private String dayWindDir;
        @JsonProperty("nightWindDir")
        private String nightWindDir;
        @JsonProperty("dayWindPower")
        private String dayWindPower;
        @JsonProperty("nightWindPower")
        private String nightWindPower;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IpResult extends AmapResponse {
        private String province;
        private String city;
        private String adcode;
        private String rectangle;
    }
}
