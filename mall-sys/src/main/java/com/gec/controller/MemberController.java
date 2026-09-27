package com.gec.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gec.components.FileTemplate;
import com.gec.domain.entity.Member;
import com.gec.service.IMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/Member")
public class MemberController extends BaseController {

    @Autowired
    private IMemberService memberService;

    @Autowired
    private com.gec.service.IPointLogService pointLogService;

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
                  @RequestBody Member param) {
        Page pageObj = newPage(page, limit);
        IPage<Member> retPage = memberService.listMember(pageObj, param);
        return R.convertPage(retPage);
    }

    /* 1.1 顶部统计卡片 */
    @GetMapping("/stat")
    public R stat() {
        return R.ok(memberService.statMember());
    }

    /* 2.新增会员 */
    @PostMapping("/addMember")
    public R addMember(@RequestBody Member member) {
        if (member.getRegisterTime() == null || member.getRegisterTime().isEmpty()) {
            member.setRegisterTime(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));
        }
        boolean ret = memberService.save(member);
        if (!ret) {
            throw new RuntimeException("添加会员失败");
        }
        return R.ok();
    }

    /* 3.更新会员 */
    @PutMapping("/updateMember")
    public R updateMember(@RequestBody Member member) {
        /* 先查旧积分 */
        Member old = memberService.getById(member.getId());
        Integer oldPoints = old != null && old.getPoints() != null ? old.getPoints() : 0;
        Integer newPoints = member.getPoints() != null ? member.getPoints() : oldPoints;
        Integer diff = newPoints - oldPoints;

        boolean ret = memberService.updateById(member);
        if (!ret) {
            throw new RuntimeException("更新会员失败");
        }

        /* 如果积分变了，写积分流水 */
        if (diff != 0 && pointLogService != null) {
            com.gec.domain.entity.PointLog log = new com.gec.domain.entity.PointLog();
            log.setMemberId(member.getId());
            log.setOrderId(0);
            log.setChangeType(diff > 0 ? "add" : "subtract");
            log.setPoints(Math.abs(diff));
            log.setBalance(newPoints);
            log.setRemark("管理员调整积分");
            pointLogService.save(log);
        }

        return R.ok();
    }

    /* 4.删除会员 */
    @DeleteMapping("/deleteMember/{id}")
    public R deleteMember(@PathVariable("id") Integer id) {
        boolean ret = memberService.removeById(id);
        if (!ret) {
            throw new RuntimeException("删除会员失败");
        }
        return R.ok();
    }

    /* 5.批量更新状态 */
    @PostMapping("/batchStatus")
    public R batchStatus(@RequestBody java.util.Map<String, Object> param) {
        @SuppressWarnings("unchecked")
        java.util.List<Integer> ids = (java.util.List<Integer>) param.get("ids");
        Integer status = (Integer) param.get("status");
        com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Member> wrapper =
            new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<>();
        wrapper.in(Member::getId, ids).set(Member::getStatus, status);
        boolean ret = memberService.update(wrapper);
        if (!ret) throw new RuntimeException("批量更新失败");
        return R.ok();
    }

    /* 6.批量删除 */
    @PostMapping("/batchDelete")
    public R batchDelete(@RequestBody java.util.Map<String, Object> param) {
        @SuppressWarnings("unchecked")
        java.util.List<Integer> ids = (java.util.List<Integer>) param.get("ids");
        boolean ret = memberService.removeByIds(ids);
        if (!ret) throw new RuntimeException("批量删除失败");
        return R.ok();
    }

    @ExceptionHandler
    public void exceptionHandler(Exception e, HttpServletResponse resp) {
        R.err(e).write(resp);
    }
}
