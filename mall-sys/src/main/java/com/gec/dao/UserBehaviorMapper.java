package com.gec.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gec.domain.entity.UserBehavior;
import org.apache.ibatis.annotations.Select;

public interface UserBehaviorMapper extends BaseMapper<UserBehavior> {

    @Select("SELECT b.*, m.nickname FROM tbl_user_behavior b " +
            "LEFT JOIN tbl_member m ON b.member_id = m.id " +
            "ORDER BY b.id DESC")
    IPage<UserBehavior> selectPageWithMember(Page<UserBehavior> page);
}

