package com.gec.domain.search;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gec.domain.entity.BaseEntity;
import lombok.Data;

@Data
public class UserSearch {
    private Integer id;
    private String account;
    private String nickName;
    private String sex;
    private String no;
    private Integer deptId;

    //2.关联 ID.
    private Integer roleId;

}
