package com.gec.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gec.components.FileTemplate;
import com.gec.domain.entity.Logistics;
import com.gec.service.ILogisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/Logistics")
public class LogisticsController extends BaseController {

    @Autowired
    private ILogisticsService logisticsService;

    @Autowired
    private FileTemplate fileTemplate;
    @Override
    protected FileTemplate getFileTemplate() { return fileTemplate; }

    @PostMapping("/list/{page}/{limit}")
    public R list(@PathVariable("page") Integer page, @PathVariable("limit") Integer limit, @RequestBody Logistics param) {
        Page pageObj = newPage(page, limit);
        IPage<Logistics> retPage = logisticsService.listLogistics(pageObj, param);
        return R.convertPage(retPage);
    }

    @PostMapping("/addLogistics")
    public R add(@RequestBody Logistics obj) {
        boolean ret = logisticsService.save(obj);
        if (!ret) throw new RuntimeException("添加失败");
        return R.ok();
    }

    @PutMapping("/updateLogistics")
    public R update(@RequestBody Logistics obj) {
        boolean ret = logisticsService.updateById(obj);
        if (!ret) throw new RuntimeException("更新失败");
        return R.ok();
    }

    @DeleteMapping("/deleteLogistics/{id}")
    public R delete(@PathVariable("id") Integer id) {
        boolean ret = logisticsService.removeById(id);
        if (!ret) throw new RuntimeException("删除失败");
        return R.ok();
    }

    @ExceptionHandler
    public void exceptionHandler(Exception e, HttpServletResponse resp) { R.err(e).write(resp); }
}



