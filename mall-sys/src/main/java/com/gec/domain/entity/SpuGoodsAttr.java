package com.gec.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName(value="tbl_spu_goods_attr")
public class SpuGoodsAttr
    implements java.io.Serializable {
    @TableId(type= IdType.AUTO)
    private Integer id;
    private Integer spuId;
    private Integer attrId;
    private String attrName;
    private String attrValue;
    private Integer attrSort;
}
