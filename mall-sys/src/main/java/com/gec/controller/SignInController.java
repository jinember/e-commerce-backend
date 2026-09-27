package com.gec.controller;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gec.components.FileTemplate;
import com.gec.domain.entity.SignIn;
import com.gec.service.ISignInService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/SignIn")
public class SignInController extends BaseController {

    @Autowired
    private ISignInService Service;
    @Autowired
    private FileTemplate fileTemplate;
    @Autowired
    private com.gec.dao.SignRuleMapper signRuleMapper;

    @Override
    protected FileTemplate getFileTemplate() { return fileTemplate; }

    @PostMapping("/list/{page}/{limit}")
    public R list(@PathVariable Integer page, @PathVariable Integer limit, @RequestBody SignIn param) {
        Page<SignIn> p = new Page<>(page, limit);
        return R.convertPage(Service.listSignIn(p));
    }

    @PostMapping("/save")
    public R save(@RequestBody SignIn obj) { Service.saveOrUpdate(obj); return R.ok(); }

    @PostMapping("/delete/{id}")
    public R delete(@PathVariable Integer id) { Service.removeById(id); return R.ok(); }

    @GetMapping("/{id}")
    public R getById(@PathVariable Integer id) { return R.ok(Service.getById(id)); }

    @GetMapping("/rule")
    public R rule() {
        return R.ok(signRuleMapper.selectById(1));
    }

    @PostMapping("/saveRule")
    public R saveRule(@RequestBody com.gec.domain.entity.SignRule rule) {
        rule.setId(1);
        if (signRuleMapper.selectById(1) != null) { signRuleMapper.updateById(rule); }
        else { signRuleMapper.insert(rule); }
        return R.ok();
    }
}
