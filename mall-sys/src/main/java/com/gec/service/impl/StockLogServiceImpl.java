package com.gec.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gec.dao.StockLogMapper;
import com.gec.domain.entity.StockLog;
import com.gec.service.IStockLogService;
import org.springframework.stereotype.Service;

@Service
public class StockLogServiceImpl
        extends ServiceImpl<StockLogMapper, StockLog>
        implements IStockLogService {

    @Override
    public IPage<StockLog> listStockLog(Page page, StockLog param) {
        QueryWrapper<StockLog> QW = new QueryWrapper<>();
        if (param.getStockId() != null) {
            QW.eq("stock_id", param.getStockId());
        }
        if (param.getChangeType() != null) {
            QW.like("change_type", String.valueOf(param.getChangeType()));
        }
        return baseMapper.selectPage(page, QW);
    }
}

