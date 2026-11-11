package com.emotion.infrastructure.client;

import com.emotion.common.exception.BizException;
import com.emotion.common.response.ErrorCode;
import com.emotion.infrastructure.config.QweatherProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.zip.GZIPInputStream;

@Slf4j
@Component
public class QweatherClient {

    private static final String SUCCESS_CODE = "200";

    private final QweatherProperties qweatherProperties;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public QweatherClient(QweatherProperties qweatherProperties) {
        this.qweatherProperties = qweatherProperties;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public NowResponse getWeatherNow(String location) {
        String url = qweatherProperties.getApiHost() + "/v7/weather/now?location=" + location;
        return exchange(url, NowResponse.class, false);
    }

    public DailyResponse getWeather3d(String location) {
        String url = qweatherProperties.getApiHost() + "/v7/weather/3d?location=" + location;
        return exchange(url, DailyResponse.class, false);
    }

    public GeoResponse cityLookup(String location) {
        String url = "https://geoapi.qweather.com/v2/city/lookup?location=" + location + "&range=cn&number=1&key=" + qweatherProperties.getApiKey();
        return exchange(url, GeoResponse.class, true);
    }

    private <T> T exchange(String url, Class<T> responseType, boolean useQueryKey) {
        String encodedUrl = url.replace(" ", "%20");
        log.info("请求和风天气API: {}", encodedUrl);

        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(encodedUrl))
                    .header("Accept", "application/json")
                    .header("Accept-Encoding", "gzip");
            if (!useQueryKey) {
                requestBuilder.header("X-QW-Api-Key", qweatherProperties.getApiKey());
            }
            HttpRequest request = requestBuilder.GET().build();

            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

            int statusCode = response.statusCode();
            byte[] body = response.body();

            log.debug("API响应状态码: {}", statusCode);

            if (statusCode != 200) {
                String errorBody = (body != null && body.length > 0) ? new String(body, StandardCharsets.UTF_8) : "";
                log.error("天气API返回非200状态码: {}, 响应: {}", statusCode, errorBody);
                throw new BizException(ErrorCode.WEATHER_API_ERROR, "HTTP状态码: " + statusCode);
            }

            if (body == null || body.length == 0) {
                throw new BizException(ErrorCode.WEATHER_DATA_EMPTY);
            }

            String jsonStr;
            try {
                if (body.length >= 2 && body[0] == (byte) 0x1f && body[1] == (byte) 0x8b) {
                    log.debug("响应为gzip压缩格式，开始解压");
                    try (GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(body));
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
            } catch (BizException e) {
                throw e;
            } catch (Exception e) {
                log.error("gzip解压失败", e);
                throw new BizException(ErrorCode.WEATHER_DATA_PARSE_ERROR);
            }

            log.debug("API响应内容: {}", jsonStr);

            try {
                T result = objectMapper.readValue(jsonStr, responseType);

                if (result instanceof BaseResponse) {
                    String code = ((BaseResponse) result).getCode();
                    if (!SUCCESS_CODE.equals(code)) {
                        log.error("天气API返回错误码: {}", code);
                        throw new BizException(ErrorCode.WEATHER_API_ERROR, "错误码: " + code);
                    }
                }

                return result;
            } catch (BizException e) {
                throw e;
            } catch (Exception e) {
                log.error("JSON解析失败", e);
                throw new BizException(ErrorCode.WEATHER_DATA_PARSE_ERROR);
            }
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("天气API请求异常", e);
            throw new BizException(ErrorCode.WEATHER_API_ERROR, e.getMessage());
        }
    }

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

    @Data
    public static class GeoResponse extends BaseResponse {
        private List<GeoLocation> location;
    }

    @Data
    public static class GeoLocation {
        private String name;
        private String id;
        private String lat;
        private String lon;
        private String adm1;
        private String adm2;
        private String country;
    }
}
