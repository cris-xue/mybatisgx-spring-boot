package com.mybatisgx.boot.test.dao;

import com.mybatisgx.annotation.Statement;
import com.mybatisgx.boot.test.model.entity.TestUser;
import com.mybatisgx.boot.test.model.entity.TestUserQuery;
import com.mybatisgx.dao.SimpleDao;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 共存集成测试：新 MyBatisGX DAO，方法名派生 SQL / QueryEntity / @Statement。
 *
 * @author ccxuef
 * @description 共存集成测试新 MyBatisGX DAO
 * @date 2026/8/22
 */
@Repository
public interface TestUserDao extends SimpleDao<TestUser, TestUserQuery, Long> {

    /**
     * 方法名派生 SQL：按 name like 查询。
     */
    List<TestUser> findByNameLike(@Param("name") String name);

    /**
     * mgxql limit 子句：验证共存模式下带 limit 的 SQL 预生成正常。
     */
    @Statement("select * from TestUser u order by u.id asc limit 0, 5")
    List<TestUser> findFirstFive();
}
