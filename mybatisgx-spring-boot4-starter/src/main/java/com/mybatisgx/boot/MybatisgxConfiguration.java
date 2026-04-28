package com.mybatisgx.boot;

import org.apache.ibatis.session.Configuration;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.boot.autoconfigure.SqlSessionFactoryBeanCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class MybatisgxConfiguration {

    @Bean
    public SqlSessionFactoryBeanCustomizer sqlSessionFactoryBeanCustomizer() {
        return factoryBean -> {
            Field field = ReflectionUtils.findField(SqlSessionFactoryBean.class, "configuration");
            field.setAccessible(true);
            Configuration configuration = (Configuration) ReflectionUtils.getField(field, factoryBean);
            com.mybatisgx.ext.session.MybatisgxConfiguration mybatisgxConfiguration = this.copyNonFinalFields(configuration);
            factoryBean.setConfiguration(mybatisgxConfiguration);
        };
    }

    private com.mybatisgx.ext.session.MybatisgxConfiguration copyNonFinalFields(Configuration source) {
        com.mybatisgx.ext.session.MybatisgxConfiguration mybatisgxConfiguration = new com.mybatisgx.ext.session.MybatisgxConfiguration();
        ReflectionUtils.doWithFields(Configuration.class, field -> {
            if (Modifier.isFinal(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) {
                return;
            }
            ReflectionUtils.makeAccessible(field);
            Object value = ReflectionUtils.getField(field, source);
            ReflectionUtils.setField(field, mybatisgxConfiguration, value);
        });
        return mybatisgxConfiguration;
    }
}
