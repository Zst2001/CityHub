package com.cityhub.config;

import com.cityhub.entity.SeckillTicket;
import com.cityhub.entity.ReservationOrder;
import com.cityhub.service.IReservationOrderService;
import com.cityhub.service.ISeckillTicketService;
import com.cityhub.utils.RedisConstants;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * Initializes the Redis stock keys used by the existing Lua reservation script
 * from the current development database on application startup.
 */
@Component
public class SeckillStockInitializer implements ApplicationRunner {

    @Resource
    private ISeckillTicketService seckillTicketService;

    @Resource
    private IReservationOrderService reservationOrderService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void run(ApplicationArguments args) {
        List<SeckillTicket> tickets = seckillTicketService.list();
        for (SeckillTicket ticket : tickets) {
            if (ticket.getTicketId() != null && ticket.getStock() != null) {
                stringRedisTemplate.delete(RedisConstants.SECKILL_ORDER_KEY + ticket.getTicketId());
                stringRedisTemplate.opsForValue().set(
                        RedisConstants.SECKILL_STOCK_KEY + ticket.getTicketId(),
                        ticket.getStock().toString());
            }
        }
        for (ReservationOrder order : reservationOrderService.list()) {
            stringRedisTemplate.opsForSet().add(
                    RedisConstants.SECKILL_ORDER_KEY + order.getTicketId(), order.getUserId().toString());
        }
    }
}
