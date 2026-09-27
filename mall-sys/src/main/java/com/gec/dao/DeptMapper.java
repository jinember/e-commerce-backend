package com.gec.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gec.domain.entity.Dept;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface DeptMapper extends BaseMapper<Dept> {

    //{1}获取某部门的用户数量. (where 前要有空格)
    @Select("SELECT count(*) cnt FROM tbl_user u" +
            " WHERE u.dept_id = #{deptId}")
    int getUserCount(@Param("deptId") Integer deptId);

    //{2}获取子部门的数量 (某个部门下有几个 "一级" 下属部门)
    //   (where 前要有空格)
    @Select("SELECT count(*) FROM tbl_dept" +
            " WHERE parent_id = #{deptId}")
    int getSubDeptCount(@Param("deptId") Integer deptId);

    //{3} Dashboard 用：一次性统计各部门的人数（左连接，人数为 0 的部门也要出现）
    @Select("SELECT d.dept_name AS deptName, COUNT(u.id) AS userCount " +
            "FROM tbl_dept d LEFT JOIN tbl_user u ON u.dept_id = d.id " +
            "GROUP BY d.id, d.dept_name ORDER BY userCount DESC")
    List<Map<String, Object>> statUserCount();

    //List<Dept> getList();
}







