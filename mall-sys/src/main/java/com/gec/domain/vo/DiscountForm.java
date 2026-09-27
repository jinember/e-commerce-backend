package com.gec.domain.vo;

import lombok.Data;

/* 每个 SKU 行独立的折扣/满减/会员价设置(随 skuList 一起缓存)。 */
@Data
public class DiscountForm
    implements java.io.Serializable {
    private Integer fullCount;           // 满几件
    private Double  discount;            // 打几折(如 0.8)
    private Boolean stackable;           // 折扣可叠加优惠
    private Double  fullAmount;          // 满多少元
    private Double  minusAmount;         // 减多少元
    private Boolean fullMinusStackable;  // 满减可叠加优惠
    private Double  memberGold;          // 黄金会员价
    private Double  memberDiamond;       // 钻石会员价
}
