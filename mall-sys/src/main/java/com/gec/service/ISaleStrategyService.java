package com.gec.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gec.domain.entity.SaleStrategy;
import com.gec.domain.search.SaleStrategySearch;

public interface ISaleStrategyService extends IService<SaleStrategy> {

    /* 1.销售策略分页列表(带搜索)。 */
    IPage<SaleStrategy> listStrategy(Page page, SaleStrategySearch param);

    /* 2.新增/修改销售策略。 */
    void saveStrategy(SaleStrategy st);

    /* 3.删除销售策略。 */
    void deleteStrategy(Integer id);

    /* 4.更新策略状态(启用/停用)。 */
    void updateStatus(SaleStrategy st);
}
