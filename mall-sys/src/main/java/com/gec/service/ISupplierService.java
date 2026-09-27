package com.gec.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gec.domain.entity.Supplier;

public interface ISupplierService extends IService<Supplier> {
    IPage<Supplier> listSupplier(Page page, Supplier param);
}

