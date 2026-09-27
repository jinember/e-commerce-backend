package com.gec.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.gec.domain.vo.OptionVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface OptionMapper
    extends BaseMapper<OptionVO> {

    @Select("SELECT id value, group_name label "+
            " FROM tbl_goods_attr_group "+
            " WHERE category_id=#{categoryId}" )
    List<OptionVO> groupByCategory(
        @Param("categoryId") Integer categoryId);

    @Select("SELECT id value, role_name label "+
            " FROM tbl_role ")
    List<OptionVO> roleOptions();

    @Select("SELECT bc.brand_id value, bc.brand_name label " +
            " FROM tbl_brand_category bc " +
            "WHERE bc.category_id = #{categoryId}")
    List<OptionVO> brandOptions(
            @Param("categoryId") Integer categoryId
    );

    /* 查询品牌关联的分类ID列表 */
    @Select("SELECT category_id FROM tbl_brand_category WHERE brand_id = #{brandId}")
    List<Integer> getBrandCategoryIds(@Param("brandId") Integer brandId);

    /* 删除品牌的所有旧关联 */
    @org.apache.ibatis.annotations.Delete("DELETE FROM tbl_brand_category WHERE brand_id = #{brandId}")
    void deleteBrandCategories(@Param("brandId") Integer brandId);

    /* 批量插入品牌-分类关联 */
    @org.apache.ibatis.annotations.Insert("<script>" +
        "INSERT INTO tbl_brand_category(brand_id, category_id) VALUES " +
        "<foreach collection='categoryIds' item='cid' separator=','>" +
        "(#{brandId}, #{cid})" +
        "</foreach>" +
        "</script>")
    void insertBrandCategories(@Param("brandId") Integer brandId, @Param("categoryIds") List<Integer> categoryIds);

    /* 保存品牌关联：先删后插 */
    default void saveBrandCategories(Integer brandId, List<Integer> categoryIds) {
        deleteBrandCategories(brandId);
        if (categoryIds != null && !categoryIds.isEmpty()) {
            insertBrandCategories(brandId, categoryIds);
        }
    }

}
