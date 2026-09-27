package com.gec.domain.vo;

import com.gec.domain.entity.BaseEntity;
import lombok.Data;

@Data
public class BrandVO extends BaseEntity {
    private Integer id;          //id

    private String brandName;    //brand_name
    private String logoName;      //logo_url
    private String brandDesc;    //brand_desc
    private Integer showStatus;  //show_status

    //{ps}视图属性..
    private Integer isAssociate;

}
