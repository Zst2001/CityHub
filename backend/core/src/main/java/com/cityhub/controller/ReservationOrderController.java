package com.cityhub.controller;

import com.cityhub.dto.Result;
import com.cityhub.service.IReservationOrderService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/reservation")
public class ReservationOrderController {

    @Resource
    private IReservationOrderService reservationOrderService;

    @PostMapping("/seckill/{id}")
    public Result seckillTicket(@PathVariable("id") Long ticketId) {
        return reservationOrderService.seckillTicket(ticketId);
    }
}
