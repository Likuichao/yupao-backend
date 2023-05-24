package com.lkc.yupao.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson配置
 * @author lkc
 * @version 1.0
 */
@Configuration
@ConfigurationProperties(prefix = "spring.redis") //和yaml文件对应，自动为同名变量赋值
@Data //@ConfigurationProperties需要使用set方法为变量赋值
public class RedissonConfig {
    private String host;

    private String port;

    private String password;

    @Bean
    public RedissonClient redissonClient(){
        // 1. 创建配置
        Config config = new Config();
        String redisAddress = String.format("redis://%s:%s",host,port);
        config.useSingleServer().setAddress(redisAddress).setDatabase(3);//设置redis地址，用哪个数据库
        config.useSingleServer().setPassword(password);
        // 2. 创建实例
        RedissonClient redisson = Redisson.create(config);
        return redisson;
    }
}
