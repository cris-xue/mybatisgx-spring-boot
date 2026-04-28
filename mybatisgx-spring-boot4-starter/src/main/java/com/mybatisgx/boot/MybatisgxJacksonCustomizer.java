package com.mybatisgx.boot;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

/**
 * @author：薛承城
 * @description：一句话描述
 * @date：2026/4/26 18:24
 */
@Component
public class MybatisgxJacksonCustomizer {

    private final ObjectMapper objectMapper;

    public MybatisgxJacksonCustomizer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void customize() {
        objectMapper.addMixIn(Object.class, IgnoreHandlerMixin.class);
    }
}
