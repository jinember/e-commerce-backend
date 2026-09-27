package com.gec.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gec.domain.entity.PointLog;
import org.apache.ibatis.annotations.Select;

public interface PointLogMapper extends BaseMapper<PointLog> {

    @Select("SELECT p.*, m.nickname FROM tbl_point_log p " +
            "LEFT JOIN tbl_member m ON p.member_id = m.id " +
            "ORDER BY p.id DESC")
    IPage<PointLog> selectPageWithMember(Page<PointLog> page);
}

