package com.gec.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gec.components.FileTemplate;
import com.gec.domain.entity.Stock;
import com.gec.domain.entity.StockLog;
import com.gec.service.IStockLogService;
import com.gec.service.IStockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/Stock")
public class StockController extends BaseController {

    @Autowired
    private IStockService stockService;

    @Autowired
    private IStockLogService stockLogService;

    @Autowired
    private com.gec.service.IMessageService messageService;

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
                  @RequestBody Stock param) {
        Page pageObj = newPage(page, limit);
        IPage<Stock> retPage = stockService.listStock(pageObj, param);
        return R.convertPage(retPage);
    }

    /* 检查库存预警：库存低于安全库存时发消息 */
    private void checkAndSendWarning(Stock stock) {
        int safe = stock.getSafeStock() == null ? 0 : stock.getSafeStock();
        int cur = stock.getStock() == null ? 0 : stock.getStock();
        if (safe > 0 && cur <= safe) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            com.gec.domain.entity.Message warn = new com.gec.domain.entity.Message();
            warn.setMemberId(0);
            warn.setTitle("库存预警：" + stock.getGoodsName());
            warn.setContent("商品【" + stock.getGoodsName() + "】库存仅剩 " + cur + " 件，低于安全库存 " + safe + " 件");
            warn.setType("system");
            warn.setIsRead(0);
            warn.setSendTime(sdf.format(new java.util.Date()));
            messageService.save(warn);
        }
    }

    /* 2.新增库存（初始化录入）：写一条初始化流水 */
    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/addStock")
    public R addStock(@RequestBody Stock stock) {
        boolean ret = stockService.save(stock);
        if (!ret) {
            throw new RuntimeException("新增库存失败");
        }
        /* 初始化流水 */
        if (stock.getStock() != null && stock.getStock() > 0) {
            StockLog log = new StockLog();
            log.setStockId(stock.getId());
            log.setChangeType("in");
            log.setQuantity(stock.getStock());
            log.setRemark("库存初始化");
            log.setOperator("admin");
            stockLogService.save(log);
        }
        return R.ok();
    }

    /* 3.更新库存（调库存）：自动算差值并写流水，保证验收③可追溯 */
    @PutMapping("/updateStock")
    public R updateStock(@RequestBody Stock stock) {
        Stock old = stockService.getById(stock.getId());
        if (old == null) throw new RuntimeException("库存记录不存在");
        int oldStock = old.getStock() == null ? 0 : old.getStock();
        int newStock = stock.getStock() == null ? 0 : stock.getStock();
        if (newStock < 0) throw new RuntimeException("库存不能为负数");
        int diff = newStock - oldStock;

        boolean ret = stockService.updateById(stock);
        if (!ret) {
            throw new RuntimeException("更新库存失败");
        }
        /* 差值不为0才写流水 */
        if (diff != 0) {
            StockLog log = new StockLog();
            log.setStockId(stock.getId());
            log.setChangeType(diff > 0 ? "in" : "out");
            log.setQuantity(Math.abs(diff));
            log.setRemark("手动调库存：" + oldStock + " → " + newStock);
            log.setOperator("admin");
            stockLogService.save(log);
        }
        /* 调库存后检查预警 */
        Stock updated = stockService.getById(stock.getId());
        checkAndSendWarning(updated);
        return R.ok();
    }

    /* 4.删除库存 */
    @DeleteMapping("/deleteStock/{id}")
    public R deleteStock(@PathVariable("id") Integer id) {
        boolean ret = stockService.removeById(id);
        if (!ret) {
            throw new RuntimeException("删除库存失败");
        }
        return R.ok();
    }

    /* 查全部库存(供出入库下拉) */
    @GetMapping("/all")
    public R all() {
        return R.ok(stockService.list());
    }

    /* 批量删除库存 */
    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/batchDelete")
    public R batchDelete(@RequestBody List<Integer> ids) {
        if (ids == null || ids.isEmpty()) throw new RuntimeException("请先勾选要删除的记录");
        boolean ret = stockService.removeByIds(ids);
        if (!ret) throw new RuntimeException("批量删除失败");
        return R.ok().put("msg", "已删除" + ids.size() + "条库存记录");
    }

    /* 批量入库：勾选多个SKU，一次性给每个入库N件，逐条写流水 */
    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/batchInbound")
    public R batchInbound(@RequestBody List<Map<String, Object>> items) {
        if (items == null || items.isEmpty()) throw new RuntimeException("没有要入库的商品");
        int success = 0;
        for (Map<String, Object> it : items) {
            Integer id = Integer.parseInt(it.get("id").toString());
            Integer qty = Integer.parseInt(it.get("quantity").toString());
            if (qty == null || qty <= 0) continue;
            Stock stock = stockService.getById(id);
            if (stock == null) continue;
            int cur = stock.getStock() == null ? 0 : stock.getStock();
            stock.setStock(cur + qty);
            stockService.updateById(stock);
            StockLog log = new StockLog();
            log.setStockId(stock.getId());
            log.setChangeType("in");
            log.setQuantity(qty);
            log.setRemark("批量入库");
            log.setOperator("admin");
            stockLogService.save(log);
            success++;
        }
        return R.ok().put("msg", "批量入库完成，共" + success + "个SKU");
    }

    /* Excel 批量入库：上传 .xlsx/.xls 文件，第一列商品ID，第二列数量 */
    @PostMapping("/importExcel")
    public R importExcel(@RequestParam("file") org.springframework.web.multipart.MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) throw new RuntimeException("请选择Excel文件");
        String name = file.getOriginalFilename();
        if (!name.matches(".*\\.(xlsx|xls)$")) throw new RuntimeException("只支持.xlsx/.xls文件");
        org.apache.poi.ss.usermodel.Workbook wb;
        try (InputStream is = file.getInputStream()) {
            wb = org.apache.poi.ss.usermodel.WorkbookFactory.create(is);
        }
        org.apache.poi.ss.usermodel.Sheet sheet = wb.getSheetAt(0);
        int success = 0, fail = 0;
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {  // 第0行是表头
            org.apache.poi.ss.usermodel.Row row = sheet.getRow(i);
            if (row == null) continue;
            try {
                int id = (int) row.getCell(0).getNumericCellValue();
                int qty = (int) row.getCell(1).getNumericCellValue();
                if (qty <= 0) continue;
                Stock stock = stockService.getById(id);
                if (stock == null) { fail++; continue; }
                int cur = stock.getStock() == null ? 0 : stock.getStock();
                stock.setStock(cur + qty);
                stockService.updateById(stock);
                StockLog log = new StockLog();
                log.setStockId(stock.getId());
                log.setChangeType("in");
                log.setQuantity(qty);
                log.setRemark("Excel批量入库");
                log.setOperator("admin");
                stockLogService.save(log);
                success++;
            } catch (Exception e) { fail++; }
        }
        wb.close();
        return R.ok().put("msg", "导入完成：成功" + success + "条，失败" + fail + "条");
    }

    /* 5.批量更新库存(验收①)：前端勾选多行后一次性保存，每条自动写流水 */
    @Transactional(rollbackFor = Exception.class)
    @PutMapping("/batchUpdate")
    public R batchUpdate(@RequestBody List<Stock> list) {
        if (list == null || list.isEmpty()) {
            throw new RuntimeException("没有要保存的库存记录");
        }
        int logCount = 0;
        for (Stock row : list) {
            Stock old = stockService.getById(row.getId());
            if (old == null) continue;
            int oldStock = old.getStock() == null ? 0 : old.getStock();
            int newStock = row.getStock() == null ? 0 : row.getStock();
            if (newStock < 0) throw new RuntimeException("商品[" + row.getGoodsName() + "]库存不能为负数");
            int diff = newStock - oldStock;
            if (diff != 0) {
                StockLog log = new StockLog();
                log.setStockId(row.getId());
                log.setChangeType(diff > 0 ? "in" : "out");
                log.setQuantity(Math.abs(diff));
                log.setRemark("批量调库存：" + oldStock + " → " + newStock);
                log.setOperator("admin");
                stockLogService.save(log);
                logCount++;
            }
        }
        boolean ret = stockService.updateBatchById(list);
        if (!ret) {
            throw new RuntimeException("批量更新库存失败");
        }
        return R.ok().put("msg", "成功更新" + list.size() + "条库存，写流水" + logCount + "条");
    }

    /* 6.库存预警列表(验收④)：当前库存 <= 安全库存 的所有SKU */
    @GetMapping("/warning")
    public R warning() {
        LambdaQueryWrapper<Stock> qw = new LambdaQueryWrapper<>();
        qw.apply("stock <= safe_stock")
          .orderByAsc(Stock::getStock);
        List<Stock> list = stockService.list(qw);
        return R.ok(list);
    }

    /* 7.库存盘点：提交实盘数量，自动算盘盈盘亏并写流水 */
    @PostMapping("/check")
    public R check(@RequestBody Map<String, Object> body) {
        Integer id = (Integer) body.get("id");
        Integer actual = body.get("actualStock") == null ? 0 : Integer.parseInt(body.get("actualStock").toString());
        Stock stock = stockService.getById(id);
        if (stock == null) throw new RuntimeException("库存记录不存在");
        int book = stock.getStock() == null ? 0 : stock.getStock();
        if (actual < 0) throw new RuntimeException("实盘数量不能为负数");
        int diff = actual - book;
        if (diff == 0) {
            return R.ok().put("msg", "账实相符，无需调整").put("diff", 0);
        }
        /* 更新库存为实盘数 */
        stock.setStock(actual);
        stockService.updateById(stock);
        /* 写盘点流水：盘盈为in，盘亏为out */
        StockLog log = new StockLog();
        log.setStockId(stock.getId());
        log.setChangeType(diff > 0 ? "in" : "out");
        log.setQuantity(Math.abs(diff));
        log.setRemark("库存盘点：账面" + book + "，实盘" + actual + (diff > 0 ? "(盘盈)" : "(盘亏)"));
        log.setOperator("盘点员");
        stockLogService.save(log);

        /* 盘亏后检查预警 */
        int safe = stock.getSafeStock() == null ? 0 : stock.getSafeStock();
        if (diff < 0 && safe > 0 && actual <= safe) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            com.gec.domain.entity.Message warn = new com.gec.domain.entity.Message();
            warn.setMemberId(0);
            warn.setTitle("库存预警：" + stock.getGoodsName());
            warn.setContent("盘点后商品【" + stock.getGoodsName() + "】库存仅剩 " + actual + " 件，低于安全库存 " + safe + " 件");
            warn.setType("system");
            warn.setIsRead(0);
            warn.setSendTime(sdf.format(new java.util.Date()));
            messageService.save(warn);
        }
        return R.ok().put("msg", "盘点完成，差异" + (diff > 0 ? "+" : "") + diff).put("diff", diff);
    }

    @ExceptionHandler
    public void exceptionHandler(Exception e, HttpServletResponse resp) {
        R.err(e).write(resp);
    }
}
