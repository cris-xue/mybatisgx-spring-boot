package com.mybatisgx.boot;

import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.baomidou.mybatisplus.autoconfigure.SqlSessionFactoryBeanCustomizer;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.github.pagehelper.PageInterceptor;
import com.mybatisgx.ext.scripting.xmltags.MgxsqlLanguageDriver;
import com.mybatisgx.mybatisplus.MybatisgxPlusConfiguration;
import org.apache.ibatis.type.TypeAliasRegistry;
import org.junit.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.core.ResolvableType;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * MybatisgxConfiguration 单元测试（Spring Boot 2 / JUnit 4）。
 *
 * <p>验证 sb2 装配让位的两个关键行为：
 * <ol>
 *   <li>{@code SqlSessionFactoryBeanCustomizer} 把 MP 构建的 Configuration 替换为
 *       {@link MybatisgxPlusConfiguration}，并注册 {@code mgxsql} 类型别名</li>
 *   <li>分页拦截器（PageHelper + MP）、Jackson 定制器与双继承检测器等 bean 正常创建</li>
 * </ol></p>
 *
 * @author ccxuef
 * @description MybatisgxConfiguration 单元测试（sb2）
 * @date 2026/8/22
 */
public class MybatisgxConfigurationTest {

    /**
     * 定制器把 factory bean 的 Configuration 替换为 MybatisgxPlusConfiguration 并注册 mgxsql 别名。
     */
    @Test
    public void test01_customizerSwapsConfigurationAndRegistersAlias() {
        MybatisgxConfiguration configuration = new MybatisgxConfiguration(emptyConfigurationCustomizersProvider());
        SqlSessionFactoryBeanCustomizer customizer = configuration.mybatisgxPlusSqlSessionFactoryBeanCustomizer(new MybatisgxProperties());
        assertNotNull("customizer 不应为 null", customizer);

        MybatisSqlSessionFactoryBean factoryBean = new MybatisSqlSessionFactoryBean();
        customizer.customize(factoryBean);

        assertTrue("factory 的 Configuration 应为 MybatisgxPlusConfiguration",
                factoryBean.getConfiguration() instanceof MybatisgxPlusConfiguration);

        TypeAliasRegistry typeAliasRegistry = factoryBean.getConfiguration().getTypeAliasRegistry();
        Map<String, Class<?>> aliases = typeAliasRegistry.getTypeAliases();
        assertTrue("应注册 mgxsql 别名", aliases.containsKey("mgxsql"));
        assertEquals("mgxsql 别名应指向 MgxsqlLanguageDriver", MgxsqlLanguageDriver.class, aliases.get("mgxsql"));
    }

    /**
     * PageHelper 分页拦截器 bean 正常创建（MyBatisGX findPage 依赖）。
     */
    @Test
    public void test02_pageInterceptorBean() {
        assertNotNull(new MybatisgxConfiguration(emptyConfigurationCustomizersProvider()).pageInterceptor());
    }

    /**
     * MP 分页拦截器 bean 正常创建（MP selectPage 依赖）。
     */
    @Test
    public void test03_mybatisPlusInterceptorBean() {
        MybatisPlusInterceptor interceptor = new MybatisgxConfiguration(emptyConfigurationCustomizersProvider()).mybatisPlusInterceptor();
        assertNotNull(interceptor);
        assertTrue("应包含分页内拦截器", interceptor.getInterceptors().size() > 0);
    }

    /**
     * Jackson 定制器 bean 正常创建（懒加载序列化忽略 handler 字段）。
     */
    @Test
    public void test04_jacksonCustomizerBean() {
        Jackson2ObjectMapperBuilderCustomizer customizer =
                new MybatisgxConfiguration(emptyConfigurationCustomizersProvider()).customizer();
        assertNotNull(customizer);
    }

    /**
     * 双继承检测器静态 bean 方法正常返回实例。
     */
    @Test
    public void test05_doubleInheritanceCheckerBean() {
        assertNotNull(MybatisgxConfiguration.mybatisgxMpDoubleInheritanceChecker());
    }

    /**
     * 空 ConfigurationCustomizer 的 ObjectProvider（DefaultListableBeanFactory 提供）。
     */
    private ObjectProvider<List<ConfigurationCustomizer>> emptyConfigurationCustomizersProvider() {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        return beanFactory.getBeanProvider(ResolvableType.forClassWithGenerics(List.class, ConfigurationCustomizer.class));
    }
}
