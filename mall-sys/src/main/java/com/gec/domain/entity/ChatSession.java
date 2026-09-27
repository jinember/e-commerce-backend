package com.gec.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 客服会话（客服工作台 / 会话质量分析）
 * 对应表 tbl_chat_session
 */
@Data
@TableName(value = "tbl_chat_session")
@EqualsAndHashCode(callSuper = false)
public class ChatSession extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String chatNo;              // 会话编号
    private Integer memberId;
    private Integer spuId;
    private Integer categoryId;
    private String sessionId;           // 会话内嵌点 ID，与 tbl_user_behavior.session_id 对应
    private String chatType;            // 商品咨询/物流查询/退换货/售后服务/优惠咨询/投诉建议/其他
    private Integer userMsgCount;       // 用户发言数
    private Integer merchantReplyCount; // 客服回复数（人工，代表商家行为）
    private Integer botReplyCount;      // 智能客服应答数（不算商家回复）
    private Integer firstResponseSec;   // 首次「人工」响应时长(秒)
    private Integer sessionDurationSec; // 会话时长(秒)
    private Integer isConverted;        // 是否转化 1是 0否
    private String convertAction;       // 转化动作 下单/加购/收藏
    private String startTime;
    private String endTime;
    private Integer staffId;            // 处理客服ID(tbl_user.id)
    private String staffReadTime;       // 客服最后已读时间（未读计算基准）

    @TableField(exist = false)
    private String nickname;    // 关联查询：会员昵称
    @TableField(exist = false)
    private String staffName;   // 关联查询：客服姓名
    @TableField(exist = false)
    private String lastMsg;     // 聊天列表：最后一条消息内容
    @TableField(exist = false)
    private String lastTime;    // 聊天列表：最后一条消息时间
    @TableField(exist = false)
    private String lastSender;  // 聊天列表：最后一条消息发送方
    @TableField(exist = false)
    private Integer unread;     // 聊天列表：该会话未读数（会员新消息数）
}
