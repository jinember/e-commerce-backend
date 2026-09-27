package com.gec.dao;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gec.domain.entity.Ticket;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface TicketDao extends BaseMapper<Ticket> {

    @Select("SELECT t.*, m.nickname FROM tbl_ticket t " +
            "LEFT JOIN tbl_member m ON t.member_id = m.id " +
            "ORDER BY t.id DESC")
    IPage<Ticket> selectPageWithMember(Page<Ticket> page);
}