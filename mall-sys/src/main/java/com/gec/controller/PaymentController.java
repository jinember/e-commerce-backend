package com.gec.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gec.components.FileTemplate;
import com.gec.domain.entity.Payment;
import com.gec.service.IPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/Payment")
public class PaymentController extends BaseController {

    @Autowired
    private IPaymentService paymentService;

    @Autowired
    private FileTemplate fileTemplate;
    @Override
    protected FileTemplate getFileTemplate() { return fileTemplate; }

    @PostMapping("/list/{page}/{limit}")
    public R list(@PathVariable("page") Integer page, @PathVariable("limit") Integer limit, @RequestBody Payment param) {
        Page pageObj = newPage(page, limit);
        IPage<Payment> retPage = paymentService.listPayment(pageObj, param);
        return R.convertPage(retPage);
    }

    @PostMapping("/addPayment")
    public R add(@RequestBody Payment obj) {
        boolean ret = paymentService.save(obj);
        if (!ret) throw new RuntimeException("添加失败");
        return R.ok();
    }

    @PutMapping("/updatePayment")
    public R update(@RequestBody Payment obj) {
        boolean ret = paymentService.updateById(obj);
        if (!ret) throw new RuntimeException("更新失败");
        return R.ok();
    }

    @DeleteMapping("/deletePayment/{id}")
    public R delete(@PathVariable("id") Integer id) {
        boolean ret = paymentService.removeById(id);
        if (!ret) throw new RuntimeException("删除失败");
        return R.ok();
    }

    @ExceptionHandler
    public void exceptionHandler(Exception e, HttpServletResponse resp) { R.err(e).write(resp); }
}



