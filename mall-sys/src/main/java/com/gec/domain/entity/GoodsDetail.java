package com.gec.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName(value="tbl_spu_detail")
public class GoodsDetail
    implements java.io.Serializable {
    @TableId(type= IdType.AUTO)
    private Integer id;
    private String goodsName;
    private String goodsDetails;
    private Integer categoryId;
    private Integer brandId;
    private String mainImage;
    private String weight;
    private Integer status;
    private Integer viewCount;
    private Integer commentCount;
    private Double goodRate;
    private String createDate;
    private String updateDate;

    // 非持久化：商品分类名（查询时关联填充）
    @TableField(exist = false)
    private String categoryName;

    // 非持久化：该SPU下SKU最低价
    @TableField(exist = false)
    private Double minPrice;

    // ===== 非持久化：营销联动（命中当前生效活动时填充，前端据此显示划线价与活动标签）=====
    /** 命中的活动名称，没有命中则为 null */
    @TableField(exist = false)
    private String promoName;
    /** 活动类型（限时折扣 / 拼团活动 / 秒杀活动…） */
    @TableField(exist = false)
    private String promoType;
    /** 折扣率，0.62 表示 6.2 折 */
    @TableField(exist = false)
    private Double promoDiscount;
    /** 折后最低价 = minPrice * promoDiscount */
    @TableField(exist = false)
    private Double promoMinPrice;
}
