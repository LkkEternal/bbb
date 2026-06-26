package com.citrus.aspect;

import com.citrus.auth.model.LoginUser;
import com.citrus.auth.vo.LoginVO;
import com.citrus.operationLog.service.OperationLogService;
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
import java.lang.reflect.Parameter;
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
        
        // 获取请求信息（增强异常处理）
        String ipAddress = "unknown";
        String userAgent = "";
        try {
            HttpServletRequest request = getRequest();
            ipAddress = getClientIp(request);
            userAgent = request != null ? request.getHeader("User-Agent") : "";
        } catch (Exception e) {
            log.warn("获取请求信息失败: {}", e.getMessage());
        }
        
        // 执行目标方法
        Object result = point.proceed();
        
        long executionTime = System.currentTimeMillis() - startTime;
        
        // 从返回结果中获取用户ID（增强异常处理）
        Long userId = null;
        try {
            userId = extractUserId(result);
        } catch (Exception e) {
            log.warn("提取用户ID失败: {}", e.getMessage());
        }
        
        // 解析 resourceId
        String resourceId = null;
        try {
            resourceId = parseResourceId(point, result, operationLog.resourceId());
        } catch (Exception e) {
            log.warn("解析resourceId失败: {}", e.getMessage());
        }
        
        // 构建日志对象并异步保存（增强异常处理）
        try {
            com.citrus.common.entity.OperationLog logEntity = com.citrus.common.entity.OperationLog.builder()
                    .userId(userId)
                    .module(operationLog.module())
                    .action(operationLog.action())
                    .resourceType(operationLog.resourceType())
                    .resourceId(resourceId)
                    .description(operationLog.description())
                    .detail("{\"executionTime\": " + executionTime + "ms}")
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .createdAt(LocalDateTime.now())
                    .build();
            
            operationLogService.saveLog(logEntity);
        } catch (Exception e) {
            log.error("保存操作日志失败: {}", e.getMessage());
        }
        
        return result;
    }

    /**
     * 解析资源ID
     * 支持两种格式：
     * 1. 纯数字：直接返回
     * 2. 参数名：如 "id"，从方法参数中获取
     * 3. #result.xxx：从返回结果中获取
     */
    private String parseResourceId(ProceedingJoinPoint point, Object result, String resourceIdExpr) {
        if (resourceIdExpr == null || resourceIdExpr.isEmpty()) {
            return null;
        }
        
        // 如果是纯数字，直接返回
        if (resourceIdExpr.matches("\\d+")) {
            return resourceIdExpr;
        }
        
        try {
            MethodSignature signature = (MethodSignature) point.getSignature();
            Method method = signature.getMethod();
            Parameter[] parameters = method.getParameters();
            Object[] args = point.getArgs();
            
            // 处理 #result.xxx 格式
            if (resourceIdExpr.startsWith("#result.")) {
                String fieldName = resourceIdExpr.substring(8);
                if (result != null) {
                    try {
                        // 尝试通过 getter 方法获取字段值
                        String getterName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
                        java.lang.reflect.Method getter = result.getClass().getMethod(getterName);
                        Object value = getter.invoke(result);
                        return value != null ? value.toString() : null;
                    } catch (NoSuchMethodException e) {
                        // 尝试 isXxx 方法
                        String isName = "is" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
                        try {
                            java.lang.reflect.Method isMethod = result.getClass().getMethod(isName);
                            Object value = isMethod.invoke(result);
                            return value != null ? value.toString() : null;
                        } catch (NoSuchMethodException ex) {
                            log.warn("无法获取结果对象的字段: {}", fieldName);
                        }
                    }
                }
                return null;
            }
            
            // 处理 #result 格式
            if ("#result".equals(resourceIdExpr)) {
                return result != null ? result.toString() : null;
            }
            
            // 处理参数名格式
            for (int i = 0; i < parameters.length; i++) {
                if (parameters[i].getName().equals(resourceIdExpr)) {
                    return args[i] != null ? args[i].toString() : null;
                }
            }
            
            log.warn("无法解析resourceId表达式: {}", resourceIdExpr);
            return null;
        } catch (Exception e) {
            log.warn("解析resourceId失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从返回结果中提取用户ID
     * 1. 优先从返回结果中提取（登录/注册场景）
     * 2. 从 SecurityContext 中获取当前登录用户ID（修改密码等场景）
     */
    private Long extractUserId(Object result) {
        // 1. 从返回结果中提取（登录/注册返回 LoginVO 的场景）
        if (result instanceof LoginVO loginVO) {
            return loginVO.getId();
        }

        // 2. 从 SecurityContext 中获取当前登录用户ID
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
                return loginUser.getUserId();
            }
        } catch (Exception e) {
            log.warn("获取当前登录用户ID失败: {}", e.getMessage());
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
