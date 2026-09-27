package com.gec.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName(value = "tbl_promotion")
@EqualsAndHashCode(callSuper = false)
public class Promotion extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;    //name
    private String type;    //type
    private Double discount;    //discount
    private String startTime;    //start_time
    private String endTime;    //end_time
    private Integer status;    //status

    /* 【营销联动】活动关联的商品 SPU：
       NULL = 全场活动（对所有商品生效）；
       有值  = 只对这一个商品生效。
       新增一列是为了让后台配置的活动能真的作用到 C 端商品的售价上。 */
    private Integer spuId;    //spu_id
}
