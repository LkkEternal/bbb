package com.citrus.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志注解
 * 用于AOP切面记录关键操作日志
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperationLog {

    /**
     * 操作模块
     * 如：auth、user、orchard、knowledge、feedback
     */
    String module() default "";

    /**
     * 操作类型
     * 如：login、logout、register、create、update、delete、export、import、upload
     */
    String action() default "";

    /**
     * 操作描述
     */
    String description() default "";

    /**
     * 资源类型
     * 如：document、orchard、user
     */
    String resourceType() default "";

    /**
     * 资源ID的SpEL表达式
     * 如：#id、#result.id
     */
    String resourceId() default "";
}
