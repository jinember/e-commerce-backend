package com.gec.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gec.dao.ChatSessionDao;
import com.gec.domain.entity.ChatSession;
import com.gec.service.IChatSessionService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ChatSessionServiceImpl extends ServiceImpl<ChatSessionDao, ChatSession>
        implements IChatSessionService {

    @Override
    public IPage<ChatSession> listChatSession(Page<ChatSession> page, ChatSession param) {
        if (param == null) param = new ChatSession();
        return baseMapper.selectPageWithMember(page, param.getId(), param.getChatNo(),
                param.getNickname(), param.getChatType());
    }

    @Override
    public IPage<ChatSession> listPending(Page<ChatSession> page) {
        return baseMapper.selectPending(page);
    }

    @Override
    public Integer countPending() {
        return baseMapper.countPending();
    }

    @Override
    public List<Map<String, Object>> statByChatType() {
        return baseMapper.statByChatType();
    }

    @Override
    public IPage<ChatSession> chatList(Page<ChatSession> page) {
        return baseMapper.selectChatList(page);
    }

    @Override
    public Integer countUnreadSessions() {
        return baseMapper.countUnreadSessions();
    }

    @Override
    public int markRead(Integer id) {
        return baseMapper.markRead(id);
    }
}
