package com.gec.controller;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gec.components.FileTemplate;
import com.gec.domain.entity.Message;
import com.gec.service.IMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/Message")
public class MessageController extends BaseController {

    @Autowired
    private IMessageService Service;
    @Autowired
    private FileTemplate fileTemplate;

    @Override
    protected FileTemplate getFileTemplate() { return fileTemplate; }

    @PostMapping("/list/{page}/{limit}")
    public R list(@PathVariable Integer page, @PathVariable Integer limit, @RequestBody Message param) {
        Page<Message> p = new Page<>(page, limit);
        return R.convertPage(Service.listMessage(p));
    }

    @PostMapping("/save")
    public R save(@RequestBody Message obj) { Service.saveOrUpdate(obj); return R.ok(); }

    @PostMapping("/delete/{id}")
    public R delete(@PathVariable Integer id) { Service.removeById(id); return R.ok(); }

    @GetMapping("/{id}")
    public R getById(@PathVariable Integer id) { return R.ok(Service.getById(id)); }
}