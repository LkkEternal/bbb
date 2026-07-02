package com.citrus.push.service.impl;

import com.citrus.push.dto.AlertEmailDTO;
import com.citrus.push.entity.AlertMessage;
import com.citrus.push.service.EmailTemplateRenderer;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 基于 Thymeleaf 的邮件模板渲染实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailTemplateRendererImpl implements EmailTemplateRenderer {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final TemplateEngine templateEngine;

    @Override
    public String render(AlertMessage alert) {
        return render(buildDTO(alert));
    }

    @Override
    public String render(AlertEmailDTO dto) {
        Context ctx = new Context();
        ctx.setVariable("dto", dto);
        return templateEngine.process("alert-email", ctx);
    }

    /**
     * 从 AlertMessage + extraData JSON 组装 DTO
     */
    public AlertEmailDTO buildDTO(AlertMessage alert) {
        Map<String, Object> extra = parseExtra(alert.getExtraData());

        String levelLabel = switch (alert.getLevel()) {
            case 1 -> "L1 低风险";
            case 2 -> "L2 中风险";
            case 3 -> "L3 高风险";
            default -> "L" + alert.getLevel();
        };
        String levelColor = switch (alert.getLevel()) {
            case 1 -> "#3D8B40";
            case 2 -> "#E68A00";
            case 3 -> "#E53935";
            default -> "#666";
        };
        String levelBadge = switch (alert.getLevel()) {
            case 1 -> "🟢";
            case 2 -> "⚠️";
            case 3 -> "🔴";
            default -> "●";
        };

        Double probability = num(extra, "probability");
        int riskScore = probability != null ? (int) Math.round(probability * 100) : 0;

        return AlertEmailDTO.builder()
                .orchardName(str(extra, "orchardName"))
                .level(alert.getLevel())
                .levelLabel(levelLabel)
                .levelColor(levelColor)
                .levelBadge(levelBadge)
                .riskScore(riskScore)
                .sendTime(alert.getCreatedAt() != null
                        ? alert.getCreatedAt().format(DTF)
                        : LocalDateTime.now().format(DTF))
                .summary(alert.getSummary())
                .phenology(str(extra, "phenology"))
                .temperature(num(extra, "temperature"))
                .humidity(num(extra, "humidity"))
                .diseaseName(str(extra, "diseaseName"))
                .probability(probability)
                .deviceTimestamp(str(extra, "deviceTimestamp"))
                .imageUrl(str(extra, "imageUrl"))
                .adviceItems(splitAdvice(alert.getAdvice()))
                .consequences(str(extra, "consequences"))
                .urgency(str(extra, "urgency"))
                .build();
    }

    // ========================================================================

    private Map<String, Object> parseExtra(String extraData) {
        if (extraData == null || extraData.isBlank()) return Map.of();
        try {
            return OBJECT_MAPPER.readValue(extraData, new TypeReference<>() {});
        } catch (Exception e) {
            log.warn("解析 extraData 失败: {}", e.getMessage());
            return Map.of();
        }
    }

    private static String str(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v != null ? v.toString() : null;
    }

    private static Double num(Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (v instanceof Number n) return n.doubleValue();
        return null;
    }

    /**
     * 将防治建议文本按编号拆分
     */
    private static List<String> splitAdvice(String advice) {
        if (advice == null || advice.isBlank()) return List.of();
        String[] parts = advice.split("\\d+[.、）]\\s*|①|②|③|④|⑤|⑥|⑦|⑧|⑨|⑩");
        List<String> result = new ArrayList<>();
        for (String p : parts) {
            String trimmed = p.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }
}
