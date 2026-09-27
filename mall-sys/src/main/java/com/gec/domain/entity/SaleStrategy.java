package com.gec.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName(value = "tbl_sale_strategy")
public class SaleStrategy extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private String strategyName;     //strategy_name  策略名称
    private Integer strategyType;    //strategy_type  1折扣/2满减/3会员价
    private BigDecimal strategyValue;   //strategy_value 策略值(折扣率/满减金额/会员折扣)
    private BigDecimal thresholdValue;  //threshold_value 满减门槛(满XX元)
    private Integer status;          //status   0停用/1启用
    private String remark;           //remark   策略说明

    //create_date / update_date 由 BaseEntity 提供
}
