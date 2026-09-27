package com.gec.domain.search;

import lombok.Data;

/*
*  SKU 搜索条件。
*  [1]skuId
*  [2]spuId       所属商品ID
*  [3]skuName     SKU名称
*  [4]categoryId  分类ID
*  [5]brandId     品牌ID
*  [6]priceMin    价格下限
*  [7]priceMax    价格上限
*/
@Data
public class SkuSearch {
    private Integer skuId;
    private Integer spuId;
    private String skuName;
    private Integer categoryId;
    private Integer brandId;
    private Double priceMin;
    private Double priceMax;
}
