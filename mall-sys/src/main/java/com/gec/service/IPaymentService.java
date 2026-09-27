package com.gec.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gec.domain.entity.Payment;

public interface IPaymentService extends IService<Payment> {
    IPage<Payment> listPayment(Page page, Payment param);
}

