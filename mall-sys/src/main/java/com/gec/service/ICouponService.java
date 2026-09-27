package com.gec.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gec.domain.entity.Coupon;

public interface ICouponService extends IService<Coupon> {
    IPage<Coupon> listCoupon(Page page, Coupon param);
}

