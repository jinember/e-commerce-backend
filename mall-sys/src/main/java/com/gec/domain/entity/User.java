package com.gec.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.List;

@Data
@TableName("tbl_user")
public class User extends BaseEntity {
    @TableId(type=IdType.AUTO)
    private Integer id;
    private String account;
    private String password;
    private String nickName;
    private String phone;
    private String sex;
    private String no;
    private String email;
    private Integer deptId;

    /* 透传字段：前端提交的多角色 ID 列表，不对应 tbl_user 列 */
    @TableField(exist = false)
    private List<Integer> roleIds;

}
