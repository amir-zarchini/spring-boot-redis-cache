package com.example.springbootrediscache.config;

import com.example.springbootrediscache.model.Product;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class AppConfig {

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory();
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(){
        RedisTemplate<String, Object> productRedisTemplate = new RedisTemplate<>();
        productRedisTemplate.setConnectionFactory(redisConnectionFactory());
//        productRedisTemplate.setDefaultSerializer(new GenericJackson2JsonRedisSerializer());
//        productRedisTemplate.setKeySerializer(new StringRedisSerializer());
//        productRedisTemplate.setHashKeySerializer(new GenericJackson2JsonRedisSerializer());
//        productRedisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
//        productRedisTemplate.setKeySerializer(new StringRedisSerializer());
//        productRedisTemplate.setValueSerializer(new StringRedisSerializer());

//        productRedisTemplate.setHashValueSerializer(new StringRedisSerializer());
//        productRedisTemplate.setHashKeySerializer(new StringRedisSerializer());
        return productRedisTemplate;
    }

//    RedisTemplate<String, Object> template = new RedisTemplate<>();
//        template.setConnectionFactory(redisConnectionFactory());
//        template.setKeySerializer(new StringRedisSerializer());
//        template.setHashKeySerializer(new StringRedisSerializer());
//        template.setHashKeySerializer(new JdkSerializationRedisSerializer());
//        template.setValueSerializer(new JdkSerializationRedisSerializer());
//        template.setEnableTransactionSupport(true);
//        template.afterPropertiesSet();
//        return template;

//    @Bean
//    public RedisTemplate<String, Object> template() {
//        RedisTemplate<String, Object> template = new RedisTemplate<>();
//        template.setConnectionFactory(redisConnectionFactory());
//        template.setKeySerializer(new StringRedisSerializer());
//        template.setHashKeySerializer(new StringRedisSerializer());
//        template.setHashKeySerializer(new JdkSerializationRedisSerializer());
//        template.setValueSerializer(new JdkSerializationRedisSerializer());
//        template.setEnableTransactionSupport(true);
//        template.afterPropertiesSet();
//        return template;
//    }


//    @Bean
//    public RedisTemplate<String, Object> redisTemplate() {
//        final RedisTemplate<String, Object> template = new RedisTemplate<String, Object>();
//        template.setConnectionFactory(jedisConnectionFactory());
//        template.setKeySerializer(new StringRedisSerializer());
//        template.setValueSerializer(new StringRedisSerializer());
//
//        // the following is not required
//        template.setHashValueSerializer(new StringRedisSerializer());
//        template.setHashKeySerializer(new StringRedisSerializer());
//
//        return template;
//    }
}
