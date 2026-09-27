package com.gec.service;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gec.domain.entity.SignIn;

public interface ISignInService extends IService<SignIn> {
    IPage<SignIn> listSignIn(Page<SignIn> page);
}