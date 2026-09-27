package com.gec.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gec.dao.UserMapper;
import com.gec.domain.bo.UserBO;
import com.gec.domain.entity.User;
import com.gec.service.IRoleService;
import com.gec.service.IUserService;
import com.gec.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl
    extends ServiceImpl<UserMapper,User>
    implements IUserService {
    /*
    *  以下提示注入错误, 可以这样处理。
    *  ALT + ENTER
    */
    @Autowired
    private UserMapper userMapper;
    @Autowired                        //(打开注释)
    private IRoleService roleService; //(打开注释)

    @Override
    public IPage<UserBO> listUser(
        Page page, Map<String, Object> param ) {
		/*-- 请填入代码 --*/
//      1.调用数据查询接口，获取用户列表。
        Page<UserBO> retPage = userMapper.getList(page,param);
//      2.返回携带有分页信息的对象出去。
        return retPage;

    }

    @Override
    public void saveUser(User user, List<Integer> roleIds) {
        /*-- 请填入代码 --*/
//      0.密码入库前先哈希（已经是哈希值的说明页面回显后原样提交，不重复加密）
        if (user.getPassword() != null
                && user.getPassword().trim().length() > 0
                && !PasswordUtil.isHashed(user.getPassword())) {
            user.setPassword(PasswordUtil.hash(user.getPassword()));
        }
//      1.保存用户数据（新增与更新）【mybatis-plus内部方法】
        boolean ret = saveOrUpdate(user);
        if(!ret){
            throw new RuntimeException("保存用户失败。");
        }
        Integer userId = user.getId();
//        2.实现用户-角色的多对多关联设置（先删后插，支持多角色）
        roleService.addUserRoleAssociations(userId, roleIds);
    }

    @Override
    public User getUser(Integer id) {
        return getById(id);
    }
    @Override
    public void deleteUser(Integer id) {

    }
}
