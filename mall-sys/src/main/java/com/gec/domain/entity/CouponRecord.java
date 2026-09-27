package com.gec.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName(value = "tbl_coupon_record")
@EqualsAndHashCode(callSuper = false)
public class CouponRecord extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer memberId;
    private Integer couponId;
    private String status;
    private String receiveTime;
    private String useTime;
    private Integer orderId;
}
