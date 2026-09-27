package com.gec.controller;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gec.components.FileTemplate;
import com.gec.domain.entity.Ticket;
import com.gec.domain.entity.Message;
import com.gec.service.ITicketService;
import com.gec.service.IMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
@RequestMapping("/Ticket")
public class TicketController extends BaseController {

    @Autowired
    private ITicketService Service;
    @Autowired
    private IMessageService messageService;
    @Autowired
    private FileTemplate fileTemplate;

    @Override
    protected FileTemplate getFileTemplate() { return fileTemplate; }

    @PostMapping("/list/{page}/{limit}")
    public R list(@PathVariable Integer page, @PathVariable Integer limit, @RequestBody Ticket param) {
        Page<Ticket> p = new Page<>(page, limit);
        return R.convertPage(Service.listTicket(p));
    }

    @PostMapping("/save")
    public R save(@RequestBody Ticket obj) {
        Service.saveOrUpdate(obj);

        /* 【联动】如果回复了工单，写站内消息通知用户 */
        if (obj.getReply() != null && obj.getReply().length() > 0 && obj.getMemberId() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Message msg = new Message();
            msg.setMemberId(obj.getMemberId());
            msg.setTitle("客服已回复您的工单");
            msg.setContent("您提交的工单【" + obj.getTitle() + "】客服已回复：" + obj.getReply());
            msg.setType("system");
            msg.setIsRead(0);
            msg.setSendTime(sdf.format(new Date()));
            messageService.save(msg);
        }

        return R.ok();
    }

    @PostMapping("/delete/{id}")
    public R delete(@PathVariable Integer id) { Service.removeById(id); return R.ok(); }

    @GetMapping("/{id}")
    public R getById(@PathVariable Integer id) { return R.ok(Service.getById(id)); }
}