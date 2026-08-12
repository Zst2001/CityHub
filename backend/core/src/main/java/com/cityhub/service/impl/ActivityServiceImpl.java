package com.cityhub.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cityhub.dto.Result;
import com.cityhub.entity.Activity;
import com.cityhub.mapper.ActivityMapper;
import com.cityhub.service.IActivityService;
import com.cityhub.utils.CacheClient;
import com.cityhub.utils.RedisConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Service
public class ActivityServiceImpl extends ServiceImpl<ActivityMapper, Activity> implements IActivityService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private CacheClient cacheClient;

    @Override
    public Result queryById(Long id) {
        Activity activity = cacheClient.queryWithPassThrough(
                RedisConstants.CACHE_ACTIVITY_KEY, id, Activity.class, this::getById,
                RedisConstants.CACHE_ACTIVITY_TTL, RedisConstants.CACHE_ACTIVITY_TTL_JITTER, TimeUnit.MINUTES);
        return activity == null ? Result.fail("活动不存在") : Result.ok(activity);
    }

    @Override
    @Transactional
    public Result update(Activity activity) {
        if (activity.getId() == null) {
            return Result.fail("活动 id 不存在");
        }
        updateById(activity);
        stringRedisTemplate.delete(RedisConstants.CACHE_ACTIVITY_KEY + activity.getId());
        return Result.ok();
    }

    public Activity queryWithLogicalExpire(Long id) {
        return cacheClient.queryWithLogicalExpire(
                RedisConstants.CACHE_ACTIVITY_KEY, id, Activity.class, this::getById,
                RedisConstants.CACHE_ACTIVITY_TTL, TimeUnit.MINUTES);
    }

    public void saveActivityToRedis(Long id, Long expireSeconds) {
        Activity activity = getById(id);
        cacheClient.set(RedisConstants.CACHE_ACTIVITY_KEY + id, activity, expireSeconds);
    }
}
