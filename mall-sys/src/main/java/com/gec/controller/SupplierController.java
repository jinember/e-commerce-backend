package com.gec.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gec.components.FileTemplate;
import com.gec.domain.entity.Supplier;
import com.gec.service.ISupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/Supplier")
public class SupplierController extends BaseController {

    @Autowired
    private ISupplierService supplierService;

    @Autowired
    private FileTemplate fileTemplate;
    @Override
    protected FileTemplate getFileTemplate() { return fileTemplate; }

    @PostMapping("/list/{page}/{limit}")
    public R list(@PathVariable("page") Integer page, @PathVariable("limit") Integer limit, @RequestBody Supplier param) {
        Page pageObj = newPage(page, limit);
        IPage<Supplier> retPage = supplierService.listSupplier(pageObj, param);
        return R.convertPage(retPage);
    }

    @PostMapping("/addSupplier")
    public R add(@RequestBody Supplier obj) {
        boolean ret = supplierService.save(obj);
        if (!ret) throw new RuntimeException("添加失败");
        return R.ok();
    }

    @PutMapping("/updateSupplier")
    public R update(@RequestBody Supplier obj) {
        boolean ret = supplierService.updateById(obj);
        if (!ret) throw new RuntimeException("更新失败");
        return R.ok();
    }

    @DeleteMapping("/deleteSupplier/{id}")
    public R delete(@PathVariable("id") Integer id) {
        boolean ret = supplierService.removeById(id);
        if (!ret) throw new RuntimeException("删除失败");
        return R.ok();
    }

    /* 查全部供应商(供下拉) */
    @GetMapping("/all")
    public R all() {
        return R.ok(supplierService.list());
    }

    @ExceptionHandler
    public void exceptionHandler(Exception e, HttpServletResponse resp) { R.err(e).write(resp); }
}



