package com.gec.domain.vo;

import com.gec.domain.entity.GoodsAttrValue;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 商品草稿快照：把发布流程暂存在 Redis 的四段数据打包，
 * 序列化成 JSON 存到 tbl_goods_draft.spu_form 字段。
 * 继续编辑/发布时再反序列化灌回 Redis。
 */
@Data
public class GoodsDraftDataVO implements Serializable {
    /* 第1步：基本信息。 */
    private GoodsBaseInfoVO baseInfo;
    /* 第2步：规格参数。 */
    private List<GoodsAttrValue> goodsAttr;
    /* 第3步：销售属性。 */
    private List<GoodsAttrValue> saleAttr;
    /* 第4步：SKU 信息。 */
    private List<SkuLineVO> skuList;
}
