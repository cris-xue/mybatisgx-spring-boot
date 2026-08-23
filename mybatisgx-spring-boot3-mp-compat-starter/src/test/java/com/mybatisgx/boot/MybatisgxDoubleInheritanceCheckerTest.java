package com.mybatisgx.boot;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mybatisgx.dao.Dao;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * MybatisgxDoubleInheritanceChecker 单元测试（Spring Boot 3 / JUnit 5）。
 *
 * <p>正向用例利用 {@code Dao} 空标记接口构造可编译的双继承接口
 * （CurdDao + BaseMapper 的真实双继承会被 Java 编译期 name clash 拦截）。
 * SqlSessionFactory 用 JDK 动态代理构造（对 MyBatis 版本免疫）。</p>
 *
 * @author ccxuef
 * @description 双继承检测器单元测试（sb3）
 * @date 2026/8/22
 */
public class MybatisgxDoubleInheritanceCheckerTest {

    /**
     * 同时继承 BaseMapper 与 Dao（空标记接口）：双继承正向用例。
     */
    interface BothMapper extends BaseMapper<TestUser>, Dao {
    }

    /**
     * 仅继承 BaseMapper（老 MP mapper）：不应被误报。
     */
    interface MpOnlyMapper extends BaseMapper<TestUser> {
    }

    /**
     * 仅继承 Dao（新 MyBatisGX DAO 根接口）：不应被误报。
     */
    interface GxOnlyDao extends Dao {
    }

    static class TestUser {
    }

    /**
     * 正向：同时继承 BaseMapper 与 Dao 的接口被识别为双继承。
     */
    @Test
    public void test01_doubleInheritanceDetected() {
        assertTrue(MybatisgxDoubleInheritanceChecker.isDoubleInheritance(BothMapper.class));
    }

    /**
     * 反向：仅继承一方的接口不应被误报。
     */
    @Test
    public void test02_singleInheritanceNotDetected() {
        assertFalse(MybatisgxDoubleInheritanceChecker.isDoubleInheritance(MpOnlyMapper.class));
        assertFalse(MybatisgxDoubleInheritanceChecker.isDoubleInheritance(GxOnlyDao.class));
        assertFalse(MybatisgxDoubleInheritanceChecker.isDoubleInheritance(Object.class));
    }

    /**
     * BeanPostProcessor 路径：正常 mapper 放行（不抛异常）。
     */
    @Test
    public void test03_postProcessorAllowsNormalMappers() {
        Configuration configuration = new Configuration();
        configuration.addMapper(MpOnlyMapper.class);
        configuration.addMapper(GxOnlyDao.class);

        MybatisgxDoubleInheritanceChecker checker = new MybatisgxDoubleInheritanceChecker();
        SqlSessionFactory sqlSessionFactory = mockFactory(configuration);
        Object bean = checker.postProcessAfterInitialization(sqlSessionFactory, "sqlSessionFactory");
        assertEquals(sqlSessionFactory, bean, "非 SqlSessionFactory bean 原样返回");
    }

    /**
     * BeanPostProcessor 路径：双继承 mapper 触发明确异常。
     */
    @Test
    public void test04_postProcessorRejectsDoubleInheritance() {
        Configuration configuration = new Configuration();
        configuration.addMapper(BothMapper.class);

        MybatisgxDoubleInheritanceChecker checker = new MybatisgxDoubleInheritanceChecker();
        SqlSessionFactory sqlSessionFactory = mockFactory(configuration);
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> checker.postProcessAfterInitialization(sqlSessionFactory, "sqlSessionFactory"));
        assertTrue(exception.getMessage().contains(BothMapper.class.getName()));
        assertTrue(exception.getMessage().contains("同时继承了 BaseMapper 与 Dao"));
    }

    /**
     * 非 SqlSessionFactory bean：原样返回，不做检测。
     */
    @Test
    public void test05_nonFactoryBeanUntouched() {
        MybatisgxDoubleInheritanceChecker checker = new MybatisgxDoubleInheritanceChecker();
        Object bean = new Object();
        assertEquals(bean, checker.postProcessAfterInitialization(bean, "someBean"));
    }

    /**
     * 用 JDK 动态代理构造仅实现 getConfiguration() 的 SqlSessionFactory 替身。
     */
    private SqlSessionFactory mockFactory(Configuration configuration) {
        return (SqlSessionFactory) Proxy.newProxyInstance(
                SqlSessionFactory.class.getClassLoader(),
                new Class<?>[]{SqlSessionFactory.class},
                (proxy, method, args) -> {
                    if ("getConfiguration".equals(method.getName())) {
                        return configuration;
                    }
                    if (method.getDeclaringClass() == Object.class) {
                        switch (method.getName()) {
                            case "toString":
                                return "MockSqlSessionFactory";
                            case "hashCode":
                                return System.identityHashCode(proxy);
                            case "equals":
                                return proxy == args[0];
                            default:
                                throw new UnsupportedOperationException(method.getName());
                        }
                    }
                    throw new UnsupportedOperationException("not used in this test: " + method.getName());
                });
    }
}
