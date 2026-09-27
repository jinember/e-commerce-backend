package com.gec.domain.bo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class UserBO {
    private Integer id;
    private String account;
    private String password;
    private String nickName;
    private String phone;
    private String sex;
    private String no;
    private String email;
    private Integer deptId;
    private String deptName;
    private Integer roleId;
    private String roleName;
    /* 该用户绑定的全部角色 ID，逗号分隔（多角色回显用） */
    private String roleIds;

    @JsonFormat(pattern="yyyy-MM-dd")
    private Date createDate;
}


