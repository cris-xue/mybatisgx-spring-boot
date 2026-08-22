package com.mybatisgx.boot;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * MybatisgxProperties 单元测试（Spring Boot 2 / JUnit 4）。
 *
 * <p>sb2 共存模式下 {@code MybatisgxProperties} 继承 MP 的 {@link MybatisPlusProperties}，
 * 前缀统一为 {@code mybatisgx}。Configuration 的合并（换成 {@code MybatisgxPlusConfiguration}）
 * 已由 {@link MybatisgxConfiguration} 的定制器完成（见 MybatisgxConfigurationTest），
 * properties 仅承载 MP 的 {@code CoreConfiguration} 配置项。</p>
 *
 * @author ccxuef
 * @description MybatisgxProperties 单元测试（sb2）
 * @date 2026/8/22
 */
public class MybatisgxPropertiesTest {

    /**
     * 默认不初始化 CoreConfiguration（未绑定 mybatisgx.configuration.* 时为 null），
     * 与 MybatisgxConfiguration 定制器中的空判断一致。
     */
    @Test
    public void test01_defaultConfigurationNotInitialized() {
        MybatisgxProperties properties = new MybatisgxProperties();
        assertNull("默认 CoreConfiguration 应为 null（未绑定配置）", properties.getConfiguration());
    }

    /**
     * 配置前缀为 mybatisgx，供 @ConfigurationProperties 绑定使用。
     */
    @Test
    public void test02_prefixIsMybatisgx() {
        assertTrue("mybatisgx".equals(MybatisgxProperties.MYBATIS_PREFIX));
    }

    /**
     * getConfiguration/setConfiguration 类型为 MP 的 CoreConfiguration
     * （定制器 applyTo 到 MybatisgxPlusConfiguration 的输入）。
     */
    @Test
    public void test03_configurationTypeIsCoreConfiguration() {
        MybatisgxProperties properties = new MybatisgxProperties();
        MybatisPlusProperties.CoreConfiguration coreConfiguration = new MybatisPlusProperties.CoreConfiguration();
        properties.setConfiguration(coreConfiguration);
        assertEquals(coreConfiguration, properties.getConfiguration());
    }
}
