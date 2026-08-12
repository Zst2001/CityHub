package com.cityhub.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cityhub.dto.Result;
import com.cityhub.entity.Activity;

public interface IActivityService extends IService<Activity> {

    Result queryById(Long id);

    Result update(Activity activity);
}
