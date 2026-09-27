package com.gec.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gec.components.FileTemplate;
import com.gec.domain.entity.PointLog;
import com.gec.service.IPointLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/PointLog")
public class PointLogController extends BaseController {

    @Autowired
    private IPointLogService pointlogService;

    @Autowired
    private FileTemplate fileTemplate;
    @Override
    protected FileTemplate getFileTemplate() { return fileTemplate; }

    @PostMapping("/list/{page}/{limit}")
    public R list(@PathVariable("page") Integer page, @PathVariable("limit") Integer limit, @RequestBody PointLog param) {
        Page pageObj = newPage(page, limit);
        IPage<PointLog> retPage = pointlogService.listPointLog(pageObj, param);
        return R.convertPage(retPage);
    }

    @PostMapping("/addPointLog")
    public R add(@RequestBody PointLog obj) {
        boolean ret = pointlogService.save(obj);
        if (!ret) throw new RuntimeException("添加失败");
        return R.ok();
    }

    @PutMapping("/updatePointLog")
    public R update(@RequestBody PointLog obj) {
        boolean ret = pointlogService.updateById(obj);
        if (!ret) throw new RuntimeException("更新失败");
        return R.ok();
    }

    @DeleteMapping("/deletePointLog/{id}")
    public R delete(@PathVariable("id") Integer id) {
        boolean ret = pointlogService.removeById(id);
        if (!ret) throw new RuntimeException("删除失败");
        return R.ok();
    }

    @ExceptionHandler
    public void exceptionHandler(Exception e, HttpServletResponse resp) { R.err(e).write(resp); }
}



