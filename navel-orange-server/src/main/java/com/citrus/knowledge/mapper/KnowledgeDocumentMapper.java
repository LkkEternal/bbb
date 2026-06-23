package com.citrus.knowledge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.citrus.knowledge.entity.KnowledgeDocument;
import org.apache.ibatis.annotations.Mapper;

/**
 * 知识文献 Mapper
 */
@Mapper
public interface KnowledgeDocumentMapper extends BaseMapper<KnowledgeDocument> {
}
