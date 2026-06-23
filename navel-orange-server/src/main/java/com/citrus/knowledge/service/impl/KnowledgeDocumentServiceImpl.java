package com.citrus.knowledge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.citrus.knowledge.config.PythonRagClient;
import com.citrus.knowledge.entity.KnowledgeChunk;
import com.citrus.knowledge.entity.KnowledgeDocument;
import com.citrus.knowledge.mapper.KnowledgeChunkMapper;
import com.citrus.knowledge.mapper.KnowledgeDocumentMapper;
import com.citrus.knowledge.service.KnowledgeDocumentService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 知识文献管理服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeDocumentServiceImpl implements KnowledgeDocumentService {

    private final KnowledgeDocumentMapper documentMapper;
    private final KnowledgeChunkMapper chunkMapper;
    private final PythonRagClient pythonRagClient;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public KnowledgeDocument upload(MultipartFile file, String category, Long operatorId) {
        // 校验文件
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        String fileType = getFileType(originalName);
        if (fileType == null) {
            throw new IllegalArgumentException("不支持的文件类型，仅支持 pdf/docx/txt");
        }

        if (file.getSize() > 20 * 1024 * 1024) {
            throw new IllegalArgumentException("文件大小不能超过 20MB");
        }

        // 创建文献记录
        KnowledgeDocument doc = new KnowledgeDocument();
        doc.setTitle(originalName);
        doc.setFileType(fileType);
        doc.setFileSize(file.getSize());
        doc.setCategory(category != null ? category : "");
        doc.setSource("ADMIN_UPLOAD");
        doc.setStatus(0);  // 待处理
        doc.setUploadedBy(operatorId);
        documentMapper.insert(doc);

        log.info("Document uploaded: id={}, name={}, type={}", doc.getId(), originalName, fileType);
        return doc;
    }

    @Override
    public Page<KnowledgeDocument> listDocuments(int page, int size, String category, String keyword) {
        Page<KnowledgeDocument> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<KnowledgeDocument> wrapper = new LambdaQueryWrapper<>();
        if (category != null && !category.isBlank()) {
            wrapper.eq(KnowledgeDocument::getCategory, category);
        }
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(KnowledgeDocument::getTitle, keyword);
        }
        wrapper.orderByDesc(KnowledgeDocument::getCreatedAt);
        return documentMapper.selectPage(pageObj, wrapper);
    }

    @Override
    public KnowledgeDocument getById(Long id) {
        return documentMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        chunkMapper.deleteByDocumentId(id);
        documentMapper.deleteById(id);
        log.info("Document deleted: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processDocument(Long documentId) {
        KnowledgeDocument doc = documentMapper.selectById(documentId);
        if (doc == null) {
            throw new IllegalArgumentException("文献不存在: " + documentId);
        }
        if (doc.getStatus() == 2) {
            log.warn("Document already processed: id={}", documentId);
            return;
        }

        // 标记处理中
        doc.setStatus(1);
        documentMapper.updateById(doc);

        try {
            // 调用 Python 服务处理文档
            String resultJson = pythonRagClient.processDocument(doc.getFileUrl(), doc.getFileType(), documentId);
            JsonNode root = objectMapper.readTree(resultJson);

            // 解析返回的 chunks
            JsonNode chunksNode = root.get("chunks");
            List<KnowledgeChunk> chunks = new ArrayList<>();
            for (JsonNode chunkNode : chunksNode) {
                KnowledgeChunk chunk = new KnowledgeChunk();
                chunk.setDocumentId(documentId);
                chunk.setChunkIndex(chunkNode.get("chunk_index").asInt());
                chunk.setContent(chunkNode.get("content").asText());

                // 解析 embedding
                JsonNode embNode = chunkNode.get("embedding");
                float[] embedding = new float[embNode.size()];
                for (int i = 0; i < embNode.size(); i++) {
                    embedding[i] = (float) embNode.get(i).asDouble();
                }
                chunk.setEmbedding(embedding);

                chunk.setTokenCount(chunkNode.get("token_count").asInt());
                if (chunkNode.has("metadata") && !chunkNode.get("metadata").isNull()) {
                    chunk.setMetadata(chunkNode.get("metadata").toString());
                }
                chunks.add(chunk);
            }

            // 批量插入分块
            if (!chunks.isEmpty()) {
                chunkMapper.batchInsert(chunks);
            }

            // 更新文献状态
            doc.setStatus(2);  // 已完成
            doc.setChunkCount(chunks.size());
            doc.setEmbeddingModel(root.get("model").asText());
            documentMapper.updateById(doc);

            log.info("Document processed: id={}, chunks={}", documentId, chunks.size());

        } catch (Exception e) {
            log.error("Document processing failed: id={}, error={}", documentId, e.getMessage());
            doc.setStatus(3);  // 处理失败
            doc.setErrorMessage(e.getMessage());
            documentMapper.updateById(doc);
            throw new RuntimeException("文档处理失败: " + e.getMessage(), e);
        }
    }

    // ── 辅助 ─────────────────────────────────────────────

    private String getFileType(String fileName) {
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".pdf")) return "pdf";
        if (lower.endsWith(".docx")) return "docx";
        if (lower.endsWith(".txt")) return "txt";
        return null;
    }
}
