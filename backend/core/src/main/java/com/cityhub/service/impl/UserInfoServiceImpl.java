package com.cityhub.service.impl;

import com.cityhub.entity.UserInfo;
import com.cityhub.mapper.UserInfoMapper;
import com.cityhub.service.IUserInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;


@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements IUserInfoService {

}
