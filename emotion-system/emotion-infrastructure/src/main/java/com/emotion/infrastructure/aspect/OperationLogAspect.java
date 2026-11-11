package com.emotion.infrastructure.aspect;

import com.emotion.common.annotation.OperationLog;
import com.emotion.infrastructure.entity.OperationLogEntity;
import com.emotion.infrastructure.service.OperationLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final OperationLogService operationLogService;
    private final ObjectMapper objectMapper;

    @Around("@annotation(com.emotion.common.annotation.OperationLog)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        OperationLogEntity logEntity = new OperationLogEntity();
        logEntity.setOperationTime(LocalDateTime.now());

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        OperationLog annotation = method.getAnnotation(OperationLog.class);
        logEntity.setModule(annotation.module());
        logEntity.setAction(annotation.action());
        logEntity.setResourceType(annotation.resourceType());
        logEntity.setResourceId(annotation.resourceId());
        logEntity.setDescription(annotation.description());

        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                logEntity.setIpAddress(getClientIp(request));
            }
        } catch (Exception e) {
            // ignore
        }

        extractUserId(logEntity);

        try {
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                logEntity.setDetail(objectMapper.writeValueAsString(args));
            }
        } catch (Exception e) {
            // ignore
        }

        try {
            return joinPoint.proceed();
        } catch (Throwable e) {
            extractUserId(logEntity);
            throw e;
        } finally {
            if (logEntity.getUserId() == null) {
                extractUserId(logEntity);
            }
            long executionTime = System.currentTimeMillis() - startTime;
            log.info("[OperationLog] 切面触发: module={}, action={}, ip={}, userId={}, 耗时={}ms",
                    logEntity.getModule(), logEntity.getAction(),
                    logEntity.getIpAddress(), logEntity.getUserId(), executionTime);
            operationLogService.saveLog(logEntity);
        }
    }

    private void extractUserId(OperationLogEntity logEntity) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() != null) {
                Object principal = authentication.getPrincipal();
                if (principal instanceof Long) {
                    logEntity.setUserId((Long) principal);
                } else if (principal instanceof Integer) {
                    logEntity.setUserId(((Integer) principal).longValue());
                } else if (principal instanceof String) {
                    try {
                        logEntity.setUserId(Long.parseLong((String) principal));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        } catch (Exception e) {
            log.error("[OperationLog] 获取用户ID异常", e);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
