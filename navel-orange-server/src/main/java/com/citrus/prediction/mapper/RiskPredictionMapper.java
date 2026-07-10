package com.citrus.prediction.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.citrus.prediction.entity.RiskPrediction;
import org.apache.ibatis.annotations.Mapper;

/**
 * 风险预测结果 Mapper
 */
@Mapper
public interface RiskPredictionMapper extends BaseMapper<RiskPrediction> {
}
