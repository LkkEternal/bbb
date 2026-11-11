package com.emotion.admin.service;

import com.emotion.admin.dto.AlertEmailData;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 邮件发送服务（用于情绪预警邮件提醒）
 * <p>
 * 固定静态模板 + 占位符替换：邮件版式由静态 HTML 模板（alert_email.html）定义，
 * 发送时仅需将结构化数据（标题、称呼、情绪、次数、建议、时间）填入占位符即可，
 * 不再解析文案文本。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private static final String TEMPLATE_PATH = "templates/email/alert_email.html";

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String from;

    /**
     * 发送预警提醒邮件
     *
     * @param data 结构化邮件数据（to/subject 必填，其余占位符可空）
     * @return 是否发送成功
     */
    public boolean sendAlertEmail(AlertEmailData data) {
        if (data == null || data.getTo() == null || data.getTo().isBlank()) {
            log.warn("[Mail] 收件人邮箱为空，跳过");
            return false;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(data.getTo());
            helper.setSubject(data.getSubject() != null && !data.getSubject().isBlank()
                    ? data.getSubject() : "表情检测预警提醒");
            helper.setText(buildHtml(data), true);
            mailSender.send(message);
            log.info("[Mail] 预警邮件发送成功: to={}, subject={}", data.getTo(), data.getSubject());
            return true;
        } catch (Exception e) {
            log.error("[Mail] 预警邮件发送失败: to={}, subject={}", data.getTo(), data.getSubject(), e);
            return false;
        }
    }

    /**
     * 加载静态模板并替换占位符
     */
    private String buildHtml(AlertEmailData data) throws Exception {
        String template = StreamUtils.copyToString(
                new ClassPathResource(TEMPLATE_PATH).getInputStream(), StandardCharsets.UTF_8);

        Map<String, String> params = new LinkedHashMap<>();
        params.put("{{title}}", data.getTitle());
        params.put("{{userName}}", data.getUserName());
        params.put("{{emotion}}", data.getEmotion());
        params.put("{{count}}", data.getCount() == null ? "" : String.valueOf(data.getCount()));
        params.put("{{advice}}", data.getAdvice());
        params.put("{{lastTime}}", data.getLastTime());

        for (Map.Entry<String, String> entry : params.entrySet()) {
            template = template.replace(entry.getKey(), escapeHtml(entry.getValue()));
        }
        return template;
    }

    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
