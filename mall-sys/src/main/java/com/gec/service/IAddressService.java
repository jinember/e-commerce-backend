package com.gec.service;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gec.domain.entity.Address;

public interface IAddressService extends IService<Address> {
    IPage<Address> listAddress(Page<Address> page);
}