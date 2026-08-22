package com.mybatisgx.boot;

import com.mybatisgx.spring.SqlSessionFactoryBeanPostProcessor;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

/**
 * MyBatisGX + MyBatis-Plus 共存扫描注册器
 *
 * <p>注册 {@link SqlSessionFactoryBeanPostProcessor}，在 MP 创建 SqlSessionFactory 后加载 MyBatisGX DAO。</p>
 *
 * @author ccxuef
 * @description MyBatisGX + MyBatis-Plus 共存扫描注册器
 * @date 2026/8/15
 */
public class MybatisgxRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        this.registrySqlSessionFactoryBeanPostProcessor(importingClassMetadata, registry);
    }

    private void registrySqlSessionFactoryBeanPostProcessor(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        AnnotationAttributes annotationAttributes = AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(MybatisgxScan.class.getName()));
        if (annotationAttributes != null) {
            String[] entityBasePackages = (String[]) annotationAttributes.get("entityBasePackages");
            String[] daoBasePackages = (String[]) annotationAttributes.get("daoBasePackages");

            ConstructorArgumentValues constructorArgumentValues = new ConstructorArgumentValues();
            constructorArgumentValues.addIndexedArgumentValue(0, entityBasePackages);
            constructorArgumentValues.addIndexedArgumentValue(1, daoBasePackages);
            GenericBeanDefinition genericBeanDefinition = new GenericBeanDefinition();
            genericBeanDefinition.setBeanClass(SqlSessionFactoryBeanPostProcessor.class);
            genericBeanDefinition.setConstructorArgumentValues(constructorArgumentValues);
            registry.registerBeanDefinition("sqlSessionFactoryBeanPostProcessor", genericBeanDefinition);
        }
    }
}
