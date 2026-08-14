package com.cityhub.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cityhub.dto.LoginFormDTO;
import com.cityhub.dto.Result;
import com.cityhub.entity.User;

import javax.servlet.http.HttpSession;


public interface IUserService extends IService<User> {

    Result sendCode(String phone, HttpSession session) ;

    Result login(LoginFormDTO loginForm, HttpSession session);

    Result adminLogin(LoginFormDTO loginForm);



}
