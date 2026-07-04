package com.citrus.util;

import com.aliyun.dypnsapi20170525.Client;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeRequest;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeResponse;
import com.aliyun.teaopenapi.models.Config;
import com.citrus.common.constant.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 短信验证码工具类（阿里云号码认证服务 - 短信认证）
 */
@Slf4j
@Component
public class SmsCodeUtil {

    private final Random random = new Random();

    @Value("${aliyun.sms.access-key-id:}")
    private String accessKeyId;

    @Value("${aliyun.sms.access-key-secret:}")
    private String accessKeySecret;

    @Value("${aliyun.sms.endpoint:}")
    private String endpoint;

    @Value("${aliyun.sms.sign-name:}")
    private String signName;

    @Value("${aliyun.sms.template-code:}")
    private String templateCode;

    @Value("${aliyun.sms.scheme-name:默认方案}")
    private String schemeName;

    private volatile Client smsClient;

    /**
     * 获取号码认证服务 Client（懒加载）
     */
    private Client getSmsClient() {
        if (smsClient == null) {
            synchronized (this) {
                if (smsClient == null) {
                    try {
                        Config config = new Config()
                                .setAccessKeyId(accessKeyId)
                                .setAccessKeySecret(accessKeySecret)
                                .setEndpoint(endpoint);
                        smsClient = new Client(config);
                    } catch (Exception e) {
                        log.error("号码认证服务 Client 初始化失败", e);
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return smsClient;
    }

    /**
     * 发送验证码短信（阿里云号码认证服务 - 短信认证）
     *
     * @param phone         手机号
     * @param code          验证码
     * @param expireMinutes 过期分钟数
     * @return true=发送成功
     */
    public boolean sendCode(String phone, String code, int expireMinutes) {
        if (accessKeyId == null || accessKeyId.isBlank()) {
            log.warn("阿里云短信未配置, 跳过发送: phone={}, code={}", phone, code);
            return false;
        }
        try {
            String templateParam = String.format("{\"code\":\"%s\",\"min\":\"%d\"}", code, expireMinutes);
            SendSmsVerifyCodeRequest req = new SendSmsVerifyCodeRequest()
                    .setPhoneNumber(phone)
                    .setSignName(signName)
                    .setTemplateCode(templateCode)
                    .setTemplateParam(templateParam)
                    .setSchemeName(schemeName)
                    .setCountryCode("86")
                    .setValidTime((long) expireMinutes * 60)
                    .setReturnVerifyCode(true);
            SendSmsVerifyCodeResponse resp = getSmsClient().sendSmsVerifyCode(req);
            boolean success = resp.getBody().getSuccess();
            if (success) {
                log.info("验证码发送成功: phone={}, verifyCode={}",
                        phone, resp.getBody().getModel() != null ? resp.getBody().getModel().getVerifyCode() : "N/A");
            } else {
                log.warn("验证码发送失败: phone={}, code={}, msg={}",
                        phone, resp.getBody().getCode(), resp.getBody().getMessage());
            }
            return success;
        } catch (Exception e) {
            log.error("验证码发送异常: phone={}, err={}", phone, e.getMessage());
            return false;
        }
    }

    /**
     * 生成指定长度的数字验证码
     *
     * @param length 验证码长度
     * @return 验证码字符串
     */
    public String generateCode(int length) {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < length; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    /**
     * 存储验证码到 Redis
     *
     * @param redisTemplate  Redis 模板
     * @param scene          场景（register/login/reset_password）
     * @param phone          手机号
     * @param code           验证码
     * @param expireMinutes  过期时间（分钟）
     */
    public void saveCode(StringRedisTemplate redisTemplate,
                         String scene, String phone, String code, int expireMinutes) {
        String key = buildCodeKey(scene, phone);
        redisTemplate.opsForValue().set(key, code, expireMinutes, TimeUnit.MINUTES);
        log.debug("验证码已存储: key={}, code={}, expire={}min", key, code, expireMinutes);
    }

    /**
     * 验证验证码
     *
     * @param redisTemplate  Redis 模板
     * @param scene          场景
     * @param phone          手机号
     * @param code           用户输入的验证码
     * @return 验证是否成功
     */
    public boolean verifyCode(StringRedisTemplate redisTemplate,
                              String scene, String phone, String code) {
        String key = buildCodeKey(scene, phone);
        String storedCode = redisTemplate.opsForValue().get(key);

        if (storedCode != null && storedCode.equals(code)) {
            redisTemplate.delete(key); // 验证成功后删除
            log.debug("验证码验证成功: key={}", key);
            return true;
        }

        log.debug("验证码验证失败: key={}, input={}, stored={}", key, code, storedCode);
        return false;
    }

    /**
     * 检查发送频率（是否在限制时间内）
     *
     * @param redisTemplate  Redis 模板
     * @param phone          手机号
     * @return true=在限制时间内，false=可以发送
     */
    public boolean isRateLimited(StringRedisTemplate redisTemplate, String phone) {
        String key = buildRateLimitKey(phone);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 设置发送频率限制
     *
     * @param redisTemplate  Redis 模板
     * @param phone          手机号
     */
    public void setRateLimit(StringRedisTemplate redisTemplate, String phone) {
        String key = buildRateLimitKey(phone);
        redisTemplate.opsForValue().set(key, "1",
                Constants.SMS_SEND_INTERVAL_SECONDS, TimeUnit.SECONDS);
        log.debug("发送频率限制已设置: key={}, expire={}s", key, Constants.SMS_SEND_INTERVAL_SECONDS);
    }

    /**
     * 检查是否达到每日发送上限
     *
     * @param redisTemplate  Redis 模板
     * @param phone          手机号
     * @return true=已达到上限，false=未达到上限
     */
    public boolean isDailyLimitReached(StringRedisTemplate redisTemplate, String phone) {
        String key = buildDailyLimitKey(phone);
        String countStr = redisTemplate.opsForValue().get(key);
        int count = countStr != null ? Integer.parseInt(countStr) : 0;
        return count >= Constants.SMS_DAILY_LIMIT;
    }

    /**
     * 增加每日发送次数
     *
     * @param redisTemplate  Redis 模板
     * @param phone          手机号
     */
    public void incrementDailyCount(StringRedisTemplate redisTemplate, String phone) {
        String key = buildDailyLimitKey(phone);
        Long count = redisTemplate.opsForValue().increment(key);
        
        // 如果是第一次设置，设置过期时间到当天结束（每天0点重置）
        if (count != null && count == 1) {
            // 计算到当天结束的秒数
            long secondsUntilEndOfDay = getSecondsUntilEndOfDay();
            redisTemplate.expire(key, secondsUntilEndOfDay, TimeUnit.SECONDS);
            log.debug("每日计数已初始化: key={}, expire={}s", key, secondsUntilEndOfDay);
        }
        
        log.debug("每日发送次数已增加: key={}, count={}", key, count);
    }

    /**
     * 获取今日已发送次数
     *
     * @param redisTemplate  Redis 模板
     * @param phone          手机号
     * @return 已发送次数
     */
    public int getDailyCount(StringRedisTemplate redisTemplate, String phone) {
        String key = buildDailyLimitKey(phone);
        String countStr = redisTemplate.opsForValue().get(key);
        return countStr != null ? Integer.parseInt(countStr) : 0;
    }

    /**
     * 计算到当天结束的秒数（每天0点重置）
     */
    private long getSecondsUntilEndOfDay() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.LocalDateTime endOfDay = now.toLocalDate().plusDays(1).atStartOfDay();
        return java.time.Duration.between(now, endOfDay).getSeconds();
    }

    /**
     * 构建验证码 Redis Key
     */
    private String buildCodeKey(String scene, String phone) {
        return Constants.REDIS_SMS_CODE + scene + ":" + phone;
    }

    /**
     * 构建发送频率限制 Redis Key
     */
    private String buildRateLimitKey(String phone) {
        return Constants.REDIS_SMS_CODE + "rate:" + phone;
    }

    /**
     * 构建每日发送次数限制 Redis Key
     */
    private String buildDailyLimitKey(String phone) {
        return Constants.REDIS_SMS_DAILY + phone;
    }
}
