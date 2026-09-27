package com.gec.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gec.dao.LogisticsMapper;
import com.gec.domain.entity.Logistics;
import com.gec.service.ILogisticsService;
import org.springframework.stereotype.Service;

@Service
public class LogisticsServiceImpl
        extends ServiceImpl<LogisticsMapper, Logistics>
        implements ILogisticsService {

    @Override
    public IPage<Logistics> listLogistics(Page page, Logistics param) {
        QueryWrapper<Logistics> QW = new QueryWrapper<>();
        if (param.getOrderNo() != null && !param.getOrderNo().isEmpty()) {
            QW.apply("order_id IN (SELECT id FROM tbl_order_info WHERE order_no LIKE {0})",
                    "%" + param.getOrderNo() + "%");
        }
        if (param.getOrderId() != null) {
            QW.eq("order_id", param.getOrderId());
        }
        if (param.getCompany() != null) {
            QW.like("company", String.valueOf(param.getCompany()));
        }
        if (param.getStatus() != null) {
            QW.like("status", String.valueOf(param.getStatus()));
        }
        return baseMapper.selectPage(page, QW);
    }
}

