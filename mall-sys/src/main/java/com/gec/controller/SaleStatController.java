package com.gec.controller;

import com.gec.components.FileTemplate;
import com.gec.dao.OrderInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/SaleStat")
public class SaleStatController extends BaseController {

    @Autowired
    private OrderInfoMapper orderInfoMapper;
    @Autowired
    private com.gec.dao.DeptMapper deptMapper;
    @Autowired
    private FileTemplate fileTemplate;

    @Override
    protected FileTemplate getFileTemplate() {
        return fileTemplate;
    }

    /* 1.销售汇总(总销售额/订单数/销量 + 各状态订单数)。 */
    @GetMapping("/summary")
    public R summary() {
        Map<String, Object> total = orderInfoMapper.summaryTotal();
        List<Map<String, Object>> byStatus = orderInfoMapper.countByStatus();
        return R.ok()
                .put("total", total)
                .put("byStatus", byStatus);
    }

    /* 2.商品销售额排行。 */
    @GetMapping("/topGoods")
    public R topGoods() {
        return R.ok(orderInfoMapper.topGoods());
    }

    /**
     * 3.首页 Dashboard 一张图要用的四组数据，一次拿齐，避免前端发 4 个请求。
     * 原来首页 4 个 echarts 图全是前端写死的数组，答辩一改数据就穿帮。
     */
    @GetMapping("/dashboard")
    public R dashboard() {
        return R.ok()
                .put("deptUsers", deptMapper.statUserCount())
                .put("quarterSales", orderInfoMapper.quarterSales())
                .put("monthlySales", orderInfoMapper.monthlySales())
                .put("categorySales", orderInfoMapper.categorySales());
    }
}
