package com.gec.service.impl;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gec.dao.AddressDao;
import com.gec.domain.entity.Address;
import com.gec.service.IAddressService;
import org.springframework.stereotype.Service;

@Service
public class AddressServiceImpl extends ServiceImpl<AddressDao, Address> implements IAddressService {

    @Override
    public IPage<Address> listAddress(Page<Address> page) {
        return baseMapper.selectPageWithMember(page);
    }
}