package com.mybatisgx.boot.test.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mybatisgx.annotation.Entity;
import com.mybatisgx.annotation.Id;
import com.mybatisgx.annotation.Table;

import java.math.BigDecimal;

/**
 * 共存集成测试实体：同时满足 MyBatisGX 注解映射与 MP BaseMapper 默认映射。
 *
 * <p>表名统一为 {@code t_user}（避免 H2 保留字 user）；MyBatisGX 用 {@code @Table}，
 * MP 用 {@code @TableName}，两者指向同一张表。普通 POJO，不依赖 Lombok。</p>
 *
 * @author ccxuef
 * @description 共存集成测试实体
 * @date 2026/8/22
 */
@Entity
@Table(name = "t_user")
@TableName("t_user")
public class TestUser {

    @Id
    private Long id;

    private String name;

    private String code;

    private Integer status;

    private Integer age;

    private BigDecimal salary;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }
}
