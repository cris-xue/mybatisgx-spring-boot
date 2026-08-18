package com.mybatisgx.boot;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.baomidou.mybatisplus.autoconfigure.SqlSessionFactoryBeanCustomizer;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.github.pagehelper.PageInterceptor;
import com.mybatisgx.ext.scripting.xmltags.MgxsqlLanguageDriver;
import com.mybatisgx.mybatisplus.MybatisgxPlusConfiguration;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.TypeAliasRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

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
@ConditionalOnClass({MybatisPlusAutoConfiguration.class, MybatisSqlSessionFactoryBean.class, MybatisgxPlusConfiguration.class})
public class MybatisgxCompatAutoConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(MybatisgxCompatAutoConfiguration.class);

    /**
     * 将 MP 构建的 MybatisConfiguration 替换为 MybatisgxPlusConfiguration。
     *
     * <p>MP 的 applyConfiguration 已 new MybatisConfiguration 并 setConfiguration，
     * 本 customizer 在其后执行：拷贝原生 Configuration 非 final 字段到新的
     * MybatisgxPlusConfiguration，再 setConfiguration 回 factory bean。</p>
     */
    @Bean
    public SqlSessionFactoryBeanCustomizer mybatisgxPlusSqlSessionFactoryBeanCustomizer() {
        return factoryBean -> {
            MybatisConfiguration source = factoryBean.getConfiguration();
            MybatisgxPlusConfiguration mybatisgxPlusConfiguration = new MybatisgxPlusConfiguration();
            if (source != null) {
                this.copyNonFinalFields(source, mybatisgxPlusConfiguration);
            }
            // 注册 mgxsql 别名（标准 starter 通过 ConfigurationCustomizer 注册，
            // 共存模式 factory 由 MP 创建不回调该接口，故在此直接注册）
            TypeAliasRegistry typeAliasRegistry = mybatisgxPlusConfiguration.getTypeAliasRegistry();
            typeAliasRegistry.registerAlias("mgxsql", MgxsqlLanguageDriver.class);
            factoryBean.setConfiguration(mybatisgxPlusConfiguration);
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

    private void copyNonFinalFields(Configuration source, MybatisgxPlusConfiguration target) {
        ReflectionUtils.doWithFields(Configuration.class, field -> {
            if (Modifier.isFinal(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) {
                return;
            }
            ReflectionUtils.makeAccessible(field);
            Object value = ReflectionUtils.getField(field, source);
            ReflectionUtils.setField(field, target, value);
        });
    }
}
