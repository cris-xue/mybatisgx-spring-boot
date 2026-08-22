package com.mybatisgx.boot;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusPropertiesCustomizer;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.github.pagehelper.PageInterceptor;
import com.mybatisgx.ext.scripting.xmltags.MgxsqlLanguageDriver;
import org.apache.ibatis.type.TypeAliasRegistry;
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
    public MybatisPlusPropertiesCustomizer mybatisgxPlusConfigurationCustomizer() {
        return mybatisPlusProperties -> {
            MybatisConfiguration mybatisConfiguration = mybatisPlusProperties.getConfiguration();
            TypeAliasRegistry typeAliasRegistry = mybatisConfiguration.getTypeAliasRegistry();
            typeAliasRegistry.registerAlias("mgxsql", MgxsqlLanguageDriver.class);
        };
    }

    /**
     * 双继承检测：同一接口同时继承 BaseMapper 与 Dao 时启动报错。
     */
    @Bean
    public static MybatisgxDoubleInheritanceChecker mybatisgxMpDoubleInheritanceChecker() {
        return new MybatisgxDoubleInheritanceChecker();
    }
}
