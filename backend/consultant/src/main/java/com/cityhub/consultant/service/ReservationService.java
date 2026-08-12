package com.cityhub.consultant.service;

import com.cityhub.consultant.mapper.ReservationMapper;
import com.cityhub.consultant.mapper.ShopMapper;
import com.cityhub.consultant.pojo.Reservation;
import com.cityhub.consultant.pojo.Shop;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReservationService {
    @Autowired
    private ReservationMapper reservationMapper;

    //1.添加预约信息的方法
    public void insert(Reservation reservation) {
        reservationMapper.insert(reservation);
    }

    //2.查询预约信息的方法(根据手机号查询)
    public List<Reservation> findByPhone(String phone) {
        return reservationMapper.findByPhone(phone);
    }
}
