package com.pincio.telegramwebhook.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
@Slf4j
public class RedisConf {

    @Value("${redis.host}")
    private String REDIS_HOST;

    @Value("${redis.port}")
    private int REDIS_PORT;


    @Bean
    JedisConnectionFactory jedisConnectionFactory() {
        JedisConnectionFactory factory = null;
        try {
            factory = new JedisConnectionFactory();
            factory.getStandaloneConfiguration().setHostName(REDIS_HOST); // Usa variabili d'ambiente
            factory.getStandaloneConfiguration().setPort(REDIS_PORT);
        }catch (Exception e){
            log.error("Errore durante la configurazione di JedisConnectionFactory");
        }
        return factory;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {

        RedisTemplate<String, Object> template = null;
        try {
            template = new RedisTemplate<>();
            template.setConnectionFactory(jedisConnectionFactory());
        }catch (Exception e){
            log.error("Errore durante la configurazione di RedisTemplate");
        }
        return template;
    }

}
