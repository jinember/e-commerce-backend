package com.gec.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gec.components.FileTemplate;
import com.gec.domain.entity.Refund;
import com.gec.service.IRefundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/Refund")
public class RefundController extends BaseController {

    @Autowired
    private IRefundService refundService;

    @Autowired
    private FileTemplate fileTemplate;
    @Override
    protected FileTemplate getFileTemplate() { return fileTemplate; }

    @PostMapping("/list/{page}/{limit}")
    public R list(@PathVariable("page") Integer page, @PathVariable("limit") Integer limit, @RequestBody Refund param) {
        Page pageObj = newPage(page, limit);
        IPage<Refund> retPage = refundService.listRefund(pageObj, param);
        return R.convertPage(retPage);
    }

    @PostMapping("/addRefund")
    @Transactional(rollbackFor = Exception.class)
    public R add(@RequestBody Refund obj) {
        if (obj.getOrderId() == null) throw new RuntimeException("订单不能为空");

        /* 幂等校验：同一订单已有退款申请时，不允许重复提交 */
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Refund> qw =
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        qw.eq(Refund::getOrderId, obj.getOrderId());
        qw.orderByDesc(Refund::getId);
        Refund exist = refundService.getOne(qw, false);
        if (exist != null && exist.getStatus() != null) {
            if (exist.getStatus() == 0) {
                return R.ok().put("result", "failed").put("cause", "该订单已提交退款申请，请等待处理");
            }
            if (exist.getStatus() == 1) {
                return R.ok().put("result", "failed").put("cause", "该订单已完成退款，无需重复申请");
            }
            /* status==2（已拒绝）允许重新申请退款 */
        }

        /* 补全申请信息 */
        String ts = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
        obj.setStatus(0);              /* 0=待审核 */
        obj.setApplyTime(ts);
        if (obj.getReason() == null || obj.getReason().trim().isEmpty()) {
            obj.setReason("七天无理由");
        }
        boolean ret = refundService.save(obj);
        if (!ret) throw new RuntimeException("添加失败");

        /* 订单状态改为退款中(4) */
        com.gec.domain.entity.OrderInfo order = orderInfoService.getById(obj.getOrderId());
        if (order != null && (order.getStatus() == 1 || order.getStatus() == 2)) {
            order.setStatus(4);
            orderInfoService.updateById(order);
        }

        /* 【联动】站内消息：通知管理员处理退款 */
        com.gec.domain.entity.Message msg = new com.gec.domain.entity.Message();
        msg.setMemberId(0); /* 0=管理员 */
        msg.setTitle("退款申请：订单 " + (order != null ? order.getOrderNo() : ""));
        msg.setContent("用户申请退款，原因：" + obj.getReason() + "，金额：¥" + obj.getAmount() + "，请及时处理。");
        msg.setType("system");
        msg.setIsRead(0);
        msg.setSendTime(ts);
        messageService.save(msg);

        return R.ok().put("msg", "退款申请已提交，等待管理员审核");
    }

    @PutMapping("/updateRefund")
    public R update(@RequestBody Refund obj) {
        boolean ret = refundService.updateById(obj);
        if (!ret) throw new RuntimeException("更新失败");
        return R.ok();
    }

    @DeleteMapping("/deleteRefund/{id}")
    public R delete(@PathVariable("id") Integer id) {
        boolean ret = refundService.removeById(id);
        if (!ret) throw new RuntimeException("删除失败");
        return R.ok();
    }

    /* 同意退款：执行真正的退款逻辑 */
    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/approve/{id}")
    public R approve(@PathVariable("id") Integer id) {
        Refund refund = refundService.getById(id);
        if (refund == null) throw new RuntimeException("退款记录不存在");

        /* 更新退款状态为已退款 */
        refund.setStatus(1);
        refundService.updateById(refund);

        /* 查订单 */
        com.gec.domain.entity.OrderInfo order = orderInfoService.getById(refund.getOrderId());
        if (order != null) {
            /* 订单状态改为已退款(5) */
            order.setStatus(5);
            orderInfoService.updateById(order);

            /* 【联动】退款回滚：库存加回去 + 写入库流水 */
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.gec.domain.entity.Stock> sw =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            sw.eq(com.gec.domain.entity.Stock::getSkuId, order.getSkuId());
            com.gec.domain.entity.Stock s = stockService.getOne(sw);
            if (s != null) {
                int cur = s.getStock() == null ? 0 : s.getStock();
                int cnt = order.getCount() == null ? 1 : order.getCount();
                s.setStock(cur + cnt);
                stockService.updateById(s);
                com.gec.domain.entity.StockLog log = new com.gec.domain.entity.StockLog();
                log.setStockId(s.getId());
                log.setChangeType("in");
                log.setQuantity(cnt);
                log.setRemark("退款入库: " + order.getOrderNo());
                log.setOperator("admin");
                stockLogService.save(log);
            }

            /* 【联动】退款回退：商品销量减回去 */
            com.gec.domain.entity.SkuInfo sku = skuInfoService.getById(order.getSkuId());
            if (sku != null) {
                int oldSale = sku.getSaleCount() == null ? 0 : sku.getSaleCount();
                sku.setSaleCount(Math.max(0, oldSale - order.getCount()));
                skuInfoService.updateById(sku);
            }

            /* 【联动】退款回退：支付流水改成已退款 */
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.gec.domain.entity.Payment> pw =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            pw.eq(com.gec.domain.entity.Payment::getOrderId, order.getId());
            com.gec.domain.entity.Payment pay = paymentService.getOne(pw);
            if (pay != null) {
                pay.setPayStatus(2); /* 2=已退款 */
                paymentService.updateById(pay);
            }

            /* 【联动】退款回退：扣回之前送的积分（balance 记「扣减后的累计余额」，并同步会员积分） */
            int refundPoints = (int)(order.getTotalPrice().doubleValue() * 1);
            Integer rmid = order.getMemberId() == null ? 1 : order.getMemberId();
            com.gec.domain.entity.Member rmb = memberService.getById(rmid);
            int oldPts = (rmb == null || rmb.getPoints() == null) ? 0 : rmb.getPoints();
            int newPts = Math.max(0, oldPts - refundPoints);
            com.gec.domain.entity.PointLog pointLog = new com.gec.domain.entity.PointLog();
            pointLog.setMemberId(rmid);
            pointLog.setOrderId(order.getId());
            pointLog.setChangeType("subtract");
            pointLog.setPoints(refundPoints);
            pointLog.setBalance(newPts);
            pointLog.setRemark("退款扣回积分: " + order.getOrderNo());
            pointLogService.save(pointLog);
            if (rmb != null) {
                rmb.setPoints(newPts);
                memberService.updateById(rmb);
            }

            /* 【联动】站内消息：通知用户退款成功 */
            com.gec.domain.entity.Message msg = new com.gec.domain.entity.Message();
            msg.setMemberId(order.getMemberId());
            msg.setTitle("退款成功");
            msg.setContent("您的订单 " + order.getOrderNo() + " 退款已成功，金额¥" + refund.getAmount() + " 将原路退回。");
            msg.setType("system");
            msg.setIsRead(0);
            msg.setSendTime(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));
            messageService.save(msg);
        }

        return R.ok().put("msg", "退款成功");
    }

    /* 拒绝退款：订单状态恢复为已发货/已付款 */
    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/reject/{id}")
    public R reject(@PathVariable("id") Integer id) {
        Refund refund = refundService.getById(id);
        if (refund == null) throw new RuntimeException("退款记录不存在");

        /* 更新退款状态为已拒绝 */
        refund.setStatus(2);
        refundService.updateById(refund);

        /* 订单状态恢复为已发货(2) */
        com.gec.domain.entity.OrderInfo order = orderInfoService.getById(refund.getOrderId());
        if (order != null) {
            order.setStatus(2);
            orderInfoService.updateById(order);
        }

        /* 【联动】站内消息：通知用户退款被拒绝 */
        com.gec.domain.entity.Message msg = new com.gec.domain.entity.Message();
        msg.setMemberId(order != null ? order.getMemberId() : 1);
        msg.setTitle("退款申请被拒绝");
        msg.setContent("您的订单 " + (order != null ? order.getOrderNo() : "") + " 退款申请未通过，如有疑问请联系客服。");
        msg.setType("system");
        msg.setIsRead(0);
        msg.setSendTime(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));
        messageService.save(msg);

        return R.ok().put("msg", "已拒绝退款申请");
    }

    @Autowired
    private com.gec.service.IOrderInfoService orderInfoService;
    @Autowired
    private com.gec.service.IStockService stockService;
    @Autowired
    private com.gec.service.IStockLogService stockLogService;
    @Autowired
    private com.gec.service.ISkuInfoService skuInfoService;
    @Autowired
    private com.gec.service.IPaymentService paymentService;
    @Autowired
    private com.gec.service.IPointLogService pointLogService;
    @Autowired
    private com.gec.service.IMessageService messageService;
    @Autowired
    private com.gec.service.IMemberService memberService;

    @ExceptionHandler
    public void exceptionHandler(Exception e, HttpServletResponse resp) { R.err(e).write(resp); }
}



