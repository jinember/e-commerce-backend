package com.gec.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gec.domain.entity.GoodsAttrValue;
import com.gec.domain.entity.GoodsDetail;
import com.gec.domain.vo.GoodsAttrValuesVO;
import com.gec.domain.vo.GoodsBaseInfoVO;
import com.gec.domain.vo.SkuInfoVO;
import com.gec.domain.vo.SkuLineVO;

import java.util.List;

public interface IGoodsDetailService
    extends IService<GoodsDetail> {

    String saveBaseInfoCache(GoodsBaseInfoVO biVO);

    String saveGoodsAttrValCache(
        GoodsAttrValuesVO gavVO);

    GoodsBaseInfoVO getBaseInfoCache(String pubKey);

    List<GoodsAttrValue> getGoodsAttrValCache(String pubKey);

    String saveGoodsSaleAttrCache(
        GoodsAttrValuesVO gavVO);

    List<GoodsAttrValue> getGoodsSaleAttrCache(String pubKey);

    /* 【07】保存SKU信息缓存。 */
    String saveSkuInfoCache(SkuInfoVO skuVO);

    /* 【08】读取SKU信息缓存(回显用)。 */
    List<SkuLineVO> getSkuInfoCache(String pubKey);

    /* 【09】发布商品：写入数据库 + 清理Redis缓存。 */
    Integer publishGoods(String pubKey);

}
