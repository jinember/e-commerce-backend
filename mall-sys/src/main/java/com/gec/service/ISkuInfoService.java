package com.gec.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gec.domain.entity.SkuInfo;
import com.gec.domain.search.SkuSearch;
import com.gec.domain.vo.SkuVO;

public interface ISkuInfoService extends IService<SkuInfo> {
    IPage<SkuVO> listSku(Page page, SkuSearch param);
}
