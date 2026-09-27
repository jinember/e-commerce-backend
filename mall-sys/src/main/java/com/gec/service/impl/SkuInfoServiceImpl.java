package com.gec.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gec.dao.SkuInfoMapper;
import com.gec.domain.entity.SkuInfo;
import com.gec.domain.search.SkuSearch;
import com.gec.domain.vo.SkuVO;
import com.gec.service.ISkuInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SkuInfoServiceImpl
    extends ServiceImpl<SkuInfoMapper, SkuInfo>
    implements ISkuInfoService {

    @Autowired
    private SkuInfoMapper skuInfoMapper;

    @Override
    public IPage<SkuVO> listSku(Page page, SkuSearch param) {
        /* 1.调用自定义分页查询(联表取分类名、品牌名、默认图片) */
        return skuInfoMapper.selectSkuPage(page, param);
    }
}
