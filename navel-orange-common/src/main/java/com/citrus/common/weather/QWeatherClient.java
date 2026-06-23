package com.citrus.common.weather;

import com.citrus.common.exception.BizException;
import com.citrus.common.result.ResultCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.zip.GZIPInputStream;

@Slf4j
@Component
public class QWeatherClient {

    private static final String SUCCESS_CODE = "200";

    @Value("${weather.qweather.api-key}")
    private String apiKey;

    @Value("${weather.qweather.api-host}")
    private String apiHost;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public QWeatherClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 获取实时天气
     * @param location 经纬度，格式："经度,纬度"（如：114.93,25.38）
     * @return 实时天气数据
     */
    public NowResponse getWeatherNow(String location) {
        String url = apiHost + "/v7/weather/now?location=" + location;
        return exchange(url, NowResponse.class);
    }

    /**
     * 获取24小时天气预报
     * @param location 经纬度，格式："经度,纬度"
     * @return 24小时预报数据
     */
    public HourlyResponse getWeatherHourly(String location) {
        String url = apiHost + "/v7/weather/24h?location=" + location;
        return exchange(url, HourlyResponse.class);
    }

    /**
     * 获取7天天气预报
     * @param location 经纬度，格式："经度,纬度"（如：114.93,25.38）
     * @return 7天预报数据
     */
    public DailyResponse getWeather7d(String location) {
        String url = apiHost + "/v7/weather/7d?location=" + location;
        return exchange(url, DailyResponse.class);
    }

    private <T> T exchange(String url, Class<T> responseType) {
        // 对URL中的特殊字符进行编码（如空格编码为%20）
        String encodedUrl = url.replace(" ", "%20");
        log.info("请求和风天气API: {}", encodedUrl);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(encodedUrl))
                    .header("X-QW-Api-Key", apiKey)
                    .header("Accept", "application/json")
                    .header("Accept-Encoding", "gzip")
                    .GET()
                    .build();

            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

            int statusCode = response.statusCode();
            byte[] body = response.body();

            log.debug("API响应状态码: {}", statusCode);

            if (body == null || body.length == 0) {
                throw new BizException(ResultCode.WEATHER_DATA_EMPTY);
            }

            String jsonStr;
            try {
                // 判断是否为gzip压缩格式
                if (body.length >= 2 && body[0] == (byte) 0x1f && body[1] == (byte) 0x8b) {
                    log.debug("响应为gzip压缩格式，开始解压");
                    // gzip解压
                    try (GZIPInputStream gzipInputStream = new GZIPInputStream(new java.io.ByteArrayInputStream(body));
                         BufferedReader reader = new BufferedReader(new InputStreamReader(gzipInputStream, StandardCharsets.UTF_8))) {
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            sb.append(line);
                        }
                        jsonStr = sb.toString();
                    }
                } else {
                    jsonStr = new String(body, StandardCharsets.UTF_8);
                }
            } catch (Exception e) {
                log.error("gzip解压失败", e);
                throw new BizException(ResultCode.WEATHER_DATA_PARSE_ERROR);
            }

            log.debug("API响应内容: {}", jsonStr);

            // 检查HTTP状态码
            if (statusCode != 200) {
                log.error("天气API返回非200状态码: {}, 响应: {}", statusCode, jsonStr);
                throw new BizException(ResultCode.WEATHER_API_ERROR, "HTTP状态码: " + statusCode);
            }

            try {
                T result = objectMapper.readValue(jsonStr, responseType);

                if (result instanceof BaseResponse) {
                    String code = ((BaseResponse) result).getCode();
                    if (!SUCCESS_CODE.equals(code)) {
                        log.error("天气API返回错误码: {}", code);
                        throw new BizException(ResultCode.WEATHER_API_ERROR, "错误码: " + code);
                    }
                }

                return result;
            } catch (BizException e) {
                throw e;
            } catch (Exception e) {
                log.error("JSON解析失败", e);
                throw new BizException(ResultCode.WEATHER_DATA_PARSE_ERROR);
            }
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("天气API请求异常", e);
            throw new BizException(ResultCode.WEATHER_API_ERROR, e.getMessage());
        }
    }

    // ========== 响应DTO内部类 ==========

    @Data
    public static class BaseResponse {
        private String code;
        private String updateTime;
        private String fxLink;
        private Refer refer;

        @Data
        public static class Refer {
            private List<String> sources;
            private List<String> license;
        }
    }

    @Data
    public static class NowResponse extends BaseResponse {
        private Now now;

        @Data
        public static class Now {
            private String obsTime;
            private String temp;
            private String feelsLike;
            private String icon;
            private String text;
            private String wind360;
            private String windDir;
            private String windScale;
            private String windSpeed;
            private String humidity;
            private String precip;
            private String pressure;
            private String vis;
            private String cloud;
            private String dew;
        }
    }

    @Data
    public static class HourlyResponse extends BaseResponse {
        private List<Hourly> hourly;

        @Data
        public static class Hourly {
            private String fxTime;
            private String temp;
            private String icon;
            private String text;
            private String wind360;
            private String windDir;
            private String windScale;
            private String windSpeed;
            private String humidity;
            private String pop;
            private String precip;
            private String pressure;
            private String cloud;
            private String dew;
        }
    }

    @Data
    public static class DailyResponse extends BaseResponse {
        private List<Daily> daily;

        @Data
        public static class Daily {
            private String fxDate;
            private String sunrise;
            private String sunset;
            private String moonrise;
            private String moonset;
            private String moonPhase;
            private String moonPhaseIcon;
            private String tempMax;
            private String tempMin;
            private String iconDay;
            private String textDay;
            private String iconNight;
            private String textNight;
            private String wind360Day;
            private String windDirDay;
            private String windScaleDay;
            private String windSpeedDay;
            private String wind360Night;
            private String windDirNight;
            private String windScaleNight;
            private String windSpeedNight;
            private String humidity;
            private String precip;
            private String pressure;
            private String vis;
            private String cloud;
            private String uvIndex;
        }
    }
}
