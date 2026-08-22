package com.mybatisgx.boot;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusPropertiesCustomizer;
import com.github.pagehelper.PageInterceptor;
import com.mybatisgx.ext.scripting.xmltags.MgxsqlLanguageDriver;
import org.apache.ibatis.type.TypeAliasRegistry;
import org.junit.Test;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * MybatisgxConfiguration 单元测试（Spring Boot 2 / JUnit 4）。
 *
 * <p>验证 sb2 装配让位的两个关键行为：
 * <ol>
 *   <li>{@code MybatisPlusPropertiesCustomizer} 在 MP 创建 factory 前，把
 *       {@code mgxsql} 语言驱动注册进配置的类型别名表</li>
 *   <li>分页拦截器与双继承检测器等辅助 bean 正常创建</li>
 * </ol>
 * 注意：定制器收到的是 {@link MybatisgxProperties}（getConfiguration 覆写返回非 null 的
 * MybatisgxPlusConfiguration），因此测试传 MybatisgxProperties 而非 MybatisPlusProperties。</p>
 *
 * @author ccxuef
 * @description MybatisgxConfiguration 单元测试（sb2）
 * @date 2026/8/22
 */
public class MybatisgxConfigurationTest {

    /**
     * 定制器把 mgxsql 类型别名注册进 Configuration 的类型别名表。
     */
    @Test
    public void test01_registersMgxsqlAlias() {
        MybatisgxConfiguration configuration = new MybatisgxConfiguration();
        MybatisPlusPropertiesCustomizer customizer = configuration.mybatisgxPlusConfigurationCustomizer();
        assertNotNull("customizer 不应为 null", customizer);

        MybatisgxProperties properties = new MybatisgxProperties();
        customizer.customize(properties);

        TypeAliasRegistry typeAliasRegistry = properties.getConfiguration().getTypeAliasRegistry();
        Map<String, Class<?>> aliases = typeAliasRegistry.getTypeAliases();
        assertTrue("应注册 mgxsql 别名", aliases.containsKey("mgxsql"));
        assertEquals("mgxsql 别名应指向 MgxsqlLanguageDriver", MgxsqlLanguageDriver.class, aliases.get("mgxsql"));
    }

    /**
     * PageInterceptor 分页拦截器 bean 正常创建（MyBatisGX findPage 依赖）。
     */
    @Test
    public void test02_pageInterceptorBean() {
        assertNotNull(new MybatisgxConfiguration().pageInterceptor());
    }

    /**
     * Jackson 定制器 bean 正常创建（懒加载序列化忽略 handler 字段）。
     */
    @Test
    public void test03_jacksonCustomizerBean() {
        Jackson2ObjectMapperBuilderCustomizer customizer = new MybatisgxConfiguration().customizer();
        assertNotNull(customizer);
    }

    /**
     * 双继承检测器静态 bean 方法正常返回实例。
     */
    @Test
    public void test04_doubleInheritanceCheckerBean() {
        assertNotNull(MybatisgxConfiguration.mybatisgxMpDoubleInheritanceChecker());
    }
}
