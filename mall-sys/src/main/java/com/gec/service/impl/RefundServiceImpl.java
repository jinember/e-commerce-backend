package com.gec.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gec.dao.RefundMapper;
import com.gec.domain.entity.Refund;
import com.gec.service.IRefundService;
import org.springframework.stereotype.Service;

@Service
public class RefundServiceImpl
        extends ServiceImpl<RefundMapper, Refund>
        implements IRefundService {

    @Override
    public IPage<Refund> listRefund(Page page, Refund param) {
        QueryWrapper<Refund> QW = new QueryWrapper<>();
        if (param.getOrderNo() != null && !param.getOrderNo().isEmpty()) {
            QW.apply("order_id IN (SELECT id FROM tbl_order_info WHERE order_no LIKE {0})",
                    "%" + param.getOrderNo() + "%");
        }
        if (param.getOrderId() != null) {
            QW.eq("order_id", param.getOrderId());
        }
        if (param.getReason() != null) {
            QW.like("reason", String.valueOf(param.getReason()));
        }
        if (param.getStatus() != null) {
            QW.like("status", String.valueOf(param.getStatus()));
        }
        return baseMapper.selectPage(page, QW);
    }
}

