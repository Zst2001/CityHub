package com.cityhub.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cityhub.dto.Result;
import com.cityhub.entity.ActivityCategory;
import com.cityhub.mapper.ActivityCategoryMapper;
import com.cityhub.service.IActivityCategoryService;
import com.cityhub.utils.RedisConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ActivityCategoryServiceImpl extends ServiceImpl<ActivityCategoryMapper, ActivityCategory>
        implements IActivityCategoryService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryList() {
        String cacheKey = RedisConstants.CACHE_ACTIVITY_KEY + "category:list";
        List<String> cached = stringRedisTemplate.opsForList().range(cacheKey, 0, -1);
        if (cached != null && !cached.isEmpty()) {
            return Result.ok(cached.stream()
                    .map(value -> JSONUtil.toBean(value, ActivityCategory.class))
                    .collect(Collectors.toList()));
        }
        List<ActivityCategory> categories = list(new LambdaQueryWrapper<ActivityCategory>()
                .orderByAsc(ActivityCategory::getSort));
        if (categories.isEmpty()) {
            return Result.fail("活动分类不存在");
        }
        stringRedisTemplate.opsForList().rightPushAll(cacheKey,
                categories.stream().map(JSONUtil::toJsonStr).collect(Collectors.toList()));
        stringRedisTemplate.expire(cacheKey, RedisConstants.CACHE_ACTIVITY_TTL, TimeUnit.MINUTES);
        return Result.ok(categories);
    }
}
