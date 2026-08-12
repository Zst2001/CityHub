package com.cityhub.consultant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cityhub.consultant.pojo.Voucher;
import com.cityhub.consultant.pojo.VoucherOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;


@Mapper
public interface VoucherOrderMapper extends BaseMapper<VoucherOrder> {

    @Select("select voucher_id from tb_voucher_order where phone = #{phone}")
    List<Long> findByPhone(String phone);
}
