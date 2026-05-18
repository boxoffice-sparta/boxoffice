package com.boxoffice.common.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Redis 캐시 기본 설정 클래스.
 * 기본 TTL은 1시간이며 각 서비스에서 캐시별 TTL을 추가로 설정할 수 있다.
 * 키는 String, 값은 JSON으로 직렬화한다.
 * 각 서비스에서 캐시별 TTL 설정 예시:
 *   @Cacheable(value = "hubs", key = "#hubId")
 *   application.yml에서 spring.cache.redis.time-to-live 설정
 */
@Configuration
@EnableCaching
public class RedisConfig {

    /**
     * Redis 캐시 매니저를 생성한다.
     * 기본 TTL 1시간, null 값 캐싱 비활성화.
     *
     * @param factory Redis 연결 팩토리
     * @return Redis 캐시 매니저
     */
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration
                .defaultCacheConfig()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .entryTtl(Duration.ofHours(1))
                .disableCachingNullValues();

        return RedisCacheManager.builder(factory)
                .cacheDefaults(defaultConfig)
                .build();
    }
}