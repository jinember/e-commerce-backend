package com.gec.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gec.components.FileTemplate;
import com.gec.domain.entity.GoodsDraft;
import com.gec.service.IGoodsDraftService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/GoodsDraft")
public class GoodsDraftController extends BaseController {

    @Autowired
    private IGoodsDraftService draftService;

    /* 草稿不涉及文件导入导出，按基类要求返回 null。 */
    @Override
    protected FileTemplate getFileTemplate() { return null; }

    /* 草稿列表（支持按草稿名模糊、按状态过滤）。 */
    @PostMapping("/list/{page}/{limit}")
    public R list(@PathVariable Integer page, @PathVariable Integer limit, @RequestBody(required = false) GoodsDraft param) {
        Page<GoodsDraft> pageObj = newPage(page, limit);
        LambdaQueryWrapper<GoodsDraft> qw = new LambdaQueryWrapper<>();
        if (param != null) {
            if (param.getDraftName() != null && !param.getDraftName().isEmpty()) {
                qw.like(GoodsDraft::getDraftName, param.getDraftName());
            }
            if (param.getStatus() != null) {
                qw.eq(GoodsDraft::getStatus, param.getStatus());
            }
        }
        qw.orderByDesc(GoodsDraft::getUpdateTime);
        return R.convertPage(draftService.page(pageObj, qw));
    }

    /* 【保存草稿】给当前发布流程的 Redis 缓存拍快照。 */
    @PostMapping("/saveFromCache")
    public R saveFromCache(@RequestBody Map<String, Object> body) {
        String pubKey = body.get("pubKey") == null ? null : body.get("pubKey").toString();
        String draftName = body.get("draftName") == null ? null : body.get("draftName").toString();
        Integer step = body.get("step") == null ? 1 : Integer.parseInt(body.get("step").toString());
        Integer id = draftService.saveFromCache(pubKey, draftName, step);
        return R.ok().put("id", id);
    }

    /* 【继续编辑】把草稿灌回 Redis，返回 pubKey。 */
    @PostMapping("/resume/{id}")
    public R resume(@PathVariable Integer id) {
        String pubKey = draftService.resume(id);
        return R.ok().put("pubKey", pubKey);
    }

    /* 【立即发布】把草稿灌回 Redis 后走发布逻辑写库。 */
    @PostMapping("/publish/{id}")
    public R publish(@PathVariable Integer id) {
        Integer spuId = draftService.publishNow(id);
        return R.ok().put("spuId", spuId);
    }

    /* 【定时发布】设置定时发布时间。 */
    @PostMapping("/schedule/{id}")
    public R schedule(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        Object t = body.get("publishTime");
        if (t == null || t.toString().isEmpty()) {
            throw new RuntimeException("请选择定时发布时间");
        }
        try {
            Date publishTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(t.toString());
            draftService.schedule(id, publishTime);
        } catch (java.text.ParseException pe) {
            throw new RuntimeException("时间格式不正确，应为 yyyy-MM-dd HH:mm:ss");
        }
        return R.ok();
    }

    /* 保存草稿（直接传 JSON 的通用接口，保留兼容）。 */
    @PostMapping("/save")
    public R save(@RequestBody Map<String, Object> body) {
        GoodsDraft draft = new GoodsDraft();
        if (body.get("id") != null) {
            draft.setId(Integer.parseInt(body.get("id").toString()));
        }
        draft.setDraftName((String) body.get("draftName"));
        draft.setSpuForm((String) body.get("spuForm"));
        draftService.saveOrUpdate(draft);
        return R.ok().put("id", draft.getId());
    }

    /* 获取草稿详情。 */
    @GetMapping("/get/{id}")
    public R get(@PathVariable Integer id) {
        return R.ok(draftService.getById(id));
    }

    /* 删除草稿。 */
    @DeleteMapping("/delete/{id}")
    public R delete(@PathVariable Integer id) {
        draftService.removeById(id);
        return R.ok();
    }

    @ExceptionHandler
    public void exceptionHandler(Exception e, HttpServletResponse resp) {
        R.err(e).write(resp);
    }
}
