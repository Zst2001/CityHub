package com.cityhub.controller;

import com.cityhub.dto.Result;
import com.cityhub.entity.Ticket;
import com.cityhub.service.ITicketService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/ticket")
public class TicketController {

    @Resource
    private ITicketService ticketService;

    @PostMapping
    public Result addTicket(@RequestBody Ticket ticket) {
        ticketService.save(ticket);
        return Result.ok(ticket.getId());
    }

    @PostMapping("/seckill")
    public Result addSeckillTicket(@RequestBody Ticket ticket) {
        ticketService.addSeckillTicket(ticket);
        return Result.ok(ticket.getId());
    }

    @GetMapping("/list/{activityId}")
    public Result queryTicketOfActivity(@PathVariable("activityId") Long activityId) {
        return ticketService.queryTicketOfActivity(activityId);
    }
}
