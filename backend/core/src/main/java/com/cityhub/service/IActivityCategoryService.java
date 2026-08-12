package com.cityhub.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cityhub.dto.Result;
import com.cityhub.entity.ActivityCategory;

public interface IActivityCategoryService extends IService<ActivityCategory> {

    Result queryList();
}
