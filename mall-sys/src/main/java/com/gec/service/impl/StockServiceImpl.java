package com.gec.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gec.dao.StockMapper;
import com.gec.domain.entity.Stock;
import com.gec.service.IStockService;
import org.springframework.stereotype.Service;

@Service
public class StockServiceImpl
        extends ServiceImpl<StockMapper, Stock>
        implements IStockService {

    @Override
    public IPage<Stock> listStock(Page page, Stock param) {
        QueryWrapper<Stock> QW = new QueryWrapper<>();
        if (param.getGoodsName() != null && !param.getGoodsName().isEmpty()) {
            QW.like("goods_name", param.getGoodsName());
        }
        if (param.getWarehouse() != null && !param.getWarehouse().isEmpty()) {
            QW.eq("warehouse", param.getWarehouse());
        }
        return baseMapper.selectPage(page, QW);
    }
}
