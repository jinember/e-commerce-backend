package com.gec.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gec.domain.entity.Favorite;

public interface IFavoriteService extends IService<Favorite> {
    IPage<Favorite> listFavorite(Page page, Favorite param);
}

