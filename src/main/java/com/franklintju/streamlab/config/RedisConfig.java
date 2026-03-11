package com.franklintju.streamlab.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisConfig {

    public static final String CACHE_VIDEO_COMMENTS = "videoComments";
    public static final String CACHE_COMMENT_REPLIES = "commentReplies";
    public static final String CACHE_USER_COMMENTS = "userComments";
    public static final String CACHE_COMMENT_COUNT = "commentCount";
    public static final String CACHE_VIDEO_HISTORY = "videoHistory";
    public static final String CACHE_VIDEO_PROGRESS = "videoProgress";

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();

        // 评论列表缓存 - 5分钟
        cacheConfigs.put(CACHE_VIDEO_COMMENTS, defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigs.put(CACHE_COMMENT_REPLIES, defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigs.put(CACHE_USER_COMMENTS, defaultConfig.entryTtl(Duration.ofMinutes(5)));

        // 评论数缓存 - 2分钟（更新较频繁）
        cacheConfigs.put(CACHE_COMMENT_COUNT, defaultConfig.entryTtl(Duration.ofMinutes(2)));

        // 观看历史缓存 - 10分钟
        cacheConfigs.put(CACHE_VIDEO_HISTORY, defaultConfig.entryTtl(Duration.ofMinutes(10)));

        // 播放进度缓存 - 7天（长期保存）
        cacheConfigs.put(CACHE_VIDEO_PROGRESS, defaultConfig.entryTtl(Duration.ofDays(7)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig.entryTtl(Duration.ofMinutes(10)))
                .withInitialCacheConfigurations(cacheConfigs)
                .transactionAware()
                .build();
    }
}
