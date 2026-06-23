package com.citrus.knowledge.mapper;

import com.citrus.knowledge.entity.KnowledgeChunk;
import com.citrus.knowledge.vo.ChunkSearchResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 知识分块 Mapper —— 含 pgvector 向量检索
 */
@Mapper
public interface KnowledgeChunkMapper extends com.baomidou.mybatisplus.core.mapper.BaseMapper<KnowledgeChunk> {

    /**
     * pgvector 余弦相似度检索
     *
     * @param queryEmbedding 查询向量 (float[])
     * @param minScore       最低相似度阈值 (0~1)
     * @param topK           返回 top K 条
     * @return 检索结果列表
     */
    List<ChunkSearchResult> searchByVector(
            @Param("queryEmbedding") float[] queryEmbedding,
            @Param("minScore") double minScore,
            @Param("topK") int topK
    );

    /**
     * 混合检索：向量相似度 + 全文关键词匹配（BM25 加权）
     *
     * @param queryEmbedding 查询向量
     * @param keyword        关键词（用于 tsvector 全文检索）
     * @param minScore       最低相似度
     * @param topK           返回 top K
     */
    List<ChunkSearchResult> hybridSearch(
            @Param("queryEmbedding") float[] queryEmbedding,
            @Param("keyword") String keyword,
            @Param("minScore") double minScore,
            @Param("topK") int topK
    );

    /**
     * 根据文档 ID 删除所有分块
     */
    int deleteByDocumentId(@Param("documentId") Long documentId);

    /**
     * 批量插入分块
     */
    int batchInsert(@Param("list") List<KnowledgeChunk> chunks);
}
