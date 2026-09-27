package com.gec.controller;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gec.components.FileTemplate;
import com.gec.domain.entity.GoodsDetail;
import com.gec.domain.search.SpuSearch;
import com.gec.domain.vo.SpuVO;
import com.gec.service.ISpuDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/Spu")
public class SpuController extends BaseController {

    @Autowired
    private ISpuDetailService spuDetailService;
    @Autowired
    private com.gec.service.CacheService cacheService;
    @Autowired
    private FileTemplate fileTemplate;

    @Override
    protected FileTemplate getFileTemplate() {
        return fileTemplate;
    }

    /* 1.商品(SPU)分页列表。 */
    @PostMapping(value = "/list/{page}/{limit}")
    public R list(@PathVariable("page") Integer page,
                  @PathVariable("limit") Integer limit,
                  @RequestBody SpuSearch param) {
        /* 1.参数检查与封装分页对象 */
        Page frmnPage = newPage(page, limit);
        /* 2.调用 service 查询(带搜索、分页) */
        IPage<SpuVO> retPage = spuDetailService.listSpu(frmnPage, param);
        /* 3.封装分页数据给前端 */
        return R.convertPage(retPage);
    }

    /* 2.设置上架/下架状态。 */
    @PostMapping(value = "/setStatus")
    public R setStatus(@RequestBody GoodsDetail gd) {
        /* 1.创建条件更新器 */
        UpdateWrapper<GoodsDetail> UW = new UpdateWrapper<>();
        UW.eq("id", gd.getId());
        UW.set("status", gd.getStatus());
        /* 2.执行更新 */
        boolean ret = spuDetailService.update(UW);
        /* 3.商品可见性变了，清掉 C 端商品列表缓存 */
        cacheService.deleteByPrefix("shop:");
        if (ret) {
            return R.ok();
        }
        else {
            throw new RuntimeException("设置商品上架状态失败");
        }
    }

    /* 3.编辑商品(名称、详情、主图) */
    @PutMapping("/update")
    public R update(@RequestBody GoodsDetail goods){
        boolean ret = spuDetailService.updateById(goods);
        cacheService.deleteByPrefix("shop:");
        if(ret){ return R.ok(); }
        else { throw new RuntimeException("更新商品失败"); }
    }

    @ExceptionHandler
    public void exceptionHandler(
        Exception e, javax.servlet.http.HttpServletResponse resp){
        R.err( e ).write( resp );
    }
}
