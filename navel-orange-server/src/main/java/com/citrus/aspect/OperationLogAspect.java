package com.citrus.aspect;

import com.citrus.auth.vo.LoginVO;
import com.citrus.operationLog.service.OperationLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

/**
 * 操作日志切面
 * 拦截带有 @OperationLog 注解的方法，记录操作日志
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final OperationLogService operationLogService;

    /**
     * 环绕通知，拦截带有 @OperationLog 注解的方法
     *
     * @param point         连接点
     * @param operationLog  日志注解
     * @return 方法执行结果
     * @throws Throwable 方法执行异常
     */
    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint point, com.citrus.annotation.OperationLog operationLog) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        // 获取请求信息
        HttpServletRequest request = getRequest();
        String ipAddress = getClientIp(request);
        String userAgent = request != null ? request.getHeader("User-Agent") : "";
        
        // 执行目标方法
        Object result = point.proceed();
        
        long executionTime = System.currentTimeMillis() - startTime;
        
        // 从返回结果中获取用户ID（仅适用于登录/注册场景）
        Long userId = extractUserId(result);
        
        // 构建日志对象并异步保存
        com.citrus.common.entity.OperationLog logEntity = com.citrus.common.entity.OperationLog.builder()
                .userId(userId)
                .module(operationLog.module())
                .action(operationLog.action())
                .description(operationLog.description())
                .detail("{\"executionTime\": " + executionTime + "ms}")
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .createdAt(LocalDateTime.now())
                .build();
        
        // 异步保存日志
        operationLogService.saveLog(logEntity);
        
        return result;
    }

    /**
     * 从返回结果中提取用户ID
     * 适用于登录/注册返回 LoginVO 的场景
     */
    private Long extractUserId(Object result) {
        if (result instanceof LoginVO loginVO) {
            return loginVO.getId();
        }
        return null;
    }

    /**
     * 获取当前请求对象
     */
    private HttpServletRequest getRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) 
                    RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            log.warn("获取请求对象失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取客户端真实IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        
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
        
        // 多个代理时，第一个IP为客户端真实IP
        if (ip != null && ip.contains(",")) {
            ip = ip.substring(0, ip.indexOf(",")).trim();
        }
        
        return ip;
    }
}
