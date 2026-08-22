package com.mybatisgx.boot;

import com.mybatisgx.spring.SqlSessionFactoryBeanPostProcessor;
import org.junit.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.type.AnnotationMetadata;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * MybatisgxRegistrar 单元测试（Spring Boot 2 / JUnit 4）。
 *
 * <p>验证 {@code @MybatisgxScan} 触发 Registrar 后，注册了名为
 * {@code sqlSessionFactoryBeanPostProcessor} 的 bean 定义，且构造参数
 * 为注解上的 entityBasePackages / daoBasePackages。</p>
 *
 * @author ccxuef
 * @description 扫描注册器单元测试（sb2）
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

        assertTrue("应注册 sqlSessionFactoryBeanPostProcessor bean 定义", registry.containsBeanDefinition("sqlSessionFactoryBeanPostProcessor"));
        BeanDefinition beanDefinition = registry.getBeanDefinition("sqlSessionFactoryBeanPostProcessor");
        assertNotNull(beanDefinition);
        assertTrue("bean 类应为 SqlSessionFactoryBeanPostProcessor",
                SqlSessionFactoryBeanPostProcessor.class.getName().equals(beanDefinition.getBeanClassName()));

        ConstructorArgumentValues constructorArgumentValues = beanDefinition.getConstructorArgumentValues();
        assertNotNull("构造参数应存在", constructorArgumentValues);

        ConstructorArgumentValues.ValueHolder entityHolder = constructorArgumentValues.getIndexedArgumentValue(0, String[].class);
        ConstructorArgumentValues.ValueHolder daoHolder = constructorArgumentValues.getIndexedArgumentValue(1, String[].class);
        assertNotNull("第 0 个参数应为 entityBasePackages", entityHolder);
        assertNotNull("第 1 个参数应为 daoBasePackages", daoHolder);
        assertArrayEquals(new String[]{ENTITY_PACKAGE}, (String[]) entityHolder.getValue());
        assertArrayEquals(new String[]{"com.example.test.dao", "com.example.test.mapper"}, (String[]) daoHolder.getValue());
    }
}
