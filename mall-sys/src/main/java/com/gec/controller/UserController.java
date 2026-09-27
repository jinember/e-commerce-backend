package com.gec.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gec.components.FileTemplate;
import com.gec.domain.entity.User;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gec.service.IUserService;
import com.gec.util.JwtUtil;
import com.gec.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import com.baomidou.mybatisplus.core.metadata.IPage;

@RestController
@RequestMapping("/User")
public class UserController extends BaseController {
    /* 1.自动装配 UserService 接口. */
    @Autowired
    private IUserService userService;
    @Autowired
    private com.gec.service.IDeptService deptService;
    @Autowired
    private com.gec.dao.UserMapper userMapper;
    @Autowired
    private com.gec.service.IRoleService roleService;

	/* 2.用户列表 list(). */
@PostMapping(
        value = "/list/{page}/{limit}",
        produces = "application/json;charset = UTF-8"
)
public R list(
        @PathVariable("page") int page,
        @PathVariable("limit") int limit,
        @RequestBody Map map){
//    1.把页码，页大小封装为一个对象。
    Page frmPage = new Page(page, limit);
    IPage retPage = null;
    try{
//        2.用户Service获取用户列表。
        retPage = userService.listUser(frmPage,map);
//        3.转retPage转为R，MVC会自动转为json格式。
        return  R.convertPage(retPage);
    }catch (Exception e){
        e.printStackTrace();
        return R.err(e);
    }
}
    /* 3.添加用户-POST请求.（多角色：roleIds 放在请求体） */
@PostMapping(
        value = "/addUser",
        produces = "application/json;charset = UTF-8"
)
public R addUser(
        @RequestBody User user
){
    try{
//        1.调用Service实现用户的添加。
        userService.saveUser(user, user.getRoleIds());
//        2.操作成功，返回一个表示成功的JSON数据。
        return R.ok();
    }catch (Exception e){
        e.printStackTrace();
        return R.err(e);
    }
}
    /* 4.更新用户-PUT请求.（多角色：roleIds 放在请求体） */
@PutMapping(
        value = "/updateUser",
        produces = "application/json;charset = UTF-8"
)
public R updateUser(
        @RequestBody User user
){
    try{
//        1.调用Service实现用户的更新。
        userService.saveUser(user, user.getRoleIds());
//        2.操作成功，返回一个表示成功的JSON数据。
        return R.ok();
    }catch (Exception e){
        e.printStackTrace();
        return R.err(e);
    }
}

    /* 5.获取用户-GET请求.*/
    @GetMapping(
            value = "/getUser/{id}",
            produces = "application/json;charset = UTF-8"
    )
public R getUser(
        @PathVariable("id")Integer id
    ){
        try{
//            1.调用Service实现用户的添加。
            User user = userService.getUser(id);
//            2.操作成功，返回一个表示成功的JSON数据。
            return R.ok(user);
        }catch (Exception e){
            e.printStackTrace();
            return R.err(e);
        }
    }
    /* 登录：密码按哈希比对，成功后签发 JWT */
    @PostMapping("/login")
    public R login(@RequestBody User user){
        if(user.getAccount() == null || user.getAccount().trim().length() == 0){
            return R.err(new RuntimeException("账号不能为空"));
        }
        // 1.先按账号查，避免把明文密码拼进 SQL 条件
        LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<>();
        qw.eq(User::getAccount, user.getAccount());
        User u = userService.getOne(qw);
        if(u == null){
            return R.err(new RuntimeException("账号或密码错误"));
        }
        // 2.哈希比对（兼容尚未迁移的历史明文密码）
        String stored = u.getPassword();
        boolean ok = PasswordUtil.isHashed(stored)
                ? PasswordUtil.matches(user.getPassword(), stored)
                : stored != null && stored.equals(user.getPassword());
        if(!ok){
            return R.err(new RuntimeException("账号或密码错误"));
        }
        u.setPassword(null);
        // 3.按用户查全部角色，合并多个角色的菜单权限（去重）
        java.util.List<Integer> roleIds = userMapper.selectRoleIdsByUserId(u.getId());
        Integer primaryRoleId = (roleIds != null && !roleIds.isEmpty()) ? roleIds.get(0) : null;
        String menuPerms = null;
        if (roleIds != null && !roleIds.isEmpty()) {
            java.util.Set<String> perms = new java.util.LinkedHashSet<>();
            for (Integer rid : roleIds) {
                com.gec.domain.entity.Role role = roleService.getById(rid);
                if (role != null && role.getMenuPerms() != null) {
                    for (String p : role.getMenuPerms().split(",")) {
                        if (p != null && p.trim().length() > 0) {
                            perms.add(p.trim());
                        }
                    }
                }
            }
            menuPerms = String.join(",", perms);
        }
        // 4.签发 token（原来是伪 token "admin-"+id，任何人都能伪造）
        java.util.Map<String,Object> claims = new java.util.HashMap<>();
        claims.put("account", u.getAccount());
        claims.put("userId", u.getId());
        claims.put("roleId", primaryRoleId);
        String token = JwtUtil.createToken(String.valueOf(u.getId()), claims);
        return R.ok(u)
                .put("token", token)
                .put("menuPerms", menuPerms)
                .put("roleId", primaryRoleId)
                .put("roleIds", roleIds);
    }

    /* 6.删除用户-DELETE请求. */
    @DeleteMapping("/delete/{id}")
    public R delete(@PathVariable("id") Integer id){
        try{
            userService.removeById(id);
            return R.ok();
        }catch (Exception e){
            e.printStackTrace();
            return R.err(e);
        }
    }

    @Override
    protected FileTemplate getFileTemplate() {
        return null;
    }
}
