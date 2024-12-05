package com.pincio.telegramwebhook.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

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

            // Configura il serializzatore per chiavi e valori
            template.setKeySerializer(new StringRedisSerializer());
            template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        }catch (Exception e){
            log.error("Errore durante la configurazione di RedisTemplate");
        }
        return template;
    }

    @Bean
    public RestTemplate restTemplate() {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(5000); // Timeout di connessione
        factory.setReadTimeout(5000);    // Timeout di lettura
        return new RestTemplate(factory);
    }

}
