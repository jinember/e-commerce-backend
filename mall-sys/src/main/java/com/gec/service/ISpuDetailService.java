package com.gec.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gec.domain.entity.GoodsDetail;
import com.gec.domain.search.SpuSearch;
import com.gec.domain.vo.SpuVO;

public interface ISpuDetailService extends IService<GoodsDetail> {
    IPage<SpuVO> listSpu(Page page, SpuSearch param);
}
