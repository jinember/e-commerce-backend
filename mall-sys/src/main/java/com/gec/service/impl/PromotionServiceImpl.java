package com.gec.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gec.dao.PromotionMapper;
import com.gec.domain.entity.Promotion;
import com.gec.service.IPromotionService;
import org.springframework.stereotype.Service;

@Service
public class PromotionServiceImpl
        extends ServiceImpl<PromotionMapper, Promotion>
        implements IPromotionService {

    @Override
    public IPage<Promotion> listPromotion(Page page, Promotion param) {
        QueryWrapper<Promotion> QW = new QueryWrapper<>();
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

