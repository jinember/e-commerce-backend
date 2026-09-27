package com.gec.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gec.dao.PaymentMapper;
import com.gec.domain.entity.Payment;
import com.gec.service.IPaymentService;
import org.springframework.stereotype.Service;

@Service
public class PaymentServiceImpl
        extends ServiceImpl<PaymentMapper, Payment>
        implements IPaymentService {

    @Override
    public IPage<Payment> listPayment(Page page, Payment param) {
        QueryWrapper<Payment> QW = new QueryWrapper<>();
        if (param.getOrderNo() != null && !param.getOrderNo().isEmpty()) {
            QW.apply("order_id IN (SELECT id FROM tbl_order_info WHERE order_no LIKE {0})",
                    "%" + param.getOrderNo() + "%");
        }
        if (param.getOrderId() != null) {
            QW.eq("order_id", param.getOrderId());
        }
        if (param.getPayType() != null) {
            QW.like("pay_type", String.valueOf(param.getPayType()));
        }
        if (param.getPayStatus() != null) {
            QW.like("pay_status", String.valueOf(param.getPayStatus()));
        }
        return baseMapper.selectPage(page, QW);
    }
}

