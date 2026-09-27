package com.gec.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName(value = "tbl_refund")
@EqualsAndHashCode(callSuper = false)
public class Refund extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer orderId;    //order_id
    private String reason;    //reason
    private Double amount;    //amount
    private Integer status;    //status
    private String applyTime;    //apply_time
    private String handleTime;    //handle_time

    /* 检索透传字段：订单号（模糊匹配 tbl_order_info.order_no，非表列） */
    @TableField(exist = false)
    private String orderNo;
}
