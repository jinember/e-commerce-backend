package com.gec.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gec.dao.FavoriteMapper;
import com.gec.domain.entity.Favorite;
import com.gec.service.IFavoriteService;
import org.springframework.stereotype.Service;

@Service
public class FavoriteServiceImpl
        extends ServiceImpl<FavoriteMapper, Favorite>
        implements IFavoriteService {

    @Override
    public IPage<Favorite> listFavorite(Page page, Favorite param) {
        return baseMapper.selectPageWithMember(page, param);
    }
}

