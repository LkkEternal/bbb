package com.citrus.common.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Slf4j
@SpringBootTest(
    classes = HuaweiObsClientTest.TestConfig.class,
    properties = {
        "huawei.obs.endpoint=obs.cn-south-1.myhuaweicloud.com",
        "huawei.obs.access-key=HPUA9WPRCR06ZD8MZHYB",
        "huawei.obs.secret-key=nirRWipIYIWHHmy7oBjjOSKRtEF0oqL2YfgMmN5F",
        "huawei.obs.bucket-name=qcbch",
        "huawei.obs.url-prefix=https://qcbch.obs.cn-south-1.myhuaweicloud.com"
    }
)
public class HuaweiObsClientTest {

    @SpringBootApplication
    static class TestConfig {
    }

    @Autowired
    private HuaweiObsClient huaweiObsClient;

    // 请修改为你的本地图片目录
    private static final String IMAGE_DIR = "C:\\Users\\28705\\Pictures\\Screenshots";

    @Test
    public void testUploadLocalImages() throws IOException {
        log.info("===== 测试上传本地图片到华为云OBS =====");

        File dir = new File(IMAGE_DIR);
        if (!dir.exists()) {
            log.error("目录不存在: {}", IMAGE_DIR);
            return;
        }

        File[] files = dir.listFiles((d, name) ->
            name.toLowerCase().endsWith(".jpg") ||
            name.toLowerCase().endsWith(".jpeg") ||
            name.toLowerCase().endsWith(".png"));

        if (files == null || files.length == 0) {
            log.error("目录中没有图片文件");
            return;
        }

        // 上传前2张图片
        int count = 0;
        for (File file : files) {
            if (count >= 2) break;

            String contentType = Files.probeContentType(file.toPath());
            if (contentType == null) {
                contentType = file.getName().endsWith(".png") ? "image/png" : "image/jpeg";
            }

            MultipartFile multipartFile = new MockMultipartFile(
                    "file",
                    file.getName(),
                    contentType,
                    Files.readAllBytes(file.toPath())
            );

            log.info("开始上传: {} ({}KB)", file.getName(), file.length() / 1024);
            String url = huaweiObsClient.upload(multipartFile, "images/upload");
            log.info("上传成功: {}", url);

            count++;
        }

        log.info("===== 上传完成，共{}张图片 =====", count);
    }

    @Test
    public void testUploadSingleImage() throws IOException {
        log.info("===== 测试上传单张图片到华为云OBS =====");

        File dir = new File(IMAGE_DIR);
        File[] files = dir.listFiles((d, name) ->
            name.toLowerCase().endsWith(".jpg") ||
            name.toLowerCase().endsWith(".jpeg") ||
            name.toLowerCase().endsWith(".png"));

        if (files == null || files.length == 0) {
            log.error("没有找到图片文件");
            return;
        }

        File file = files[0];
        String contentType = Files.probeContentType(file.toPath());
        if (contentType == null) {
            contentType = file.getName().endsWith(".png") ? "image/png" : "image/jpeg";
        }

        MultipartFile multipartFile = new MockMultipartFile(
                "file",
                file.getName(),
                contentType,
                Files.readAllBytes(file.toPath())
        );

        log.info("上传文件: {} ({}KB)", file.getName(), file.length() / 1024);
        String url = huaweiObsClient.upload(multipartFile, "images/single");
        log.info("访问URL: {}", url);
    }

    @Test
    public void testDeleteFile() {
        log.info("===== 测试删除华为云OBS文件 =====");

        // 请替换为实际的对象名称
        String objectName = "images/upload/69b5e011bdb64d2e9b3868bff7e6b10c.png";

        try {
            huaweiObsClient.delete(objectName);
            log.info("删除成功: {}", objectName);
        } catch (Exception e) {
            log.error("删除失败: {}", e.getMessage());
        }
    }
}
