package com.gec.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gec.components.FileTemplate;
import com.gec.domain.entity.SaleStrategy;
import com.gec.domain.search.SaleStrategySearch;
import com.gec.service.ISaleStrategyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/SaleStrategy")
public class SaleStrategyController extends BaseController {

    @Autowired
    private ISaleStrategyService saleStrategyService;
    @Autowired
    private FileTemplate fileTemplate;

    @Override
    protected FileTemplate getFileTemplate() {
        return fileTemplate;
    }

    /* 1.销售策略分页列表(带搜索)。 */
    @PostMapping("/list/{page}/{limit}")
    public R list(@PathVariable("page") Integer page,
                  @PathVariable("limit") Integer limit,
                  @RequestBody SaleStrategySearch param) {
        Page frmnPage = newPage(page, limit);
        IPage<SaleStrategy> retPage = saleStrategyService.listStrategy(frmnPage, param);
        return R.convertPage(retPage);
    }

    /* 2.新增/修改销售策略。 */
    @PostMapping("/save")
    public R save(@RequestBody SaleStrategy st) {
        saleStrategyService.saveStrategy(st);
        return R.ok();
    }

    /* 3.删除销售策略。 */
    @PostMapping("/delete/{id}")
    public R delete(@PathVariable("id") Integer id) {
        saleStrategyService.deleteStrategy(id);
        return R.ok();
    }

    /* 4.更新策略状态(启用/停用)。 */
    @PostMapping("/updateStatus")
    public R updateStatus(@RequestBody SaleStrategy st) {
        saleStrategyService.updateStatus(st);
        return R.ok();
    }
}
