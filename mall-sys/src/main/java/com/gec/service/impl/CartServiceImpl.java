package com.gec.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gec.dao.CartMapper;
import com.gec.domain.entity.Cart;
import com.gec.service.ICartService;
import org.springframework.stereotype.Service;

@Service
public class CartServiceImpl
        extends ServiceImpl<CartMapper, Cart>
        implements ICartService {

    @Override
    public IPage<Cart> listCart(Page page, Cart param) {
        QueryWrapper<Cart> QW = new QueryWrapper<>();
        if (param.getMemberId() != null) {
            QW.eq("member_id", param.getMemberId());
        }
        if (param.getSpuId() != null) {
            QW.eq("spu_id", param.getSpuId());
        }
        return baseMapper.selectPage(page, QW);
    }
}

