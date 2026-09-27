package com.gec.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName(value = "tbl_supplier")
@EqualsAndHashCode(callSuper = false)
public class Supplier extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;    //name
    private String contact;    //contact
    private String phone;    //phone
    private String address;    //address
    private Integer status;    //status
}
