package com.gec.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName(value = "tbl_ad")
@EqualsAndHashCode(callSuper = false)
public class Ad extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Integer id;        //id
    private String title;      //广告标题
    private String position;   //广告位置
    private String link;       //跳转链接
    private String imgUrl;     //广告图片
    private String startTime;  //投放开始时间
    private String endTime;    //投放结束时间
    private Integer sort;      //排序
    private Integer status;    //状态: 1上架 0下架
    private Integer clickCount;  //点击量
}
