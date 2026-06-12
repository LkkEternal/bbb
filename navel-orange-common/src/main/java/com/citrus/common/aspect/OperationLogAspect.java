package com.citrus.common.aspect;

import com.citrus.common.annotation.OperationLog;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 操作日志切面
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final ObjectMapper objectMapper;

    /**
     * 日志存储接口，由业务模块实现
     */
    private final LogStorage logStorage;

    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperationLog operationLog) throws Throwable {
        long startTime = System.currentTimeMillis();

        // 构建日志对象
        LogRecord record = new LogRecord();
        record.setModule(operationLog.module().getCode());
        record.setAction(operationLog.action().getCode());
        record.setDescription(operationLog.description());
        record.setCreatedAt(LocalDateTime.now());

        // 获取请求信息
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                record.setIpAddress(getClientIp(request));
                record.setUserAgent(request.getHeader("User-Agent"));
            }
        } catch (Exception e) {
            log.warn("获取请求信息失败", e);
        }

        // 获取方法信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        record.setResourceType(signature.getDeclaringType().getSimpleName());
        record.setResourceMethod(method.getName());

        // 保存请求参数
        if (operationLog.saveParams()) {
            try {
                String params = objectMapper.writeValueAsString(joinPoint.getArgs());
                record.setDetail(params.length() > 2000 ? params.substring(0, 2000) : params);
            } catch (Exception e) {
                log.warn("序列化请求参数失败", e);
            }
        }

        Object result = null;
        try {
            // 执行原方法
            result = joinPoint.proceed();

            // 保存响应结果
            if (operationLog.saveResult() && result != null) {
                try {
                    String resultStr = objectMapper.writeValueAsString(result);
                    if (record.getDetail() == null) {
                        record.setDetail(resultStr.length() > 2000 ? resultStr.substring(0, 2000) : resultStr);
                    } else {
                        record.setDetail(record.getDetail() + "\n--- Response ---\n" + 
                                (resultStr.length() > 1000 ? resultStr.substring(0, 1000) : resultStr));
                    }
                } catch (Exception e) {
                    log.warn("序列化响应结果失败", e);
                }
            }

            record.setStatus(1); // 成功
            return result;
        } catch (Throwable e) {
            record.setStatus(0); // 失败
            record.setDetail("异常: " + e.getMessage());
            throw e;
        } finally {
            record.setCostTime(System.currentTimeMillis() - startTime);
            // 异步保存日志
            try {
                logStorage.save(record);
            } catch (Exception e) {
                log.error("保存操作日志失败", e);
            }
            log.info("操作日志: [{}] {} - {} - {}ms", 
                    record.getModule(), record.getAction(), record.getDescription(), record.getCostTime());
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理时取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.substring(0, ip.indexOf(",")).trim();
        }
        return ip;
    }

    /**
     * 日志存储接口
     */
    public interface LogStorage {
        void save(LogRecord record);
    }

    /**
     * 日志记录
     */
    @lombok.Data
    public static class LogRecord {
        private Long userId;
        private String module;
        private String action;
        private String resourceType;
        private String resourceId;
        private String resourceMethod;
        private String description;
        private String detail;
        private String ipAddress;
        private String userAgent;
        private Integer status;
        private Long costTime;
        private LocalDateTime createdAt;
    }
}
