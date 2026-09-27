package com.gec.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName(value = "tbl_logistics")
@EqualsAndHashCode(callSuper = false)
public class Logistics extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer orderId;    //order_id
    private String company;    //company
    private String trackingNo;    //tracking_no
    private String shipTime;    //ship_time
    private String receiveTime;    //receive_time
    private Integer status;    //status
    private String province;    //province 省
    private String city;    //city 市

    /* 检索透传字段：订单号（模糊匹配 tbl_order_info.order_no，非表列） */
    @TableField(exist = false)
    private String orderNo;
}
