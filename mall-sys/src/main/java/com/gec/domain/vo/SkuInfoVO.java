package com.gec.domain.vo;

import lombok.Data;

import java.util.List;

@Data
public class SkuInfoVO
    implements java.io.Serializable {
    private String pubKey;
    private List<SkuLineVO> skuList;
}
