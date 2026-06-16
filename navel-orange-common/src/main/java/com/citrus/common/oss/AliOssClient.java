package com.citrus.common.oss;

import com.citrus.common.exception.BizException;
import com.citrus.common.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.UUID;

/**
 * 阿里云OSS工具类
 */
@Slf4j
@Component
public class AliOssClient {

    @Value("${aliyun.oss.endpoint:}")
    private String endpoint;

    @Value("${aliyun.oss.access-key-id:}")
    private String accessKeyId;

    @Value("${aliyun.oss.access-key-secret:}")
    private String accessKeySecret;

    @Value("${aliyun.oss.bucket-name:}")
    private String bucketName;

    @Value("${aliyun.oss.url-prefix:}")
    private String urlPrefix;

    private final HttpClient httpClient;

    public AliOssClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * 上传文件
     * @param file 文件
     * @param directory 目录（如 "images/2026/06"）
     * @return 文件访问URL
     */
    public String upload(MultipartFile file, String directory) {
        try {
            return upload(file.getInputStream(), file.getOriginalFilename(), file.getContentType(), directory);
        } catch (IOException e) {
            log.error("读取文件失败", e);
            throw new BizException(ResultCode.INTERNAL_ERROR, "读取文件失败");
        }
    }

    /**
     * 上传文件
     * @param inputStream 文件流
     * @param originalFilename 原始文件名
     * @param contentType 文件类型
     * @param directory 目录
     * @return 文件访问URL
     */
    public String upload(InputStream inputStream, String originalFilename, String contentType, String directory) {
        try {
            // 生成唯一文件名
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String objectName = directory + "/" + UUID.randomUUID().toString().replace("-", "") + extension;

            // 读取文件内容
            byte[] content = inputStream.readAllBytes();

            // 构建请求
            String url = "https://" + bucketName + "." + endpoint + "/" + objectName;
            String date = ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.RFC_1123_DATE_TIME);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Date", date)
                    .header("Content-Type", contentType != null ? contentType : "application/octet-stream")
                    .header("Authorization", buildAuthorization("PUT", objectName, date, contentType))
                    .PUT(HttpRequest.BodyPublishers.ofByteArray(content))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                String accessUrl = urlPrefix + "/" + objectName;
                log.info("文件上传成功: {}", accessUrl);
                return accessUrl;
            } else {
                log.error("OSS上传失败: {} - {}", response.statusCode(), response.body());
                throw new BizException(ResultCode.INTERNAL_ERROR, "OSS上传失败: " + response.statusCode());
            }
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("OSS上传异常", e);
            throw new BizException(ResultCode.INTERNAL_ERROR, "OSS上传异常: " + e.getMessage());
        }
    }

    /**
     * 上传字节数组
     * @param data 字节数组
     * @param fileName 文件名
     * @param contentType 文件类型
     * @param directory 目录
     * @return 文件访问URL
     */
    public String upload(byte[] data, String fileName, String contentType, String directory) {
        return upload(new ByteArrayInputStream(data), fileName, contentType, directory);
    }

    /**
     * 删除文件
     * @param objectName 对象名称（不含bucket前缀）
     */
    public void delete(String objectName) {
        try {
            String url = "https://" + bucketName + "." + endpoint + "/" + objectName;
            String date = ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.RFC_1123_DATE_TIME);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Date", date)
                    .header("Authorization", buildAuthorization("DELETE", objectName, date, null))
                    .DELETE()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("文件删除成功: {}", objectName);
            } else {
                log.error("OSS删除失败: {} - {}", response.statusCode(), response.body());
                throw new BizException(ResultCode.INTERNAL_ERROR, "OSS删除失败");
            }
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("OSS删除异常", e);
            throw new BizException(ResultCode.INTERNAL_ERROR, "OSS删除异常: " + e.getMessage());
        }
    }

    /**
     * 根据URL获取对象名称
     * @param url 文件完整URL
     * @return 对象名称
     */
    public String getObjectName(String url) {
        if (url != null && url.startsWith(urlPrefix + "/")) {
            return url.substring(urlPrefix.length() + 1);
        }
        return url;
    }

    /**
     * 构建OSS签名授权头
     */
    private String buildAuthorization(String method, String objectName, String date, String contentType) {
        try {
            String contentMd5 = "";
            String resource = "/" + bucketName + "/" + objectName;

            StringBuilder stringToSign = new StringBuilder();
            stringToSign.append(method).append("\n");
            stringToSign.append(contentMd5).append("\n");
            stringToSign.append(contentType != null ? contentType : "").append("\n");
            stringToSign.append(date).append("\n");
            stringToSign.append(resource);

            // HMAC-SHA1签名
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA1");
            mac.init(new javax.crypto.spec.SecretKeySpec(accessKeySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
            byte[] signBytes = mac.doFinal(stringToSign.toString().getBytes(StandardCharsets.UTF_8));
            String signature = Base64.getEncoder().encodeToString(signBytes);

            return "OSS " + accessKeyId + ":" + signature;
        } catch (Exception e) {
            log.error("构建OSS签名失败", e);
            throw new BizException(ResultCode.INTERNAL_ERROR, "构建OSS签名失败");
        }
    }
}
