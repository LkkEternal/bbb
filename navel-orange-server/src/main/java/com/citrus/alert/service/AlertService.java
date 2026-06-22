package com.citrus.alert.service;

import com.citrus.alert.vo.RiskAssessmentVO;

/**
 * 风险评估服务接口
 */
public interface AlertService {

    /**
     * 评估果园风险等级
     *
     * @param orchardId 果园ID
     * @return 风险评估结果
     */
    RiskAssessmentVO assessRisk(Long orchardId);
}
