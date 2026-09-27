package com.gec.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gec.dao.SaleStrategyMapper;
import com.gec.domain.entity.SaleStrategy;
import com.gec.domain.search.SaleStrategySearch;
import com.gec.service.ISaleStrategyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SaleStrategyServiceImpl
        extends ServiceImpl<SaleStrategyMapper, SaleStrategy>
        implements ISaleStrategyService {

    @Autowired
    private SaleStrategyMapper saleStrategyMapper;

    @Override
    public IPage<SaleStrategy> listStrategy(Page page, SaleStrategySearch param) {
        /*1.创建条件查询器*/
        QueryWrapper<SaleStrategy> QW = new QueryWrapper<>();
        /*2.动态设置查询条件*/
        if (param.getStrategyName() != null) {
            QW.like("strategy_name", param.getStrategyName());
        }
        if (param.getStrategyType() != null) {
            QW.eq("strategy_type", param.getStrategyType());
        }
        /*3.按创建时间倒序*/
        QW.orderByDesc("create_date");
        /*4.分页查询*/
        return saleStrategyMapper.selectPage(page, QW);
    }

    @Override
    public void saveStrategy(SaleStrategy st) {
        /*1.没有ID表示新增*/
        if (st.getId() == null) {
            if (!save(st)) {
                throw new RuntimeException("新增销售策略失败");
            }
        } else {
            /*2.有ID表示修改*/
            if (!updateById(st)) {
                throw new RuntimeException("修改销售策略失败");
            }
        }
    }

    @Override
    public void deleteStrategy(Integer id) {
        if (!removeById(id)) {
            throw new RuntimeException("删除销售策略失败");
        }
    }

    @Override
    public void updateStatus(SaleStrategy st) {
        UpdateWrapper<SaleStrategy> UW = new UpdateWrapper<>();
        UW.eq("id", st.getId()).set("status", st.getStatus());
        if (saleStrategyMapper.update(null, UW) == 0) {
            throw new RuntimeException("策略状态更新失败");
        }
    }
}
