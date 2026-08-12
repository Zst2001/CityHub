package com.cityhub.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cityhub.dto.Result;
import com.cityhub.entity.SeckillTicket;
import com.cityhub.entity.Ticket;
import com.cityhub.mapper.TicketMapper;
import com.cityhub.service.ISeckillTicketService;
import com.cityhub.service.ITicketService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class TicketServiceImpl extends ServiceImpl<TicketMapper, Ticket> implements ITicketService {

    @Resource
    private ISeckillTicketService seckillTicketService;

    @Override
    public Result queryTicketOfActivity(Long activityId) {
        List<Ticket> tickets = getBaseMapper().queryTicketOfActivity(activityId);
        return Result.ok(tickets);
    }

    @Override
    @Transactional
    public void addSeckillTicket(Ticket ticket) {
        save(ticket);
        SeckillTicket seckillTicket = new SeckillTicket();
        seckillTicket.setTicketId(ticket.getId());
        seckillTicket.setStock(ticket.getStock());
        seckillTicket.setBeginTime(ticket.getBeginTime());
        seckillTicket.setEndTime(ticket.getEndTime());
        seckillTicketService.save(seckillTicket);
    }
}
