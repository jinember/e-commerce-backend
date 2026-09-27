package com.gec.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gec.components.FileTemplate;
import com.gec.domain.entity.OrderInfo;
import com.gec.domain.search.OrderSearch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gec.dao.PaymentMapper;
import com.gec.dao.LogisticsMapper;
import com.gec.dao.OrderOpLogMapper;
import com.gec.domain.entity.Payment;
import com.gec.domain.entity.Logistics;
import com.gec.domain.entity.OrderOpLog;
import com.gec.service.IOrderInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/Order")
public class OrderController extends BaseController {

    @Autowired
    private IOrderInfoService orderInfoService;
    @Autowired
    private FileTemplate fileTemplate;
    @Autowired
    private PaymentMapper paymentMapper;
    @Autowired
    private LogisticsMapper logisticsMapper;
    @Autowired
    private OrderOpLogMapper orderOpLogMapper;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private com.gec.service.IMessageService messageService;

    /* 记录一条订单操作日志 */
    private void writeLog(Integer orderId, String opType, Integer fromStatus, Integer toStatus, String remark){
        try {
            OrderOpLog log = new OrderOpLog();
            log.setOrderId(orderId);
            log.setOperator(request.getHeader("operator"));
            if(log.getOperator()==null || log.getOperator().trim().isEmpty()){
                log.setOperator("系统");
            }
            log.setOpType(opType);
            log.setFromStatus(fromStatus);
            log.setToStatus(toStatus);
            log.setRemark(remark);
            log.setCreateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            orderOpLogMapper.insert(log);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /* 状态码转中文，与前端保持一致 */
    private String statusName(Integer s){
        if(s==null) return "未知";
        switch(s){
            case 0: return "待支付";
            case 1: return "已支付";
            case 2: return "已发货";
            case 3: return "已完成";
            case 4: return "退款中";
            case 5: return "已退款";
            case 6: return "已取消";      /* 未付款超时关闭，不进退款流程，也不计入销售统计 */
            default: return "状态"+s;
        }
    }

    @Override
    protected FileTemplate getFileTemplate() {
        return fileTemplate;
    }

    /* 1.订单分页列表(带搜索)。 */
    @PostMapping("/list/{page}/{limit}")
    public R list(@PathVariable("page") Integer page,
                  @PathVariable("limit") Integer limit,
                  @RequestBody OrderSearch param) {
        /*1.封装分页对象*/
        Page frmnPage = newPage(page, limit);
        /*2.调用service查询*/
        IPage<OrderInfo> retPage = orderInfoService.listOrder(frmnPage, param);
        /*3.封装分页数据给前端*/
        return R.convertPage(retPage);
    }

    /* 2.更新订单状态(发货/完成/取消)。 */
    @PostMapping("/updateStatus")
    public R updateStatus(@RequestBody OrderInfo orderInfo) {
        /* 先查出原订单信息 */
        OrderInfo oldOrder = orderInfoService.getById(orderInfo.getId());
        orderInfoService.updateStatus(orderInfo);

        /* 如果是发货操作(status=2)，写物流记录和站内消息 */
        if (orderInfo.getStatus() != null && orderInfo.getStatus() == 2 && oldOrder != null) {
            /* 写物流记录 */
            Logistics logistics = new Logistics();
            logistics.setOrderId(orderInfo.getId());
            String[] companies = {"顺丰速运", "中通快递", "圆通速递", "韵达快递"};
            java.util.Random random = new java.util.Random();
            logistics.setCompany(companies[random.nextInt(companies.length)]);
            logistics.setTrackingNo("YT" + System.currentTimeMillis() + random.nextInt(10000));
            logistics.setShipTime(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));
            logistics.setStatus(1);
            logisticsMapper.insert(logistics);

            /* 写站内消息通知用户 */
            com.gec.domain.entity.Message msg = new com.gec.domain.entity.Message();
            msg.setMemberId(oldOrder.getMemberId());
            msg.setTitle("商品已发货");
            msg.setContent("您的订单 " + oldOrder.getOrderNo() + " 已发货，快递公司：" + logistics.getCompany() + "，运单号：" + logistics.getTrackingNo());
            msg.setType("logistics");
            msg.setIsRead(0);
            msg.setSendTime(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));
            // 注入messageService
            if (messageService != null) {
                messageService.save(msg);
            }
        }
        /* 写操作日志 */
        Integer oldStatus = (oldOrder != null) ? oldOrder.getStatus() : null;
        Integer newStatus = orderInfo.getStatus();
        String opType = (newStatus != null && newStatus == 2) ? "发货" : "状态变更";
        writeLog(orderInfo.getId(), opType, oldStatus, newStatus,
                "订单状态：" + statusName(oldStatus) + " → " + statusName(newStatus));
        return R.ok();
    }

    /* 3.订单详情 */
    @GetMapping("/{id}")
    public R detail(@PathVariable Integer id) {
        return R.ok(orderInfoService.getById(id));
    }

    /* 4.订单关联的支付记录 */
    @GetMapping("/payment/{orderId}")
    public R payment(@PathVariable Integer orderId) {
        LambdaQueryWrapper<Payment> qw = new LambdaQueryWrapper<>();
        qw.eq(Payment::getOrderId, orderId);
        return R.ok(paymentMapper.selectList(qw));
    }

    /* 5.订单关联的物流 */
    @GetMapping("/logistics/{orderId}")
    public R logistics(@PathVariable Integer orderId) {
        LambdaQueryWrapper<Logistics> qw = new LambdaQueryWrapper<>();
        qw.eq(Logistics::getOrderId, orderId);
        return R.ok(logisticsMapper.selectList(qw));
    }

    /* 6.保存订单备注 */
    @PostMapping("/saveRemark")
    public R saveRemark(@RequestBody OrderInfo order) {
        OrderInfo upd = new OrderInfo();
        upd.setId(order.getId());
        upd.setRemark(order.getRemark());
        orderInfoService.updateById(upd);
        writeLog(order.getId(), "修改备注", null, null, "订单备注：" + order.getRemark());
        return R.ok();
    }

    /* 7.批量改状态(ids + status) */
    @PostMapping("/batchStatus")
    public R batchStatus(@RequestBody Map<String, Object> body) {
        Object idsObj = body.get("ids");
        Object statusObj = body.get("status");
        if (idsObj == null || statusObj == null) return R.ok();
        Integer status = ((Number) statusObj).intValue();
        for (Object o : (List<?>) idsObj) {
            int oid = ((Number) o).intValue();
            OrderInfo old = orderInfoService.getById(oid);
            OrderInfo upd = new OrderInfo();
            upd.setId(oid);
            upd.setStatus(status);
            orderInfoService.updateById(upd);
            Integer oldStatus = (old != null) ? old.getStatus() : null;
            writeLog(oid, "批量操作", oldStatus, status,
                    "批量操作：状态 " + statusName(oldStatus) + " → " + statusName(status));
        }
        return R.ok();
    }

    /* 8.订单操作日志 */
    @GetMapping("/opLog/{orderId}")
    public R opLog(@PathVariable Integer orderId) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<OrderOpLog> qw =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        qw.eq(OrderOpLog::getOrderId, orderId).orderByDesc(OrderOpLog::getId);
        return R.ok(orderOpLogMapper.selectList(qw));
    }
}
