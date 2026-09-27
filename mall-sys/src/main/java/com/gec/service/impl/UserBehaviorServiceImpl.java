package com.gec.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gec.dao.UserBehaviorMapper;
import com.gec.domain.entity.UserBehavior;
import com.gec.service.IUserBehaviorService;
import org.springframework.stereotype.Service;

@Service
public class UserBehaviorServiceImpl
        extends ServiceImpl<UserBehaviorMapper, UserBehavior>
        implements IUserBehaviorService {

    @Override
    public IPage<UserBehavior> listUserBehavior(Page page, UserBehavior param) {
        return baseMapper.selectPageWithMember(page);
    }
}

