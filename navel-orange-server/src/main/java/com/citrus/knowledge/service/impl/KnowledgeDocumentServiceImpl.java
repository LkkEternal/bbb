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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
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

        // 保存文件到临时目录，用于后续转发给 Python RAG 服务
        try {
            String tmpDir = System.getProperty("java.io.tmpdir") + File.separator + "navel-orange-rag-uploads";
            Path dir = Path.of(tmpDir);
            Files.createDirectories(dir);
            String filename = System.currentTimeMillis() + "_" + originalName;
            Path targetPath = dir.resolve(filename);
            file.transferTo(targetPath.toFile());
            doc.setFileUrl(targetPath.toAbsolutePath().toString());
            log.info("File saved to temp: {}", targetPath);
        } catch (IOException e) {
            throw new RuntimeException("文件保存失败: " + e.getMessage(), e);
        }

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
            // 判断文件来源：本地文件（以盘符或 /tmp 开头）或远程 URL
            String fileUrl = doc.getFileUrl();
            String resultJson;
            if (fileUrl != null && !fileUrl.isBlank()
                    && !fileUrl.startsWith("http://") && !fileUrl.startsWith("https://")) {
                // 本地临时文件 → 使用 multipart 上传到 Python RAG
                log.info("Processing local file: {}", fileUrl);
                resultJson = pythonRagClient.processDocumentUpload(new java.io.File(fileUrl), doc.getFileType(), documentId);
            } else {
                // 远程 URL → 使用 URL 方式
                resultJson = pythonRagClient.processDocument(fileUrl, doc.getFileType(), documentId);
            }
            JsonNode root = objectMapper.readTree(resultJson);

            // 解析返回的 chunks
            JsonNode chunksNode = root.get("chunks");
            List<KnowledgeChunk> chunks = new ArrayList<>();
            for (JsonNode chunkNode : chunksNode) {
                KnowledgeChunk chunk = new KnowledgeChunk();
                chunk.setDocumentId(documentId);
                chunk.setChunkIndex(chunkNode.get("chunk_index").asInt());
                String content = chunkNode.get("content").asText();
                chunk.setContent(content);
                chunk.setContentHash(sha256(content));

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

            // 处理成功后删除临时文件
            String localFile = doc.getFileUrl();
            if (localFile != null && !localFile.startsWith("http://") && !localFile.startsWith("https://")) {
                try {
                    Files.deleteIfExists(Path.of(localFile));
                    log.debug("Temp file deleted: {}", localFile);
                } catch (Exception ignored) {
                    // 删除临时文件失败不影响主流程
                }
            }

        } catch (Exception e) {
            String msg = e.getMessage();
            // 检查整个异常链是否包含领域关键词驳回信息
            boolean isDomainRejected = false;
            Throwable cause = e;
            while (cause != null) {
                String causeMsg = cause.getMessage();
                if (causeMsg != null && causeMsg.contains("Content not relevant to domain")) {
                    isDomainRejected = true;
                    msg = causeMsg;
                    break;
                }
                cause = cause.getCause();
            }
            log.error("Document processing failed: id={}, error={}", documentId, msg);

            if (isDomainRejected) {
                doc.setStatus(4);  // 已驳回
                doc.setErrorMessage("内容与脐橙领域不相关，已被驳回");
            } else {
                doc.setStatus(3);  // 处理失败
                doc.setErrorMessage(msg);
            }
            documentMapper.updateById(doc);
            // 不抛异常，让事务提交，保留已更新的状态
        } finally {
            // 仅在处理成功后删除临时文件（失败时保留，允许重试）
            // 判断依据：try 块正常执行完毕会将 status 置为 2
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

    /** SHA-256 哈希，用于 content_hash 字段 */
    private String sha256(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
