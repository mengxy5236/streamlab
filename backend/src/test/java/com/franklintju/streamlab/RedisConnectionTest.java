package com.franklintju.streamlab;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "spring.data.redis.host=39.97.231.249",
        "spring.data.redis.port=6379",
        "spring.data.redis.password=040626xw"
})
class RedisConnectionTest {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    void testRedisConnection() {
        // 测试写入
        String testKey = "test:connection";
        String testValue = "Hello Redis! " + System.currentTimeMillis();
        
        redisTemplate.opsForValue().set(testKey, testValue);
        
        // 测试读取
        String result = redisTemplate.opsForValue().get(testKey);
        
        // 验证
        assertNotNull(result);
        assertEquals(testValue, result);
        
        // 清理测试数据
        redisTemplate.delete(testKey);
        
        System.out.println("✅ Redis 连接测试成功！");
        System.out.println("写入值: " + testValue);
        System.out.println("读取值: " + result);
    }
    
    @Test
    void testRedisPing() {
        String result = redisTemplate.getConnectionFactory()
                .getConnection().ping();
        
        assertNotNull(result);
        System.out.println("✅ PING: " + result);
    }
}
