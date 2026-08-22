package com.mybatisgx.boot;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.mybatisgx.mybatisplus.MybatisgxPlusConfiguration;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * MybatisgxProperties 单元测试（Spring Boot 2 / JUnit 4）。
 *
 * <p>共存模式下 MP 创建 SqlSessionFactory 前，配置层默认 Configuration
 * 必须是 {@link MybatisgxPlusConfiguration}（继承 MP Configuration 并实现
 * MyBatisGX 扩展能力），保证两者在同一 Configuration 上共存。</p>
 *
 * @author ccxuef
 * @description MybatisgxProperties 单元测试（sb2）
 * @date 2026/8/22
 */
public class MybatisgxPropertiesTest {

    /**
     * getConfiguration() 默认返回 MybatisgxPlusConfiguration 实例。
     */
    @Test
    public void test01_defaultConfigurationIsMybatisgxPlusConfiguration() {
        MybatisgxProperties properties = new MybatisgxProperties();
        MybatisConfiguration configuration = properties.getConfiguration();
        assertNotNull("configuration 不应为 null", configuration);
        assertTrue("默认 Configuration 应为 MybatisgxPlusConfiguration",
                configuration instanceof MybatisgxPlusConfiguration);
    }

    /**
     * 配置前缀为 mybatisgx，供 @ConfigurationProperties 绑定使用。
     */
    @Test
    public void test02_prefixIsMybatisgx() {
        assertTrue("mybatisgx".equals(MybatisgxProperties.MYBATIS_PREFIX));
    }
}
