package com.mybatisgx.boot;

import org.mybatis.spring.boot.autoconfigure.MybatisProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * mybatisgx自动配置类
 *
 * @author：薛承城
 */
@ConfigurationProperties(prefix = MybatisgxProperties.MYBATIS_PREFIX)
public class MybatisgxProperties extends MybatisProperties {

    public static final String MYBATIS_PREFIX = "mybatisgx";
}
