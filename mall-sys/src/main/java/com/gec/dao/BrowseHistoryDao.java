package com.gec.dao;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gec.domain.entity.BrowseHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface BrowseHistoryDao extends BaseMapper<BrowseHistory> {

    @Select("SELECT b.*, m.nickname FROM tbl_browse_history b " +
            "LEFT JOIN tbl_member m ON b.member_id = m.id " +
            "ORDER BY b.id DESC")
    IPage<BrowseHistory> selectPageWithMember(Page<BrowseHistory> page);
}