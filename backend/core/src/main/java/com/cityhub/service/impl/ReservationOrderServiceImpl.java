package com.cityhub.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cityhub.dto.Result;
import com.cityhub.entity.ReservationOrder;
import com.cityhub.mapper.ReservationOrderMapper;
import com.cityhub.service.IReservationOrderService;
import com.cityhub.service.ISeckillTicketService;
import com.cityhub.utils.RedisConstants;
import com.cityhub.utils.RedisIDWorker;
import com.cityhub.utils.UserHolder;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Collections;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class ReservationOrderServiceImpl extends ServiceImpl<ReservationOrderMapper, ReservationOrder>
        implements IReservationOrderService {

    @Resource
    private RedisIDWorker redisIDWorker;
    @Resource
    private ISeckillTicketService seckillTicketService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private RedissonClient redissonClient;

    private IReservationOrderService proxy;

    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;

    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }

    private final BlockingQueue<ReservationOrder> orderTasks = new ArrayBlockingQueue<>(1024 * 1024);
    private static final ExecutorService SECKILL_ORDER_EXECUTOR = Executors.newSingleThreadExecutor();

    @PostConstruct
    private void init() {
        SECKILL_ORDER_EXECUTOR.submit(new ReservationOrderHandler());
    }

    private void handleReservationOrder(ReservationOrder reservationOrder) {
        Long userId = reservationOrder.getUserId();
        RLock lock = redissonClient.getLock("order:" + userId);
        boolean isLock = lock.tryLock();
        if (!isLock) {
            log.error("不允许重复预约");
            return;
        }
        try {
            proxy.createReservationOrderAsync(reservationOrder);
        } finally {
            lock.unlock();
        }
    }

    private class ReservationOrderHandler implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    handleReservationOrder(orderTasks.take());
                } catch (Exception e) {
                    log.error("异步预约订单处理失败", e);
                }
            }
        }
    }

    @Override
    public Result seckillTicket(Long ticketId) {
        Long userId = UserHolder.getUser().getId();
        Long result = stringRedisTemplate.execute(SECKILL_SCRIPT, Collections.emptyList(),
                ticketId.toString(), userId.toString());
        int code = result.intValue();
        if (code != 0) {
            return Result.fail(code == 1 ? "预约凭证已售罄" : "每位用户只能预约一次");
        }
        long orderId = redisIDWorker.nextId(RedisConstants.RESERVATION_ORDER_ID_KEY);
        ReservationOrder order = new ReservationOrder();
        order.setId(orderId);
        order.setTicketId(ticketId);
        order.setUserId(userId);
        proxy = (IReservationOrderService) AopContext.currentProxy();
        orderTasks.add(order);
        return Result.ok(orderId);
    }

    @Override
    @Transactional
    public Result createReservationOrder(Long ticketId) {
        Long userId = UserHolder.getUser().getId();
        Integer count = query().eq("user_id", userId).eq("ticket_id", ticketId).count();
        if (count > 0) {
            return Result.fail("该用户已经预约过该凭证");
        }
        boolean success = seckillTicketService.update()
                .setSql("stock = stock - 1")
                .eq("ticket_id", ticketId).gt("stock", 0)
                .update();
        if (!success) {
            return Result.fail("预约凭证已售罄");
        }
        long orderId = redisIDWorker.nextId(RedisConstants.RESERVATION_ORDER_ID_KEY);
        ReservationOrder order = new ReservationOrder();
        order.setId(orderId);
        order.setTicketId(ticketId);
        order.setUserId(userId);
        save(order);
        return Result.ok(orderId);
    }

    @Override
    @Transactional
    public void createReservationOrderAsync(ReservationOrder reservationOrder) {
        Long userId = reservationOrder.getUserId();
        Long ticketId = reservationOrder.getTicketId();
        Integer count = query().eq("user_id", userId).eq("ticket_id", ticketId).count();
        if (count > 0) {
            log.error("该用户已经预约过该凭证");
            return;
        }
        boolean success = seckillTicketService.update()
                .setSql("stock = stock - 1")
                .eq("ticket_id", ticketId).gt("stock", 0)
                .update();
        if (!success) {
            log.error("扣减预约凭证库存失败");
            return;
        }
        save(reservationOrder);
    }
}
