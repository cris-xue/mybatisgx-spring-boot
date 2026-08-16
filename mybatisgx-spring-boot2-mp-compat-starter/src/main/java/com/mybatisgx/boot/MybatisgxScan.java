package com.mybatisgx.boot;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * MyBatisGX + MyBatis-Plus 共存扫描注解
 *
 * <p>与标准 {@code @MybatisgxScan} 同名，但在共存 starter 中独立提供，
 * 避免依赖 mybatisgx-spring-boot2-starter 的自动配置。
 * 与标准版不同，此处不含 {@code @MapperScan} 组合：共存模式下 MyBatisGX DAO 与 MP mapper
 * 分别位于不同包，需在启动类用显式 {@code @MapperScan} 声明扫描范围，避免注解合并冲突。</p>
 *
 * @author ccxuef
 * @description MyBatisGX + MyBatis-Plus 共存扫描注解
 * @date 2026/8/16
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({MybatisgxRegistrar.class})
public @interface MybatisgxScan {

    String[] entityBasePackages();

    String[] daoBasePackages();
}
