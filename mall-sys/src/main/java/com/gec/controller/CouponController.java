package com.gec.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gec.components.FileTemplate;
import com.gec.domain.entity.Coupon;
import com.gec.service.ICouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/Coupon")
public class CouponController extends BaseController {

    @Autowired
    private ICouponService couponService;

    @Autowired
    private FileTemplate fileTemplate;
    @Override
    protected FileTemplate getFileTemplate() { return fileTemplate; }

    @PostMapping("/list/{page}/{limit}")
    public R list(@PathVariable("page") Integer page, @PathVariable("limit") Integer limit, @RequestBody Coupon param) {
        Page pageObj = newPage(page, limit);
        IPage<Coupon> retPage = couponService.listCoupon(pageObj, param);
        return R.convertPage(retPage);
    }

    @PostMapping("/addCoupon")
    public R add(@RequestBody Coupon obj) {
        boolean ret = couponService.save(obj);
        if (!ret) throw new RuntimeException("添加失败");
        return R.ok();
    }

    @PutMapping("/updateCoupon")
    public R update(@RequestBody Coupon obj) {
        boolean ret = couponService.updateById(obj);
        if (!ret) throw new RuntimeException("更新失败");
        return R.ok();
    }

    @DeleteMapping("/deleteCoupon/{id}")
    public R delete(@PathVariable("id") Integer id) {
        boolean ret = couponService.removeById(id);
        if (!ret) throw new RuntimeException("删除失败");
        return R.ok();
    }

    @ExceptionHandler
    public void exceptionHandler(Exception e, HttpServletResponse resp) { R.err(e).write(resp); }
}



