package com.cityhub.controller;

import com.cityhub.dto.LoginFormDTO;
import com.cityhub.dto.Result;
import com.cityhub.entity.Activity;
import com.cityhub.entity.Ticket;
import com.cityhub.service.IActivityService;
import com.cityhub.service.ITicketService;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;

@RestController
@RequestMapping("/admin")
public class AdminController {
    @Resource private com.cityhub.service.IUserService userService;
    @Resource private IActivityService activityService;
    @Resource private ITicketService ticketService;

    @PostMapping("/login")
    public Result login(@RequestBody LoginFormDTO form) { return userService.adminLogin(form); }
    @GetMapping("/activities")
    public Result activities(@RequestParam(value = "keyword", required = false) String keyword,
                             @RequestParam(value = "current", defaultValue = "1") Integer current) {
        return activityService.adminPage(keyword, current);
    }
    @PutMapping("/activities")
    public Result updateActivity(@RequestBody Activity activity) { return activityService.update(activity); }
    @GetMapping("/activities/{activityId}/tickets")
    public Result tickets(@PathVariable Long activityId) { return ticketService.queryTicketOfActivity(activityId); }
    @PutMapping("/tickets/{ticketId}")
    public Result updateTicket(@PathVariable Long ticketId, @RequestBody Ticket ticket) {
        ticket.setId(ticketId); return ticketService.updateAdminTicket(ticket);
    }
}
