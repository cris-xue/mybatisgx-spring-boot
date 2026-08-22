package com.mybatisgx.boot;

import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusPropertiesCustomizer;
import com.baomidou.mybatisplus.autoconfigure.SqlSessionFactoryBeanCustomizer;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.spring.MybatisSqlSessionFactoryBean;
import com.mybatisgx.mybatisplus.MybatisgxPlusConfiguration;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.type.TypeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ResourceLoader;

import java.util.List;

/**
 * MyBatisGX + MyBatis-Plus 共存自动配置
 *
 * <p>装配让位：SqlSessionFactory 由 MP 的 {@link MybatisPlusAutoConfiguration} 创建，
 * 本配置通过 MP 的 {@link SqlSessionFactoryBeanCustomizer} 将 MP 构建的
 * {@link MybatisConfiguration} 替换为 {@link MybatisgxPlusConfiguration}
 * （继承 MP Configuration 并实现 MyBatisGX 扩展能力），使两者在同一 SqlSessionFactory 上共存。</p>
 *
 * <p>该 starter 独立发布：移除依赖即可回退到纯 MP / 纯 MyBatisGX 模式，不污染其他 starter。</p>
 *
 * @author ccxuef
 * @description MyBatisGX + MyBatis-Plus 共存自动配置
 * @date 2026/8/15
 */
@AutoConfiguration
@Import({MybatisgxConfiguration.class})
@ConditionalOnClass({MybatisPlusAutoConfiguration.class, MybatisSqlSessionFactoryBean.class, MybatisgxPlusConfiguration.class})
@EnableConfigurationProperties(MybatisgxProperties.class)
public class MybatisgxAutoConfiguration extends MybatisPlusAutoConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(MybatisgxAutoConfiguration.class);

    public MybatisgxAutoConfiguration(MybatisgxProperties properties, ObjectProvider<Interceptor[]> interceptorsProvider, ObjectProvider<TypeHandler[]> typeHandlersProvider, ObjectProvider<LanguageDriver[]> languageDriversProvider, ResourceLoader resourceLoader, ObjectProvider<DatabaseIdProvider> databaseIdProvider, ObjectProvider<List<ConfigurationCustomizer>> configurationCustomizersProvider, ObjectProvider<List<SqlSessionFactoryBeanCustomizer>> sqlSessionFactoryBeanCustomizers, ObjectProvider<List<MybatisPlusPropertiesCustomizer>> mybatisPlusPropertiesCustomizerProvider, ApplicationContext applicationContext) {
        super(properties, interceptorsProvider, typeHandlersProvider, languageDriversProvider, resourceLoader, databaseIdProvider, configurationCustomizersProvider, sqlSessionFactoryBeanCustomizers, mybatisPlusPropertiesCustomizerProvider, applicationContext);
    }
}
