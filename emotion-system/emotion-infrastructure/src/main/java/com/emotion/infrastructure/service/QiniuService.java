package com.emotion.infrastructure.service;

import com.emotion.infrastructure.config.QiniuConfig;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class QiniuService {

    private final Auth qiniuAuth;
    private final Configuration qiniuConfiguration;
    private final QiniuConfig qiniuConfig;

    public String uploadAvatar(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String ext = getExtension(originalFilename);
        String key = "avatars/" + UUID.randomUUID() + "." + ext;

        UploadManager uploadManager = new UploadManager(qiniuConfiguration);
        com.qiniu.http.Response response = uploadManager.put(
                file.getInputStream(),
                key,
                qiniuAuth.uploadToken(qiniuConfig.getBucket()),
                null,
                null);

        if (response.isOK()) {
            String url = qiniuConfig.getDomain() + "/" + key;
            log.info("[Qiniu] 头像上传成功: {}", url);
            return url;
        } else {
            log.error("[Qiniu] 头像上传失败: {}", response.bodyString());
            throw new RuntimeException("文件上传失败: " + response.error);
        }
    }

    public UploadResult uploadDetectImage(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String ext = getExtension(originalFilename);
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String key = "detect/" + datePath + "/" + UUID.randomUUID() + "." + ext;

        UploadManager uploadManager = new UploadManager(qiniuConfiguration);
        com.qiniu.http.Response response = uploadManager.put(
                file.getInputStream(),
                key,
                qiniuAuth.uploadToken(qiniuConfig.getBucket()),
                null,
                null);

        if (response.isOK()) {
            String url = qiniuConfig.getDomain() + "/" + key;
            log.info("[Qiniu] 检测图片上传成功: {}", url);
            return new UploadResult(key, url);
        } else {
            log.error("[Qiniu] 检测图片上传失败: {}", response.bodyString());
            throw new RuntimeException("文件上传失败: " + response.error);
        }
    }

    public void deleteFile(String url) {
        try {
            String key = urlToKey(url);
            if (key == null) return;

            BucketManager bucketManager = new BucketManager(qiniuAuth, qiniuConfiguration);
            bucketManager.delete(qiniuConfig.getBucket(), key);
            log.info("[Qiniu] 文件删除成功: {}", key);
        } catch (Exception e) {
            log.error("[Qiniu] 文件删除异常: {}", url, e);
        }
    }

    private String urlToKey(String url) {
        if (url == null || url.isEmpty()) return null;
        String prefix = qiniuConfig.getDomain() + "/";
        if (url.startsWith(prefix)) {
            return url.substring(prefix.length());
        }
        return null;
    }

    private String getExtension(String filename) {
        if (filename == null) return "jpg";
        int dot = filename.lastIndexOf('.');
        if (dot > 0) {
            return filename.substring(dot + 1).toLowerCase();
        }
        return "jpg";
    }

    public record UploadResult(String objectKey, String url) {}
}
