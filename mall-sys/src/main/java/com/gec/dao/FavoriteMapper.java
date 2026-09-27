package com.gec.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gec.domain.entity.Favorite;
import org.apache.ibatis.annotations.Select;

public interface FavoriteMapper extends BaseMapper<Favorite> {

    @Select("<script>" +
            "SELECT f.*, m.nickname FROM tbl_favorite f " +
            "LEFT JOIN tbl_member m ON f.member_id = m.id " +
            "<where>" +
            "  <if test='param.memberId != null'> AND f.member_id = #{param.memberId} </if>" +
            "  <if test='param.spuId != null'> AND f.spu_id = #{param.spuId} </if>" +
            "</where>" +
            "ORDER BY f.id DESC" +
            "</script>")
    IPage<Favorite> selectPageWithMember(Page<Favorite> page, @org.apache.ibatis.annotations.Param("param") Favorite param);
}

