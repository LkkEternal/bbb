package com.citrus;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

/**
 * IoTDA 命令下发独立测试
 * <p>
 * 直接使用华为云 REST API 发送 control_ptz 命令到 DiseaseMonitor 设备，
 * 不依赖 Spring 容器，用于验证命令下发链路是否通畅。
 * <p>
 * 用法: 直接运行 main 方法
 */
public class IotdaCommandTest {

    // ==================== 从 application-dev.yml 读取的配置 ====================

    /** IoTDA 应用侧接入地址（不加 https://） */
    private static final String IOTDA_ENDPOINT = "bf593413cf.st1.iotda-app.cn-south-1.myhuaweicloud.com";

    /** 项目 ID */
    private static final String PROJECT_ID = "019e966a75dd7184ab7e7e69f2ad38a5";

    /** IAM 认证 AK/SK */
    private static final String ACCESS_KEY = "HPUA9WPRCR06ZD8MZHYB";
    private static final String SECRET_KEY = "nirRWipIYIWHHmy7oBjjOSKRtEF0oqL2YfgMmN5F";

    // ==================== 设备信息 ====================

    /** IoTDA 上的设备 ID（即 device_id） */
    private static final String DEVICE_ID = "6a2e6c41c9429d337f50a67c_Equipment01";

    /** 服务 ID（产品模型中定义） */
    private static final String SERVICE_ID = "DiseaseMonitor";

    /** 命令名称 */
    private static final String COMMAND_NAME = "control_ptz";

    // ==================== HTTP 客户端 ====================

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // 模式: sync = 同步, async = 异步（缓存）, status = 仅查询设备状态
    private static final String MODE = "status";  // sync / async / status

    // ==================== 主入口 ====================

    public static void main(String[] args) throws Exception {
        System.out.println("═══════════════════════════════════════════════");
        System.out.println("  IoTDA 命令下发测试");
        System.out.println("  设备: " + DEVICE_ID);
        System.out.println("  服务: " + SERVICE_ID);
        System.out.println("  命令: " + COMMAND_NAME);
        System.out.println("  模式: " + (MODE.equals("sync") ? "同步" : "异步（缓存）"));
        System.out.println("═══════════════════════════════════════════════");
        System.out.println();

        // 1. 获取 Token
        System.out.println("[1/3] 获取 IAM Token ...");
        String token = getToken();
        System.out.println("  ✅ Token 获取成功");
        System.out.println();

        // 2. 构造命令参数
        Map<String, Object> paras = new LinkedHashMap<>();
        paras.put("pitch", 0);
        paras.put("roll", 0);
        paras.put("yaw", 45);
        System.out.println("[2/3] 命令参数: " + objectMapper.writeValueAsString(paras));
        System.out.println();

        // 3. 发送命令或查询状态
        System.out.println("[3/3] " + (MODE.equals("status") ? "查询设备状态 ..." : "发送命令到 IoTDA ..."));
        String result;
        if (MODE.equals("status")) {
            result = getDeviceDetail(token, DEVICE_ID);
        } else if (MODE.equals("sync")) {
            result = sendSyncCommand(token, DEVICE_ID, SERVICE_ID, COMMAND_NAME, paras);
        } else {
            result = sendAsyncCommand(token, DEVICE_ID, SERVICE_ID, COMMAND_NAME, paras);
        }
        System.out.println("  响应: " + result);

        // 4. 解析结果
        JsonNode root = objectMapper.readTree(result);
        System.out.println();
        System.out.println("═══════════════════════════════════════════════");

        if (MODE.equals("status")) {
            // 设备详情响应
            String deviceName = root.has("device_name") ? root.get("device_name").asText() : "N/A";
            String status = root.has("status") ? root.get("status").asText() : "N/A";
            String productName = root.has("product_name") ? root.get("product_name").asText() : "N/A";
            System.out.println("  设备名称: " + deviceName);
            System.out.println("  设备状态: " + status + (status.equals("ONLINE") ? " ✅ 在线" : " ⛔ 离线"));
            System.out.println("  产品名称: " + productName);
            System.out.println();
            System.out.println("  ℹ️ MQTT 设备仅支持同步命令下发");
            System.out.println("  ℹ️ 同步命令要求设备在线");
            System.out.println("  ℹ️ 如需测试命令下发，请先用 MQTT 客户端连接设备");
            System.out.println("     (host: bf593413cf.st1.iotda-device.cn-south-1.myhuaweicloud.com:8883)");
        } else if (root.has("command_id")) {
            System.out.println("  命令 ID: " + root.get("command_id").asText());
        }
        if (root.has("status")) {
            System.out.println("  状态: " + root.get("status").asText());
        }

        if (root.has("response")) {
            JsonNode resp = root.get("response");
            int resultCode = resp.has("result_code") ? resp.get("result_code").asInt() : -1;
            String responseName = resp.has("response_name") ? resp.get("response_name").asText() : "N/A";
            System.out.println("  设备响应码: " + resultCode + (resultCode == 0 ? " (成功)" : " (失败)"));
            System.out.println("  响应名称: " + responseName);
        }
        if (root.has("error_code")) {
            System.out.println("  错误码: " + root.get("error_code").asText());
            System.out.println("  错误信息: " + root.get("error_msg").asText());
        }
        System.out.println("═══════════════════════════════════════════════");
    }

    // ==================== 获取 IAM Token ====================

    private static String getToken() throws Exception {
        String region = IOTDA_ENDPOINT.contains("cn-south-1") ? "cn-south-1" : "cn-north-4";
        String url = "https://iam." + region + ".myhuaweicloud.com/v3/auth/tokens?nocatalog=true";

        String body = "{"
                + "\"auth\":{"
                + "\"identity\":{"
                + "\"methods\":[\"hw_ak_sk\"],"
                + "\"hw_ak_sk\":{"
                + "\"access\":{\"key\":\"" + ACCESS_KEY + "\"},"
                + "\"secret\":{\"key\":\"" + SECRET_KEY + "\"}"
                + "}"
                + "},"
                + "\"scope\":{"
                + "\"project\":{\"name\":\"" + region + "\"}"
                + "}"
                + "}"
                + "}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return response.headers().firstValue("X-Subject-Token").orElseThrow(() -> new RuntimeException("Token not found"));
        } else {
            throw new RuntimeException("获取Token失败: " + response.statusCode() + " - " + response.body());
        }
    }

    // ==================== 查询设备详情 ====================

    private static String getDeviceDetail(String token, String deviceId) throws Exception {
        String url = "https://" + IOTDA_ENDPOINT + "/v5/iot/" + PROJECT_ID + "/devices/" + deviceId;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("X-Auth-Token", token)
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    // ==================== 下发同步命令 ====================

    private static String sendSyncCommand(String token, String deviceId, String serviceId,
                                           String commandName, Map<String, Object> paras) throws Exception {
        String url = "https://" + IOTDA_ENDPOINT + "/v5/iot/" + PROJECT_ID + "/devices/" + deviceId + "/commands";

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("service_id", serviceId);
        body.put("command_name", commandName);
        body.put("paras", paras);

        String requestBody = objectMapper.writeValueAsString(body);
        System.out.println("  请求 URL: " + url);
        System.out.println("  请求体: " + requestBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("X-Auth-Token", token)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("  状态码: " + response.statusCode());
        return response.body();
    }

    // ==================== 下发异步命令（缓存） ====================

    private static String sendAsyncCommand(String token, String deviceId, String serviceId,
                                            String commandName, Map<String, Object> paras) throws Exception {
        String url = "https://" + IOTDA_ENDPOINT + "/v5/iot/" + PROJECT_ID + "/devices/" + deviceId + "/async-commands";

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("service_id", serviceId);
        body.put("command_name", commandName);
        body.put("paras", paras);
        body.put("expire_time", 3600);         // 缓存 1 小时
        body.put("send_strategy", "delay");    // 缓存下发，设备上线后再执行

        String requestBody = objectMapper.writeValueAsString(body);
        System.out.println("  请求 URL: " + url);
        System.out.println("  请求体: " + requestBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("X-Auth-Token", token)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("  状态码: " + response.statusCode());
        return response.body();
    }
}
