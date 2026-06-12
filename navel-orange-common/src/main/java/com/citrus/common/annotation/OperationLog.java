package com.citrus.common.annotation;

import com.citrus.common.enums.LogAction;
import com.citrus.common.enums.LogModule;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperationLog {

    /**
     * 模块
     */
    LogModule module();

    /**
     * 操作类型
     */
    LogAction action();

    /**
     * 操作描述
     */
    String description() default "";

    /**
     * 是否保存请求参数
     */
    boolean saveParams() default false;

    /**
     * 是否保存响应结果
     */
    boolean saveResult() default false;
}
