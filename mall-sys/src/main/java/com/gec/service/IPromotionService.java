package com.gec.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gec.domain.entity.Promotion;

public interface IPromotionService extends IService<Promotion> {
    IPage<Promotion> listPromotion(Page page, Promotion param);
}

