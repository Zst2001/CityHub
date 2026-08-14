package com.cityhub.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cityhub.dto.Result;
import com.cityhub.entity.Ticket;

public interface ITicketService extends IService<Ticket> {

    Result queryTicketOfActivity(Long activityId);

    void addSeckillTicket(Ticket ticket);

    Result updateAdminTicket(Ticket ticket);
}
