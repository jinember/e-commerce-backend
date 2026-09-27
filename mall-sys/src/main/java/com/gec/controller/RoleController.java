package com.gec.controller;

import com.gec.components.FileTemplate;
import com.gec.dao.OptionMapper;
import com.gec.domain.vo.OptionVO;
import com.gec.domain.entity.Role;
import com.gec.service.IRoleService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/Role")
public class RoleController extends BaseController {
    /* 1.自动装配 OptionMapper 接口. */
    @Autowired
    private OptionMapper optionMapper;
    @Autowired
    private IRoleService roleService;

    /* 2.方法一 */
    /* (请在此处填入代码) */
    @GetMapping("/roleOptions")
    public R roleOptions(){
        List<OptionVO> ops = optionMapper.roleOptions();
        return R.ok(ops);
    }

    /* 角色分页列表 */
    @GetMapping("/list/{page}/{limit}")
    public R list(@PathVariable Integer page, @PathVariable Integer limit){
        Page<Role> pg = new Page<>(page, limit);
        IPage<Role> ret = roleService.page(pg);
        return R.convertPage(ret);
    }

    /* 添加角色 */
    @PostMapping("/add")
    public R add(@RequestBody Role role){
        roleService.save(role);
        return R.ok();
    }

    /* 更新角色 */
    @PostMapping("/update")
    public R update(@RequestBody Role role){
        roleService.updateById(role);
        return R.ok();
    }

    /* 删除角色 */
    @PostMapping("/delete/{id}")
    public R delete(@PathVariable Integer id){
        roleService.removeById(id);
        return R.ok();
    }

    @Override
    protected FileTemplate getFileTemplate() {
        return null;
    }
}
