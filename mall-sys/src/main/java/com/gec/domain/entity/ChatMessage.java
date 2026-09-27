package com.gec.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 客服会话的对话明细（一问一答）
 * 对应表 tbl_chat_message，session_id 指向 tbl_chat_session.id
 * sender 取值：user=会员　bot=智能客服　merchant=人工客服(商家)
 */
@Data
@TableName(value = "tbl_chat_message")
@EqualsAndHashCode(callSuper = false)
public class ChatMessage extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Integer sessionId;  // 客服会话ID(tbl_chat_session.id)
    private String chatNo;      // 会话编号
    private Integer memberId;
    private String sender;      // user=会员 bot=智能客服 merchant=人工客服
    private String msgType;     // 消息类型，默认 text
    private String content;
    private String sendTime;
    private Integer staffId;    // 处理客服ID(tbl_user.id)，仅人工回复有

    @TableField(exist = false)
    private String staffName;   // 关联查询：客服姓名
}
