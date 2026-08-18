package com.mybatisgx.boot;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Jackson 混入：序列化时忽略 MyBatis 懒加载代理对象的 handler 字段。
 *
 * <p>无此定制时，MyBatisGX 关联查询的懒加载代理对象序列化会因 handler 字段报错。</p>
 *
 * @author ccxuef
 * @description Jackson 混入：忽略懒加载 handler 字段
 * @date 2026/4/25 20:45
 */
@JsonIgnoreProperties({"handler"})
public class IgnoreHandlerMixin {
}
