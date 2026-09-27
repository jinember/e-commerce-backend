package com.gec.domain.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SkuLineVO
    implements java.io.Serializable {
    private Boolean selected;      // 是否勾选该组合
    private String skuName;        // 商品名称(组合名)
    private String skuTitle;       // 标题
    private String skuSubtitle;    // 副标题
    private Double price;          // 价格
    private List<SkuAttrItem> attrList = new ArrayList<>();  // 该行销售属性值
    private String images;         // 图片集(分号分隔)
    private String defaultImage;   // 默认图
    private DiscountForm discountForm;  // 该行独立的折扣/满减/会员价
}
