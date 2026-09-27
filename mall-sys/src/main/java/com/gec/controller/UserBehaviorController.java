package com.gec.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gec.components.FileTemplate;
import com.gec.domain.entity.UserBehavior;
import com.gec.service.IUserBehaviorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/UserBehavior")
public class UserBehaviorController extends BaseController {

    @Autowired
    private IUserBehaviorService userbehaviorService;

    @Autowired
    private FileTemplate fileTemplate;
    @Override
    protected FileTemplate getFileTemplate() { return fileTemplate; }

    @PostMapping("/list/{page}/{limit}")
    public R list(@PathVariable("page") Integer page, @PathVariable("limit") Integer limit, @RequestBody UserBehavior param) {
        Page pageObj = newPage(page, limit);
        IPage<UserBehavior> retPage = userbehaviorService.listUserBehavior(pageObj, param);
        return R.convertPage(retPage);
    }

    @PostMapping("/addUserBehavior")
    public R add(@RequestBody UserBehavior obj) {
        boolean ret = userbehaviorService.save(obj);
        if (!ret) throw new RuntimeException("添加失败");
        return R.ok();
    }

    @PutMapping("/updateUserBehavior")
    public R update(@RequestBody UserBehavior obj) {
        boolean ret = userbehaviorService.updateById(obj);
        if (!ret) throw new RuntimeException("更新失败");
        return R.ok();
    }

    @DeleteMapping("/deleteUserBehavior/{id}")
    public R delete(@PathVariable("id") Integer id) {
        boolean ret = userbehaviorService.removeById(id);
        if (!ret) throw new RuntimeException("删除失败");
        return R.ok();
    }

    @ExceptionHandler
    public void exceptionHandler(Exception e, HttpServletResponse resp) { R.err(e).write(resp); }
}



