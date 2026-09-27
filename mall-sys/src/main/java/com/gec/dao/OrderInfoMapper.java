package com.gec.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gec.domain.entity.OrderInfo;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface OrderInfoMapper extends BaseMapper<OrderInfo> {

    /* 1.销售统计：汇总(总销售额/订单数/销量)，统计口径：仅成交订单(已支付/已发货/已完成)，排除待支付·退款中·已退款。 */
    @Select("SELECT IFNULL(SUM(total_price),0) AS totalSales, " +
            "COUNT(*) AS orderCount, IFNULL(SUM(count),0) AS totalCount " +
            "FROM tbl_order_info WHERE status IN (1,2,3)")
    Map<String, Object> summaryTotal();

    /* 2.销售统计：按状态统计订单数量。 */
    @Select("SELECT status, COUNT(*) AS num FROM tbl_order_info GROUP BY status")
    List<Map<String, Object>> countByStatus();

    /* 3.销售统计：商品销售额排行。 */
    @Select("SELECT goods_name AS goodsName, IFNULL(SUM(total_price),0) AS sales, " +
            "SUM(count) AS cnt FROM tbl_order_info WHERE status IN (1,2,3) " +
            "GROUP BY goods_name ORDER BY sales DESC")
    List<Map<String, Object>> topGoods();

    /* 4.按会员统计订单数与累计消费(排除取消订单)。 */
    @Select("SELECT member_id, COUNT(*) AS orderCount, IFNULL(SUM(total_price),0) AS totalSpend FROM tbl_order_info WHERE status IN (1,2,3) GROUP BY member_id")
    List<Map<String, Object>> statByMember();

    /* ===== 以下 3 个给首页 Dashboard 用（原来首页图表是前端写死的假数据） ===== */

    /* 5.按月销售趋势（统计口径：仅成交订单(已支付/已发货/已完成)，排除待支付·退款中·已退款） */
    @Select("SELECT DATE_FORMAT(create_date,'%Y-%m') AS month, " +
            "IFNULL(SUM(total_price),0) AS sales, COUNT(*) AS orderCount " +
            "FROM tbl_order_info WHERE status IN (1,2,3) AND create_date IS NOT NULL " +
            "GROUP BY month ORDER BY month")
    List<Map<String, Object>> monthlySales();

    /* 6.按季度销售额（统计口径：仅成交订单(已支付/已发货/已完成)，排除待支付·退款中·已退款） */
    @Select("SELECT CONCAT(YEAR(create_date),'-Q',QUARTER(create_date)) AS quarter, " +
            "IFNULL(SUM(total_price),0) AS sales, COUNT(*) AS orderCount " +
            "FROM tbl_order_info WHERE status IN (1,2,3) AND create_date IS NOT NULL " +
            "GROUP BY quarter ORDER BY quarter")
    List<Map<String, Object>> quarterSales();

    /* 7.按商品品类统计销售额 Top5（订单 -> SPU -> 品类，统计口径：仅成交订单(已支付/已发货/已完成)，排除待支付·退款中·已退款） */
    @Select("SELECT IFNULL(c.category_name,'未分类') AS categoryName, " +
            "IFNULL(SUM(o.total_price),0) AS sales, IFNULL(SUM(o.`count`),0) AS cnt " +
            "FROM tbl_order_info o " +
            "LEFT JOIN tbl_spu_detail s ON o.goods_id = s.id " +
            "LEFT JOIN tbl_goods_category c ON s.category_id = c.id " +
            "WHERE o.status IN (1,2,3) " +
            "GROUP BY c.id, c.category_name ORDER BY sales DESC LIMIT 5")
    List<Map<String, Object>> categorySales();
}
