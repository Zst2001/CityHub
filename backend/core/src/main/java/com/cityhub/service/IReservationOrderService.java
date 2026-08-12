package com.cityhub.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cityhub.dto.Result;
import com.cityhub.entity.ReservationOrder;

public interface IReservationOrderService extends IService<ReservationOrder> {

    Result seckillTicket(Long ticketId);

    Result createReservationOrder(Long ticketId);

    void createReservationOrderAsync(ReservationOrder reservationOrder);
}
