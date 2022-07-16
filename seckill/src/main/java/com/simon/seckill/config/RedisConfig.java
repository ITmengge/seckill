package com.simon.seckill.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis配置类，实现序列化，因为存进redis的数据是二进制的
 */
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        // 设置key和value的序列化
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        // 设置hash的key和value序列化
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        // 先注入连接工厂
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }

//    @Bean
//    public DefaultRedisScript<Boolean> script(){
//        DefaultRedisScript<Boolean> redisScript = new DefaultRedisScript<>();
//        // lua脚本放在和application.yml同级目录下
//        redisScript.setLocation(new ClassPathResource("lock.lua"));
//        redisScript.setResultType(Boolean.class);
//        return redisScript;
//    }

    /**
     * lua脚本（未解决超卖问题）
     * @return
     */
//    @Bean
//    public DefaultRedisScript<Long> script(){
//        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
//        // lua脚本放在和application.yml同级目录下
//        redisScript.setLocation(new ClassPathResource("stock.lua"));
//        redisScript.setResultType(Long.class);
//        return redisScript;
//    }
}
