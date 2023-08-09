package com.example.springbootrediscache.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class AppConfig {

    private final RedisConnectionFactory redisConnectionFactory;

    /*
    کانفیگ ردیس برای ProductCachingService
     */
    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(60))
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.
                        SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
    }

    /*
    کانفیگ با ttl های مختلف برای key های متفاوت
     */
    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
        return (builder) -> builder
                .withCacheConfiguration("customer",
                        RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(10)))
                .withCacheConfiguration("product",
                        RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(5)));
    }

    /*
    کانفیگ ردیس برای ProductService
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(){
        RedisTemplate<String, Object> productRedisTemplate = new RedisTemplate<>();
        productRedisTemplate.setConnectionFactory(redisConnectionFactory);
        productRedisTemplate.setKeySerializer(new StringRedisSerializer());
        productRedisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer(Object.class));
        productRedisTemplate.afterPropertiesSet();
        return productRedisTemplate;
    }
}
