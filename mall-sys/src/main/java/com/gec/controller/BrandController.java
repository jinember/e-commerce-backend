package com.gec.controller;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gec.components.FileTemplate;
import com.gec.dao.OptionMapper;
import com.gec.domain.entity.Brand;
import com.gec.domain.search.BrandSearch;
import com.gec.domain.vo.BrandVO;
import com.gec.domain.vo.OptionVO;
import com.gec.service.IBrandService;
import com.gec.util.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/Brand")
public class BrandController extends BaseController {
    @Autowired
    private IBrandService brandService;
    @Autowired
    private OptionMapper optionMapper;

    /* 1.获取列表。【POST请求】 */
    @PostMapping(value = "/list/{page}/{limit}")
    public R list(@PathVariable("page") Integer page,
                  @PathVariable("limit") Integer limit,
                  @RequestBody BrandSearch param) {
        /*1.调用父类方法来参数检查与封装*/
        Page frmnPage = newPage(page, limit);
        /*2.调用service方法实现查询（有搜索，分页）*/
        IPage<Brand> retPage = brandService.listBrand(
                frmnPage,param
        );
        /*3.封装分页数据给到前端*/
        return R.convertPage(retPage);
    }


    /*
        2.设置状态。
          设置其是否在商城的主页中显示出来。
    */
    @PostMapping(value = "/setStatus")
    public R setStatus(@RequestBody Brand brand) {
        /*1.创建一个条件设置器（更新专用的）*/
        UpdateWrapper<Brand> UW = new UpdateWrapper<>();
        /*2.动态设置条件与修改数据*/
        UW.eq("id", brand.getId());
        UW.set("show_status", brand.getShowStatus());
        /*3.调用内置方法去更新数据*/
        boolean ret = brandService.update(UW);
        if (ret) {
            return R.ok();
        }
        else{
            throw new RuntimeException("设置品牌显示状态失败");
        }
    }

    @ExceptionHandler
    public void exceptionHandler(
        Exception e, HttpServletResponse resp){
        R.err( e ).write( resp );
    }

    /* 3.添加品牌。*/
    @PostMapping("/addBrand")
    public R addBrand(@RequestBody Brand brand) {
        boolean ret = brandService.save(brand);
        if (!ret) {
            throw new RuntimeException("添加品牌失败");
        }
        return R.ok();
    }

    /* 4.更新品牌。*/
    @PutMapping("/updateBrand")
    public R updateBrand(@RequestBody Brand brand) {
        UpdateWrapper<Brand> UW = new UpdateWrapper<>();
        UW.eq("id", brand.getId());
        boolean ret = brandService.updateById(brand);
        if (!ret) {
            throw new RuntimeException("更新品牌失败");
        }
        return R.ok();
    }

    /* 5.删除品牌。*/
    @DeleteMapping("/deleteBrand/{id}")
    public R deleteBrand(@PathVariable("id") Integer id) {
        brandService.deleteBrand(id);
        return R.ok();
    }

    /* 6.关联类别。查询品牌关联的分类ID列表 */
    @GetMapping("/getCategories/{brandId}")
    public R getCategories(@PathVariable("brandId") Integer brandId) {
        List<Integer> categoryIds = optionMapper.getBrandCategoryIds(brandId);
        return R.ok(categoryIds);
    }

    /* 6.1 保存品牌关联的分类 */
    @PostMapping("/saveCategories")
    public R saveCategories(@RequestBody java.util.Map<String, Object> body) {
        Integer brandId = Integer.parseInt(body.get("brandId").toString());
        @SuppressWarnings("unchecked")
        List<Integer> categoryIds = (List<Integer>) body.get("categoryIds");
        optionMapper.saveBrandCategories(brandId, categoryIds);
        return R.ok().put("msg", "关联分类保存成功");
    }

    //7.文件上传。
    @Autowired
    private FileTemplate fileTemplate;
    //7.声明当前模块的图片上传的目录。
    private String SUB_DIR = "brand";

    @Override
    protected FileTemplate getFileTemplate() {
        return fileTemplate;
    }

    /*
    * 此方法用于产生一个随机文件名。
    */
    private String makeNewName(MultipartFile mFile){
        //1.生成一个 UUID
        String UUID = FileUtils.makeUUID();
        //2.拿到文件名。如: d01.png
        String fileName = mFile.getOriginalFilename();
        //3.获取它的扩展名。如:.png
        String extName = FileUtils.extName(fileName);
        //4.返回新的文件名。
        return UUID + extName;
    }

    /*
    * 此方法用于读取本地的一张图片。
    * 把数据输出到请求端（浏览器）。
    * 浏览器即能展示出来。
    */
    @GetMapping("/showImg/{imgName}")
    public void showImg(@PathVariable("imgName") String imgName,
                        HttpServletResponse resp) {
        try{
            outFile(resp,SUB_DIR,imgName);
        }catch (Exception e){
            e.printStackTrace();
            //图片不存在，发送404图片
            send404File(resp);
        }
    }

    /*
    * upload()。
    */
    @PostMapping("/upload")
    public R upload(@RequestParam("file") MultipartFile mFile,
                    @RequestParam("userId")Integer userId
    ) {
        /*1.判断上传的文件是否为空*/
        boolean isEmpty = mFile.isEmpty();
        if (isEmpty) {
            throw new RuntimeException("文件不能为空");
        }
        /*2.把上传的文件关联到模板类*/
        fileTemplate.setMultipartFile(mFile);
        /*3.自动生成新的名称*/
        String newName = makeNewName(mFile);
        String logoUri = "/Brand/showImg/" + newName;
        try{
            /*4.保存图片到D:\e-commerce\space\brand*/
            fileTemplate.saveFile(SUB_DIR,newName);
            /*5.把图片名，uri返回给前端*/
            /*uri用来显示用的，文件名用来保存数据库的*/
            return R.ok()
                    .put("logoUri", logoUri)
                    .put("fileName", newName);
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("上传文件失败");
        }
    }

    /*
     * listByCategory()。
     */
    @PostMapping("/listByCategory/{page}/{limit}")
    public R listByCategory(
            @PathVariable("page") Integer page,
            @PathVariable("limit") Integer limit,
            @RequestBody BrandSearch param){
                Page frmnPage = newPage(page, limit);
                /*1.调用service方法获取列表*/
        IPage<BrandVO> retPage = brandService.getListByCategory(frmnPage,param);
        /*2.把页对象转化为指定的json格式*/
        return R.convertPage(retPage);
    }

    @GetMapping("/brandOption/{categoryId}")
    public R brandOptions(
            @PathVariable("categoryId") Integer categoryId) {
        List<OptionVO> options = optionMapper.brandOptions(categoryId);
        return R.ok(options);
    }



}
