package com.gec.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName(value="tbl_dept")
@EqualsAndHashCode(callSuper = false)
public class Dept extends BaseEntity implements Node {
    @TableId(type=IdType.AUTO)
    private Integer id;
    private String deptName;     //dept_name
    private String deptDesc;     //dept_desc
    private Integer parentId;    //parent_id

    @TableField(value="p_ids")
    private String pIds;         //p_ids
    private String menuPerms;    //menu_perms

}
