package com.gec.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName(value = "tbl_stock")
@EqualsAndHashCode(callSuper = false)
public class Stock extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Integer id;           //id
    private String goodsName;     //商品名称
    private String skuSpec;       //SKU规格
    private Integer skuId;        //关联SKU
    private String warehouse;      //所在仓库
    private Integer stock;        //库存数量
    private Integer safeStock;    //预警阈值
    private Integer supplierId;  //主供应商ID
}
