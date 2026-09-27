package com.gec.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName(value = "tbl_member")
@EqualsAndHashCode(callSuper = false)
public class Member extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Integer id;           //id
    private String nickname;      //昵称
    private String gender;        //性别
    private Integer age;          //年龄
    private String city;          //城市
    private String channel;       //注册渠道
    private String phone;         //手机号
    private String password;       //登录密码
    private String level;         //会员等级
    private String source;        //注册来源(APP/小程序/PC/H5)
    private Integer points;       //积分
    private Double balance;      //会员余额
    private String avatar;       //头像URL
    private Integer status;       //状态: 1正常 0黑名单
    private String registerTime;  //注册时间

    @TableField(exist=false)
    private Integer orderCount;   //订单数(统计)
    @TableField(exist=false)
    private Double totalSpend;    //累计消费(统计)
}
