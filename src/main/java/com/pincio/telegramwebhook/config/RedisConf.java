package com.pincio.telegramwebhook.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

public class RedisConf {

    @Value("${redis.host}")
    private String REDIS_HOST;

    @Value("${redis.port}")
    private int REDIS_PORT;


    @Bean
    JedisConnectionFactory jedisConnectionFactory() {
        JedisConnectionFactory jedisConFactory
                = new JedisConnectionFactory();
        jedisConFactory.setHostName(REDIS_HOST);
        jedisConFactory.setPort(REDIS_PORT);
        return jedisConFactory;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory());
        return template;
    }

}
