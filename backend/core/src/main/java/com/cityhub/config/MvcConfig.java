package com.cityhub.config;

import com.cityhub.utils.LoginInterceptor;
import com.cityhub.utils.RefreshTokenInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor())
                .excludePathPatterns(
                        "/user/code",
                        "/blog/hot",
                        "/blog/of/activity",
                        "/blog/likes/**",
                        "/blog/*",
                        "/activity/**",
                        "/activity-category/**",
                        "/upload/**",
                        "/ticket/**",
                        "/user/login",
                        "/user/login"
                ).order(1);

        registry.addInterceptor(new RefreshTokenInterceptor(stringRedisTemplate)).addPathPatterns("/**").order(0);
    }


}
