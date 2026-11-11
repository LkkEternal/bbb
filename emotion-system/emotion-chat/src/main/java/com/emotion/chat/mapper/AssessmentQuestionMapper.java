package com.emotion.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.emotion.chat.entity.AssessmentQuestion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AssessmentQuestionMapper extends BaseMapper<AssessmentQuestion> {

    @Select("SELECT * FROM assessment_question WHERE questionnaire_id = #{questionnaireId} AND is_active = 1 ORDER BY question_index ASC")
    List<AssessmentQuestion> selectByQuestionnaireId(@Param("questionnaireId") Long questionnaireId);

    @Select("SELECT * FROM assessment_question WHERE id = #{id} AND is_active = 1 LIMIT 1")
    AssessmentQuestion selectActiveById(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM assessment_question WHERE questionnaire_id = #{questionnaireId} AND is_active = 1")
    int countByQuestionnaireId(@Param("questionnaireId") Long questionnaireId);
}
