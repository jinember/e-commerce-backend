package com.gec.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gec.domain.entity.GoodsDetail;
import com.gec.domain.search.SpuSearch;
import com.gec.domain.vo.SpuVO;
import org.apache.ibatis.annotations.Param;

public interface SpuDetailMapper
     extends BaseMapper<GoodsDetail> {

    /*
    * 分页查询商品(SPU)列表。
    * 联表：分类名称、品牌名称。
    */
    Page<SpuVO> selectSpuPage(
            @Param("page") Page<SpuVO> page,
            @Param("param") SpuSearch param );

}
