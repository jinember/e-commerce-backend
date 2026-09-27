package com.gec.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gec.domain.entity.ChatSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ChatSessionDao extends BaseMapper<ChatSession> {

    /* 会话列表：带会员昵称 + 处理客服姓名；支持按 id/会话编号/会员昵称/咨询类型 检索 */
    @Select("<script>SELECT c.*, u.nickname, s.nick_name AS staffName FROM tbl_chat_session c " +
            "LEFT JOIN tbl_member u ON c.member_id = u.id " +
            "LEFT JOIN tbl_user s ON s.id = c.staff_id " +
            "<where>" +
            "  <if test='id != null'>AND c.id = #{id}</if>" +
            "  <if test='chatNo != null and chatNo != \"\"'>AND c.chat_no LIKE CONCAT('%', #{chatNo}, '%')</if>" +
            "  <if test='nickname != null and nickname != \"\"'>AND u.nickname LIKE CONCAT('%', #{nickname}, '%')</if>" +
            "  <if test='chatType != null and chatType != \"\"'>AND c.chat_type LIKE CONCAT('%', #{chatType}, '%')</if>" +
            "</where>" +
            "ORDER BY c.start_time DESC</script>")
    IPage<ChatSession> selectPageWithMember(Page<ChatSession> page,
            @Param("id") Integer id, @Param("chatNo") String chatNo,
            @Param("nickname") String nickname, @Param("chatType") String chatType);

    /* 待回复会话：最后一条消息是会员发的（含申请转人工），商家还没回 */
    @Select("SELECT c.*, u.nickname, s.nick_name AS staffName FROM tbl_chat_session c " +
            "LEFT JOIN tbl_member u ON c.member_id = u.id " +
            "LEFT JOIN tbl_user s ON s.id = c.staff_id " +
            "WHERE EXISTS (SELECT 1 FROM tbl_chat_message m WHERE m.id = " +
            "  (SELECT MAX(x.id) FROM tbl_chat_message x WHERE x.session_id = c.id) AND m.sender = 'user') " +
            "ORDER BY c.end_time DESC")
    IPage<ChatSession> selectPending(Page<ChatSession> page);

    /* 待回复会话数 */
    @Select("SELECT COUNT(*) FROM tbl_chat_session c WHERE EXISTS (SELECT 1 FROM tbl_chat_message m " +
            "WHERE m.id = (SELECT MAX(x.id) FROM tbl_chat_message x WHERE x.session_id = c.id) " +
            "AND m.sender = 'user')")
    Integer countPending();

    /* 会话质量看板：按咨询类型汇总 */
    @Select("SELECT chat_type AS chatType, COUNT(*) AS cnt, " +
            "ROUND(IFNULL(SUM(is_converted),0)*100.0/COUNT(*),2) AS convertRate " +
            "FROM tbl_chat_session GROUP BY chat_type")
    java.util.List<java.util.Map<String, Object>> statByChatType();

    /* ============ 客服聊天工作台 ============ */

    /* 聊天列表：按最后一条消息时间倒序，带会员昵称、最后消息、未读数（会员新消息 > 客服已读时间） */
    @Select("SELECT c.*, u.nickname, s.nick_name AS staffName, " +
            "  (SELECT m.content FROM tbl_chat_message m WHERE m.session_id = c.id ORDER BY m.id DESC LIMIT 1) AS lastMsg, " +
            "  (SELECT m.send_time FROM tbl_chat_message m WHERE m.session_id = c.id ORDER BY m.id DESC LIMIT 1) AS lastTime, " +
            "  (SELECT m.sender FROM tbl_chat_message m WHERE m.session_id = c.id ORDER BY m.id DESC LIMIT 1) AS lastSender, " +
            "  (SELECT COUNT(*) FROM tbl_chat_message m WHERE m.session_id = c.id AND m.sender = 'user' " +
            "     AND (c.staff_read_time IS NULL OR m.send_time > c.staff_read_time)) AS unread " +
            "FROM tbl_chat_session c " +
            "LEFT JOIN tbl_member u ON c.member_id = u.id " +
            "LEFT JOIN tbl_user s ON s.id = c.staff_id " +
            "ORDER BY (SELECT m.id FROM tbl_chat_message m WHERE m.session_id = c.id ORDER BY m.id DESC LIMIT 1) DESC")
    IPage<ChatSession> selectChatList(Page<ChatSession> page);

    /* 未读会话总数（存在会员新消息且客服未读） */
    @Select("SELECT COUNT(*) FROM tbl_chat_session c WHERE EXISTS ( " +
            "  SELECT 1 FROM tbl_chat_message m WHERE m.session_id = c.id AND m.sender = 'user' " +
            "  AND (c.staff_read_time IS NULL OR m.send_time > c.staff_read_time))")
    Integer countUnreadSessions();

    /* 客服标记会话已读 */
    @Update("UPDATE tbl_chat_session SET staff_read_time = NOW() WHERE id = #{id}")
    int markRead(@Param("id") Integer id);
}
