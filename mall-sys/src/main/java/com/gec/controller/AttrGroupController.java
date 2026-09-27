package com.gec.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gec.components.FileTemplate;
import com.gec.dao.OptionMapper;
import com.gec.domain.entity.GoodsAttrGroup;
import com.gec.domain.vo.OptionVO;
import com.gec.service.IAttrGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/AttrGroup")
public class AttrGroupController extends BaseController {
    @Autowired
    private IAttrGroupService attrGroupService;
    @Autowired
    private OptionMapper optionMapper;

    /*
        Link: http://localhost:8095/mall-pms/AttrGroup/list/1/10/11
        Vue: getGoodsAttrList( page, limit, param );
		1.����: R list();
    */
    @GetMapping(value = "/list/{page}/{limit}/{categoryId}")
    public R list(
            @PathVariable("page")Integer page,
            @PathVariable("limit")Integer limit,
            @PathVariable("categoryId")Integer categoryId
    ){
        Page frmPage = new Page(page, limit);
        IPage retPage = attrGroupService.listAttrGroup(frmPage, categoryId);
        return R.convertPage(retPage);
    }

	/*  2.����: R add(). */
    @PostMapping("/add")
    public R add(@RequestBody GoodsAttrGroup  attrGroup){
        attrGroupService.addAttrGroup(attrGroup);
        return R.ok();
    }

	/*  3.����: R update(). */
    @PutMapping("/update")
    public R update(@RequestBody GoodsAttrGroup  attrGroup){
        attrGroupService.updateAttrGroup(attrGroup);
        return R.ok();
    }
	
	/*  4.����: R delete(). */
    @DeleteMapping("/delete/{id}")
    public R delete(@PathVariable("id") Integer id){
        attrGroupService.removeById(id);
        return R.ok();
    }
	
	/*  5.����: R optionsByCategory(). */
    @GetMapping("/optionsByCategory/{categoryId}")
    public R optionsByCategory(
            @PathVariable("categoryId") Integer categoryId){
        List<OptionVO> list = optionMapper.groupByCategory(categoryId);
        return R.ok(list);
    }

    @ExceptionHandler(Exception.class)
    public R exceptionFallback(Exception E){
        E.printStackTrace();
        return R.err( E );
    }

    @Override
    protected FileTemplate getFileTemplate() {
        return null;
    }

}
