package com.emotion.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.emotion.chat.entity.AssessmentQuestionnaire;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AssessmentQuestionnaireMapper extends BaseMapper<AssessmentQuestionnaire> {

    @Select("SELECT * FROM assessment_questionnaire WHERE is_active = 1 ORDER BY created_at DESC")
    List<AssessmentQuestionnaire> selectActiveList();

    @Select("SELECT * FROM assessment_questionnaire WHERE code = #{code} AND is_active = 1 LIMIT 1")
    AssessmentQuestionnaire selectByCode(@Param("code") String code);
}
