package com.gec.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gec.domain.entity.ChatMessage;

import java.util.List;

/**
 * 客服对话明细
 */
public interface IChatMessageService extends IService<ChatMessage> {

    /* 某会话的完整对话（时间正序） */
    List<ChatMessage> listBySession(Integer sessionId);

    /* 该会话第一条客服回复时间 */
    String firstMerchantTime(Integer sessionId);
}
