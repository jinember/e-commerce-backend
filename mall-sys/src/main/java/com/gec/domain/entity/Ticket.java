package com.gec.domain.entity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName(value = "tbl_ticket")
@EqualsAndHashCode(callSuper = false)
public class Ticket extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer memberId;
    private Integer orderId;
    private String title;
    private String content;
    private String type;
    private String status;
    private String reply;

    @TableField(exist = false)
    private String nickname;    //关联查询：用户昵称
}