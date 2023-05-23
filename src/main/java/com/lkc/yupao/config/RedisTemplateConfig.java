package com.lkc.yupao.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * @author lkc
 * @version 1.0
 * 为了防止写入Redis的数据乱码、浪费空间等，可以自定义序列化器
 */
@Configuration
public class RedisTemplateConfig {

    //redisConnectionFactory爆红：有 2.7.* 的版本，都会有这个问题。而降到 2.6.* 版本，就不会有这个提示。
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(RedisSerializer.string());//key序列化字符串
        return redisTemplate;
    }
}
