package com.citrus.alert.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.citrus.alert.entity.AlertRule;
import com.citrus.alert.mapper.AlertRuleMapper;
import com.citrus.alert.service.AlertService;
import com.citrus.alert.vo.RiskAssessmentVO;
import com.citrus.collector.entity.DiseaseCatalog;
import com.citrus.collector.entity.DiseaseDetection;
import com.citrus.collector.entity.EnvironmentData;
import com.citrus.collector.mapper.DiseaseCatalogMapper;
import com.citrus.collector.mapper.DiseaseDetectionMapper;
import com.citrus.collector.mapper.EnvironmentDataMapper;
import com.citrus.common.exception.BizException;
import com.citrus.common.result.ResultCode;
import com.citrus.orchard.entity.Orchard;
import com.citrus.orchard.mapper.OrchardMapper;
import com.citrus.push.entity.AlertMessage;
import com.citrus.push.mapper.AlertMessageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 风险评估服务实现类
 *
 * 实现简化版风险评估逻辑（if判断），根据以下条件评估果园风险等级：
 * 1. 环境数据（温度、湿度）
 * 2. 病害识别次数（近N天）
 * 3. 冷却时间（避免重复预警）
 *
 * 风险等级说明：
 * - L1（低风险-关注）：仅推送小程序消息，每日汇总
 * - L2（中风险-预警）：推送小程序消息，实时推送
 * - L3（高风险-紧急）：推送小程序消息+短信，实时推送
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlertServiceImpl implements AlertService {

    private final AlertRuleMapper alertRuleMapper;
    private final EnvironmentDataMapper environmentDataMapper;
    private final DiseaseDetectionMapper diseaseDetectionMapper;
    private final AlertMessageMapper alertMessageMapper;
    private final OrchardMapper orchardMapper;
    private final DiseaseCatalogMapper diseaseCatalogMapper;

    /**
     * 评估果园风险等级
     *
     * 实现逻辑：
     * 1. 查询果园信息，验证果园是否存在
     * 2. 查询果园最新环境数据（温度、湿度）
     * 3. 查询所有已启用的预警规则
     * 4. 遍历规则，使用if判断检查每个条件是否满足
     * 5. 如果所有条件都满足，则记录风险因子和建议
     * 6. 添加环境因子
     * 7. 计算风险分数
     * 8. 组装返回结果
     *
     * @param orchardId 果园ID
     * @return 风险评估结果，包含风险等级、风险因子、建议等
     * @throws BizException 果园不存在时抛出异常
     */
    @Override
    public RiskAssessmentVO assessRisk(Long orchardId) {
        // 1. 查询果园信息，验证果园是否存在
        Orchard orchard = orchardMapper.selectById(orchardId);
        if (orchard == null || Boolean.TRUE.equals(orchard.getDeleted())) {
            throw new BizException(ResultCode.ORCHARD_NOT_FOUND);
        }

        // 2. 查询果园最新环境数据（温度、湿度）
        EnvironmentData envData = getLatestEnvironmentData(orchardId);
        BigDecimal currentTemp = envData != null ? envData.getTemp() : null;
        BigDecimal currentHumidity = envData != null ? envData.getHumidity() : null;

        // 3. 查询所有启用状态的预警规则，封装为列表返回。
        List<AlertRule> enabledRules = alertRuleMapper.selectList(
                new LambdaQueryWrapper<AlertRule>()
                        .eq(AlertRule::getEnabled, true)
        );

        // 4. 获取病害名称映射（用于显示病害中文名称）
        Map<String, String> diseaseNameMap = getDiseaseNameMap();

        // 5. 遍历规则，使用if判断检查每个条件是否满足
        List<RiskAssessmentVO.RiskFactor> factors = new ArrayList<>();  // 风险因子列表
        List<String> suggestions = new ArrayList<>();  // 建议列表
        int maxRiskLevel = 1;  // 最高风险等级（默认为L1）

        for (AlertRule rule : enabledRules) {
            boolean match = true;  // 标记当前规则是否匹配

            // 检查温度条件
            if (currentTemp != null) {
                // 检查温度下限：如果当前温度低于下限，则不匹配
                if (rule.getTempMin() != null && currentTemp.compareTo(rule.getTempMin()) < 0) {
                    match = false;
                }
                // 检查温度上限：如果当前温度高于上限，则不匹配
                if (rule.getTempMax() != null && currentTemp.compareTo(rule.getTempMax()) > 0) {
                    match = false;
                }
            }

            // 检查湿度条件
            if (currentHumidity != null) {
                // 检查湿度下限：如果当前湿度低于下限，则不匹配
                if (rule.getHumidityMin() != null && currentHumidity.compareTo(rule.getHumidityMin()) < 0) {
                    match = false;
                }
                // 检查湿度上限：如果当前湿度高于上限，则不匹配
                if (rule.getHumidityMax() != null && currentHumidity.compareTo(rule.getHumidityMax()) > 0) {
                    match = false;
                }
            }

            // 检查病害识别次数
            if (rule.getDiseaseCode() != null && rule.getDetectionCount() != null) {
                // 获取统计天数，默认为7天
                int days = rule.getDetectionDays() != null ? rule.getDetectionDays() : 7;
                // 统计近N天该病害的识别次数
                long count = countDetections(orchardId, rule.getDiseaseCode(), days);
                // 如果识别次数低于阈值，则不匹配
                if (count < rule.getDetectionCount()) {
                    match = false;
                }
            }

            // 检查冷却时间（避免重复预警）
            if (isInCooldown(orchardId, rule.getId(), rule.getCooldownHours())) {
                match = false;  // 如果在冷却时间内，则不匹配
            }

            // 如果所有条件都满足，记录风险因子和建议
            if (match) {
                // 创建风险因子
                RiskAssessmentVO.RiskFactor factor = new RiskAssessmentVO.RiskFactor();
                factor.setType("disease");  // 类型为病害
                factor.setDiseaseName(diseaseNameMap.getOrDefault(rule.getDiseaseCode(), rule.getDiseaseCode()));
                factor.setRiskLevel(rule.getLevel());

                // 生成风险描述
                String description = generateDescription(rule, orchardId, diseaseNameMap);
                factor.setDescription(description);

                factors.add(factor);

                // 添加建议（使用规则的advice_template字段）
                if (rule.getAdviceTemplate() != null && !rule.getAdviceTemplate().isEmpty()) {
                    suggestions.add(rule.getAdviceTemplate());
                }

                // 更新最高风险等级
                if (rule.getLevel() > maxRiskLevel) {
                    maxRiskLevel = rule.getLevel();
                }
            }
        }

        // 6. 添加环境因子（如果有环境数据）
        if (currentTemp != null && currentHumidity != null) {
            RiskAssessmentVO.RiskFactor envFactor = new RiskAssessmentVO.RiskFactor();
            envFactor.setType("environment");  // 类型为环境
            envFactor.setRiskLevel(calculateEnvironmentRiskLevel(currentTemp, currentHumidity));
            envFactor.setDescription(String.format("当前温度%s℃，湿度%s%%", currentTemp, currentHumidity));
            factors.add(envFactor);

            // 更新最高风险等级
            if (envFactor.getRiskLevel() > maxRiskLevel) {
                maxRiskLevel = envFactor.getRiskLevel();
            }
        }

        // 7. 计算风险分数（0-100分）
        int riskScore = calculateRiskScore(maxRiskLevel, factors.size());

        // 8. 组装返回结果
        RiskAssessmentVO result = new RiskAssessmentVO();
        result.setOrchardId(orchardId);
        result.setOrchardName(orchard.getName());
        result.setRiskLevel(maxRiskLevel);
        result.setRiskScore(riskScore);
        result.setFactors(factors);
        result.setSuggestions(suggestions.isEmpty() ? getDefaultSuggestions() : suggestions);
        result.setUpdatedAt(LocalDateTime.now());

        return result;
    }

    /**
     * 查询果园最新环境数据
     *
     * 按记录时间降序排序，取最新的一条记录
     *
     * @param orchardId 果园ID
     * @return 最新环境数据，如果没有数据则返回null
     */
    private EnvironmentData getLatestEnvironmentData(Long orchardId) {
        return environmentDataMapper.selectOne(
                new LambdaQueryWrapper<EnvironmentData>()
                        .eq(EnvironmentData::getOrchardId, orchardId)
                        .orderByDesc(EnvironmentData::getRecordTime)
                        .last("LIMIT 1")
        );
    }

    /**
     * 统计近N天病害识别次数
     *
     * 查询指定果园、指定病害编码、在指定时间范围内的识别记录数量
     *
     * @param orchardId   果园ID
     * @param diseaseCode 病害编码（如S14=溃疡病）
     * @param days        统计天数
     * @return 识别次数
     */
    private long countDetections(Long orchardId, String diseaseCode, int days) {
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);
        return diseaseDetectionMapper.selectCount(
                new LambdaQueryWrapper<DiseaseDetection>()
                        .eq(DiseaseDetection::getOrchardId, orchardId)
                        .eq(DiseaseDetection::getTopClassCode, diseaseCode)
                        .ge(DiseaseDetection::getCreatedAt, startTime)
        );
    }

    /**
     * 检查是否在冷却时间内
     *
     * 冷却时间的作用：避免同一规则在短时间内重复触发预警
     * 查询alert_message表中该果园、该规则最近一条消息的创建时间，
     * 判断是否超过了规则配置的冷却时间
     *
     * @param orchardId     果园ID
     * @param ruleId        规则ID
     * @param cooldownHours 冷却时间（小时）
     * @return true=在冷却时间内（不应触发），false=不在冷却时间内（可以触发）
     */
    private boolean isInCooldown(Long orchardId, Integer ruleId, int cooldownHours) {
        // 如果冷却时间小于等于0，则不检查冷却
        if (cooldownHours <= 0) {
            return false;
        }
        // 计算冷却截止时间
        LocalDateTime cooldownTime = LocalDateTime.now().minusHours(cooldownHours);
        // 查询是否有在冷却时间内的预警消息
        return alertMessageMapper.selectCount(
                new LambdaQueryWrapper<AlertMessage>()
                        .eq(AlertMessage::getOrchardId, orchardId)
                        .eq(AlertMessage::getRuleId, ruleId)
                        .eq(AlertMessage::getMessageType, "alert")
                        .ge(AlertMessage::getCreatedAt, cooldownTime)
        ) > 0;
    }

    /**
     * 生成风险描述
     *
     * 根据规则条件生成人类可读的风险描述，包括：
     * - 病害识别次数
     * - 温度范围
     * - 湿度条件
     *
     * @param rule           预警规则
     * @param orchardId      果园ID
     * @param diseaseNameMap 病害名称映射
     * @return 风险描述字符串
     */
    private String generateDescription(AlertRule rule, Long orchardId, Map<String, String> diseaseNameMap) {
        StringBuilder sb = new StringBuilder();
        String diseaseName = diseaseNameMap.getOrDefault(rule.getDiseaseCode(), rule.getDiseaseCode());

        // 添加病害识别次数描述
        if (rule.getDetectionCount() != null && rule.getDetectionDays() != null) {
            long count = countDetections(orchardId, rule.getDiseaseCode(), rule.getDetectionDays());
            sb.append(String.format("近%d天识别到%s%d次", rule.getDetectionDays(), diseaseName, count));
        }

        // 添加温度条件描述
        if (rule.getTempMin() != null || rule.getTempMax() != null) {
            if (sb.length() > 0) sb.append("，");
            if (rule.getTempMin() != null && rule.getTempMax() != null) {
                sb.append(String.format("温度在%s-%s℃之间", rule.getTempMin(), rule.getTempMax()));
            } else if (rule.getTempMin() != null) {
                sb.append(String.format("温度≥%s℃", rule.getTempMin()));
            } else {
                sb.append(String.format("温度≤%s℃", rule.getTempMax()));
            }
        }

        // 添加湿度条件描述
        if (rule.getHumidityMin() != null) {
            if (sb.length() > 0) sb.append("，");
            sb.append(String.format("湿度≥%s%%", rule.getHumidityMin()));
        }

        // 如果没有生成描述，则使用规则的默认描述
        return sb.length() > 0 ? sb.toString() : rule.getDescription();
    }

    /**
     * 根据环境数据计算风险等级
     *
     * 简化逻辑：
     * - 高温高湿（≥30℃且≥80%）：L3 高风险
     * - 中温中湿（≥25℃且≥70%）：L2 中风险
     * - 其他：L1 低风险
     *
     * @param temp     当前温度
     * @param humidity 当前湿度
     * @return 风险等级（1=L1, 2=L2, 3=L3）
     */
    private int calculateEnvironmentRiskLevel(BigDecimal temp, BigDecimal humidity) {
        if (temp.compareTo(new BigDecimal("30")) >= 0 && humidity.compareTo(new BigDecimal("80")) >= 0) {
            return 3;  // 高温高湿，L3高风险
        } else if (temp.compareTo(new BigDecimal("25")) >= 0 && humidity.compareTo(new BigDecimal("70")) >= 0) {
            return 2;  // 中温中湿，L2中风险
        }
        return 1;  // 其他情况，L1低风险
    }

    /**
     * 计算风险分数（0-100分）
     *
     * 计算公式：
     * - 基础分数 = 风险等级 × 25（L1=25, L2=50, L3=75）
     * - 因子加分 = min(因子数量 × 10, 50)（最多加50分）
     * - 最终分数 = min(基础分数 + 因子加分, 100)
     *
     * @param riskLevel   最高风险等级
     * @param factorCount 风险因子数量
     * @return 风险分数（0-100）
     */
    private int calculateRiskScore(int riskLevel, int factorCount) {
        int baseScore = riskLevel * 25;  // 基础分数
        int factorBonus = Math.min(factorCount * 10, 50);  // 因子加分（最多50分）
        return Math.min(baseScore + factorBonus, 100);  // 最终分数（最多100分）
    }

    /**
     * 获取默认建议
     *
     * 当没有匹配到任何规则时，返回通用建议
     *
     * @return 默认建议列表
     */
    private List<String> getDefaultSuggestions() {
        return Arrays.asList(
                "建议定期巡园，观察果树生长状况",
                "注意果园通风，保持适宜的温湿度"
        );
    }

    /**
     * 获取病害名称映射
     *
     * 从病害字典表中查询所有病害的编码和名称，构建映射关系
     * 例如：S14 -> 溃疡病, S10 -> 炭疽病
     *
     * @return 病害编码到名称的映射Map
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
}
