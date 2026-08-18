package com.mybatisgx.boot;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusPropertiesCustomizer;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.github.pagehelper.PageInterceptor;
import com.mybatisgx.ext.scripting.xmltags.MgxsqlLanguageDriver;
import com.mybatisgx.mybatisplus.MybatisgxPlusConfiguration;
import org.apache.ibatis.type.TypeAliasRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatisGX + MyBatis-Plus 共存自动配置（Spring Boot 2）
 *
 * <p>装配让位：SqlSessionFactory 由 MP 的 {@link MybatisPlusAutoConfiguration} 创建，
 * 本配置注册 {@link MybatisPlusPropertiesCustomizer}，在 MP 创建 factory 前将
 * {@link MybatisPlusProperties#setConfiguration} 替换为 {@link MybatisgxPlusConfiguration}
 * （继承 MP Configuration 并实现 MyBatisGX 扩展能力），使两者在同一 SqlSessionFactory 上共存。</p>
 *
 * <p>说明：MP 3.5.0 的 sb2 版没有 {@code SqlSessionFactoryBeanCustomizer}（3.5.10 才引入），
 * 因此 sb2 版通过 MybatisPlusPropertiesCustomizer 在配置层替换 Configuration，
 * 效果与 sb3 版在 factory 层替换等价。</p>
 *
 * <p>该 starter 独立发布：移除依赖即可回退到纯 MP / 纯 MyBatisGX 模式，不污染其他 starter。</p>
 *
 * @author ccxuef
 * @description MyBatisGX + MyBatis-Plus 共存自动配置（Spring Boot 2）
 * @date 2026/8/16
 */
@Configuration
@ConditionalOnClass({MybatisPlusAutoConfiguration.class, MybatisSqlSessionFactoryBean.class, MybatisgxPlusConfiguration.class})
public class MybatisgxCompatAutoConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(MybatisgxCompatAutoConfiguration.class);

    /**
     * 将 MP 的 {@link MybatisPlusProperties#configuration} 替换为 MybatisgxPlusConfiguration。
     *
     * <p>MP 3.5.0 的 applyConfiguration 在未显式设置 configuration 时会 new MybatisConfiguration，
     * 此处提前替换，使 MP 创建 factory 时直接使用 MybatisgxPlusConfiguration。</p>
     */
    @Bean
    public MybatisPlusPropertiesCustomizer mybatisgxPlusConfigurationCustomizer() {
        return properties -> {
            MybatisgxPlusConfiguration mybatisgxPlusConfiguration = new MybatisgxPlusConfiguration();
            // 注册 mgxsql 别名（标准 starter 通过 ConfigurationCustomizer 注册，
            // 共存模式 factory 由 MP 创建不回调该接口，故在此直接注册）
            TypeAliasRegistry typeAliasRegistry = mybatisgxPlusConfiguration.getTypeAliasRegistry();
            typeAliasRegistry.registerAlias("mgxsql", MgxsqlLanguageDriver.class);
            properties.setConfiguration(mybatisgxPlusConfiguration);
            LOGGER.info("MyBatisGX: replace MybatisConfiguration with MybatisgxPlusConfiguration for MP coexistence");
        };
    }

    /**
     * PageHelper 分页拦截器：MyBatisGX 分页方法（带 Pageable 参数）依赖此拦截器
     * 消费 ThreadLocal 改写 SQL、回填 total。
     *
     * <p>与 MP 的 MybatisPlusInterceptor 判别机制互斥——PageHelper 靠 ThreadLocal，
     * MP 靠入参 IPage，两者同链不互相误触发。</p>
     */
    @Bean
    public PageInterceptor pageInterceptor() {
        return new PageInterceptor();
    }

    /**
     * Jackson 混入：序列化时忽略 MyBatis 懒加载代理对象的 handler 字段，
     * 否则 MyBatisGX 关联查询懒加载序列化会报错。
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer mybatisgxJacksonCustomizer() {
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
