package com.gec.domain.entity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.Date;

@Data
@TableName(value = "tbl_sign_in")
@EqualsAndHashCode(callSuper = false)
public class SignIn extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer memberId;
    private Date signDate;
    private Integer continuousDays;
    private Integer points;

    @TableField(exist = false)
    private String nickname;    //关联查询：用户昵称
}