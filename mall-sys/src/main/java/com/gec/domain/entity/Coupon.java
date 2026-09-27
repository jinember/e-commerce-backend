package com.gec.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName(value = "tbl_coupon")
@EqualsAndHashCode(callSuper = false)
public class Coupon extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;    //name
    private String type;    //type
    private Double value;    //value
    private Double minAmount;    //min_amount
    private Integer totalCount;    //total_count
    private Integer usedCount;    //used_count
    private String startTime;    //start_time
    private String endTime;    //end_time
    private Integer status;    //status
}
