package com.citrus.push.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * 预警邮件 HTML 模板构建工具
 * <p>
 * 根据 AlertMessage 数据生成格式化的预警通知邮件 HTML。
 * 参考样式：文本风格表格布局，适配各类邮件客户端。
 */
@UtilityClass
public class AlertEmailTemplate {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * 构建预警邮件 HTML
     *
     * @param title       邮件标题
     * @param level       预警等级 (1=L1, 2=L2, 3=L3)
     * @param summary     摘要
     * @param advice      防治建议
     * @param extraData   extra_data JSON 字符串（可为 null）
     * @param createdAt   创建时间
     * @return 完整 HTML 字符串
     */
    @SneakyThrows
    public String build(String title, int level, String summary,
                        String advice, String extraData, LocalDateTime createdAt) {

        Map<String, Object> extra = parseExtraData(extraData);
        String diseaseName = str(extra, "diseaseName");
        String orchardName = str(extra, "orchardName");
        Double probability = num(extra, "probability");
        String imageUrl = str(extra, "imageUrl");
        String phenology = str(extra, "phenology");
        Double temperature = num(extra, "temperature");
        Double humidity = num(extra, "humidity");
        String urgency = str(extra, "urgency");
        String consequences = str(extra, "consequences");
        String deviceTimestamp = str(extra, "deviceTimestamp");

        String levelLabel = switch (level) {
            case 1 -> "L1 低风险";
            case 2 -> "L2 中风险";
            case 3 -> "L3 高风险";
            default -> "L" + level;
        };
        String levelColor = switch (level) {
            case 1 -> "#3D8B40";
            case 2 -> "#E68A00";
            case 3 -> "#E53935";
            default -> "#666";
        };
        String levelBadge = switch (level) {
            case 1 -> "🟢";
            case 2 -> "⚠️";
            case 3 -> "🔴";
            default -> "●";
        };
        String timeStr = createdAt != null ? createdAt.format(DTF) : LocalDateTime.now().format(DTF);

        StringBuilder html = new StringBuilder();
        html.append("""
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1.0"></head>
            <body style="margin:0;padding:0;background-color:#f4f4f4;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI','PingFang SC','Microsoft YaHei',sans-serif;">
            <table width="100%" cellpadding="0" cellspacing="0" style="background-color:#f4f4f4;padding:20px 0;">
            <tr><td align="center">
            <table width="600" cellpadding="0" cellspacing="0" style="background-color:#ffffff;border-radius:12px;overflow:hidden;box-shadow:0 2px 12px rgba(0,0,0,0.08);">
            """);

        // Header
        html.append("""
            <tr><td style="background:linear-gradient(135deg,#2E7D32,#43A047);padding:28px 32px;text-align:center;">
            <div style="font-size:28px;line-height:1.2;">🍊</div>
            <div style="color:#ffffff;font-size:20px;font-weight:600;margin-top:4px;">脐橙病虫害监测平台</div>
            <div style="color:rgba(255,255,255,0.85);font-size:13px;margin-top:4px;">预警通知</div>
            </td></tr>
            """);

        // Time
        html.append("<tr><td style=\"padding:16px 32px 8px;color:#888;font-size:13px;text-align:right;\">发出时间：").append(timeStr).append("</td></tr>\n");

        // Level & Orchard
        html.append("<tr><td style=\"padding:8px 32px;\"><table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\"><tr>\n")
                .append("<td style=\"font-size:22px;font-weight:700;color:").append(levelColor).append(";\">")
                .append(levelBadge).append(" [").append(levelLabel).append("]</td>\n");
        if (orchardName != null) {
            html.append("<td style=\"text-align:right;font-size:15px;color:#444;\">果园「").append(orchardName).append("」</td>\n");
        }
        html.append("</tr></table></td></tr>\n");

        // Risk score
        if (probability != null) {
            int score = (int) Math.round(probability * 100);
            html.append("<tr><td style=\"padding:4px 32px 16px;\"><div style=\"font-size:14px;color:#888;\">风险评分：<span style=\"font-weight:600;color:")
                    .append(levelColor).append(";\">").append(score).append("/100</span></div></td></tr>\n");
        }

        // Divider
        html.append("<tr><td style=\"padding:0 32px;\"><hr style=\"border:none;border-top:1px dashed #e0e0e0;margin:0;\"></td></tr>\n");

        // Section: 风险摘要
        html.append(buildSection("一、风险摘要", buildRiskSummaryRows(extra, summary)));

        // Section: 检测详情
        html.append(buildSection("二、检测详情", buildDetectionDetailRows(diseaseName, probability, deviceTimestamp, imageUrl)));

        // Section: 防治建议
        html.append(buildAdviceSection(advice, urgency, consequences));

        // Section: 快速操作
        html.append(buildSection("四、快速操作", buildActionLinks()));

        // Footer
        html.append("""
            <tr><td style="padding:20px 32px;background-color:#f9f9f9;text-align:center;">
            <div style="font-size:12px;color:#aaa;line-height:1.8;">
            此邮件由脐橙病虫害监测平台自动发送，请勿直接回复<br>
            登录小程序查看更多预警信息
            </div>
            </td></tr>
            </table></td></tr></table></body></html>
            """);

        return html.toString();
    }

    // ========================================================================
    // 私有辅助方法
    // ========================================================================

    private Map<String, Object> parseExtraData(String extraData) {
        if (extraData == null || extraData.isBlank()) return Map.of();
        try {
            return OBJECT_MAPPER.readValue(extraData, new TypeReference<>() {});
        } catch (Exception e) {
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

    private static String buildSection(String title, String body) {
        StringBuilder sb = new StringBuilder();
        sb.append("<tr><td style=\"padding:0 32px;\"><div style=\"font-size:15px;font-weight:600;color:#333;padding:14px 0 8px;border-bottom:2px solid #2E7D32;\">")
                .append(title).append("</div></td></tr>\n");
        sb.append("<tr><td style=\"padding:8px 32px 14px;\"><table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\">")
                .append(body).append("</table></td></tr>\n");
        sb.append("<tr><td style=\"padding:0 32px;\"><hr style=\"border:none;border-top:1px dashed #e0e0e0;margin:0;\"></td></tr>\n");
        return sb.toString();
    }

    private static String buildRiskSummaryRows(Map<String, Object> extra, String summary) {
        String phenology = str(extra, "phenology");
        Double temperature = num(extra, "temperature");
        Double humidity = num(extra, "humidity");
        String deviceTimestamp = str(extra, "deviceTimestamp");

        StringBuilder sb = new StringBuilder();
        sb.append(kvRow("触发规则", summary != null ? summary : "-"));
        if (phenology != null) sb.append(kvRow("当前物候", phenology));
        if (temperature != null || humidity != null) {
            StringBuilder env = new StringBuilder();
            if (temperature != null) env.append("温度 ").append(temperature).append("℃");
            if (temperature != null && humidity != null) env.append(" / ");
            if (humidity != null) env.append("湿度 ").append(humidity).append("%");
            sb.append(kvRow("环境数据", env.toString()));
        }
        if (deviceTimestamp != null) {
            sb.append(kvRow("最近识别", deviceTimestamp));
        }
        return sb.toString();
    }

    private static String buildDetectionDetailRows(String diseaseName, Double probability,
                                                    String deviceTimestamp, String imageUrl) {
        StringBuilder sb = new StringBuilder();
        sb.append(kvRow("病害名称", diseaseName != null ? diseaseName : "-"));
        if (probability != null) {
            sb.append(kvRow("识别置信度", String.format("%.1f%%", probability * 100)));
        }
        if (deviceTimestamp != null) {
            sb.append(kvRow("识别时间", deviceTimestamp));
        }
        if (imageUrl != null && !imageUrl.isBlank()) {
            sb.append("<tr><td colspan=\"2\" style=\"padding:6px 0;\">")
                    .append("<a href=\"").append(imageUrl).append("\" style=\"color:#1976D2;font-size:13px;\">📷 查看识别图片</a>")
                    .append("</td></tr>\n");
        }
        return sb.toString();
    }

    private static String buildAdviceSection(String advice, String urgency, String consequences) {
        StringBuilder body = new StringBuilder();

        if (advice != null && !advice.isBlank()) {
            // Split by numbered list markers like "1." or "①" or "1、"
            String[] lines = advice.split("\\d+[.、）]\\s*|①|②|③|④|⑤|⑥|⑦|⑧|⑨|⑩");
            int idx = 1;
            for (String line : lines) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty()) {
                    body.append("<tr><td style=\"padding:4px 0;font-size:14px;color:#444;line-height:1.6;\">")
                            .append("&emsp;").append(idx).append(". ").append(trimmed)
                            .append("</td></tr>\n");
                    idx++;
                }
            }
            // If no split worked, show the whole text
            if (idx == 1) {
                body.setLength(0);
                body.append("<tr><td style=\"padding:4px 0;font-size:14px;color:#444;line-height:1.6;\">")
                        .append(advice).append("</td></tr>\n");
            }
        }

        if (consequences != null && !consequences.isBlank()) {
            body.append("<tr><td style=\"padding:8px 0 4px;font-size:13px;color:#E53935;font-weight:600;\">⚠️ 不处理的后果：</td></tr>\n");
            body.append("<tr><td style=\"padding:0 0 6px;font-size:13px;color:#666;line-height:1.6;\">")
                    .append(consequences).append("</td></tr>\n");
        }

        if (urgency != null && !urgency.isBlank()) {
            body.append("<tr><td style=\"padding:6px 0;font-size:13px;color:#E68A00;font-weight:600;\">🔴 紧急程度：")
                    .append(urgency).append("</td></tr>\n");
        }

        if (body.isEmpty()) {
            body.append("<tr><td style=\"padding:4px 0;font-size:14px;color:#888;\">无防治建议</td></tr>\n");
        }

        return buildSection("三、防治建议", body.toString());
    }

    private static String buildActionLinks() {
        return """
            <tr><td style="padding:4px 0;font-size:14px;">📱 <a href="https://xxx.com/alerts" style="color:#1976D2;text-decoration:none;">查看详情</a></td></tr>
            <tr><td style="padding:4px 0;font-size:14px;">✅ <a href="https://xxx.com/alerts" style="color:#1976D2;text-decoration:none;">标记已处理</a></td></tr>
            <tr><td style="padding:4px 0;font-size:14px;">📖 <a href="https://xxx.com/kb" style="color:#1976D2;text-decoration:none;">知识库</a></td></tr>
            """;
    }

    private static String kvRow(String key, String value) {
        return "<tr>"
                + "<td style=\"padding:5px 0;font-size:13px;color:#888;width:100px;white-space:nowrap;vertical-align:top;\">" + key + "</td>"
                + "<td style=\"padding:5px 0 5px 12px;font-size:14px;color:#444;line-height:1.5;\">" + value + "</td>"
                + "</tr>\n";
    }
}
