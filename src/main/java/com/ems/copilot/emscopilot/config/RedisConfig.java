package com.ems.copilot.emscopilot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 설정
 *
 * 환자 바이탈 정보를 Redis에 임시 저장하기 위한 설정
 * - 개인정보 보호를 위해 DB가 아닌 Redis에만 저장
 * - TTL 30분 후 자동 삭제
 * - 서버 재시작해도 데이터 유지 (Redis는 별도 프로세스)
 */
@Configuration
@EnableRedisRepositories(basePackages = "com.ems.copilot.emscopilot.repository")
public class RedisConfig {

    /**
     * RedisTemplate 설정
     * Key-Value 직렬화 방식 설정
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Key는 String으로 직렬화
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // Value는 JSON으로 직렬화
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        return template;
    }
}
