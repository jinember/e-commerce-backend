package com.gec.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gec.dao.SpuDetailMapper;
import com.gec.domain.entity.GoodsDetail;
import com.gec.domain.search.SpuSearch;
import com.gec.domain.vo.SpuVO;
import com.gec.service.ISpuDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SpuDetailServiceImpl
    extends ServiceImpl<SpuDetailMapper, GoodsDetail>
    implements ISpuDetailService {

    @Autowired
    private SpuDetailMapper spuDetailMapper;

    @Override
    public IPage<SpuVO> listSpu(Page page, SpuSearch param) {
        /* 1.调用自定义分页查询(联表取分类名、品牌名) */
        return spuDetailMapper.selectSpuPage(page, param);
    }
}
