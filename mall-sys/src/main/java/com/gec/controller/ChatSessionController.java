package com.gec.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gec.components.FileTemplate;
import com.gec.domain.entity.ChatMessage;
import com.gec.domain.entity.ChatSession;
import com.gec.domain.entity.User;
import com.gec.service.IChatMessageService;
import com.gec.service.IChatSessionService;
import com.gec.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 客服会话：3.列表中可查看会话质量，/statByChatType 供会话质量看板使用
 */
@RestController
@RequestMapping("/ChatSession")
public class ChatSessionController extends BaseController {

    @Autowired
    private IChatSessionService Service;
    @Autowired
    private IChatMessageService chatMessageService;
    @Autowired
    private IUserService userService;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private FileTemplate fileTemplate;

    @Override
    protected FileTemplate getFileTemplate() { return fileTemplate; }

    /* 1.分页列表（带会员昵称；支持按 id/会话编号/会员昵称/咨询类型 检索） */
    @PostMapping("/list/{page}/{limit}")
    public R list(@PathVariable Integer page, @PathVariable Integer limit,
                  @RequestBody(required = false) ChatSession param) {
        Page<ChatSession> p = new Page<>(page, limit);
        return R.convertPage(Service.listChatSession(p, param));
    }

    /* 2.会话质量看板：按咨询类型统计会话数、转化率、平均响应/时长 */
    @GetMapping("/statByChatType")
    public R statByChatType() {
        Map<String, Object> ret = new HashMap<>();
        ret.put("rows", Service.statByChatType());
        return R.ok(ret);
    }

    /* 2.1 待回复会话数（最后一条消息是会员发的，商家还没回） */
    @GetMapping("/pendingCount")
    public R pendingCount() { return R.ok().put("count", Service.countPending()); }

    /* 2.2 待回复会话列表 */
    @PostMapping("/pendingList/{page}/{limit}")
    public R pendingList(@PathVariable Integer page, @PathVariable Integer limit) {
        return R.convertPage(Service.listPending(new Page<>(page, limit)));
    }

    /* ============ 客服聊天工作台 ============ */

    /* 8.聊天列表：按最后消息时间倒序，带最后消息/未读数 */
    @GetMapping("/chatList/{page}/{limit}")
    public R chatList(@PathVariable Integer page, @PathVariable Integer limit) {
        return R.convertPage(Service.chatList(new Page<>(page, limit)));
    }

    /* 9.未读会话总数（存在会员新消息且客服未读） */
    @GetMapping("/chatUnreadTotal")
    public R chatUnreadTotal() {
        return R.ok().put("count", Service.countUnreadSessions());
    }

    /* 10.打开会话：返回对话明细 + 标记该会话已读（更新 staff_read_time）+ 顺手刷新转化 */
    @GetMapping("/chatOpen/{id}")
    public R chatOpen(@PathVariable Integer id) {
        ChatSession cs = Service.getById(id);
        if (cs != null) {
            Integer before = cs.getIsConverted();
            refreshConvert(cs);
            if (!java.util.Objects.equals(before, cs.getIsConverted())) Service.updateById(cs);
            cs.setStaffReadTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            Service.updateById(cs);
        }
        Map<String, Object> ret = new HashMap<>();
        ret.put("session", cs);
        ret.put("rows", chatMessageService.listBySession(id));
        return R.ok(ret);
    }

    /* 3.新增/修改 */
    @PostMapping("/save")
    public R save(@RequestBody ChatSession obj) { Service.saveOrUpdate(obj); return R.ok(); }

    /* 4.删除 */
    @PostMapping("/delete/{id}")
    public R delete(@PathVariable Integer id) { Service.removeById(id); return R.ok(); }

    /* 5.详情 */
    @GetMapping("/{id}")
    public R getById(@PathVariable Integer id) { return R.ok(Service.getById(id)); }

    /* 6.某会话的完整对话明细（管理端「查看对话」抽屉）；打开时顺手刷新转化标记 */
    @GetMapping("/messages/{id}")
    public R messages(@PathVariable Integer id) {
        ChatSession cs = Service.getById(id);
        if (cs != null) {
            Integer before = cs.getIsConverted();
            refreshConvert(cs);
            if (!java.util.Objects.equals(before, cs.getIsConverted())) Service.updateById(cs);
        }
        Map<String, Object> ret = new HashMap<>();
        ret.put("session", cs);
        ret.put("rows", chatMessageService.listBySession(id));
        return R.ok(ret);
    }

    /* 7.客服回复：写入一条 merchant 消息（记录处理人），并同步会话计数 / 首响 / 时长 */
    @PostMapping("/reply")
    @Transactional
    public R reply(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Integer sid = body.get("sessionId") == null ? 0 : Integer.parseInt(String.valueOf(body.get("sessionId")).trim());
        String content = body.get("content") == null ? null : String.valueOf(body.get("content")).trim();
        if (sid <= 0 || content == null || content.isEmpty()) {
            return R.ok().put("result", "failed").put("cause", "会话或回复内容为空");
        }
        ChatSession cs = Service.getById(sid);
        if (cs == null) return R.ok().put("result", "failed").put("cause", "会话不存在");

        /* 处理人 = 当前登录的后台账号（前端把 account 放在 operator 请求头里） */
        String operator = request.getHeader("operator");
        if (operator == null || operator.trim().isEmpty()) operator = request.getHeader("account");
        User staff = findStaff(operator);
        Integer staffId = staff == null ? null : staff.getId();

        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date now = new Date();
        String ts = fmt.format(now);
        if (cs.getStartTime() == null) cs.setStartTime(ts);

        ChatMessage m = new ChatMessage();
        m.setSessionId(cs.getId());
        m.setChatNo(cs.getChatNo());
        m.setMemberId(cs.getMemberId());
        m.setSender("merchant");
        m.setMsgType("text");
        m.setContent(content.length() > 200 ? content.substring(0, 200) : content);
        m.setSendTime(ts);
        m.setStaffId(staffId);
        chatMessageService.save(m);

        cs.setMerchantReplyCount((cs.getMerchantReplyCount() == null ? 0 : cs.getMerchantReplyCount()) + 1);
        cs.setStaffId(staffId);
        cs.setStaffReadTime(ts);        // 回复后视为已读
        cs.setEndTime(ts);
        long start;
        try { start = fmt.parse(cs.getStartTime()).getTime(); } catch (Exception e) { start = now.getTime(); }
        cs.setSessionDurationSec((int) Math.max(1, (now.getTime() - start) / 1000));
        if (cs.getFirstResponseSec() == null || cs.getFirstResponseSec() <= 0) {
            cs.setFirstResponseSec((int) Math.max(1, (now.getTime() - start) / 1000));
        }
        /* 回复后刷新转化标记：会话起 7 天内，该会员对该商品是否真发生了 下单/加购/收藏 */
        refreshConvert(cs);
        Service.updateById(cs);

        Map<String, Object> ret = new HashMap<>();
        ret.put("session", cs);
        ret.put("staffName", staff == null ? "" : staff.getNickName());
        ret.put("rows", chatMessageService.listBySession(sid));
        return R.ok(ret);
    }

    /* 会话转化判定：会话起 7 天内，该会员对该商品「最早发生」的下单/加购/收藏
       —— 与历史 15000 条会话同一口径（见 tools/fix_p7.py） */
    private void refreshConvert(ChatSession cs) {
        if (cs == null || cs.getMemberId() == null || cs.getSpuId() == null
                || cs.getStartTime() == null) return;
        String from = cs.getStartTime();
        String to = plusDays(from, 7);
        try {
            List<Map<String, Object>> hit = jdbcTemplate.queryForList(
                    "SELECT kind, t FROM ("
                  + "  SELECT '下单' AS kind, create_date AS t FROM tbl_order_info"
                  + "    WHERE member_id=? AND goods_id=? AND create_date>=? AND create_date<=?"
                  + "  UNION ALL"
                  + "  SELECT '加购' AS kind, create_date AS t FROM tbl_user_behavior"
                  + "    WHERE member_id=? AND spu_id=? AND behavior_type='加入购物车' AND create_date>=? AND create_date<=?"
                  + "  UNION ALL"
                  + "  SELECT '收藏' AS kind, create_date AS t FROM tbl_user_behavior"
                  + "    WHERE member_id=? AND spu_id=? AND behavior_type='收藏' AND create_date>=? AND create_date<=?"
                  + ") x ORDER BY t LIMIT 1",
                    cs.getMemberId(), cs.getSpuId(), from, to,
                    cs.getMemberId(), cs.getSpuId(), from, to,
                    cs.getMemberId(), cs.getSpuId(), from, to);
            if (hit == null || hit.isEmpty()) {
                cs.setIsConverted(0);
                cs.setConvertAction(null);
            } else {
                cs.setIsConverted(1);
                cs.setConvertAction(String.valueOf(hit.get(0).get("kind")));
            }
        } catch (Exception e) {
            /* 判定失败不影响主流程 */
        }
    }

    /* yyyy-MM-dd HH:mm:ss 加 n 天 */
    private String plusDays(String time, int days) {
        try {
            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date d = f.parse(time);
            return f.format(new Date(d.getTime() + days * 24L * 3600 * 1000));
        } catch (Exception e) {
            return time;
        }
    }

    /* 按账号找后台用户；找不到就退回默认客服 kefu01 */
    private User findStaff(String account) {
        if (account != null && !account.trim().isEmpty()) {
            User u = userService.getOne(new LambdaQueryWrapper<User>()
                    .eq(User::getAccount, account.trim()).last("LIMIT 1"), false);
            if (u != null) return u;
        }
        return userService.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getAccount, "kefu01").last("LIMIT 1"), false);
    }
}
