package com.mybatisgx.boot.mp;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusPropertiesCustomizer;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.mybatisgx.mybatisplus.MybatisgxPlusConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
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
public class MybatisgxMpCompatAutoConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(MybatisgxMpCompatAutoConfiguration.class);

    /**
     * 将 MP 的 {@link MybatisPlusProperties#configuration} 替换为 MybatisgxPlusConfiguration。
     *
     * <p>MP 3.5.0 的 applyConfiguration 在未显式设置 configuration 时会 new MybatisConfiguration，
     * 此处提前替换，使 MP 创建 factory 时直接使用 MybatisgxPlusConfiguration。</p>
     */
    @Bean
    public MybatisPlusPropertiesCustomizer mybatisgxPlusConfigurationCustomizer() {
        return properties -> {
            properties.setConfiguration(new MybatisgxPlusConfiguration());
            LOGGER.info("MyBatisGX: replace MybatisConfiguration with MybatisgxPlusConfiguration for MP coexistence");
        };
    }

    /**
     * 双继承检测：同一接口同时继承 BaseMapper 与 Dao 时启动报错。
     */
    @Bean
    public static MybatisgxMpDoubleInheritanceChecker mybatisgxMpDoubleInheritanceChecker() {
        return new MybatisgxMpDoubleInheritanceChecker();
    }
}
