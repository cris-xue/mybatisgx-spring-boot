package com.mybatisgx.boot;

import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.baomidou.mybatisplus.autoconfigure.SqlSessionFactoryBeanCustomizer;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.github.pagehelper.PageInterceptor;
import com.mybatisgx.ext.scripting.xmltags.MgxsqlLanguageDriver;
import com.mybatisgx.mybatisplus.MybatisgxPlusConfiguration;
import org.apache.ibatis.type.TypeAliasRegistry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.util.CollectionUtils;

import java.util.List;

public class MybatisgxConfiguration {

    private final List<ConfigurationCustomizer> configurationCustomizers;

    public MybatisgxConfiguration(ObjectProvider<List<ConfigurationCustomizer>> configurationCustomizersProvider) {
        this.configurationCustomizers = configurationCustomizersProvider.getIfAvailable();
    }

    @Bean
    public SqlSessionFactoryBeanCustomizer mybatisgxPlusSqlSessionFactoryBeanCustomizer(MybatisgxProperties mybatisgxProperties) {
        return factoryBean -> {
            MybatisgxPlusConfiguration mybatisgxPlusConfiguration = new MybatisgxPlusConfiguration();
            // 未配置 mybatisgx.configuration.* 时 CoreConfiguration 为 null（MP 3.5.17 不预初始化），需空判断
            MybatisgxProperties.CoreConfiguration coreConfiguration = mybatisgxProperties.getConfiguration();
            if (coreConfiguration != null) {
                coreConfiguration.applyTo(mybatisgxPlusConfiguration);
            }
            if (!CollectionUtils.isEmpty(this.configurationCustomizers)) {
                for (ConfigurationCustomizer customizer : this.configurationCustomizers) {
                    customizer.customize(mybatisgxPlusConfiguration);
                }
            }
            TypeAliasRegistry typeAliasRegistry = mybatisgxPlusConfiguration.getTypeAliasRegistry();
            typeAliasRegistry.registerAlias("mgxsql", MgxsqlLanguageDriver.class);
            factoryBean.setConfiguration(mybatisgxPlusConfiguration);
        };
    }

    /**
     * PageHelper 分页拦截器：MyBatisGX 分页方法（带 Pageable 参数）依赖此拦截器
     * 消费 ThreadLocal 改写 SQL、回填 total。
     *
     * <p>与 MP 的 MybatisPlusInterceptor 判别机制互斥——PageHelper 靠 ThreadLocal，
     * MP 靠入参 IPage，两者同链不互相误触发。</p>
     */
    @Bean
    public PageInterceptor pageInterceptor() {
        return new PageInterceptor();
    }

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        return interceptor;
    }

    /**
     * Jackson 混入：序列化时忽略 MyBatis 懒加载代理对象的 handler 字段，
     * 否则 MyBatisGX 关联查询懒加载序列化会报错。
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer mybatisgxJacksonCustomizer() {
        return builder -> builder.mixIn(Object.class, IgnoreHandlerMixin.class);
    }

    /**
     * 双继承检测：同一接口同时继承 BaseMapper 与 Dao 时启动报错。
     */
    @Bean
    public static MybatisgxDoubleInheritanceChecker mybatisgxMpDoubleInheritanceChecker() {
        return new MybatisgxDoubleInheritanceChecker();
    }
}
