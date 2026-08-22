package com.mybatisgx.boot;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mybatisgx.dao.Dao;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.TransactionIsolationLevel;
import org.junit.Test;

import java.sql.Connection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

/**
 * MybatisgxDoubleInheritanceChecker 单元测试（Spring Boot 2 / JUnit 4）。
 *
 * <p>正向用例利用 {@code Dao} 空标记接口构造可编译的双继承接口
 * （CurdDao + BaseMapper 的真实双继承会被 Java 编译期 name clash 拦截）。
 * BeanPostProcessor 路径用真实 MyBatis Configuration + mock SqlSessionFactory 验证。</p>
 *
 * @author ccxuef
 * @description 双继承检测器单元测试（sb2）
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
    public void test03_postProcessorAllowsNormalMappers() throws Exception {
        Configuration configuration = new Configuration();
        configuration.addMapper(MpOnlyMapper.class);
        configuration.addMapper(GxOnlyDao.class);

        MybatisgxDoubleInheritanceChecker checker = new MybatisgxDoubleInheritanceChecker();
        SqlSessionFactory sqlSessionFactory = new MockSqlSessionFactory(configuration);
        Object bean = checker.postProcessAfterInitialization(sqlSessionFactory, "sqlSessionFactory");
        assertEquals("非 SqlSessionFactory bean 原样返回", sqlSessionFactory, bean);
    }

    /**
     * BeanPostProcessor 路径：双继承 mapper 触发明确异常。
     */
    @Test
    public void test04_postProcessorRejectsDoubleInheritance() throws Exception {
        Configuration configuration = new Configuration();
        configuration.addMapper(BothMapper.class);

        MybatisgxDoubleInheritanceChecker checker = new MybatisgxDoubleInheritanceChecker();
        SqlSessionFactory sqlSessionFactory = new MockSqlSessionFactory(configuration);
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
     * 最小 SqlSessionFactory 测试替身（实现 getConfiguration() 即可，供检测器遍历 mapper）。
     */
    private static class MockSqlSessionFactory implements SqlSessionFactory {

        private final Configuration configuration;

        MockSqlSessionFactory(Configuration configuration) {
            this.configuration = configuration;
        }

        @Override
        public Configuration getConfiguration() {
            return configuration;
        }

        // 其余方法不支持，抛 UnsupportedOperationException

        private UnsupportedOperationException unsupported() {
            return new UnsupportedOperationException("not used in this test");
        }

        @Override
        public SqlSession openSession() {
            throw unsupported();
        }

        @Override
        public SqlSession openSession(boolean autoCommit) {
            throw unsupported();
        }

        @Override
        public SqlSession openSession(ExecutorType execType) {
            throw unsupported();
        }

        @Override
        public SqlSession openSession(TransactionIsolationLevel level) {
            throw unsupported();
        }

        @Override
        public SqlSession openSession(ExecutorType execType, TransactionIsolationLevel level) {
            throw unsupported();
        }

        @Override
        public SqlSession openSession(ExecutorType execType, boolean autoCommit) {
            throw unsupported();
        }

        @Override
        public SqlSession openSession(Connection connection) {
            throw unsupported();
        }

        @Override
        public SqlSession openSession(ExecutorType execType, Connection connection) {
            throw unsupported();
        }
    }
}
