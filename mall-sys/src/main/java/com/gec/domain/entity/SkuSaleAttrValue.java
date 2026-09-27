package com.gec.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName(value="tbl_sku_sale_attr_value")
public class SkuSaleAttrValue
    implements java.io.Serializable {
    @TableId(type= IdType.AUTO)
    private Integer id;
    private Integer spuId;
    private Integer skuId;
    private Integer attrId;
    private String attrName;
    private String attrValue;
    private Integer attrSort;
}
