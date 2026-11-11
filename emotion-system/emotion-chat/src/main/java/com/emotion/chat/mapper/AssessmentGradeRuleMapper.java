package com.emotion.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.emotion.chat.entity.AssessmentGradeRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AssessmentGradeRuleMapper extends BaseMapper<AssessmentGradeRule> {

    @Select("SELECT * FROM assessment_grade_rule WHERE questionnaire_id = #{questionnaireId} AND min_score <= #{totalScore} AND max_score >= #{totalScore} AND is_active = 1 LIMIT 1")
    AssessmentGradeRule matchRule(@Param("questionnaireId") Long questionnaireId, @Param("totalScore") Integer totalScore);

    @Select("SELECT * FROM assessment_grade_rule WHERE questionnaire_id = #{questionnaireId} AND is_active = 1 ORDER BY sort_order ASC")
    java.util.List<AssessmentGradeRule> selectByQuestionnaireId(@Param("questionnaireId") Long questionnaireId);
}
