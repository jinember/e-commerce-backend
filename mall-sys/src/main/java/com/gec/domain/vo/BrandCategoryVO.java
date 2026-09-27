package com.gec.domain.vo;

import com.gec.domain.entity.Category;
import lombok.Data;

import java.util.List;

@Data
public class BrandCategoryVO {
    private Integer brandId;
    private String brandName;
    /*
    *  Category 中包含以下属性:
    *   1.String  categoryName ==> category_name(表字段)
    *   2.Integer id ==> category_id(表字段)
    */
    private List<Category> categories;
    private List<Integer> deleteIds;
}
