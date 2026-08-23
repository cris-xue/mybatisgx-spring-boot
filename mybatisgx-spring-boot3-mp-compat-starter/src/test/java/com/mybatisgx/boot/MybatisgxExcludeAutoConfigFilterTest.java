package com.mybatisgx.boot;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * MybatisgxExcludeAutoConfigFilter 单元测试（Spring Boot 3 / JUnit 5）。
 *
 * <p>验证 match() 仅排除 MP 的 {@link MybatisPlusAutoConfiguration}，其余自动配置类放行。
 * AutoConfigurationMetadata 在 match() 中未使用，传 null 即可。</p>
 *
 * @author ccxuef
 * @description 自动配置排除过滤器单元测试（sb3）
 * @date 2026/8/22
 */
public class MybatisgxExcludeAutoConfigFilterTest {

    private static final String MP_AUTOCONFIG = MybatisPlusAutoConfiguration.class.getName();

    private final MybatisgxExcludeAutoConfigFilter filter = new MybatisgxExcludeAutoConfigFilter();

    /**
     * 含 MP 自动配置的列表：仅 MP 被排除。
     */
    @Test
    public void test01_excludeMybatisPlusAutoConfigOnly() {
        String[] classes = {"com.example.FooAutoConfiguration", MP_AUTOCONFIG, "com.example.BarAutoConfiguration"};
        boolean[] matches = filter.match(classes, null);

        assertTrue(matches[0]);
        assertFalse(matches[1], "MP 自动配置应被排除");
        assertTrue(matches[2]);
    }

    /**
     * 空数组：返回空结果。
     */
    @Test
    public void test02_emptyClasses() {
        assertArrayEquals(new boolean[0], filter.match(new String[0], null));
    }

    /**
     * 不含 MP 自动配置：全部放行。
     */
    @Test
    public void test03_noMpAutoConfigAllMatch() {
        String[] classes = {"com.example.FooAutoConfiguration", "com.example.BarAutoConfiguration"};
        boolean[] matches = filter.match(classes, null);

        assertTrue(matches[0]);
        assertTrue(matches[1]);
    }

    /**
     * 结果长度与输入数组一致。
     */
    @Test
    public void test04_resultLengthMatchesInput() {
        String[] classes = {"a", MP_AUTOCONFIG, "b", MP_AUTOCONFIG};
        boolean[] matches = filter.match(classes, null);
        assertTrue(matches.length == classes.length);
    }
}
