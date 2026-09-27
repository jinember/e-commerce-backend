package com.gec.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gec.dao.RoleMapper;
import com.gec.domain.entity.Role;
import com.gec.service.IRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
public class RoleServiceImpl
    extends ServiceImpl<RoleMapper, Role>
    implements IRoleService {	
    /*-- 请填入代码 --*/
    @Autowired
    private RoleMapper roleMapper;
    @Override
    public void addUserRoleAssociation(
        Integer userId, Integer roleId) {
        /*-- 请填入代码 --*/
//        1.把原先关联移除掉。
        removeUserRoleAssociation(userId);
//        2.在建立新的关联。
        int cnt = roleMapper.addUserRoleAssociation(userId,roleId);
        if(cnt != 1){
            throw new RuntimeException("设置用户-角色关联失败");
        }
    }
    @Override
    public void addUserRoleAssociations(Integer userId, List<Integer> roleIds) {
//        1.先移除该用户全部旧关联
        removeUserRoleAssociation(userId);
//        2.批量插入新关联（去重、跳过 null）
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        java.util.Set<Integer> distinct = new java.util.LinkedHashSet<>(roleIds);
        for (Integer rid : distinct) {
            if (rid == null) continue;
            int cnt = roleMapper.addUserRoleAssociation(userId, rid);
            if (cnt != 1) {
                throw new RuntimeException("设置用户-角色关联失败，roleId=" + rid);
            }
        }
    }
    @Override
    public void removeUserRoleAssociation(Integer userId) {
        /*-- 请填入代码 --*/
        roleMapper.removeUserRoleAssociation(userId);
    }
    @Override
    public IPage<Role> listRole(
        Page page, Map<String, Object> data) {
        return null;
    }
    @Override
    public void deleteRole(Integer id) { }
    @Override
    public void addRole(Role role) { }
    @Override
    public void updateRole(Role role) { }
    @Override
    public Role getRole(Integer id) { return null; }
}
