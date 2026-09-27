package com.gec.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gec.domain.entity.Comment;
import org.apache.ibatis.annotations.Select;

public interface CommentMapper extends BaseMapper<Comment> {

    @Select("SELECT c.*, m.nickname FROM tbl_comment c " +
            "LEFT JOIN tbl_member m ON c.member_id = m.id " +
            "ORDER BY c.id DESC")
    IPage<Comment> selectPageWithMember(Page<Comment> page);
}

