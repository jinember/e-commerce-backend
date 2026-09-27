package com.gec.domain.vo;

import com.gec.domain.entity.SkuInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/*
*  SKU 列表展示对象。
*  额外附带分类名称、品牌名称、默认图片。
*/
@Data
@EqualsAndHashCode(callSuper = false)
public class SkuVO extends SkuInfo {
    private String categoryName;
    private String brandName;
    private String defaultImage;
}
