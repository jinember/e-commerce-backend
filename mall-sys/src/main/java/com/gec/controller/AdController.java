package com.gec.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gec.components.FileTemplate;
import com.gec.domain.entity.Ad;
import com.gec.service.IAdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/Ad")
public class AdController extends BaseController {

    @Autowired
    private IAdService adService;

    /* BaseController 要求实现（本模块暂未用图片上传） */
    @Autowired
    private FileTemplate fileTemplate;
    @Override
    protected FileTemplate getFileTemplate() {
        return fileTemplate;
    }

    /* 1.分页列表(带搜索) */
    @PostMapping("/list/{page}/{limit}")
    public R list(@PathVariable("page") Integer page,
                  @PathVariable("limit") Integer limit,
                  @RequestBody Ad param) {
        Page pageObj = newPage(page, limit);
        IPage<Ad> retPage = adService.listAd(pageObj, param);
        return R.convertPage(retPage);
    }

    /* 2.新增广告 */
    @PostMapping("/addAd")
    public R addAd(@RequestBody Ad ad) {
        boolean ret = adService.save(ad);
        if (!ret) {
            throw new RuntimeException("新增广告失败");
        }
        return R.ok();
    }

    /* 3.更新广告 */
    @PutMapping("/updateAd")
    public R updateAd(@RequestBody Ad ad) {
        boolean ret = adService.updateById(ad);
        if (!ret) {
            throw new RuntimeException("更新广告失败");
        }
        return R.ok();
    }

    /* 4.删除广告 */
    @DeleteMapping("/deleteAd/{id}")
    public R deleteAd(@PathVariable("id") Integer id) {
        boolean ret = adService.removeById(id);
        if (!ret) {
            throw new RuntimeException("删除广告失败");
        }
        return R.ok();
    }

    /* 5.广告点击量+1 */
    @PostMapping("/click/{id}")
    public R click(@PathVariable("id") Integer id) {
        Ad ad = adService.getById(id);
        if (ad != null) {
            ad.setClickCount((ad.getClickCount() == null ? 0 : ad.getClickCount()) + 1);
            adService.updateById(ad);
        }
        return R.ok();
    }

    @ExceptionHandler
    public void exceptionHandler(Exception e, HttpServletResponse resp) {
        R.err(e).write(resp);
    }
}
