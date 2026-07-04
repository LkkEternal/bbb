package com.citrus.push.service.impl;

import com.citrus.push.service.EmailPushService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * 邮件推送服务实现（网易 163 邮箱）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailPushServiceImpl implements EmailPushService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromAddress;

    @Override
    public boolean sendAlertEmail(String to, String subject, String content) {
        if (fromAddress == null || fromAddress.isBlank()) {
            log.warn("网易邮箱未配置, 跳过邮件发送: to={}, subject={}", to, subject);
            return false;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true); // true = HTML

            mailSender.send(message);
            log.info("预警邮件发送成功: to={}, subject={}", to, subject);
            return true;
        } catch (MailException | MessagingException e) {
            log.error("预警邮件发送失败: to={}, subject={}, err={}", to, subject, e.getMessage());
            return false;
        }
    }
}
