package com.mybatisgx.boot.test.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mybatisgx.boot.test.model.entity.TestUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 共存集成测试：老 MP mapper（BaseMapper）。
 *
 * @author ccxuef
 * @description 共存集成测试老 MP mapper
 * @date 2026/8/22
 */
@Mapper
public interface TestUserMapper extends BaseMapper<TestUser> {
}
