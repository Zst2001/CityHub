package com.cityhub.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cityhub.entity.SeckillTicket;
import com.cityhub.mapper.SeckillTicketMapper;
import com.cityhub.service.ISeckillTicketService;
import org.springframework.stereotype.Service;

@Service
public class SeckillTicketServiceImpl extends ServiceImpl<SeckillTicketMapper, SeckillTicket>
        implements ISeckillTicketService {
}
