package com.gec.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName(value = "tbl_point_log")
@EqualsAndHashCode(callSuper = false)
public class PointLog extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer memberId;    //member_id
    private Integer orderId;    //order_id
    private String changeType;    //change_type
    private Integer points;    //points
    private Integer balance;    //balance
    private String remark;    //remark

    @TableField(exist = false)
    private String nickname;    //关联查询：用户昵称
}
