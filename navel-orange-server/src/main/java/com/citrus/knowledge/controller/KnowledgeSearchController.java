package com.citrus.knowledge.controller;

import com.citrus.common.result.Result;
import com.citrus.knowledge.dto.RagQueryDTO;
import com.citrus.knowledge.service.RagService;
import com.citrus.knowledge.vo.ChunkSearchResult;
import com.citrus.knowledge.vo.RagResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 知识检索与 RAG 问答接口（用户端）
 */
@Tag(name = "M7-知识库")
@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
public class KnowledgeSearchController {

    private final RagService ragService;

    @Operation(summary = "知识检索")
    @PostMapping("/search")
    public Result<List<ChunkSearchResult>> search(@Valid @RequestBody RagQueryDTO req) {
        List<ChunkSearchResult> results = ragService.search(
                req.getQuestion(),
                req.getDiseaseName(),
                10
        );
        return Result.success(results);
    }

    @Operation(summary = "RAG问答")
    @PostMapping("/qa")
    public Result<RagResponse> qa(@Valid @RequestBody RagQueryDTO req) {
        RagResponse response = ragService.query(
                req.getQuestion(),
                req.getDiseaseName(),
                req.getOrchardId(),
                req.getModel()
        );
        return Result.success(response);
    }

    /**
     * 小程序专用 RAG 问答入口
     */
    @Operation(summary = "RAG问答", description = "小程序端 RAG 问答，与 /api/knowledge/qa 相同")
    @PostMapping("/miniapp/qa")
    public Result<RagResponse> miniappQa(@Valid @RequestBody RagQueryDTO req) {
        return qa(req);
    }

    @Operation(summary = "知识检索", description = "小程序端知识检索")
    @PostMapping("/miniapp/search")
    public Result<List<ChunkSearchResult>> miniappSearch(@Valid @RequestBody RagQueryDTO req) {
        return search(req);
    }
}
