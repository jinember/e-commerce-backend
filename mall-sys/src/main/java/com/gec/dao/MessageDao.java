package com.gec.dao;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gec.domain.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface MessageDao extends BaseMapper<Message> {

    @Select("SELECT m.*, u.nickname FROM tbl_message m " +
            "LEFT JOIN tbl_member u ON m.member_id = u.id " +
            "ORDER BY m.id DESC")
    IPage<Message> selectPageWithMember(Page<Message> page);
}