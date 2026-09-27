package com.gec.controller;

import com.gec.components.FileTemplate;
import com.gec.domain.entity.GoodsAttrValue;
import com.gec.domain.vo.GoodsAttrValuesVO;
import com.gec.domain.vo.GoodsBaseInfoVO;
import com.gec.domain.vo.SkuInfoVO;
import com.gec.domain.vo.SkuLineVO;
import com.gec.service.IGoodsDetailService;
import com.gec.util.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/PublishGoods")
public class PublishGoodsController extends BaseController {

    @ExceptionHandler
    public void exceptionHandler(
        Exception e, HttpServletResponse resp){
        R.err( e ).write( resp );
    }

    /* 6.文件上传。*/
    @Autowired
    private FileTemplate fileTemplate;
    @Autowired
    private com.gec.service.CacheService cacheService;
    /* 7.1.声明商品主图片上传的目录。*/
    private String MAIN_DIR = "goods";
    /* 7.2.声明商品图集上传的目录。*/
    private String ALBUM_DIR = "album";

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
    *   此方法用于读取本地的一张图片。
    *   把数据输出到请求端（浏览器）。
    *   浏览器即能展示出来。
    *   http://localhost:8090/mall-sys/PublishGoods/showImg/01.png
    */
    @GetMapping("/showImg/{dir}/{imgName}")
    public void showImg(
        @PathVariable("dir")String dir,
        @PathVariable("imgName")String imgName,
        HttpServletResponse resp ){
        try{
            outFile(resp, dir, imgName);
        }catch (Exception e){
            e.printStackTrace();
            send404File(resp);
        }
    }

    /*
    * upload() 文件上传的方法。
    */
    @PostMapping("/upload")
    public R upload(
        @RequestParam("file")MultipartFile mFile,
        @RequestParam("type")Integer type ){
        /* 1.判断上传的文件是否为空。*/
        boolean isEmpty = mFile.isEmpty();
        if( isEmpty ){
            throw new RuntimeException("文件不能为空。");
        }
        /* 2.如果类型是 1，表示我上传的是主图片。*/
        String dir = MAIN_DIR;
        if( !type.equals(1) ){
            dir = ALBUM_DIR;
        }
        /* 2.把上传的文件关联到模板类。*/
        fileTemplate.setMultipartFile(mFile);
        /* 3.自动生成新的名称。*/
        String newName = makeNewName(mFile);
        String logoUri = "/PublishGoods/showImg/"+
            dir +"/"+ newName;
        try{
            /* 4.保存图片到 d:\\haida\\space\\???\\下。*/
            fileTemplate.saveFile(dir, newName);
            /* 5.把图片名，uri 返回给前端。*/
            /* uri 用来显示用的，文件名用来保存数据库的。*/
            return R.ok()
                .put("logoUri", logoUri)
                .put("fileName", newName);
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("上传文件失败。");
        }
    }

    /* 8.商品基础信息发布【保存到 REDIS】 */
    @Autowired
    private IGoodsDetailService detailService;

    /* 【02】保存商品基础信息。 */
    @PostMapping("/saveGoodsBaseInfo")
    public R saveGoodsBaseInfo(
        @RequestBody GoodsBaseInfoVO biVO ) {
        String pubKey = detailService.saveBaseInfoCache(biVO);
        return R.ok()
            .put("pubKey",pubKey);
    }

    /* 【02】保存商品规格参数(发布流程第2步)。 */
    @PostMapping("/saveGoodsAttrValues")
    public R saveGoodsAttrValues(
        @RequestBody GoodsAttrValuesVO gavVO ){
        String pubKey = detailService.saveGoodsAttrValCache(gavVO);
        return R.ok()
            .put("pubKey",pubKey);
    }

    /* 【03】读取商品基础信息缓存(回显用)。 */
    @GetMapping("/getGoodsBaseInfo/{pubKey}")
    public R getGoodsBaseInfo(
        @PathVariable("pubKey")String pubKey ) {
        GoodsBaseInfoVO biVO =
            detailService.getBaseInfoCache(pubKey);
        return R.ok()
            .put("baseInfo", biVO);
    }

    /* 【04】读取商品规格属性缓存(回显用)。 */
    @GetMapping("/getGoodsAttrValues/{pubKey}")
    public R getGoodsAttrValues(
        @PathVariable("pubKey")String pubKey ) {
        List<GoodsAttrValue> attrList =
            detailService.getGoodsAttrValCache(pubKey);
        return R.ok()
            .put("attrList", attrList);
    }

    /* 【05】保存商品销售属性(发布流程第3步)。 */
    @PostMapping("/saveGoodsSaleAttrValues")
    public R saveGoodsSaleAttrValues(
        @RequestBody GoodsAttrValuesVO gavVO ){
        String pubKey = detailService.saveGoodsSaleAttrCache(gavVO);
        return R.ok()
            .put("pubKey",pubKey);
    }

    /* 【06】读取商品销售属性缓存(回显用)。 */
    @GetMapping("/getGoodsSaleAttrValues/{pubKey}")
    public R getGoodsSaleAttrValues(
        @PathVariable("pubKey")String pubKey ) {
        List<GoodsAttrValue> attrList =
            detailService.getGoodsSaleAttrCache(pubKey);
        return R.ok()
            .put("attrList", attrList);
    }

    /* 【07】保存SKU信息(发布流程第4步)。 */
    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/saveSkuInfo")
    public R saveSkuInfo(
        @RequestBody SkuInfoVO skuVO ){
        String pubKey = detailService.saveSkuInfoCache(skuVO);
        return R.ok()
            .put("pubKey",pubKey);
    }

    /* 【08】读取SKU信息缓存(回显用)。 */
    @GetMapping("/getSkuInfo/{pubKey}")
    public R getSkuInfo(
        @PathVariable("pubKey")String pubKey ) {
        List<SkuLineVO> skuList =
            detailService.getSkuInfoCache(pubKey);
        return R.ok()
            .put("skuList", skuList);
    }

    /* 【09】发布商品：写入数据库 + 清理Redis缓存。 */
    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/publishGoods/{pubKey}")
    public R publishGoods(
        @PathVariable("pubKey")String pubKey ) {
        Integer spuId = detailService.publishGoods(pubKey);
        /* 新商品上架，C 端列表缓存必须失效 */
        cacheService.deleteByPrefix("shop:");
        return R.ok()
            .put("spuId", spuId);
    }


}
