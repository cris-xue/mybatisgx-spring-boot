package com.mybatisgx.boot;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.mybatisgx.mybatisplus.MybatisgxPlusConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * MybatisxProperties
 */
@ConfigurationProperties(prefix = MybatisgxProperties.MYBATIS_PREFIX)
public class MybatisgxProperties extends MybatisPlusProperties {

    public static final String MYBATIS_PREFIX = "mybatisgx";

    @NestedConfigurationProperty
    private MybatisgxPlusConfiguration configuration = new MybatisgxPlusConfiguration();

    @Override
    public MybatisConfiguration getConfiguration() {
        return configuration;
    }

    public MybatisPlusProperties setConfiguration(MybatisConfiguration configuration) {
        this.configuration = (MybatisgxPlusConfiguration) configuration;
        super.setConfiguration(configuration);
        return null;
    }
}
