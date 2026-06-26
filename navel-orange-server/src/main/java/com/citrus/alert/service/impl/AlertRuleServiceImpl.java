package com.citrus.alert.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.citrus.alert.dto.UpdateRuleDTO;
import com.citrus.alert.entity.AlertRule;
import com.citrus.alert.mapper.AlertRuleMapper;
import com.citrus.alert.service.AlertRuleService;
import com.citrus.alert.vo.RuleListVO;
import com.citrus.collector.entity.DiseaseCatalog;
import com.citrus.collector.mapper.DiseaseCatalogMapper;
import com.citrus.common.exception.BizException;
import com.citrus.common.result.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 预警规则服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlertRuleServiceImpl implements AlertRuleService {

    private final AlertRuleMapper alertRuleMapper;
    private final DiseaseCatalogMapper diseaseCatalogMapper;

    @Override
    public List<RuleListVO> getRuleList() {
        // 查询所有规则
        List<AlertRule> rules = alertRuleMapper.selectList(
                new LambdaQueryWrapper<AlertRule>()
                        .orderByAsc(AlertRule::getCode)
        );

        // 获取病害名称映射
        Map<String, String> diseaseNameMap = getDiseaseNameMap();

        // 转换为VO
        return rules.stream()
                .map(rule -> convertToVO(rule, diseaseNameMap))
                .collect(Collectors.toList());
    }

    @Override
    public void updateRule(Integer id, UpdateRuleDTO dto) {
        // 查询规则是否存在
        AlertRule rule = alertRuleMapper.selectById(id);
        if (rule == null) {
            throw new BizException(ResultCode.NOT_FOUND, "规则不存在");
        }

        // 更新规则阈值
        LambdaUpdateWrapper<AlertRule> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(AlertRule::getId, id);

        if (dto.getTempMin() != null) {
            updateWrapper.set(AlertRule::getTempMin, dto.getTempMin());
        }
        if (dto.getTempMax() != null) {
            updateWrapper.set(AlertRule::getTempMax, dto.getTempMax());
        }
        if (dto.getHumidityMin() != null) {
            updateWrapper.set(AlertRule::getHumidityMin, dto.getHumidityMin());
        }
        if (dto.getHumidityMax() != null) {
            updateWrapper.set(AlertRule::getHumidityMax, dto.getHumidityMax());
        }
        if (dto.getDiseaseCode() != null) {
            updateWrapper.set(AlertRule::getDiseaseCode, dto.getDiseaseCode());
        }
        if (dto.getDetectionCount() != null) {
            updateWrapper.set(AlertRule::getDetectionCount, dto.getDetectionCount());
        }
        if (dto.getDetectionDays() != null) {
            updateWrapper.set(AlertRule::getDetectionDays, dto.getDetectionDays());
        }
        if (dto.getCooldownHours() != null) {
            updateWrapper.set(AlertRule::getCooldownHours, dto.getCooldownHours());
        }

        alertRuleMapper.update(null, updateWrapper);
        log.info("更新规则阈值成功，规则ID: {}", id);
    }

    @Override
    public Map<String, Boolean> toggleRule(Integer id) {
        // 查询规则是否存在
        AlertRule rule = alertRuleMapper.selectById(id);
        if (rule == null) {
            throw new BizException(ResultCode.NOT_FOUND, "规则不存在");
        }

        // 切换启用状态
        boolean newEnabled = !Boolean.TRUE.equals(rule.getEnabled());
        LambdaUpdateWrapper<AlertRule> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(AlertRule::getId, id)
                     .set(AlertRule::getEnabled, newEnabled);
        alertRuleMapper.update(null, updateWrapper);

        log.info("切换规则启用状态成功，规则ID: {}, 新状态: {}", id, newEnabled);
        return Collections.singletonMap("enabled", newEnabled);
    }

    /**
     * 获取病害名称映射
     */
    private Map<String, String> getDiseaseNameMap() {
        List<DiseaseCatalog> catalogs = diseaseCatalogMapper.selectList(
                new LambdaQueryWrapper<DiseaseCatalog>()
                        .select(DiseaseCatalog::getDiseaseCode, DiseaseCatalog::getDiseaseName)
        );
        Map<String, String> nameMap = new HashMap<>();
        for (DiseaseCatalog catalog : catalogs) {
            nameMap.put(catalog.getDiseaseCode(), catalog.getDiseaseName());
        }
        return nameMap;
    }

    /**
     * 将实体转换为VO
     *
     * 响应格式严格遵守API接口文档：
     * - id: 规则ID
     * - name: 规则名称
     * - code: 规则编码（如 R001）
     * - description: 规则描述
     * - diseaseCode: 关联病害编码
     * - diseaseName: 关联病害名称
     * - conditions: 规则条件对象
     * - level: 预警等级
     * - cooldownHours: 冷却时间
     * - enabled: 是否启用
     * - createdAt: 创建时间
     */
    private RuleListVO convertToVO(AlertRule rule, Map<String, String> diseaseNameMap) {
        RuleListVO vo = new RuleListVO();
        vo.setId(rule.getId());
        vo.setName(rule.getName());
        vo.setCode(rule.getCode());
        vo.setDescription(rule.getDescription());
        vo.setDiseaseCode(rule.getDiseaseCode());
        // 支持多病害编码（逗号分隔），拼接多个名称
        if (rule.getDiseaseCode() != null && !rule.getDiseaseCode().isBlank()) {
            String diseaseNames = Arrays.stream(rule.getDiseaseCode().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(code -> diseaseNameMap.getOrDefault(code, code))
                    .collect(Collectors.joining("、"));
            vo.setDiseaseName(diseaseNames);
        } else {
            vo.setDiseaseName("");
        }
        vo.setLevel(rule.getLevel());
        vo.setCooldownHours(rule.getCooldownHours());
        vo.setEnabled(rule.getEnabled());
        vo.setCreatedAt(rule.getCreatedAt());

        // 组装条件Map
        Map<String, Object> conditions = new LinkedHashMap<>();
        if (rule.getTempMin() != null) conditions.put("tempMin", rule.getTempMin());
        if (rule.getTempMax() != null) conditions.put("tempMax", rule.getTempMax());
        if (rule.getHumidityMin() != null) conditions.put("humidityMin", rule.getHumidityMin());
        if (rule.getHumidityMax() != null) conditions.put("humidityMax", rule.getHumidityMax());
        if (rule.getDetectionCount() != null) conditions.put("detectionCount", rule.getDetectionCount());
        if (rule.getDetectionDays() != null) conditions.put("detectionDays", rule.getDetectionDays());
        vo.setConditions(conditions);

        return vo;
    }
}
