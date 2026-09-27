package com.gec.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gec.domain.entity.SkuInfo;
import com.gec.domain.search.SkuSearch;
import com.gec.domain.vo.SkuVO;
import org.apache.ibatis.annotations.Param;

public interface SkuInfoMapper
    extends BaseMapper<SkuInfo> {

    /*
    * 分页查询 SKU 列表。
    * 联表：分类名称、品牌名称、默认图片。
    */
    Page<SkuVO> selectSkuPage(
            @Param("page") Page<SkuVO> page,
            @Param("param") SkuSearch param );

}
