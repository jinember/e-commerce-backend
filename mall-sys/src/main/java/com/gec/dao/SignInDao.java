package com.gec.dao;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gec.domain.entity.SignIn;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SignInDao extends BaseMapper<SignIn> {

    @Select("SELECT s.*, m.nickname FROM tbl_sign_in s " +
            "LEFT JOIN tbl_member m ON s.member_id = m.id " +
            "ORDER BY s.id DESC")
    IPage<SignIn> selectPageWithMember(Page<SignIn> page);
}