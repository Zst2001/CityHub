package com.cityhub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cityhub.entity.Ticket;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TicketMapper extends BaseMapper<Ticket> {

    List<Ticket> queryTicketOfActivity(@Param("activityId") Long activityId);
}
