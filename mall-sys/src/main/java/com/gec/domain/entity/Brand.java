package com.gec.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName(value="tbl_brand")
@EqualsAndHashCode(callSuper = false)
public class Brand extends BaseEntity {
    @TableId(type= IdType.AUTO)
    private Integer id;          //id
    private String brandName;    //brand_name
    private String logoName;     //logo_url
    private String brandDesc;    //brand_desc
    private Integer showStatus;  //show_status

    //create_date
    //update_date
}
