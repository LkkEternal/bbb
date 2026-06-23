package com.citrus.knowledge.controller;

import com.citrus.common.result.PageResult;
import com.citrus.common.result.Result;
import com.citrus.knowledge.entity.KnowledgeDocument;
import com.citrus.knowledge.service.KnowledgeDocumentService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

/**
 * 知识库管理接口（管理员）
 */
@Tag(name = "M7-知识库", description = "知识库管理 - 文献上传、处理、删除")
@RestController
@RequestMapping("/api/admin/knowledge")
@RequiredArgsConstructor
public class KnowledgeDocumentController {

    private final KnowledgeDocumentService documentService;

    @Operation(summary = "上传文献")
    @PostMapping("/upload")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<KnowledgeDocument> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "category", defaultValue = "") String category,
            Principal principal) {
        Long userId = getUserId(principal);
        KnowledgeDocument doc = documentService.upload(file, category, userId);
        return Result.success(doc);
    }

    @Operation(summary = "文献列表")
    @GetMapping("/documents")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<KnowledgeDocument>> listDocuments(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword) {
        Page<KnowledgeDocument> pageResult = documentService.listDocuments(page, size, category, keyword);
        return Result.success(PageResult.of(
                pageResult.getRecords(), pageResult.getTotal(),
                (int) pageResult.getCurrent(), (int) pageResult.getSize()));
    }

    @Operation(summary = "删除文献")
    @DeleteMapping("/documents/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteDocument(@PathVariable Long id) {
        documentService.deleteById(id);
        return Result.success();
    }

    @Operation(summary = "处理文献", description = "触发文献的分块和向量化处理")
    @PostMapping("/documents/{id}/process")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> processDocument(@PathVariable Long id) {
        documentService.processDocument(id);
        return Result.success();
    }

    // ── 辅助 ─────────────────────────────────────────────

    private Long getUserId(Principal principal) {
        // 从 SecurityContext 中获取 userId，简化处理
        return principal != null ? 1L : 0L;
    }
}
