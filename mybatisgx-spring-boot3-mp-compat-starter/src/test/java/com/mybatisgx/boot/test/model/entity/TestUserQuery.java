package com.mybatisgx.boot.test.model.entity;

import com.mybatisgx.annotation.QueryEntity;

/**
 * 共存集成测试查询实体（MyBatisGX 侧使用）。
 *
 * @author ccxuef
 * @description 共存集成测试查询实体
 * @date 2026/8/22
 */
@QueryEntity(TestUser.class)
public class TestUserQuery {

    private String name;

    private Integer status;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
