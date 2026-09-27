package com.gec.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gec.domain.bo.UserBO;
import com.gec.domain.entity.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface UserMapper extends BaseMapper<User> {

    Page<UserBO> getList(
       Page page,
       @Param("param") Map<String, Object> param );

    /* 根据用户ID查其角色ID(取第一个) */
    @Select("SELECT role_id FROM tbl_user_role WHERE user_id = #{userId} LIMIT 1")
    Integer selectRoleIdByUserId(@Param("userId") Integer userId);

    /* 根据用户ID查其全部角色ID（多角色） */
    @Select("SELECT role_id FROM tbl_user_role WHERE user_id = #{userId} ORDER BY id")
    List<Integer> selectRoleIdsByUserId(@Param("userId") Integer userId);

}
