package com.emotion.common.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {

    String module() default "";

    String action() default "";

    String resourceType() default "";

    String resourceId() default "";

    String description() default "";
}
