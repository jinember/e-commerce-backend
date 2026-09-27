package com.gec.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gec.domain.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ChatMessageDao extends BaseMapper<ChatMessage> {

    /* 某个客服会话的完整对话，按时间正序（带处理客服姓名） */
    @Select("SELECT m.*, u.nick_name AS staffName FROM tbl_chat_message m "
            + "LEFT JOIN tbl_user u ON u.id = m.staff_id "
            + "WHERE m.session_id = #{sessionId} ORDER BY m.send_time, m.id")
    List<ChatMessage> listBySession(@Param("sessionId") Integer sessionId);

    /* 该会话第一条客服回复的时间（用于校准 first_response_sec） */
    @Select("SELECT MIN(send_time) FROM tbl_chat_message WHERE session_id = #{sessionId} AND sender = 'merchant'")
    String firstMerchantTime(@Param("sessionId") Integer sessionId);
}
