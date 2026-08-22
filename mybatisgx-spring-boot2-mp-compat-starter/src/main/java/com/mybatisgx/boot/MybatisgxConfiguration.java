package com.mybatisgx.boot;

import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.baomidou.mybatisplus.autoconfigure.SqlSessionFactoryBeanCustomizer;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.github.pagehelper.PageInterceptor;
import com.mybatisgx.ext.scripting.xmltags.MgxsqlLanguageDriver;
import com.mybatisgx.mybatisplus.MybatisgxPlusConfiguration;
import org.apache.ibatis.type.TypeAliasRegistry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.util.CollectionUtils;

import java.util.List;

public class MybatisgxConfiguration {

    private final List<ConfigurationCustomizer> configurationCustomizers;

    public MybatisgxConfiguration(ObjectProvider<List<ConfigurationCustomizer>> configurationCustomizersProvider) {
        this.configurationCustomizers = configurationCustomizersProvider.getIfAvailable();
    }

    @Bean
    public SqlSessionFactoryBeanCustomizer mybatisgxPlusSqlSessionFactoryBeanCustomizer(MybatisgxProperties mybatisgxProperties) {
        return factoryBean -> {
            MybatisgxPlusConfiguration mybatisgxPlusConfiguration = new MybatisgxPlusConfiguration();
            // 未配置 mybatisgx.configuration.* 时 CoreConfiguration 为 null（MP 3.5.17 不预初始化），需空判断
            MybatisgxProperties.CoreConfiguration coreConfiguration = mybatisgxProperties.getConfiguration();
            if (coreConfiguration != null) {
                coreConfiguration.applyTo(mybatisgxPlusConfiguration);
            }
            if (!CollectionUtils.isEmpty(this.configurationCustomizers)) {
                for (ConfigurationCustomizer customizer : this.configurationCustomizers) {
                    customizer.customize(mybatisgxPlusConfiguration);
                }
            }
            TypeAliasRegistry typeAliasRegistry = mybatisgxPlusConfiguration.getTypeAliasRegistry();
            typeAliasRegistry.registerAlias("mgxsql", MgxsqlLanguageDriver.class);
            factoryBean.setConfiguration(mybatisgxPlusConfiguration);
        };
    }

    @Bean
    public PageInterceptor pageInterceptor() {
        return new PageInterceptor();
    }

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        return interceptor;
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer customizer() {
        return builder -> builder.mixIn(Object.class, IgnoreHandlerMixin.class);
    }

    /**
     * 双继承检测：同一接口同时继承 BaseMapper 与 Dao 时启动报错。
     */
    @Bean
    public static MybatisgxDoubleInheritanceChecker mybatisgxMpDoubleInheritanceChecker() {
        return new MybatisgxDoubleInheritanceChecker();
    }
}
