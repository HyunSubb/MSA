package com.example.ordersystem.common.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

//    redis에 접근하기 위한 접근(connection)객체
    @Bean
    @Qualifier("rtdb") // 이 빈의 이름을 "rtdb"로 지정한다.
    public RedisConnectionFactory redisConnectionFactory(){
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
        configuration.setDatabase(0); // Redis 데이터베이스 0번 사용
        return new LettuceConnectionFactory(configuration);
    }
//    redis에 저장할 key, value의 타입지정한 template객체 생성
//    redisTemplate이라는 메서드가 config전체에 1개는 있어야함.
    @Bean
    @Qualifier("rtdb") // 이 빈의 이름을 "rtdb"로 지정한다.
    public RedisTemplate<String, Object> redisTemplate(@Qualifier("rtdb") RedisConnectionFactory redisConnectionFactory){
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer()); // 키를 String으로 직렬화
        redisTemplate.setValueSerializer(new StringRedisSerializer()); // 값을 String으로 직렬화
        redisTemplate.setConnectionFactory(redisConnectionFactory); // 연결 팩토리 설정
        return redisTemplate;
    }

}
