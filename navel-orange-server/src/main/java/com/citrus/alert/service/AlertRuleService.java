package com.citrus.alert.service;

import com.citrus.alert.dto.UpdateRuleDTO;
import com.citrus.alert.vo.RuleListVO;

import java.util.List;
import java.util.Map;

/**
 * 预警规则服务接口
 */
public interface AlertRuleService {

    /**
     * 获取规则列表
     *
     * @return 规则列表
     */
    List<RuleListVO> getRuleList();

    /**
     * 更新规则阈值
     *
     * @param id  规则ID
     * @param dto 更新参数
     */
    void updateRule(Integer id, UpdateRuleDTO dto);

    /**
     * 切换规则启用状态
     *
     * @param id 规则ID
     * @return 更新后的启用状态
     */
    Map<String, Boolean> toggleRule(Integer id);
}
