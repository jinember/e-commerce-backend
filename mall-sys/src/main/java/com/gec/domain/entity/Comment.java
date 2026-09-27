package com.gec.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName(value = "tbl_comment")
@EqualsAndHashCode(callSuper = false)
public class Comment extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer orderId;    //order_id
    private Integer spuId;    //spu_id
    private Integer memberId;    //member_id
    private String content;    //content
    private Integer rating;    //rating

    @TableField(exist = false)
    private String nickname;    //关联查询：用户昵称
}
