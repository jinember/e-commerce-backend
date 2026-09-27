package com.gec.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gec.domain.entity.UserBehavior;

public interface IUserBehaviorService extends IService<UserBehavior> {
    IPage<UserBehavior> listUserBehavior(Page page, UserBehavior param);
}

