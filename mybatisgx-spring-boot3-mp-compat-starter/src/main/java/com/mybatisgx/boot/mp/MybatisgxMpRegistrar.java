package com.mybatisgx.boot.mp;

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
public class MybatisgxMpRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        AnnotationAttributes annotationAttributes = AnnotationAttributes
                .fromMap(importingClassMetadata.getAnnotationAttributes(MybatisgxMpScan.class.getName()));

        if (annotationAttributes != null) {
            String[] entityBasePackages = (String[]) annotationAttributes.get("entityBasePackages");
            String[] daoBasePackages = (String[]) annotationAttributes.get("daoBasePackages");

            ConstructorArgumentValues constructorArgumentValues = new ConstructorArgumentValues();
            constructorArgumentValues.addIndexedArgumentValue(0, entityBasePackages);
            constructorArgumentValues.addIndexedArgumentValue(1, daoBasePackages);
            GenericBeanDefinition sqlSessionFactoryBeanPostProcessorBeanDefinition = new GenericBeanDefinition();
            sqlSessionFactoryBeanPostProcessorBeanDefinition.setBeanClass(SqlSessionFactoryBeanPostProcessor.class);
            sqlSessionFactoryBeanPostProcessorBeanDefinition.setConstructorArgumentValues(constructorArgumentValues);
            registry.registerBeanDefinition("sqlSessionFactoryBeanPostProcessorBeanDefinition", sqlSessionFactoryBeanPostProcessorBeanDefinition);
        }
    }
}
