package com.gec.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName(value = "tbl_order_info")
public class OrderInfo extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private String orderNo;      //order_no   订单编号
    private String userName;     //user_name  下单用户
    private Integer memberId;    //member_id  会员ID
    private Integer goodsId;     //goods_id   商品SPU-ID
    private String goodsName;    //goods_name 商品名称
    private Integer skuId;       //sku_id     SKU-ID
    private String skuName;      //sku_name   SKU名称
    private BigDecimal price;    //price      成交单价
    private Integer count;       //count      购买数量
    private BigDecimal totalPrice; //total_price 订单总价
    private Integer status;      //status     0待支付/1已支付/2已发货/3已完成/4已取消
    private String receiver;     //receiver   收货人
    private String phone;        //phone      联系电话
    private String memberLevel;  //member_level 会员等级
    private String address;      //address    收货地址
    private String remark;       //remark     备注

    /* 该订单最近一条退款记录的状态(0待审核/1已同意/2已拒绝)，无退款记录为 null。
       仅用于 C 端订单卡片展示，不落库。 */
    @TableField(exist = false)
    private Integer refundStatus;

    //create_date / update_date 由 BaseEntity 提供
}
