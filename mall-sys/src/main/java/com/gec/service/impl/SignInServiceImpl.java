package com.gec.service.impl;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gec.dao.SignInDao;
import com.gec.domain.entity.SignIn;
import com.gec.service.ISignInService;
import org.springframework.stereotype.Service;

@Service
public class SignInServiceImpl extends ServiceImpl<SignInDao, SignIn> implements ISignInService {

    @Override
    public IPage<SignIn> listSignIn(Page<SignIn> page) {
        return baseMapper.selectPageWithMember(page);
    }
}