package com.gec.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gec.dao.CouponMapper;
import com.gec.domain.entity.Coupon;
import com.gec.service.ICouponService;
import org.springframework.stereotype.Service;

@Service
public class CouponServiceImpl
        extends ServiceImpl<CouponMapper, Coupon>
        implements ICouponService {

    @Override
    public IPage<Coupon> listCoupon(Page page, Coupon param) {
        QueryWrapper<Coupon> QW = new QueryWrapper<>();
        if (param.getName() != null) {
            QW.like("name", String.valueOf(param.getName()));
        }
        if (param.getType() != null) {
            QW.like("type", String.valueOf(param.getType()));
        }
        if (param.getStatus() != null) {
            QW.like("status", String.valueOf(param.getStatus()));
        }
        return baseMapper.selectPage(page, QW);
    }
}

