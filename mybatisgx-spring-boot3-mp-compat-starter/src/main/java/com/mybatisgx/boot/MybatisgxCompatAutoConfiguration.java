package com.mybatisgx.boot;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.baomidou.mybatisplus.autoconfigure.SqlSessionFactoryBeanCustomizer;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.mybatisgx.mybatisplus.MybatisgxPlusConfiguration;
import org.apache.ibatis.session.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
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
            factoryBean.setConfiguration(mybatisgxPlusConfiguration);
            LOGGER.info("MyBatisGX: replace MybatisConfiguration with MybatisgxPlusConfiguration for MP coexistence");
        };
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
