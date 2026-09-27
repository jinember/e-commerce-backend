package com.gec.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gec.components.FileTemplate;
import com.gec.domain.entity.StockLog;
import com.gec.domain.entity.Stock;
import com.gec.service.IStockLogService;
import com.gec.service.IStockService;
import com.gec.service.IMessageService;
import com.gec.domain.entity.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
@RequestMapping("/StockLog")
public class StockLogController extends BaseController {

    @Autowired
    private IStockLogService stocklogService;

    @Autowired
    private IStockService stockService;

    @Autowired
    private IMessageService messageService;

    @Autowired
    private FileTemplate fileTemplate;
    @Override
    protected FileTemplate getFileTemplate() { return fileTemplate; }

    @PostMapping("/list/{page}/{limit}")
    public R list(@PathVariable("page") Integer page, @PathVariable("limit") Integer limit, @RequestBody StockLog param) {
        Page pageObj = newPage(page, limit);
        IPage<StockLog> retPage = stocklogService.listStockLog(pageObj, param);
        return R.convertPage(retPage);
    }

    @PostMapping("/addStockLog")
    public R add(@RequestBody StockLog obj) {
        Stock stock = stockService.getById(obj.getStockId());
        if (stock == null) throw new RuntimeException("库存记录不存在");
        int qty = obj.getQuantity() == null ? 0 : obj.getQuantity();
        boolean out = "out".equals(obj.getChangeType()) || "出库".equals(obj.getChangeType());
        int cur = stock.getStock() == null ? 0 : stock.getStock();

        if (out) {
            /* 原子出库：UPDATE stock SET stock=stock-qty WHERE id=? AND stock>=qty */
            com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Stock> uw =
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<>();
            uw.eq(Stock::getId, stock.getId())
              .ge(Stock::getStock, qty)
              .setSql("stock = stock - " + qty);
            boolean ok = stockService.update(uw);
            if (!ok) throw new RuntimeException("出库失败：库存不足，当前库存 " + cur);
        } else {
            stock.setStock(cur + qty);
            stockService.updateById(stock);
        }
        boolean ret = stocklogService.save(obj);
        if (!ret) throw new RuntimeException("添加失败");

        /* 出库后检查预警 */
        if (out) {
            Stock after = stockService.getById(stock.getId());
            int newStock = after.getStock() == null ? 0 : after.getStock();
            int safe = after.getSafeStock() == null ? 0 : after.getSafeStock();
            if (safe > 0 && newStock <= safe) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Message warn = new Message();
                warn.setMemberId(0);
                warn.setTitle("库存预警：" + after.getGoodsName());
                warn.setContent("商品【" + after.getGoodsName() + "】库存仅剩 " + newStock + " 件，低于安全库存 " + safe + " 件");
                warn.setType("system");
                warn.setIsRead(0);
                warn.setSendTime(sdf.format(new Date()));
                messageService.save(warn);
            }
        }
        return R.ok();
    }

    /* 改流水只允许改备注/操作员，不允许改类型和数量，避免账实不符 */
    @PutMapping("/updateStockLog")
    public R update(@RequestBody StockLog obj) {
        StockLog exist = stocklogService.getById(obj.getId());
        if (exist == null) throw new RuntimeException("流水记录不存在");
        exist.setRemark(obj.getRemark());
        exist.setOperator(obj.getOperator());
        boolean ret = stocklogService.updateById(exist);
        if (!ret) throw new RuntimeException("更新失败");
        return R.ok();
    }

    @DeleteMapping("/deleteStockLog/{id}")
    public R delete(@PathVariable("id") Integer id) {
        boolean ret = stocklogService.removeById(id);
        if (!ret) throw new RuntimeException("删除失败");
        return R.ok();
    }

    @ExceptionHandler
    public void exceptionHandler(Exception e, HttpServletResponse resp) { R.err(e).write(resp); }
}



