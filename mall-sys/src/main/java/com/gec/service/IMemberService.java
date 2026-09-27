package com.gec.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gec.domain.entity.Member;

import java.util.Map;

public interface IMemberService extends IService<Member> {
    IPage<Member> listMember(Page page, Member param);
    Map<String, Object> statMember();
}
