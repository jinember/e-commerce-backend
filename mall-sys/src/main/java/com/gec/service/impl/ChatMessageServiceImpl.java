package com.gec.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gec.dao.ChatMessageDao;
import com.gec.domain.entity.ChatMessage;
import com.gec.service.IChatMessageService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageDao, ChatMessage>
        implements IChatMessageService {

    @Override
    public List<ChatMessage> listBySession(Integer sessionId) {
        return baseMapper.listBySession(sessionId);
    }

    @Override
    public String firstMerchantTime(Integer sessionId) {
        return baseMapper.firstMerchantTime(sessionId);
    }
}
