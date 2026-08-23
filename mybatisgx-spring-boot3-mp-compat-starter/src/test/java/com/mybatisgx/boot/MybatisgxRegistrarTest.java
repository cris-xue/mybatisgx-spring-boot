package com.mybatisgx.boot;

import com.mybatisgx.spring.SqlSessionFactoryBeanPostProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.type.AnnotationMetadata;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * MybatisgxRegistrar 单元测试（Spring Boot 3 / JUnit 5）。
 *
 * <p>验证 {@code @MybatisgxScan} 触发 Registrar 后，注册了名为
 * {@code sqlSessionFactoryBeanPostProcessorBeanDefinition} 的 bean 定义，且构造参数
 * 为注解上的 entityBasePackages / daoBasePackages。
 * 注意 sb3 版 bean 名与 sb2 版（sqlSessionFactoryBeanPostProcessor）不同。</p>
 *
 * @author ccxuef
 * @description 扫描注册器单元测试（sb3）
 * @date 2026/8/22
 */
public class MybatisgxRegistrarTest {

    private static final String ENTITY_PACKAGE = "com.example.test.model.entity";

    @MybatisgxScan(
            entityBasePackages = ENTITY_PACKAGE,
            daoBasePackages = {"com.example.test.dao", "com.example.test.mapper"})
    static class ScanConfig {
    }

    /**
     * 注册 bean 定义且构造参数与注解一致。
     */
    @Test
    public void test01_registersPostProcessorBeanDefinition() {
        DefaultListableBeanFactory registry = new DefaultListableBeanFactory();
        MybatisgxRegistrar registrar = new MybatisgxRegistrar();
        registrar.registerBeanDefinitions(AnnotationMetadata.introspect(ScanConfig.class), registry);

        assertTrue(registry.containsBeanDefinition("sqlSessionFactoryBeanPostProcessorBeanDefinition"));
        BeanDefinition beanDefinition = registry.getBeanDefinition("sqlSessionFactoryBeanPostProcessorBeanDefinition");
        assertNotNull(beanDefinition);
        assertTrue(SqlSessionFactoryBeanPostProcessor.class.getName().equals(beanDefinition.getBeanClassName()));

        ConstructorArgumentValues constructorArgumentValues = beanDefinition.getConstructorArgumentValues();
        assertNotNull(constructorArgumentValues);

        ConstructorArgumentValues.ValueHolder entityHolder = constructorArgumentValues.getIndexedArgumentValue(0, String[].class);
        ConstructorArgumentValues.ValueHolder daoHolder = constructorArgumentValues.getIndexedArgumentValue(1, String[].class);
        assertNotNull(entityHolder, "第 0 个参数应为 entityBasePackages");
        assertNotNull(daoHolder, "第 1 个参数应为 daoBasePackages");
        assertArrayEquals(new String[]{ENTITY_PACKAGE}, (String[]) entityHolder.getValue());
        assertArrayEquals(new String[]{"com.example.test.dao", "com.example.test.mapper"}, (String[]) daoHolder.getValue());
    }
}
