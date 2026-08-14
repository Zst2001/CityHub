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
import com.cityhub.dto.Result;
import com.cityhub.utils.RedisConstants;
import org.springframework.data.redis.core.StringRedisTemplate;

@Service
public class TicketServiceImpl extends ServiceImpl<TicketMapper, Ticket> implements ITicketService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

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

    @Override
    @Transactional
    public Result updateAdminTicket(Ticket ticket) {
        if (ticket.getId() == null || getById(ticket.getId()) == null) return Result.fail("绁ㄥ埜涓嶅瓨鍦?");
        // stock/beginTime/endTime are mapped to tb_seckill_ticket only. A stock-only
        // administration request must not issue an empty UPDATE tb_ticket ... SET.
        if (ticket.getTitle() != null || ticket.getSubTitle() != null || ticket.getRules() != null
                || ticket.getPayValue() != null || ticket.getActualValue() != null
                || ticket.getType() != null || ticket.getStatus() != null) {
            updateById(ticket);
        }
        SeckillTicket seckill = seckillTicketService.getById(ticket.getId());
        if (seckill == null) return Result.fail("限量票券不存在");
        if (ticket.getStock() != null || ticket.getBeginTime() != null || ticket.getEndTime() != null) {
            if (ticket.getStock() != null) seckill.setStock(ticket.getStock());
            if (ticket.getBeginTime() != null) seckill.setBeginTime(ticket.getBeginTime());
            if (ticket.getEndTime() != null) seckill.setEndTime(ticket.getEndTime());
            seckillTicketService.updateById(seckill);
            if (ticket.getStock() != null) {
                stringRedisTemplate.opsForValue().set(RedisConstants.SECKILL_STOCK_KEY + ticket.getId(), ticket.getStock().toString());
            }
        }
        return Result.ok();
    }
}
