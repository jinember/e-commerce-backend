package com.gec.domain.vo;

import lombok.Data;

@Data
public class SkuAttrItem
    implements java.io.Serializable {
    private Integer attrId;     //attr_id
    private String attrName;    //attr_name
    private String attrValue;   //attr_value
}
