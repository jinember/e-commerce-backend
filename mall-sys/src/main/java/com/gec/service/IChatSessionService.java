package com.gec.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gec.domain.entity.ChatSession;

import java.util.List;
import java.util.Map;

public interface IChatSessionService extends IService<ChatSession> {
    IPage<ChatSession> listChatSession(Page<ChatSession> page, ChatSession param);

    /* 待回复会话（最后一条消息是会员发的） */
    IPage<ChatSession> listPending(Page<ChatSession> page);

    Integer countPending();

    List<Map<String, Object>> statByChatType();

    /* ============ 客服聊天工作台 ============ */
    IPage<ChatSession> chatList(Page<ChatSession> page);

    Integer countUnreadSessions();

    int markRead(Integer id);
}
