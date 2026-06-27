package com.citrus.feedback.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.citrus.feedback.entity.UserFeedback;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户反馈 Mapper
 */
@Mapper
public interface UserFeedbackMapper extends BaseMapper<UserFeedback> {
}
