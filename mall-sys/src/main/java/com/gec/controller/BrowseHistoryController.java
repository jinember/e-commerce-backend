package com.gec.controller;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gec.components.FileTemplate;
import com.gec.domain.entity.BrowseHistory;
import com.gec.service.IBrowseHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/BrowseHistory")
public class BrowseHistoryController extends BaseController {

    @Autowired
    private IBrowseHistoryService Service;
    @Autowired
    private FileTemplate fileTemplate;

    @Override
    protected FileTemplate getFileTemplate() { return fileTemplate; }

    @PostMapping("/list/{page}/{limit}")
    public R list(@PathVariable Integer page, @PathVariable Integer limit, @RequestBody BrowseHistory param) {
        Page<BrowseHistory> p = new Page<>(page, limit);
        return R.convertPage(Service.listBrowseHistory(p));
    }

    @PostMapping("/save")
    public R save(@RequestBody BrowseHistory obj) { Service.saveOrUpdate(obj); return R.ok(); }

    @PostMapping("/delete/{id}")
    public R delete(@PathVariable Integer id) { Service.removeById(id); return R.ok(); }

    @GetMapping("/{id}")
    public R getById(@PathVariable Integer id) { return R.ok(Service.getById(id)); }
}