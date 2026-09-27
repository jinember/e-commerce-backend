package com.gec.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName(value = "tbl_payment")
@EqualsAndHashCode(callSuper = false)
public class Payment extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer orderId;    //order_id
    private String payType;    //pay_type
    private Double payAmount;    //pay_amount
    private String payTime;    //pay_time
    private Integer payStatus;    //pay_status
    private String transactionNo;    //transaction_no

    /* 检索透传字段：订单号（模糊匹配 tbl_order_info.order_no，非表列） */
    @TableField(exist = false)
    private String orderNo;
}
