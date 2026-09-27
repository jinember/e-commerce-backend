package com.gec.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gec.components.FileTemplate;
import com.gec.domain.entity.Cart;
import com.gec.service.ICartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/Cart")
public class CartController extends BaseController {

    @Autowired
    private ICartService cartService;

    @Autowired
    private FileTemplate fileTemplate;
    @Override
    protected FileTemplate getFileTemplate() { return fileTemplate; }

    @PostMapping("/list/{page}/{limit}")
    public R list(@PathVariable("page") Integer page, @PathVariable("limit") Integer limit, @RequestBody Cart param) {
        Page pageObj = newPage(page, limit);
        IPage<Cart> retPage = cartService.listCart(pageObj, param);
        return R.convertPage(retPage);
    }

    @PostMapping("/addCart")
    public R add(@RequestBody Cart obj) {
        boolean ret = cartService.save(obj);
        if (!ret) throw new RuntimeException("添加失败");
        return R.ok();
    }

    @PutMapping("/updateCart")
    public R update(@RequestBody Cart obj) {
        boolean ret = cartService.updateById(obj);
        if (!ret) throw new RuntimeException("更新失败");
        return R.ok();
    }

    @DeleteMapping("/deleteCart/{id}")
    public R delete(@PathVariable("id") Integer id) {
        boolean ret = cartService.removeById(id);
        if (!ret) throw new RuntimeException("删除失败");
        return R.ok();
    }

    @ExceptionHandler
    public void exceptionHandler(Exception e, HttpServletResponse resp) { R.err(e).write(resp); }
}



