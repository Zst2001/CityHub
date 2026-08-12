package com.cityhub.controller;

import com.cityhub.dto.Result;
import com.cityhub.service.IActivityCategoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/activity-category")
public class ActivityCategoryController {

    @Resource
    private IActivityCategoryService activityCategoryService;

    @GetMapping("/list")
    public Result queryList() {
        return activityCategoryService.queryList();
    }
}
