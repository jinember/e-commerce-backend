package com.gec.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName(value = "tbl_user_behavior")
@EqualsAndHashCode(callSuper = false)
public class UserBehavior extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer memberId;    //member_id
    private String sessionId;    //session_id
    private Integer spuId;    //spu_id
    private String behaviorType;    //behavior_type
    private String searchKeyword;    //search_keyword
    private String pageUrl;    //page_url
    private Integer stayTime;    //stay_time
    private String device;    //device
    private String channel;    //channel（渠道，原混在 device 里的 mobile/APP/H5/小程序）
    private String entryPage;  //entry_page（会话入口页：搜索页/首页/分类页/购物车页/分享链接/活动页/推送落地页/订单提醒）

    @TableField(exist = false)
    private String nickname;    //关联查询：用户昵称
}
