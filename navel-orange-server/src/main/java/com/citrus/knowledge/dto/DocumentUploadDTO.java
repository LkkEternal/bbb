package com.citrus.knowledge.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文献上传请求
 */
@Data
public class DocumentUploadDTO {
    private MultipartFile file;
    private String category;
}
