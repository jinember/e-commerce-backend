package com.gec.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gec.dao.SupplierMapper;
import com.gec.domain.entity.Supplier;
import com.gec.service.ISupplierService;
import org.springframework.stereotype.Service;

@Service
public class SupplierServiceImpl
        extends ServiceImpl<SupplierMapper, Supplier>
        implements ISupplierService {

    @Override
    public IPage<Supplier> listSupplier(Page page, Supplier param) {
        QueryWrapper<Supplier> QW = new QueryWrapper<>();
        if (param.getName() != null) {
            QW.like("name", String.valueOf(param.getName()));
        }
        if (param.getContact() != null) {
            QW.like("contact", String.valueOf(param.getContact()));
        }
        if (param.getStatus() != null) {
            QW.like("status", String.valueOf(param.getStatus()));
        }
        return baseMapper.selectPage(page, QW);
    }
}

