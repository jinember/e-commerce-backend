package com.gec.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gec.components.FileTemplate;
import com.gec.domain.entity.GoodsAttr;
import com.gec.domain.search.GoodsAttrSearch;
import com.gec.domain.vo.GoodsAttrVO;
import com.gec.service.IGoodsAttrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/GoodsAttr")
public class GoodsAttrController extends BaseController {
    @Autowired
    private IGoodsAttrService goodsAttrService;

    /* 【1】list() 分页+搜索查询规格参数/销售属性列表。 */
    @GetMapping("/list/{page}/{limit}")
    public R list(
            @PathVariable("page") Integer page,
            @PathVariable("limit") Integer limit,
            GoodsAttrSearch attrSearch
    ){
        Page frmPage = new Page(page, limit);
        IPage retPage =
            goodsAttrService.listGoodsAttr(frmPage, attrSearch);
        return R.convertPage(retPage);
    }

    /* 【2】add() 新增属性。 */
    @PostMapping("/add")
    public R add(@RequestBody GoodsAttrVO attrVO){
        goodsAttrService.addGoodsAttr(attrVO);
        return R.ok();
    }

    /* 【3】update() 更新属性。 */
    @PutMapping("/update")
    public R update(@RequestBody GoodsAttrVO attrVO){
        goodsAttrService.updateGoodsAttr(attrVO);
        return R.ok();
    }

    /* 【4】delete() 删除属性。 */
    @DeleteMapping("/delete/{id}")
    public R delete(@PathVariable("id") Integer id){
        goodsAttrService.deleteGoodsAttr(id);
        return R.ok();
    }

    /* 【5】listByCategory()
     * 此方法可用于查询【基本属性】与【销售属性】。
     * 传入 ==> 1.类别ID, 2.属性类型。
     */
    @GetMapping("/listByCategory/{categoryId}/{attrType}")
    public R listByCategory(
        @PathVariable("categoryId") Integer categoryId,
        @PathVariable("attrType") Integer attrType ){
        List<GoodsAttr> list = goodsAttrService
            .queryGoodsAttrByCategory(
                categoryId, attrType );
        return R.ok( list );
    }

    @ExceptionHandler(Exception.class)
    public void exceptionFallback(
        Exception E, HttpServletResponse resp){
        E.printStackTrace();
        R.err( E ).write( resp );
    }

    @Override
    protected FileTemplate getFileTemplate() {
        return null;
    }
}
