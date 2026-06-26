package com.citrus.knowledge.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.citrus.knowledge.entity.KnowledgeDocument;
import org.springframework.web.multipart.MultipartFile;

/**
 * 知识文献管理服务
 */
public interface KnowledgeDocumentService {

    /**
     * 上传文献
     */
    KnowledgeDocument upload(MultipartFile file, String category, Long operatorId);

    /**
     * 分页查询文献列表
     * @param status 文献状态筛选（可选，0=待处理 1=处理中 2=已完成 3=处理失败）
     */
    Page<KnowledgeDocument> listDocuments(int page, int size, String category, String keyword, Integer status);

    /**
     * 获取文献详情
     */
    KnowledgeDocument getById(Long id);

    /**
     * 删除文献及其分块
     */
    void deleteById(Long id);

    /**
     * 触发文献处理（文本提取 → 分块 → 向量化）
     */
    void processDocument(Long documentId);
}
