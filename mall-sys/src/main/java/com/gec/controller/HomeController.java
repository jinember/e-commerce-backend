package com.gec.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gec.components.FileTemplate;
import com.gec.domain.entity.Member;
import com.gec.domain.entity.OrderInfo;
import com.gec.domain.entity.GoodsDetail;
import com.gec.service.IGoodsDetailService;
import com.gec.service.IMemberService;
import com.gec.service.IOrderInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/Home")
public class HomeController extends BaseController {

    @Autowired
    private IGoodsDetailService goodsDetailService;

    @Autowired
    private IMemberService memberService;

    @Autowired
    private IOrderInfoService orderInfoService;

    @Autowired
    private FileTemplate fileTemplate;
    @Override
    protected FileTemplate getFileTemplate() { return fileTemplate; }

    /* 首页统计：商品总数、今日订单、注册用户、今日销售额 */
    @GetMapping("/stats")
    public R stats() {
        Map<String, Object> map = new HashMap<>();
        long goodsCount = goodsDetailService.count();
        map.put("goodsCount", goodsCount);

        long memberCount = memberService.count();
        map.put("memberCount", memberCount);

        QueryWrapper<OrderInfo> todayQw = new QueryWrapper<>();
        todayQw.apply("DATE(create_date) = CURDATE()");
        long todayOrders = orderInfoService.count(todayQw);
        map.put("todayOrders", todayOrders);

        /* 统计口径：仅成交订单(1已支付/2已发货/3已完成)，排除待支付·退款中·已退款 */
        QueryWrapper<OrderInfo> payQw = new QueryWrapper<>();
        payQw.select("IFNULL(SUM(total_price),0) as totalPrice")
             .apply("DATE(create_date) = CURDATE()")
             .in("status", java.util.Arrays.asList(1, 2, 3));
        OrderInfo one = orderInfoService.getOne(payQw);
        double sales = (one != null && one.getTotalPrice() != null) ? one.getTotalPrice().doubleValue() : 0;
        map.put("todaySales", sales);

        return R.ok(map);
    }
}
