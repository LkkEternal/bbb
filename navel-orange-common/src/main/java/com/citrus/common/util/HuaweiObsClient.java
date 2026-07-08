package com.citrus.common.util;

import com.citrus.common.exception.BizException;
import com.citrus.common.result.ResultCode;
import com.obs.services.ObsClient;
import com.obs.services.model.*;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 华为云OBS工具类
 */
@Slf4j
@Component
public class HuaweiObsClient {

    @Value("${huawei.obs.endpoint:}")
    private String endpoint;

    @Value("${huawei.obs.access-key:}")
    private String accessKey;

    @Value("${huawei.obs.secret-key:}")
    private String secretKey;

    @Value("${huawei.obs.bucket-name:}")
    private String bucketName;

    @Value("${huawei.obs.url-prefix:}")
    private String urlPrefix;

    private ObsClient obsClient;

    @PostConstruct
    public void init() {
        if (endpoint != null && !endpoint.isEmpty() &&
                accessKey != null && !accessKey.isEmpty() &&
                secretKey != null && !secretKey.isEmpty()) {
            obsClient = new ObsClient(accessKey, secretKey, endpoint);
            log.info("华为云OBS客户端初始化成功, endpoint={}, bucket={}", endpoint, bucketName);
        } else {
            log.warn("华为云OBS配置不完整，OBS功能不可用");
        }
    }

    @PreDestroy
    public void destroy() {
        if (obsClient != null) {
            try {
                obsClient.close();
            } catch (IOException e) {
                log.warn("关闭OBS客户端异常", e);
            }
        }
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
        if (obsClient == null) {
            throw new BizException(ResultCode.INTERNAL_ERROR, "华为云OBS未配置或配置错误");
        }

        try {
            // 生成唯一文件名
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String objectName = directory + "/" + UUID.randomUUID().toString().replace("-", "") + extension;

            // 读取文件内容
            byte[] content = inputStream.readAllBytes();

            // 构建上传请求
            PutObjectRequest request = new PutObjectRequest(bucketName, objectName);
            request.setInput(new ByteArrayInputStream(content));
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(contentType != null ? contentType : "application/octet-stream");
            request.setMetadata(metadata);
            // 上传文件
            PutObjectResult result = obsClient.putObject(request);

            if (result.getStatusCode() >= 200 && result.getStatusCode() < 300) {
                String accessUrl = urlPrefix + "/" + objectName;
                log.info("文件上传成功: {}", accessUrl);
                return accessUrl;
            } else {
                log.error("OBS上传失败: {}", result.getStatusCode());
                throw new BizException(ResultCode.INTERNAL_ERROR, "OBS上传失败: " + result.getStatusCode());
            }
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("OBS上传异常", e);
            throw new BizException(ResultCode.INTERNAL_ERROR, "OBS上传异常: " + e.getMessage());
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
        if (obsClient == null) {
            throw new BizException(ResultCode.INTERNAL_ERROR, "华为云OBS未配置或配置错误");
        }

        try {
            // 先检查文件是否存在
            boolean exists = obsClient.doesObjectExist(bucketName, objectName);
            if (!exists) {
                log.warn("文件不存在，无法删除: {}", objectName);
                throw new BizException(ResultCode.NOT_FOUND, "文件不存在: " + objectName);
            }

            DeleteObjectRequest request = new DeleteObjectRequest(bucketName, objectName);
            obsClient.deleteObject(request);

            // 验证是否删除成功
            boolean stillExists = obsClient.doesObjectExist(bucketName, objectName);
            if (stillExists) {
                log.error("文件删除失败，文件仍然存在: {}", objectName);
                throw new BizException(ResultCode.INTERNAL_ERROR, "文件删除失败");
            }

            log.info("文件删除成功: {}", objectName);
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("OBS删除异常", e);
            throw new BizException(ResultCode.INTERNAL_ERROR, "OBS删除异常: " + e.getMessage());
        }
    }

    /**
     * 根据URL获取对象名称
     * @param url 文件完整URL
     * @return 对象名称
     */
    public String getObjectName(String url) {
        if (url != null && urlPrefix != null && url.startsWith(urlPrefix + "/")) {
            return url.substring(urlPrefix.length() + 1);
        }
        return url;
    }

    /**
     * 下载 OBS 文件内容
     * @param objectName 对象名称（如 "disease_records/xxx.jpg"）
     * @return 文件字节数组，以及 ContentType
     */
    public ObsObjectContent getObject(String objectName) {
        if (obsClient == null) {
            throw new BizException(ResultCode.INTERNAL_ERROR, "华为云OBS未配置或配置错误");
        }
        try {
            ObsObject obj = obsClient.getObject(bucketName, objectName);
            try (InputStream in = obj.getObjectContent()) {
                byte[] data = in.readAllBytes();
                String contentType = obj.getMetadata().getContentType();
                return new ObsObjectContent(data, contentType != null ? contentType : "application/octet-stream");
            }
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("OBS下载异常: {}", e.getMessage());
            throw new BizException(ResultCode.INTERNAL_ERROR, "OBS下载异常: " + e.getMessage());
        }
    }

    /**
     * OBS 文件内容（字节+类型）
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class ObsObjectContent {
        private byte[] data;
        private String contentType;
    }

    /**
     * OBS 上的文件信息
     */
    @lombok.Data
    public static class ObsFileInfo {
        private String fileName;
        private Long fileSize;
        private String fileUrl;
        private String etag;
        private String lastModified;
    }

    /**
     * 列举 OBS 桶中的文件
     * @param prefix 路径前缀（如 "firmware/"），传空则列出所有文件
     * @param maxKeys 每次最多返回数量
     * @return 文件信息列表
     */
    public List<ObsFileInfo> listObjects(String prefix, int maxKeys) {
        if (obsClient == null) {
            throw new BizException(ResultCode.INTERNAL_ERROR, "华为云OBS未配置或配置错误");
        }
        List<ObsFileInfo> result = new ArrayList<>();
        String nextMarker = null;
        while (true) {
            ListObjectsRequest request = new ListObjectsRequest(bucketName);
            request.setMaxKeys(maxKeys);
            if (prefix != null) {
                request.setPrefix(prefix);
            }
            if (nextMarker != null) {
                request.setMarker(nextMarker);
            }
            ObjectListing listing = obsClient.listObjects(request);
            for (ObsObject obj : listing.getObjects()) {
                ObsFileInfo info = new ObsFileInfo();
                info.setFileName(obj.getObjectKey());
                info.setFileSize(obj.getMetadata().getContentLength());
                info.setFileUrl(urlPrefix + "/" + obj.getObjectKey());
                info.setEtag(obj.getMetadata().getEtag());
                info.setLastModified(obj.getMetadata().getLastModified().toString());
                result.add(info);
            }
            if (!listing.isTruncated()) {
                break;
            }
            nextMarker = listing.getNextMarker();
        }
        return result;
    }
}
