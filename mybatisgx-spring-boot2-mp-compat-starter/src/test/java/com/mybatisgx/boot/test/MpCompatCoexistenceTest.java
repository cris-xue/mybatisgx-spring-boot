package com.mybatisgx.boot.test;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mybatisgx.boot.MybatisgxDoubleInheritanceChecker;
import com.mybatisgx.boot.test.dao.TestUserDao;
import com.mybatisgx.boot.test.mapper.TestUserMapper;
import com.mybatisgx.boot.test.model.entity.TestUser;
import com.mybatisgx.boot.test.model.entity.TestUserQuery;
import com.mybatisgx.executor.page.Page;
import com.mybatisgx.executor.page.Pageable;
import com.mybatisgx.mybatisplus.MybatisgxPlusConfiguration;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.*;

/**
 * MyBatisGX + MyBatis-Plus 共存集成测试（Spring Boot 2 / JUnit 4）。
 *
 * <p>验证同一 SqlSessionFactory 下：老 MP mapper（BaseMapper）与新 MyBatisGX DAO 共存，
 * 以及双分页链路互不干扰。测试环境用 H2（MODE=MySQL）替代 MySQL。</p>
 *
 * <p>共享上下文 + H2（DB_CLOSE_DELAY=-1），数据跨方法保留，按方法名升序执行
 * （test01 先行插入 id=1，后续依赖它；test07 用 status=2 / id=101+ 避开）。
 * 相比示例工程，本测试额外覆盖了 sb2 缺漏的分页共存场景。</p>
 *
 * @author ccxuef
 * @description 共存集成测试（sb2）
 * @date 2026/8/22
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestMpCompatApplication.class)
@ActiveProfiles("test")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MpCompatCoexistenceTest {

    @Autowired
    private TestUserMapper testUserMapper;

    @Autowired
    private TestUserDao testUserDao;

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    /**
     * 老 MP mapper：BaseMapper CRUD + Wrapper 查询。
     */
    @Test
    public void test01_mpBaseMapperCrud() {
        TestUser user = new TestUser();
        user.setId(1L);
        user.setName("mp-user");
        user.setCode("U001");
        user.setStatus(1);
        user.setAge(20);
        user.setSalary(new BigDecimal("1000.00"));
        testUserMapper.insert(user);

        TestUser fetched = testUserMapper.selectById(1L);
        assertNotNull("selectById 不应为 null", fetched);
        assertEquals("mp-user", fetched.getName());

        List<TestUser> byWrapper = testUserMapper.selectList(
                new QueryWrapper<TestUser>().eq("code", "U001"));
        assertNotNull("Wrapper 查询结果不应为 null", byWrapper);
        assertEquals(1, byWrapper.size());
    }

    /**
     * 新 MyBatisGX DAO：方法名派生 SQL + mgxql（含 limit 子句）。
     */
    @Test
    public void test02_mybatisgxMethodNameSqlAndMgxql() {
        List<TestUser> byNameLike = testUserDao.findByNameLike("mp-user");
        assertNotNull("方法名派生 SQL 结果不应为 null", byNameLike);

        List<TestUser> firstFive = testUserDao.findFirstFive();
        assertNotNull("mgxql limit 查询结果不应为 null", firstFive);
        assertFalse("limit 查询结果不应为空", firstFive.isEmpty());
    }

    /**
     * 单一 SqlSessionFactory：Configuration 为 MybatisgxPlusConfiguration，两套接口共用。
     */
    @Test
    public void test03_sharedSqlSessionFactory() {
        assertNotNull(testUserMapper);
        assertNotNull(testUserDao);
        assertNotNull("MP 接口应可调用", testUserMapper.selectById(1L));
        assertNotNull("MyBatisGX 接口应可调用", testUserDao.findById(1L));

        Configuration configuration = sqlSessionFactory.getConfiguration();
        assertTrue("Configuration 应为 MybatisgxPlusConfiguration",
                configuration instanceof MybatisgxPlusConfiguration);
    }

    /**
     * 对 MP 语句无副作用：执行 BaseMapper 查询不触发 MyBatisGX 增强逻辑。
     */
    @Test
    public void test04_mpStatementNoSideEffect() {
        List<TestUser> users = testUserMapper.selectList(null);
        assertNotNull("纯 MP 语句应正常返回", users);
    }

    /**
     * 双继承检测不误报正常接口。
     */
    @Test
    public void test05_doubleInheritanceNotFalsePositive() {
        assertFalse("仅继承 BaseMapper 不应被误报",
                MybatisgxDoubleInheritanceChecker.isDoubleInheritance(TestUserMapper.class));
        assertFalse("仅继承 Dao 不应被误报",
                MybatisgxDoubleInheritanceChecker.isDoubleInheritance(TestUserDao.class));
    }

    /**
     * mybatisgx.configuration.* 绑定生效（共存模式统一使用 mybatisgx 前缀）。
     */
    @Test
    public void test06_mybatisgxConfigurationBinding() {
        Configuration configuration = sqlSessionFactory.getConfiguration();
        assertEquals("mybatisgx.configuration.databaseId 应生效", "MySQL", configuration.getDatabaseId());
        assertTrue("mybatisgx.configuration.map-underscore-to-camel-case 应生效",
                configuration.isMapUnderscoreToCamelCase());
        assertFalse("mybatisgx.configuration.cache-enabled 应生效", configuration.isCacheEnabled());
    }

    /**
     * 共存分页：MyBatisGX findPage（PageHelper ThreadLocal）与 MP selectPage（入参 IPage）
     * 判别机制互斥，同链不互相误触发。
     */
    @Test
    public void test07_coexistencePaging() {
        // 准备数据：插入 5 条（id=101+、status=2），避开 test01 的 id=1/status=1
        for (int i = 1; i <= 5; i++) {
            TestUser user = new TestUser();
            user.setId(100L + i);
            user.setName("page-user-" + i);
            user.setCode("P00" + i);
            user.setStatus(2);
            user.setAge(20 + i);
            user.setSalary(new BigDecimal("1000.00"));
            testUserMapper.insert(user);
        }

        // 1) MyBatisGX 分页：findPage（依赖 PageInterceptor 消费 ThreadLocal）
        TestUserQuery query = new TestUserQuery();
        query.setStatus(2);
        Page<TestUser> mgxPage = testUserDao.findPage(query, Pageable.of(1, 2));
        assertNotNull("findPage 返回不应为 null", mgxPage);
        assertNotNull("分页结果 list 不应为 null", mgxPage.getList());
        assertEquals("第 1 页 pageSize=2 应返回 2 条", 2, mgxPage.getList().size());
        assertEquals("total 应为满足条件的全部记录数 5，而非当前页条数 2", 5L, mgxPage.getTotal());

        // 2) MP 分页：selectPage（依赖 MybatisPlusInterceptor 读入参 IPage）
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<TestUser> mpPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 3);
        testUserMapper.selectPage(mpPage, new QueryWrapper<TestUser>().eq("status", 2));
        List<TestUser> mpList = mpPage.getRecords();
        assertNotNull("MP selectPage 结果不应为 null", mpList);
        assertEquals("MP 第 1 页 pageSize=3 应返回 3 条", 3, mpList.size());
        assertEquals("MP total 应为 5", 5L, mpPage.getTotal());

        // 3) 互斥验证：MyBatisGX 第 2 页取到的是第 3、4 条，与第 1 页不重叠
        Page<TestUser> mgxPage2 = testUserDao.findPage(query, Pageable.of(2, 2));
        assertEquals("第 2 页应返回 2 条", 2, mgxPage2.getList().size());
        assertNotEquals("第 1、2 页首条记录 id 不应相同", mgxPage.getList().get(0).getId(), mgxPage2.getList().get(0).getId());
    }
}
