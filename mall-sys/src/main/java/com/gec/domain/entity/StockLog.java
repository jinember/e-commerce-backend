package com.gec.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName(value = "tbl_stock_log")
@EqualsAndHashCode(callSuper = false)
public class StockLog extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer stockId;    //stock_id
    private String changeType;    //change_type
    private Integer quantity;    //quantity
    private String remark;    //remark
    private String operator;    //operator
    private Integer supplierId;  //本次出入库供应商ID
}
