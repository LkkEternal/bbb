package com.emotion.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.emotion.chat.entity.AssessmentRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AssessmentRecordMapper extends BaseMapper<AssessmentRecord> {

    @Select("SELECT * FROM assessment_record WHERE user_id = #{userId} AND status = 'completed' ORDER BY completed_at DESC")
    IPage<AssessmentRecord> selectHistoryPage(Page<AssessmentRecord> page, @Param("userId") Long userId);

    @Select("SELECT * FROM assessment_record WHERE id = #{id} AND user_id = #{userId} LIMIT 1")
    AssessmentRecord selectByIdAndUser(@Param("id") Long id, @Param("userId") Long userId);
}
