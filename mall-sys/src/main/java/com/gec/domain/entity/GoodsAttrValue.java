package com.gec.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName(value="tbl_goods_attr_value")
@EqualsAndHashCode(callSuper = false)
public class GoodsAttrValue {
    @TableId(type= IdType.AUTO)
    private Integer id;         //id
    private Integer spuId;      //spu_id
    private Integer attrId;     //attr_id
    private Integer valueType;  //value_type
    private String attrValue;   //attr_value

    /* 缓存传输用，不落库(写 spu_goods_attr / sku_sale_attr_value 时需要属性名) */
    @TableField(exist = false)
    private String attrName;    //attr_name


}
