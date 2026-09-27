package com.gec.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gec.domain.entity.Refund;

public interface IRefundService extends IService<Refund> {
    IPage<Refund> listRefund(Page page, Refund param);
}

