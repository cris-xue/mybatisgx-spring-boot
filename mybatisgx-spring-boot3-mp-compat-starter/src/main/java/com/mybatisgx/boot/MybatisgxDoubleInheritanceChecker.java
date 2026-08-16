package com.mybatisgx.boot;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mybatisgx.dao.Dao;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.util.Collection;

/**
 * 双继承检测器：禁止一个 DAO 接口同时继承 MP {@code BaseMapper} 与 MyBatisGX {@code Dao}。
 *
 * <p>两者存在同名方法（insert / updateById / deleteById），会造成 MappedStatement 冲突，
 * 且 MP 的 addMappedStatement 对已存在语句静默忽略，冲突不可见。此检测在
 * SqlSessionFactory 创建后遍历已注册 mapper，发现双继承立即抛出明确异常。</p>
 *
 * @author ccxuef
 * @description MyBatisGX + MyBatis-Plus 共存双继承检测器
 * @date 2026/8/16
 */
public class MybatisgxDoubleInheritanceChecker implements BeanPostProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(MybatisgxDoubleInheritanceChecker.class);

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (bean instanceof SqlSessionFactory) {
            this.check(((SqlSessionFactory) bean).getConfiguration());
        }
        return bean;
    }

    private void check(Configuration configuration) {
        Collection<Class<?>> mapperClasses = configuration.getMapperRegistry().getMappers();
        for (Class<?> mapperClass : mapperClasses) {
            if (isDoubleInheritance(mapperClass)) {
                throw new IllegalStateException(String.format(
                        "MyBatisGX/MyBatis-Plus 共存：接口 [%s] 同时继承了 BaseMapper 与 Dao，"
                                + "两者存在同名方法（insert/updateById/deleteById）会造成 MappedStatement 冲突。"
                                + "请将接口拆分：一个接口只继承一方。",
                        mapperClass.getName()));
            }
        }
    }

    /**
     * 判断接口是否同时继承 BaseMapper 与 Dao（便于测试）。
     */
    public static boolean isDoubleInheritance(Class<?> mapperClass) {
        return BaseMapper.class.isAssignableFrom(mapperClass) && Dao.class.isAssignableFrom(mapperClass);
    }
}
