package com.mybatisgx.boot.test;

import com.mybatisgx.boot.MybatisgxScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Repository;

/**
 * 共存集成测试启动类（Spring Boot 2）。
 *
 * <p>{@code @MybatisgxScan} 提供实体扫描信息并注册 MyBatisGX 上下文加载 post-processor；
 * MyBatisGX DAO 接口（dao 包）与 MP mapper（mapper 包）分别声明扫描。
 * 共存模式下不使用 MyBatisGX 标准 starter 的自动配置，SqlSessionFactory 由 MP 创建。</p>
 *
 * @author ccxuef
 * @description 共存集成测试启动类（sb2）
 * @date 2026/8/22
 */
@MybatisgxScan(
        entityBasePackages = "com.mybatisgx.boot.test.model.entity",
        daoBasePackages = {"com.mybatisgx.boot.test.dao", "com.mybatisgx.boot.test.mapper"},
        annotationClass = Repository.class
)
@SpringBootApplication(scanBasePackages = {"com.mybatisgx.boot.test"})
public class TestMpCompatApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestMpCompatApplication.class, args);
    }
}
