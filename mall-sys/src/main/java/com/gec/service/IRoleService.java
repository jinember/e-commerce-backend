package com.gec.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gec.domain.entity.Role;

import java.util.List;
import java.util.Map;

public interface IRoleService extends IService<Role> {

    void addUserRoleAssociation(
            Integer userId, Integer roleId);
    /* 多角色绑定：先删该用户全部关联，再批量插入 */
    void addUserRoleAssociations(
            Integer userId, List<Integer> roleIds);
    void removeUserRoleAssociation(
            Integer userId);

    IPage<Role> listRole(
            Page page, Map<String, Object> data);

    void deleteRole(Integer id);

    void addRole(Role role);

    void updateRole(Role role);

    Role getRole(Integer id);

}



