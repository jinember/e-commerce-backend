package com.gec.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gec.domain.entity.StockLog;

public interface IStockLogService extends IService<StockLog> {
    IPage<StockLog> listStockLog(Page page, StockLog param);
}

