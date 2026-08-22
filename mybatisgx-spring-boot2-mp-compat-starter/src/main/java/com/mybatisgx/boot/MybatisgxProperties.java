package com.mybatisgx.boot;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * MybatisxProperties
 */
@ConfigurationProperties(prefix = MybatisgxProperties.MYBATIS_PREFIX)
public class MybatisgxProperties extends MybatisPlusProperties {

    public static final String MYBATIS_PREFIX = "mybatisgx";
}
