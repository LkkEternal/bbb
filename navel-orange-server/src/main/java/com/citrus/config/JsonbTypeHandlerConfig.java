package com.citrus.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * JSONB TypeHandler 注册配置
 * 独立于 MyBatisPlusConfig，避免循环依赖
 */
@Configuration
public class JsonbTypeHandlerConfig {

    private final SqlSessionFactory sqlSessionFactory;

    public JsonbTypeHandlerConfig(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    @PostConstruct
    public void registerTypeHandlers() {
        org.apache.ibatis.session.Configuration configuration = sqlSessionFactory.getConfiguration();
        configuration.getTypeHandlerRegistry().register(JsonbTypeHandler.class);
    }
}
