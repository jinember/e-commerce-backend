package com.gec.dao;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gec.domain.entity.Invoice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface InvoiceDao extends BaseMapper<Invoice> {

    @Select("SELECT i.*, m.nickname FROM tbl_invoice i " +
            "LEFT JOIN tbl_member m ON i.member_id = m.id " +
            "ORDER BY i.id DESC")
    IPage<Invoice> selectPageWithMember(Page<Invoice> page);
}