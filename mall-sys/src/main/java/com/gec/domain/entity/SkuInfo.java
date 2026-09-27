package com.gec.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName(value="tbl_sku_info")
public class SkuInfo
    implements java.io.Serializable {
    @TableId(type= IdType.AUTO)
    private Integer skuId;
    private Integer spuId;
    private String skuName;
    private String skuDesc;
    private Integer categoryId;
    private Integer brandId;
    private String skuTitle;
    private String skuSubtitle;
    private Double price;
    private Integer saleCount;
}
