package com.gec.domain.search;

import lombok.Data;

/*
*  商品(SPU)搜索条件。
*  [1]id
*  [2]goodsName     商品名称
*  [3]categoryId    分类ID
*  [4]brandId       品牌ID
*  [5]status        上架状态(0默认 1上架 2下架)
*/
@Data
public class SpuSearch {
    private Integer id;
    private String goodsName;
    private Integer categoryId;
    private Integer brandId;
    private Integer status;
}
