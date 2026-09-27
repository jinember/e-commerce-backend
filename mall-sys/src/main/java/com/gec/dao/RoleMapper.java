package com.gec.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gec.domain.entity.Role;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

public interface RoleMapper extends BaseMapper<Role> {
//    1.写入tbl_user_role数据，建立用户与角色关联。
    @Insert("INSERT INTO tbl_user_role(user_id,role_id)"+
            "VALUES(#{userId},#{roleId})")
    int addUserRoleAssociation(
            @Param("userId") Integer userId,
            @Param("roleId") Integer roleId
    );

//    2.移除tbl_user_role中的用户与角色关联数据
    @Delete("DELETE FROM tbl_user_role "+
            "WHERE user_id = #{userId}")
    int removeUserRoleAssociation(
            @Param("userId") Integer userId
    );

}
