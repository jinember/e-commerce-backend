package com.gec.dao;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gec.domain.entity.Address;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AddressDao extends BaseMapper<Address> {

    @Select("SELECT a.*, m.nickname FROM tbl_address a " +
            "LEFT JOIN tbl_member m ON a.member_id = m.id " +
            "ORDER BY a.id DESC")
    IPage<Address> selectPageWithMember(Page<Address> page);
}