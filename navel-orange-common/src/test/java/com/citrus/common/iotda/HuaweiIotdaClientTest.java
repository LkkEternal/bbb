package com.citrus.common.iotda;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

@Slf4j
@SpringBootTest(
    classes = HuaweiIotdaClientTest.TestConfig.class,
    properties = {
        "huawei.iotda.endpoint=bf593413cf.st1.iotda-app.cn-south-1.myhuaweicloud.com",
        "huawei.iotda.project-id=019e966a75dd7184ab7e7e69f2ad38a5",
        "huawei.iotda.access-key=HPUA9WPRCR06ZD8MZHYB",
        "huawei.iotda.secret-key=nirRWipIYIWHHmy7oBjjOSKRtEF0oqL2YfgMmN5F",
        "huawei.iotda.device-id=6a2e6c41c9429d337f50a67c_Equipment01",
        "huawei.iotda.device-secret=ce7924b9318baeec1225669dafcb4850"
    }
)
public class HuaweiIotdaClientTest {

    @SpringBootApplication
    static class TestConfig {
    }

    @Autowired
    private HuaweiIotdaClient iotdaClient;

    private final ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    private static final String DEVICE_ID = "6a2e6c41c9429d337f50a67c_Equipment01";

    @Test
    public void testGetDevice() throws JsonProcessingException {
        log.info("===== 测试查询设备详情 =====");

        try {
            HuaweiIotdaClient.DeviceDetail device = iotdaClient.getDevice(DEVICE_ID);
            log.info("设备ID: {}", device.getDeviceId());
            log.info("设备名称: {}", device.getDeviceName());
            log.info("产品ID: {}", device.getProductId());
            log.info("设备状态: {}", device.getStatus());
            log.info("完整响应:\n{}", objectMapper.writeValueAsString(device));
        } catch (Exception e) {
            log.error("查询设备失败: {}", e.getMessage());
        }
    }

    @Test
    public void testListDevices() throws JsonProcessingException {
        log.info("===== 测试查询设备列表 =====");

        try {
            HuaweiIotdaClient.DeviceListResponse response = iotdaClient.listDevices(null, 10, null);
            if (response.getDevices() != null) {
                log.info("设备数量: {}", response.getDevices().size());
                for (HuaweiIotdaClient.DeviceDetail device : response.getDevices()) {
                    log.info("  - {} ({}) 状态:{}", device.getDeviceName(), device.getDeviceId(), device.getStatus());
                }
            }
            log.info("完整响应:\n{}", objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            log.error("查询设备列表失败: {}", e.getMessage());
        }
    }

    @Test
    public void testGetDeviceShadow() throws JsonProcessingException {
        log.info("===== 测试查询设备影子数据 =====");

        try {
            HuaweiIotdaClient.DeviceShadowResponse shadow = iotdaClient.getDeviceShadow(DEVICE_ID);
            log.info("设备ID: {}", shadow.getDeviceId());
            if (shadow.getShadow() != null) {
                for (HuaweiIotdaClient.ShadowItem item : shadow.getShadow()) {
                    log.info("服务ID: {}", item.getServiceId());
                    if (item.getReported() != null) {
                        log.info("上报属性: {}", objectMapper.writeValueAsString(item.getReported().getProperties()));
                    }
                }
            }
            log.info("完整响应:\n{}", objectMapper.writeValueAsString(shadow));
        } catch (Exception e) {
            log.error("查询设备影子失败: {}", e.getMessage());
        }
    }

    @Test
    public void testGetDeviceProperties() throws JsonProcessingException {
        log.info("===== 测试获取设备属性 =====");

        try {
            Map<String, Object> properties = iotdaClient.getDeviceProperties(DEVICE_ID, null);
            if (properties != null) {
                log.info("设备属性:");
                properties.forEach((key, value) -> log.info("  {} = {}", key, value));
            } else {
                log.info("未获取到设备属性");
            }
        } catch (Exception e) {
            log.error("获取设备属性失败: {}", e.getMessage());
        }
    }

    @Test
    public void testListProducts() throws JsonProcessingException {
        log.info("===== 测试查询产品列表 =====");

        try {
            HuaweiIotdaClient.ProductListResponse response = iotdaClient.listProducts(10);
            if (response.getProducts() != null) {
                log.info("产品数量: {}", response.getProducts().size());
                for (HuaweiIotdaClient.ProductDetail product : response.getProducts()) {
                    log.info("  - {} ({})", product.getProductName(), product.getProductId());
                }
            }
            log.info("完整响应:\n{}", objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            log.error("查询产品列表失败: {}", e.getMessage());
        }
    }
}
