package com.gec.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gec.domain.entity.Logistics;

public interface ILogisticsService extends IService<Logistics> {
    IPage<Logistics> listLogistics(Page page, Logistics param);
}

