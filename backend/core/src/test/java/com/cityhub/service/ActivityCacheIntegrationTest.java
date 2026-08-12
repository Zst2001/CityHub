package com.cityhub.service;

import cn.hutool.json.JSONUtil;
import com.cityhub.entity.Activity;
import com.cityhub.service.impl.ActivityServiceImpl;
import com.cityhub.utils.RedisConstants;
import com.cityhub.utils.RedisData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class ActivityCacheIntegrationTest {

    @Autowired
    private ActivityServiceImpl activityService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @AfterEach
    void cleanUp() {
        stringRedisTemplate.delete(RedisConstants.CACHE_ACTIVITY_KEY + 1L);
        stringRedisTemplate.delete(RedisConstants.LOCK_ACTIVITY_KEY + 1L);
    }

    @Test
    void expiredLogicalCacheReturnsStaleValueAndRebuildsAsynchronously() throws InterruptedException {
        activityService.saveActivityToRedis(1L, -1L);

        Activity staleActivity = activityService.queryWithLogicalExpire(1L);
        assertNotNull(staleActivity);

        LocalDateTime deadline = LocalDateTime.now().plusSeconds(5);
        RedisData rebuilt = null;
        while (LocalDateTime.now().isBefore(deadline)) {
            String cached = stringRedisTemplate.opsForValue().get(RedisConstants.CACHE_ACTIVITY_KEY + 1L);
            rebuilt = JSONUtil.toBean(cached, RedisData.class);
            if (rebuilt.getExpireTime().isAfter(LocalDateTime.now())) {
                break;
            }
            Thread.sleep(100L);
        }
        assertNotNull(rebuilt);
        assertTrue(rebuilt.getExpireTime().isAfter(LocalDateTime.now()));
    }
}
