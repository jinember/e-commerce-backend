package com.gec.service.impl;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gec.dao.MessageDao;
import com.gec.domain.entity.Message;
import com.gec.service.IMessageService;
import org.springframework.stereotype.Service;

@Service
public class MessageServiceImpl extends ServiceImpl<MessageDao, Message> implements IMessageService {

    @Override
    public IPage<Message> listMessage(Page<Message> page) {
        return baseMapper.selectPageWithMember(page);
    }
}