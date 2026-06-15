package com.citrus.common.iotda;

import com.citrus.common.exception.BizException;
import com.citrus.common.result.ResultCode;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 华为云IoTDA工具类
 * 用于获取设备上报数据
 */
@Slf4j
@Component
public class HuaweiIotdaClient {

    @Value("${huawei.iotda.endpoint:}")
    private String endpoint;

    @Value("${huawei.iotda.project-id:}")
    private String projectId;

    @Value("${huawei.iotda.access-key:}")
    private String accessKey;

    @Value("${huawei.iotda.secret-key:}")
    private String secretKey;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public HuaweiIotdaClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    // ==================== 设备管理 ====================

    /**
     * 查询设备列表
     * @param productId 产品ID（可选）
     * @param limit 每页数量
     * @param marker 分页标记
     * @return 设备列表响应
     */
    public DeviceListResponse listDevices(String productId, Integer limit, String marker) {
        StringBuilder path = new StringBuilder("/v5/iot/" + projectId + "/devices?");
        if (productId != null) {
            path.append("product_id=").append(productId).append("&");
        }
        if (limit != null) {
            path.append("limit=").append(limit).append("&");
        }
        if (marker != null) {
            path.append("marker=").append(marker).append("&");
        }
        return doGet(path.toString(), DeviceListResponse.class);
    }

    /**
     * 查询设备详情
     * @param deviceId 设备ID
     * @return 设备详情
     */
    public DeviceDetail getDevice(String deviceId) {
        String path = "/v5/iot/" + projectId + "/devices/" + deviceId;
        return doGet(path, DeviceDetail.class);
    }

    // ==================== 设备属性 ====================

    /**
     * 查询设备属性（设备影子）
     * @param deviceId 设备ID
     * @return 设备影子数据
     */
    public DeviceShadowResponse getDeviceShadow(String deviceId) {
        String path = "/v5/iot/" + projectId + "/devices/" + deviceId + "/shadow";
        return doGet(path, DeviceShadowResponse.class);
    }

    /**
     * 查询设备属性（直接获取）
     * @param deviceId 设备ID
     * @param serviceId 服务ID（可选）
     * @return 设备属性
     */
    public Map<String, Object> getDeviceProperties(String deviceId, String serviceId) {
        String path = "/v5/iot/" + projectId + "/devices/" + deviceId + "/shadow";
        DeviceShadowResponse shadow = doGet(path, DeviceShadowResponse.class);
        if (shadow != null && shadow.getShadow() != null) {
            for (ShadowItem item : shadow.getShadow()) {
                if (serviceId == null || serviceId.equals(item.getServiceId())) {
                    return item.getReported() != null ? item.getReported().getProperties() : null;
                }
            }
        }
        return null;
    }

    // ==================== 设备消息 ====================

    /**
     * 下发设备消息
     * @param deviceId 设备ID
     * @param message 消息内容
     * @return 消息ID
     */
    public String sendMessage(String deviceId, Map<String, Object> message) {
        String path = "/v5/iot/" + projectId + "/devices/" + deviceId + "/messages";
        SendMessageResponse response = doPost(path, message, SendMessageResponse.class);
        return response != null ? response.getMessageId() : null;
    }

    /**
     * 查询设备消息列表
     * @param deviceId 设备ID
     * @param limit 每页数量
     * @return 消息列表
     */
    public MessageListResponse listMessages(String deviceId, Integer limit) {
        StringBuilder path = new StringBuilder("/v5/iot/" + projectId + "/devices/" + deviceId + "/messages?");
        if (limit != null) {
            path.append("limit=").append(limit);
        }
        return doGet(path.toString(), MessageListResponse.class);
    }

    // ==================== 设备命令 ====================

    /**
     * 下发设备同步命令
     * @param deviceId 设备ID
     * @param serviceId 服务ID
     * @param commandName 命令名称
     * @param paras 命令参数
     * @return 命令响应
     */
    public CommandResponse sendCommand(String deviceId, String serviceId, String commandName, Map<String, Object> paras) {
        String path = "/v5/iot/" + projectId + "/devices/" + deviceId + "/commands";
        Map<String, Object> body = new HashMap<>();
        body.put("service_id", serviceId);
        body.put("command_name", commandName);
        body.put("paras", paras);
        return doPost(path, body, CommandResponse.class);
    }

    // ==================== 产品管理 ====================

    /**
     * 查询产品列表
     * @param limit 每页数量
     * @return 产品列表
     */
    public ProductListResponse listProducts(Integer limit) {
        StringBuilder path = new StringBuilder("/v5/iot/" + projectId + "/products?");
        if (limit != null) {
            path.append("limit=").append(limit);
        }
        return doGet(path.toString(), ProductListResponse.class);
    }

    /**
     * 查询产品详情
     * @param productId 产品ID
     * @return 产品详情
     */
    public ProductDetail getProduct(String productId) {
        String path = "/v5/iot/" + projectId + "/products/" + productId;
        return doGet(path, ProductDetail.class);
    }

    // ==================== HTTP请求实现 ====================

    private <T> T doGet(String path, Class<T> responseType) {
        try {
            String url = "https://" + endpoint + path;
            String timestamp = ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'"));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("X-Sdk-Date", timestamp)
                    .header("Authorization", buildAuthorization("GET", path, timestamp))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return objectMapper.readValue(response.body(), responseType);
            } else {
                log.error("IoTDA请求失败: {} - {}", response.statusCode(), response.body());
                throw new BizException(ResultCode.INTERNAL_ERROR, "IoTDA请求失败: " + response.statusCode());
            }
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("IoTDA请求异常", e);
            throw new BizException(ResultCode.INTERNAL_ERROR, "IoTDA请求异常: " + e.getMessage());
        }
    }

    private <T> T doPost(String path, Object body, Class<T> responseType) {
        try {
            String url = "https://" + endpoint + path;
            String timestamp = ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'"));
            String requestBody = objectMapper.writeValueAsString(body);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("X-Sdk-Date", timestamp)
                    .header("Authorization", buildAuthorization("POST", path, timestamp))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return objectMapper.readValue(response.body(), responseType);
            } else {
                log.error("IoTDA请求失败: {} - {}", response.statusCode(), response.body());
                throw new BizException(ResultCode.INTERNAL_ERROR, "IoTDA请求失败: " + response.statusCode());
            }
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("IoTDA请求异常", e);
            throw new BizException(ResultCode.INTERNAL_ERROR, "IoTDA请求异常: " + e.getMessage());
        }
    }

    private String buildAuthorization(String method, String path, String timestamp) {
        try {
            String canonicalRequest = method + "\n" + path + "\n\n" + "host:" + endpoint + "\n\nhost\n";
            String stringToSign = "SDK-HMAC-SHA256\n" + timestamp + "\n" + sha256Hex(canonicalRequest);

            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] signature = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
            String signatureHex = bytesToHex(signature);

            return "SDK-HMAC-SHA256 Credential=" + accessKey + ", SignedHeaders=host, Signature=" + signatureHex;
        } catch (Exception e) {
            log.error("构建认证信息失败", e);
            throw new BizException(ResultCode.INTERNAL_ERROR, "构建认证信息失败");
        }
    }

    private String sha256Hex(String data) throws Exception {
        java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(hash);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    // ==================== 响应数据结构 ====================

    @Data
    public static class DeviceListResponse {
        @JsonProperty("devices")
        private List<DeviceDetail> devices;

        @JsonProperty("page")
        private PageInfo page;
    }

    @Data
    public static class DeviceDetail {
        @JsonProperty("device_id")
        private String deviceId;

        @JsonProperty("node_id")
        private String nodeId;

        @JsonProperty("product_id")
        private String productId;

        @JsonProperty("product_name")
        private String productName;

        @JsonProperty("device_name")
        private String deviceName;

        @JsonProperty("status")
        private String status;

        @JsonProperty("description")
        private String description;

        @JsonProperty("fw_version")
        private String fwVersion;

        @JsonProperty("sw_version")
        private String swVersion;

        @JsonProperty("tags")
        private List<Tag> tags;
    }

    @Data
    public static class Tag {
        @JsonProperty("tag_key")
        private String tagKey;

        @JsonProperty("tag_value")
        private String tagValue;
    }

    @Data
    public static class PageInfo {
        @JsonProperty("marker")
        private String marker;

        @JsonProperty("total")
        private Integer total;

        @JsonProperty("limit")
        private Integer limit;
    }

    @Data
    public static class DeviceShadowResponse {
        @JsonProperty("device_id")
        private String deviceId;

        @JsonProperty("shadow")
        private List<ShadowItem> shadow;
    }

    @Data
    public static class ShadowItem {
        @JsonProperty("service_id")
        private String serviceId;

        @JsonProperty("reported")
        private ShadowData reported;

        @JsonProperty("desired")
        private ShadowData desired;
    }

    @Data
    public static class ShadowData {
        @JsonProperty("properties")
        private Map<String, Object> properties;

        @JsonProperty("event_time")
        private String eventTime;
    }

    @Data
    public static class SendMessageResponse {
        @JsonProperty("message_id")
        private String messageId;
    }

    @Data
    public static class MessageListResponse {
        @JsonProperty("messages")
        private List<MessageDetail> messages;
    }

    @Data
    public static class MessageDetail {
        @JsonProperty("message_id")
        private String messageId;

        @JsonProperty("type")
        private String type;

        @JsonProperty("payload")
        private String payload;

        @JsonProperty("status")
        private String status;

        @JsonProperty("created_time")
        private String createdTime;
    }

    @Data
    public static class CommandResponse {
        @JsonProperty("command_id")
        private String commandId;

        @JsonProperty("response")
        private Object response;

        @JsonProperty("result_code")
        private Integer resultCode;

        @JsonProperty("response_name")
        private String responseName;
    }

    @Data
    public static class ProductListResponse {
        @JsonProperty("products")
        private List<ProductDetail> products;
    }

    @Data
    public static class ProductDetail {
        @JsonProperty("product_id")
        private String productId;

        @JsonProperty("product_name")
        private String productName;

        @JsonProperty("device_type")
        private String deviceType;

        @JsonProperty("description")
        private String description;

        @JsonProperty("service_capabilities")
        private List<ServiceCapability> serviceCapabilities;
    }

    @Data
    public static class ServiceCapability {
        @JsonProperty("service_id")
        private String serviceId;

        @JsonProperty("service_type")
        private String serviceType;

        @JsonProperty("description")
        private String description;
    }
}
