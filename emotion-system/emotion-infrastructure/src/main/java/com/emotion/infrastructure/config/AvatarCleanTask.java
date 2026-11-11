package com.emotion.infrastructure.config;

import com.emotion.infrastructure.service.QiniuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class AvatarCleanTask {

    private final StringRedisTemplate redisTemplate;
    private final QiniuService qiniuService;

    private static final String AVATAR_HISTORY_PREFIX = "avatar:history:";

    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanOldAvatars() {
        log.info("[AvatarClean] 开始清理旧头像...");
        Set<String> keys = redisTemplate.keys(AVATAR_HISTORY_PREFIX + "*");
        if (keys == null || keys.isEmpty()) {
            log.info("[AvatarClean] 没有需要清理的旧头像");
            return;
        }

        int deletedCount = 0;
        for (String key : keys) {
            Set<String> oldUrls = redisTemplate.opsForSet().members(key);
            if (oldUrls == null || oldUrls.isEmpty()) {
                redisTemplate.delete(key);
                continue;
            }

            for (String url : oldUrls) {
                try {
                    qiniuService.deleteFile(url);
                    deletedCount++;
                } catch (Exception e) {
                    log.error("[AvatarClean] 删除文件失败: {}", url, e);
                }
            }
            redisTemplate.delete(key);
        }

        log.info("[AvatarClean] 清理完成，共删除 {} 个旧头像", deletedCount);
    }
}
