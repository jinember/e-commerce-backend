package com.gec.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gec.components.FileTemplate;
import com.gec.domain.entity.Promotion;
import com.gec.service.IPromotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/Promotion")
public class PromotionController extends BaseController {

    @Autowired
    private IPromotionService promotionService;

    /* 活动内容变了要把 C 端商品列表缓存清掉，否则新活动最长要等 5 分钟才在商城显示 */
    @Autowired
    private com.gec.service.CacheService cacheService;
    private void clearShopCache() {
        try { cacheService.delete("shop:list"); } catch (Exception ignored) { }
    }

    @Autowired
    private FileTemplate fileTemplate;
    @Override
    protected FileTemplate getFileTemplate() { return fileTemplate; }

    @PostMapping("/list/{page}/{limit}")
    public R list(@PathVariable("page") Integer page, @PathVariable("limit") Integer limit, @RequestBody Promotion param) {
        Page pageObj = newPage(page, limit);
        IPage<Promotion> retPage = promotionService.listPromotion(pageObj, param);
        return R.convertPage(retPage);
    }

    @PostMapping("/addPromotion")
    public R add(@RequestBody Promotion obj) {
        boolean ret = promotionService.save(obj);
        if (!ret) throw new RuntimeException("添加失败");
        clearShopCache();
        return R.ok();
    }

    @PutMapping("/updatePromotion")
    public R update(@RequestBody Promotion obj) {
        boolean ret = promotionService.updateById(obj);
        if (!ret) throw new RuntimeException("更新失败");
        clearShopCache();
        return R.ok();
    }

    @DeleteMapping("/deletePromotion/{id}")
    public R delete(@PathVariable("id") Integer id) {
        boolean ret = promotionService.removeById(id);
        if (!ret) throw new RuntimeException("删除失败");
        clearShopCache();
        return R.ok();
    }

    @ExceptionHandler
    public void exceptionHandler(Exception e, HttpServletResponse resp) { R.err(e).write(resp); }
}



