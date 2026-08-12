package com.cityhub.service.impl;

import com.cityhub.entity.BlogComments;
import com.cityhub.mapper.BlogCommentsMapper;
import com.cityhub.service.IBlogCommentsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;


@Service
public class BlogCommentsServiceImpl extends ServiceImpl<BlogCommentsMapper, BlogComments> implements IBlogCommentsService {

}
