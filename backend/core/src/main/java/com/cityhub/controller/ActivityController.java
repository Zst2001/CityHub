package com.cityhub.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cityhub.dto.Result;
import com.cityhub.entity.Activity;
import com.cityhub.service.IActivityService;
import com.cityhub.utils.SystemConstants;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/activity")
public class ActivityController {

    @Resource
    private IActivityService activityService;

    @GetMapping("/{id}")
    public Result queryActivityById(@PathVariable("id") Long id) {
        return activityService.queryById(id);
    }

    @PostMapping
    public Result saveActivity(@RequestBody Activity activity) {
        activityService.save(activity);
        return Result.ok(activity.getId());
    }

    @PutMapping
    public Result updateActivity(@RequestBody Activity activity) {
        return activityService.update(activity);
    }

    @GetMapping("/of/category")
    public Result queryActivityByCategory(
            @RequestParam("categoryId") Integer categoryId,
            @RequestParam(value = "current", defaultValue = "1") Integer current) {
        Page<Activity> page = activityService.query()
                .eq("category_id", categoryId)
                .page(new Page<>(current, SystemConstants.DEFAULT_PAGE_SIZE));
        return Result.ok(page.getRecords());
    }

    /**
     * Lightweight pageable activity query for the CityHub discovery page.
     */
    @GetMapping("/page")
    public Result queryActivityPage(
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "size", defaultValue = "5") Integer size) {
        long pageSize = Math.min(Math.max(size, 1), SystemConstants.MAX_PAGE_SIZE);
        Page<Activity> page = activityService.query()
                .orderByDesc("score")
                .orderByDesc("sold")
                .page(new Page<>(current, pageSize));
        return Result.ok(page.getRecords(), page.getTotal());
    }

    @GetMapping("/of/name")
    public Result queryActivityByName(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "current", defaultValue = "1") Integer current) {
        Page<Activity> page = activityService.query()
                .like(StrUtil.isNotBlank(name), "title", name)
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        return Result.ok(page.getRecords());
    }
}
