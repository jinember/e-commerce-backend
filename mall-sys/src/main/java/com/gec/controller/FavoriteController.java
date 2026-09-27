package com.gec.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gec.components.FileTemplate;
import com.gec.domain.entity.Favorite;
import com.gec.service.IFavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/Favorite")
public class FavoriteController extends BaseController {

    @Autowired
    private IFavoriteService favoriteService;

    @Autowired
    private FileTemplate fileTemplate;
    @Override
    protected FileTemplate getFileTemplate() { return fileTemplate; }

    @PostMapping("/list/{page}/{limit}")
    public R list(@PathVariable("page") Integer page, @PathVariable("limit") Integer limit, @RequestBody Favorite param) {
        Page pageObj = newPage(page, limit);
        IPage<Favorite> retPage = favoriteService.listFavorite(pageObj, param);
        return R.convertPage(retPage);
    }

    @PostMapping("/addFavorite")
    public R add(@RequestBody Favorite obj) {
        boolean ret = favoriteService.save(obj);
        if (!ret) throw new RuntimeException("添加失败");
        return R.ok();
    }

    @PutMapping("/updateFavorite")
    public R update(@RequestBody Favorite obj) {
        boolean ret = favoriteService.updateById(obj);
        if (!ret) throw new RuntimeException("更新失败");
        return R.ok();
    }

    @DeleteMapping("/deleteFavorite/{id}")
    public R delete(@PathVariable("id") Integer id) {
        boolean ret = favoriteService.removeById(id);
        if (!ret) throw new RuntimeException("删除失败");
        return R.ok();
    }

    /* 按memberId+spuId取消收藏 */
    @DeleteMapping("/cancel")
    public R cancel(@RequestParam Integer memberId, @RequestParam Integer spuId) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Favorite> wrapper =
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getMemberId, memberId);
        wrapper.eq(Favorite::getSpuId, spuId);
        boolean ret = favoriteService.remove(wrapper);
        if (!ret) throw new RuntimeException("取消收藏失败");
        return R.ok();
    }

    @ExceptionHandler
    public void exceptionHandler(Exception e, HttpServletResponse resp) { R.err(e).write(resp); }
}



