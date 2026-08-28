package com.mybatisgx.boot;

import com.github.pagehelper.PageInterceptor;
import com.mybatisgx.executor.keygen.SnowKeyGenerator;
import com.mybatisgx.ext.scripting.xmltags.MgxsqlLanguageDriver;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.TypeAliasRegistry;
import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.mybatis.spring.boot.autoconfigure.MybatisProperties;
import org.mybatis.spring.boot.autoconfigure.SqlSessionFactoryBeanCustomizer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Modifier;
import java.util.List;

public class MybatisgxConfiguration {

    private final List<ConfigurationCustomizer> configurationCustomizers;

    public MybatisgxConfiguration(ObjectProvider<List<ConfigurationCustomizer>> configurationCustomizersProvider) {
        this.configurationCustomizers = configurationCustomizersProvider.getIfAvailable();
    }

    @Bean
    public SqlSessionFactoryBeanCustomizer sqlSessionFactoryBeanCustomizer(MybatisgxProperties mybatisgxProperties) {
        return factoryBean -> {
            com.mybatisgx.ext.session.MybatisgxConfiguration mybatisgxConfiguration = new com.mybatisgx.ext.session.MybatisgxConfiguration();
            MybatisProperties.CoreConfiguration coreConfiguration = mybatisgxProperties.getConfiguration();
            // 未配置 mybatisgx.configuration.* 时 CoreConfiguration 为 null，需空判断
            if (coreConfiguration != null) {
                coreConfiguration.applyTo(mybatisgxConfiguration);
            }
            if (mybatisgxConfiguration != null && !CollectionUtils.isEmpty(this.configurationCustomizers)) {
                for (ConfigurationCustomizer customizer : this.configurationCustomizers) {
                    customizer.customize(mybatisgxConfiguration);
                }
            }

            /*Field field = ReflectionUtils.findField(SqlSessionFactoryBean.class, "configuration");
            field.setAccessible(true);
            Configuration configuration = (Configuration) ReflectionUtils.getField(field, factoryBean);
            com.mybatisgx.ext.session.MybatisgxConfiguration mybatisgxConfiguration = this.copyNonFinalFields(configuration);*/
            TypeAliasRegistry typeAliasRegistry = mybatisgxConfiguration.getTypeAliasRegistry();
            typeAliasRegistry.registerAlias("mgxsql", MgxsqlLanguageDriver.class);
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

    @Bean
    public PageInterceptor pageInterceptor() {
        return new PageInterceptor();
    }

    @Bean
    @ConditionalOnMissingBean
    public SnowKeyGenerator snowKeyGenerator() {
        return new SnowKeyGenerator();
    }
}
