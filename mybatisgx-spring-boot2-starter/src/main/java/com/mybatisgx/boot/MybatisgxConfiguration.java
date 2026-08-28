package com.mybatisgx.boot;

import com.github.pagehelper.PageInterceptor;
import com.mybatisgx.executor.keygen.SnowKeyGenerator;
import com.mybatisgx.ext.scripting.xmltags.MgxsqlLanguageDriver;
import org.apache.ibatis.type.TypeAliasRegistry;
import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;

public class MybatisgxConfiguration {

    @Bean
    public PageInterceptor pageInterceptor() {
        return new PageInterceptor();
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer customizer() {
        return builder -> builder.mixIn(Object.class, IgnoreHandlerMixin.class);
    }

    @Bean
    public ConfigurationCustomizer configurationCustomizer() {
        return configuration -> {
            TypeAliasRegistry typeAliasRegistry = configuration.getTypeAliasRegistry();
            typeAliasRegistry.registerAlias("mgxsql", MgxsqlLanguageDriver.class);
        };
    }

    @Bean
    @ConditionalOnMissingBean
    public SnowKeyGenerator snowKeyGenerator() {
        return new SnowKeyGenerator();
    }
}
