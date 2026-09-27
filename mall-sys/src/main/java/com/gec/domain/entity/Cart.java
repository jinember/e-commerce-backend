package com.gec.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName(value = "tbl_cart")
@EqualsAndHashCode(callSuper = false)
public class Cart extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer memberId;    //member_id
    private Integer spuId;    //spu_id
    private Integer skuId;    //sku_id
    private Integer quantity;    //quantity
    private Integer selected;    //selected
    private String addTime;    //add_time
}
