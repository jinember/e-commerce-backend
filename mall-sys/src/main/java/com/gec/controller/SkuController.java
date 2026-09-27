package com.gec.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gec.components.FileTemplate;
import com.gec.domain.search.SkuSearch;
import com.gec.domain.vo.SkuVO;
import com.gec.service.ISkuInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/Sku")
public class SkuController extends BaseController {

    @Autowired
    private ISkuInfoService skuInfoService;
    @Autowired
    private FileTemplate fileTemplate;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Override
    protected FileTemplate getFileTemplate() {
        return fileTemplate;
    }

    /* 1.SKU 分页列表。 */
    @PostMapping(value = "/list/{page}/{limit}")
    public R list(@PathVariable("page") Integer page,
                  @PathVariable("limit") Integer limit,
                  @RequestBody SkuSearch param) {
        /* 1.参数检查与封装分页对象 */
        Page frmnPage = newPage(page, limit);
        /* 2.调用 service 查询(带搜索、分页) */
        IPage<SkuVO> retPage = skuInfoService.listSku(frmnPage, param);
        /* 3.封装分页数据给前端 */
        return R.convertPage(retPage);
    }

    /* 2.批量删除 SKU。 */
    @PostMapping("/deleteByIds")
    public R deleteByIds(@RequestBody List<Integer> ids) {
        if (ids == null || ids.size() == 0) {
            throw new RuntimeException("请先选择要删除的SKU");
        }
        boolean ret = skuInfoService.removeByIds(ids);
        if (!ret) {
            throw new RuntimeException("删除SKU失败");
        }
        /* 联动删除对应库存 */
        for (Integer skuId : ids) {
            jdbcTemplate.update("DELETE FROM tbl_stock WHERE sku_id = ?", skuId);
        }
        return R.ok();
    }

    /* 3.改单个价格 */
    @PutMapping("/updatePrice")
    public R updatePrice(@RequestBody java.util.Map<String, Object> body) {
        Integer skuId = (Integer) body.get("skuId");
        Double price = Double.valueOf(body.get("price").toString());
        if (price <= 0) throw new RuntimeException("价格必须大于0");
        com.gec.domain.entity.SkuInfo sku = new com.gec.domain.entity.SkuInfo();
        sku.setSkuId(skuId);
        sku.setPrice(price);
        boolean ret = skuInfoService.updateById(sku);
        if (!ret) throw new RuntimeException("改价失败");
        return R.ok();
    }

    /* 3.1 编辑SKU：改名称、价格 */
    @PutMapping("/update")
    public R updateSku(@RequestBody java.util.Map<String, Object> body) {
        Integer skuId = (Integer) body.get("skuId");
        if (skuId == null) throw new RuntimeException("缺少skuId");

        com.gec.domain.entity.SkuInfo sku = new com.gec.domain.entity.SkuInfo();
        sku.setSkuId(skuId);
        if (body.get("skuName") != null) sku.setSkuName((String) body.get("skuName"));
        if (body.get("price") != null) {
            double price = Double.valueOf(body.get("price").toString());
            if (price <= 0) throw new RuntimeException("价格必须大于0");
            sku.setPrice(price);
        }
        skuInfoService.updateById(sku);

        /* 如果传了 defaultImage，更新 tbl_sku_album（没有就插入） */
        if (body.get("defaultImage") != null) {
            String img = (String) body.get("defaultImage");
            Integer cnt = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tbl_sku_album WHERE sku_id = ?",
                Integer.class, skuId
            );
            if (cnt > 0) {
                jdbcTemplate.update(
                    "UPDATE tbl_sku_album SET default_image = ? WHERE sku_id = ?",
                    img, skuId
                );
            } else {
                jdbcTemplate.update(
                    "INSERT INTO tbl_sku_album (sku_id, images, default_image) VALUES (?, ?, ?)",
                    String.valueOf(skuId), img, img
                );
            }
        }
        return R.ok();
    }

    /* 4.批量改价格 */
    @PostMapping("/batchUpdatePrice")
    public R batchUpdatePrice(@RequestBody java.util.Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Integer> ids = (List<Integer>) body.get("ids");
        Double price = Double.valueOf(body.get("price").toString());
        if (ids == null || ids.isEmpty()) throw new RuntimeException("请先选择SKU");
        if (price <= 0) throw new RuntimeException("价格必须大于0");
        for (Integer id : ids) {
            com.gec.domain.entity.SkuInfo sku = new com.gec.domain.entity.SkuInfo();
            sku.setSkuId(id);
            sku.setPrice(price);
            skuInfoService.updateById(sku);
        }
        return R.ok();
    }

    /* 5.批量删除（POST，DELETE带body会404） */
    @PostMapping("/batchDelete")
    public R batchDelete(@RequestBody List<Integer> ids) {
        if (ids == null || ids.isEmpty()) throw new RuntimeException("请先选择要删除的SKU");
        boolean ret = skuInfoService.removeByIds(ids);
        if (!ret) throw new RuntimeException("批量删除失败");
        /* 联动删除对应库存 */
        for (Integer skuId : ids) {
            jdbcTemplate.update("DELETE FROM tbl_stock WHERE sku_id = ?", skuId);
        }
        return R.ok();
    }

    /* 6.上传SKU图片 */
    private String SUB_DIR = "album";

    @PostMapping("/uploadImg")
    public R uploadImg(@RequestParam("file") MultipartFile mFile) {
        if (mFile.isEmpty()) throw new RuntimeException("文件不能为空");
        fileTemplate.setMultipartFile(mFile);
        String fileName = com.gec.util.FileUtils.makeUUID()
            + com.gec.util.FileUtils.extName(mFile.getOriginalFilename());
        try {
            fileTemplate.saveFile(SUB_DIR, fileName);
        } catch (Exception e) {
            throw new RuntimeException("上传失败: " + e.getMessage());
        }
        return R.ok().put("fileName", fileName);
    }

    /* 7.读取SKU图片 */
    @GetMapping("/showImg/{imgName}")
    public void showImg(@PathVariable("imgName") String imgName,
                        HttpServletResponse resp) {
        try {
            outFile(resp, SUB_DIR, imgName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @ExceptionHandler
    public void exceptionHandler(
        Exception e, javax.servlet.http.HttpServletResponse resp){
        R.err( e ).write( resp );
    }
}
