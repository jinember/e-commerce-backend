package com.gec.domain.vo;

import com.gec.domain.entity.GoodsDetail;
import lombok.Data;
import lombok.EqualsAndHashCode;

/*
*  商品(SPU)列表展示对象。
*  除了实体本身的字段外，
*  额外附带分类名称、品牌名称。
*/
@Data
@EqualsAndHashCode(callSuper = false)
public class SpuVO extends GoodsDetail {
    private String categoryName;
    private String brandName;
}
