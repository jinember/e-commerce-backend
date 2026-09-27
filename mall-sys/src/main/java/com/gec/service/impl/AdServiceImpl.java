package com.gec.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gec.dao.AdMapper;
import com.gec.domain.entity.Ad;
import com.gec.service.IAdService;
import org.springframework.stereotype.Service;

@Service
public class AdServiceImpl
        extends ServiceImpl<AdMapper, Ad>
        implements IAdService {

    @Override
    public IPage<Ad> listAd(Page page, Ad param) {
        QueryWrapper<Ad> QW = new QueryWrapper<>();
        if (param.getTitle() != null && !param.getTitle().isEmpty()) {
            QW.like("title", param.getTitle());
        }
        if (param.getPosition() != null && !param.getPosition().isEmpty()) {
            QW.eq("position", param.getPosition());
        }
        QW.orderByAsc("sort");
        return baseMapper.selectPage(page, QW);
    }
}
