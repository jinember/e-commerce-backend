package com.gec.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gec.components.FileTemplate;
import com.gec.domain.entity.Comment;
import com.gec.service.ICommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/Comment")
public class CommentController extends BaseController {

    @Autowired
    private ICommentService commentService;

    @Autowired
    private FileTemplate fileTemplate;
    @Override
    protected FileTemplate getFileTemplate() { return fileTemplate; }

    @PostMapping("/list/{page}/{limit}")
    public R list(@PathVariable("page") Integer page, @PathVariable("limit") Integer limit, @RequestBody Comment param) {
        Page pageObj = newPage(page, limit);
        IPage<Comment> retPage = commentService.listComment(pageObj, param);
        return R.convertPage(retPage);
    }

    @PostMapping("/addComment")
    public R add(@RequestBody Comment obj) {
        boolean ret = commentService.save(obj);
        if (!ret) throw new RuntimeException("添加失败");
        return R.ok();
    }

    @PutMapping("/updateComment")
    public R update(@RequestBody Comment obj) {
        boolean ret = commentService.updateById(obj);
        if (!ret) throw new RuntimeException("更新失败");
        return R.ok();
    }

    @DeleteMapping("/deleteComment/{id}")
    public R delete(@PathVariable("id") Integer id) {
        boolean ret = commentService.removeById(id);
        if (!ret) throw new RuntimeException("删除失败");
        return R.ok();
    }

    @ExceptionHandler
    public void exceptionHandler(Exception e, HttpServletResponse resp) { R.err(e).write(resp); }
}



