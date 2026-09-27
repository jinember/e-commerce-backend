package com.gec.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gec.dao.CommentMapper;
import com.gec.domain.entity.Comment;
import com.gec.service.ICommentService;
import org.springframework.stereotype.Service;

@Service
public class CommentServiceImpl
        extends ServiceImpl<CommentMapper, Comment>
        implements ICommentService {

    @Override
    public IPage<Comment> listComment(Page page, Comment param) {
        return baseMapper.selectPageWithMember(page);
    }
}

