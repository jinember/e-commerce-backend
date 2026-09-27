package com.gec.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 阶段二「大数据分析结果」读取层
 * 只读 ads_* 结果表与 tbl_etl_job_log，不参与任何写入。
 * 结果表由离线 ETL（Hive → Sqoop export）产出；空表时接口返回空集合，前端显示"等待 ETL 产出"。
 */
@Mapper
public interface AnalysisMapper {

    /* ---------- 模块1 全链路行为漏斗 ---------- */
    @Select("SELECT stat_date AS statDate, entry_page AS entryPage, browse_cnt AS browseCnt, "
            + "search_cnt AS searchCnt, favorite_cnt AS favoriteCnt, cart_cnt AS cartCnt, "
            + "browse_uv AS browseUv, cart_uv AS cartUv, order_uv AS orderUv, pay_uv AS payUv, "
            + "conv_view_to_cart AS convViewToCart, conv_cart_to_order AS convCartToOrder, "
            + "conv_order_to_pay AS convOrderToPay "
            + "FROM ads_user_funnel ORDER BY stat_date DESC, entry_page")
    List<Map<String, Object>> funnelRows();

    @Select("SELECT IFNULL(SUM(browse_cnt),0) AS browseCnt, IFNULL(SUM(search_cnt),0) AS searchCnt, "
            + "IFNULL(SUM(favorite_cnt),0) AS favoriteCnt, IFNULL(SUM(cart_cnt),0) AS cartCnt, "
            + "IFNULL(SUM(browse_uv),0) AS browseUv, IFNULL(SUM(cart_uv),0) AS cartUv, "
            + "IFNULL(SUM(order_uv),0) AS orderUv, IFNULL(SUM(pay_uv),0) AS payUv "
            + "FROM ads_user_funnel")
    Map<String, Object> funnelTotal();

    /* ---------- 模块2 订单分析 ---------- */
    @Select("SELECT stat_date AS statDate, category_id AS categoryId, category_name AS categoryName, "
            + "order_cnt AS orderCnt, pay_cnt AS payCnt, refund_cnt AS refundCnt, gmv, "
            + "avg_order_amount AS avgOrderAmount, refund_rate AS refundRate, "
            + "refund_avg_hours AS refundAvgHours "
            + "FROM ads_order_analysis ORDER BY stat_date DESC, gmv DESC")
    List<Map<String, Object>> orderRows();

    /* ---------- 模块3 评价分析 ---------- */
    @Select("SELECT stat_date AS statDate, row_type AS rowType, category_id AS categoryId, "
            + "category_name AS categoryName, rating, keyword, comment_cnt AS commentCnt, "
            + "good_rate AS goodRate, avg_rating AS avgRating "
            + "FROM ads_comment_analysis ORDER BY stat_date DESC, row_type, comment_cnt DESC")
    List<Map<String, Object>> commentRows();

    /* ---------- 模块4 用户与商家交流 ---------- */
    @Select("SELECT stat_date AS statDate, metric_scope AS metricScope, dim_value AS dimValue, "
            + "session_cnt AS sessionCnt, converted_cnt AS convertedCnt, convert_rate AS convertRate, "
            + "avg_first_response_sec AS avgFirstResponseSec, avg_duration_sec AS avgDurationSec, "
            + "avg_msg_cnt AS avgMsgCnt "
            + "FROM ads_chat_analysis ORDER BY stat_date DESC, metric_scope, dim_value")
    List<Map<String, Object>> chatRows();

    /* ---------- 需求补齐表8：时段分析（行为/订单/咨询 三模块共用） ---------- */
    @Select("SELECT module, hour_of_day AS hourOfDay, cnt, uv, amount "
            + "FROM ads_hourly_analysis ORDER BY module, hour_of_day")
    List<Map<String, Object>> hourlyRows();

    /* ---------- 需求补齐表9：搜索行为分析 ---------- */
    @Select("SELECT keyword, search_cnt AS searchCnt, search_uv AS searchUv, "
            + "no_result_cnt AS noResultCnt, no_result_rate AS noResultRate "
            + "FROM ads_search_analysis ORDER BY search_cnt DESC LIMIT 50")
    List<Map<String, Object>> searchRows();

    /* ---------- 需求补齐表10：浏览/收藏指标 + 热门榜单 ---------- */
    @Select("SELECT dim_type AS dimType, dim_value AS dimValue, cnt, uv, "
            + "avg_stay_sec AS avgStaySec "
            + "FROM ads_behavior_summary ORDER BY dim_type, cnt DESC")
    List<Map<String, Object>> behaviorSummaryRows();

    /* ---------- 需求补齐表11：订单流失分析 + 客单价分布 ---------- */
    @Select("SELECT stat_type AS statType, dim_value AS dimValue, order_cnt AS orderCnt, "
            + "amount, pct FROM ads_order_loss ORDER BY stat_type, order_cnt DESC")
    List<Map<String, Object>> orderLossRows();

    /* ---------- 需求补齐表12：商品口碑 TOP 榜 + 好/中/差三分类 ---------- */
    @Select("SELECT spu_id AS spuId, goods_name AS goodsName, category_name AS categoryName, "
            + "comment_cnt AS commentCnt, avg_rating AS avgRating, good_cnt AS goodCnt, "
            + "mid_cnt AS midCnt, bad_cnt AS badCnt, good_rate AS goodRate, "
            + "level_type AS levelType "
            + "FROM ads_goods_rating ORDER BY comment_cnt DESC, avg_rating ASC")
    List<Map<String, Object>> goodsRatingRows();

    @Select("SELECT level_type AS levelType, COUNT(*) AS goodsCnt, "
            + "IFNULL(SUM(comment_cnt),0) AS commentCnt "
            + "FROM ads_goods_rating GROUP BY level_type")
    List<Map<String, Object>> goodsRatingLevel();

    /* ---------- 需求补齐表13：客服服务质量 + 热门咨询商品 ---------- */
    @Select("SELECT dim_type AS dimType, dim_value AS dimValue, session_cnt AS sessionCnt, "
            + "user_msg_sum AS userMsgSum, merchant_reply_sum AS merchantReplySum, "
            + "bot_reply_sum AS botReplySum, solved_cnt AS solvedCnt, solve_rate AS solveRate, "
            + "avg_first_resp AS avgFirstResp, avg_duration AS avgDuration "
            + "FROM ads_chat_quality ORDER BY dim_type, session_cnt DESC")
    List<Map<String, Object>> chatQualityRows();

    /* ---------- ETL 任务执行日志 ---------- */
    @Select("SELECT id, job_name AS jobName, exec_phase AS execPhase, script_path AS scriptPath, "
            + "exec_cmd AS execCmd, source_table AS sourceTable, target_tables AS targetTables, "
            + "start_time AS startTime, end_time AS endTime, duration_sec AS durationSec, "
            + "rows_read AS rowsRead, rows_written AS rowsWritten, status, message "
            + "FROM tbl_etl_job_log ORDER BY start_time DESC, id DESC LIMIT 200")
    List<Map<String, Object>> jobLogRows();

    /* ---------- 模块5 会员画像 ---------- */
    @Select("SELECT dim_type AS dimType, dim_value AS dimValue, member_cnt AS memberCnt, pct "
            + "FROM ads_member_profile ORDER BY dim_type, member_cnt DESC")
    List<Map<String, Object>> memberProfileRows();

    /* ---------- 模块6 地域分布 ---------- */
    @Select("SELECT scope, city, member_cnt AS memberCnt, order_cnt AS orderCnt, gmv "
            + "FROM ads_region_dist ORDER BY scope, COALESCE(member_cnt, order_cnt) DESC")
    List<Map<String, Object>> regionRows();

    /* ---------- 模块7 商品分析 ---------- */
    @Select("SELECT ptype, name, sale_cnt AS saleCnt, gmv, sku_cnt AS skuCnt, category_id AS categoryId "
            + "FROM ads_product_analysis ORDER BY ptype, COALESCE(sale_cnt, sku_cnt) DESC")
    List<Map<String, Object>> productRows();

    /* =======================================================================
     * 以下为 2026-09-25 新增：清洗前后 A/B 对照 + 数据质量稽核
     * -----------------------------------------------------------------------
     * 对照组数据链路：Hive mall_ads_noclean（08_ads_noclean.hql 跳过 DWD 清洗直接聚合）
     *                → Sqoop export（09_export_noclean.sh）→ ads_noclean_* 7 张表
     * 稽核表链路：   Hive mall_dwd_dirty.dqc_clean_audit（03_dwd_clean.hql 产出）
     *                → Sqoop export（11_export_dqc.sh）→ dqc_clean_audit
     * 两侧表结构逐列一致（对照组表用 CREATE TABLE ... LIKE 复制的），所以能直接并排做差。
     * 对照组表为空时接口返回空集合，前端显示「等待 C 组导出」。
     * ======================================================================= */

    /* 对照组是否已有数据（0 = 还没跑 08/09，前端据此提示） */
    @Select("SELECT COUNT(*) FROM ads_noclean_user_funnel")
    int nocleanReady();

    /* A/B 关键指标并排对照：一行一个指标，左未清洗、右已清洗 */
    @Select("SELECT '入口页类别数' AS metric, "
            + "  (SELECT COUNT(DISTINCT entry_page) FROM ads_noclean_user_funnel) AS rawVal, "
            + "  (SELECT COUNT(DISTINCT entry_page) FROM ads_user_funnel)            AS cleanVal "
            + "UNION ALL SELECT '漏斗·浏览会员数', "
            + "  (SELECT IFNULL(SUM(browse_uv),0) FROM ads_noclean_user_funnel), "
            + "  (SELECT IFNULL(SUM(browse_uv),0) FROM ads_user_funnel) "
            + "UNION ALL SELECT '漏斗·加购会员数', "
            + "  (SELECT IFNULL(SUM(cart_uv),0) FROM ads_noclean_user_funnel), "
            + "  (SELECT IFNULL(SUM(cart_uv),0) FROM ads_user_funnel) "
            + "UNION ALL SELECT '漏斗·下单会员数', "
            + "  (SELECT IFNULL(SUM(order_uv),0) FROM ads_noclean_user_funnel), "
            + "  (SELECT IFNULL(SUM(order_uv),0) FROM ads_user_funnel) "
            + "UNION ALL SELECT '漏斗·支付会员数', "
            + "  (SELECT IFNULL(SUM(pay_uv),0) FROM ads_noclean_user_funnel), "
            + "  (SELECT IFNULL(SUM(pay_uv),0) FROM ads_user_funnel) "
            + "UNION ALL SELECT '转化率>100%的异常行数', "
            + "  (SELECT COUNT(*) FROM ads_noclean_user_funnel WHERE conv_view_to_cart>100 "
            + "     OR conv_cart_to_order>100 OR conv_order_to_pay>100), "
            + "  (SELECT COUNT(*) FROM ads_user_funnel WHERE conv_view_to_cart>100 "
            + "     OR conv_cart_to_order>100 OR conv_order_to_pay>100) "
            + "UNION ALL SELECT 'GMV合计(元)', "
            + "  (SELECT IFNULL(ROUND(SUM(gmv),2),0) FROM ads_noclean_order_analysis WHERE category_id IS NULL), "
            + "  (SELECT IFNULL(ROUND(SUM(gmv),2),0) FROM ads_order_analysis WHERE category_id IS NULL) "
            + "UNION ALL SELECT '商品品类数', "
            + "  (SELECT COUNT(DISTINCT category_id) FROM ads_noclean_order_analysis WHERE category_id IS NOT NULL), "
            + "  (SELECT COUNT(DISTINCT category_id) FROM ads_order_analysis WHERE category_id IS NOT NULL) "
            + "UNION ALL SELECT '好评率(%)', "
            + "  (SELECT IFNULL(ROUND(SUM(CASE WHEN rating>=4 THEN comment_cnt END)*100.0"
            + "      /NULLIF(SUM(comment_cnt),0),2),0) FROM ads_noclean_comment_analysis WHERE row_type='rating'), "
            + "  (SELECT IFNULL(ROUND(SUM(CASE WHEN rating>=4 THEN comment_cnt END)*100.0"
            + "      /NULLIF(SUM(comment_cnt),0),2),0) FROM ads_comment_analysis WHERE row_type='rating') "
            + "UNION ALL SELECT '会话转化率(%)', "
            + "  (SELECT IFNULL(ROUND(SUM(converted_cnt)*100.0/NULLIF(SUM(session_cnt),0),2),0) "
            + "     FROM ads_noclean_chat_analysis WHERE metric_scope='session'), "
            + "  (SELECT IFNULL(ROUND(SUM(converted_cnt)*100.0/NULLIF(SUM(session_cnt),0),2),0) "
            + "     FROM ads_chat_analysis WHERE metric_scope='session') "
            + "UNION ALL SELECT '人工首响(秒)', "
            + "  (SELECT IFNULL(ROUND(AVG(avg_first_response_sec),1),0) "
            + "     FROM ads_noclean_chat_analysis WHERE metric_scope='session'), "
            + "  (SELECT IFNULL(ROUND(AVG(avg_first_response_sec),1),0) "
            + "     FROM ads_chat_analysis WHERE metric_scope='session') "
            + "UNION ALL SELECT '地域覆盖城市数', "
            + "  (SELECT COUNT(*) FROM ads_noclean_region_dist WHERE scope='member'), "
            + "  (SELECT COUNT(*) FROM ads_region_dist WHERE scope='member') "
            + "UNION ALL SELECT '会员画像维度值数', "
            + "  (SELECT COUNT(*) FROM ads_noclean_member_profile), "
            + "  (SELECT COUNT(*) FROM ads_member_profile)")
    List<Map<String, Object>> compareMetrics();

    /* A/B 入口页分布对照（最直观：未清洗会多出 '首页 '/'unknown' 这类假入口） */
    @Select("SELECT k.entry_page AS entryPage, "
            + "  IFNULL(r.uv,0) AS rawUv, IFNULL(c.uv,0) AS cleanUv "
            + "FROM (SELECT entry_page FROM ads_noclean_user_funnel "
            + "      UNION SELECT entry_page FROM ads_user_funnel) k "
            + "LEFT JOIN (SELECT entry_page, SUM(browse_uv) uv FROM ads_noclean_user_funnel "
            + "           GROUP BY entry_page) r ON r.entry_page = k.entry_page "
            + "LEFT JOIN (SELECT entry_page, SUM(browse_uv) uv FROM ads_user_funnel "
            + "           GROUP BY entry_page) c ON c.entry_page = k.entry_page "
            + "ORDER BY cleanUv DESC, rawUv DESC")
    List<Map<String, Object>> compareEntryPages();

    /* A/B 品类 GMV 对照（单位混用会让某些品类 GMV 虚高） */
    @Select("SELECT k.category_name AS categoryName, "
            + "  IFNULL(r.gmv,0) AS rawGmv, IFNULL(c.gmv,0) AS cleanGmv "
            + "FROM (SELECT category_name FROM ads_noclean_order_analysis WHERE category_id IS NOT NULL "
            + "      UNION SELECT category_name FROM ads_order_analysis WHERE category_id IS NOT NULL) k "
            + "LEFT JOIN (SELECT category_name, SUM(gmv) gmv FROM ads_noclean_order_analysis "
            + "           WHERE category_id IS NOT NULL GROUP BY category_name) r ON r.category_name = k.category_name "
            + "LEFT JOIN (SELECT category_name, SUM(gmv) gmv FROM ads_order_analysis "
            + "           WHERE category_id IS NOT NULL GROUP BY category_name) c ON c.category_name = k.category_name "
            + "ORDER BY cleanGmv DESC LIMIT 15")
    List<Map<String, Object>> compareCategories();

    /* A/B 会员画像对照（未清洗的 gender/channel/level 脏值会多出假类别） */
    @Select("SELECT k.dim_type AS dimType, k.dim_value AS dimValue, "
            + "  IFNULL(r.member_cnt,0) AS rawCnt, IFNULL(c.member_cnt,0) AS cleanCnt "
            + "FROM (SELECT dim_type, dim_value FROM ads_noclean_member_profile "
            + "      UNION SELECT dim_type, dim_value FROM ads_member_profile) k "
            + "LEFT JOIN (SELECT dim_type, dim_value, SUM(member_cnt) member_cnt FROM ads_noclean_member_profile "
            + "           GROUP BY dim_type, dim_value) r ON r.dim_type=k.dim_type AND r.dim_value=k.dim_value "
            + "LEFT JOIN (SELECT dim_type, dim_value, SUM(member_cnt) member_cnt FROM ads_member_profile "
            + "           GROUP BY dim_type, dim_value) c ON c.dim_type=k.dim_type AND c.dim_value=k.dim_value "
            + "ORDER BY k.dim_type, cleanCnt DESC")
    List<Map<String, Object>> compareMemberProfile();

    /* DQC 数据质量稽核：每类清洗动作命中了多少行 */
    @Select("SELECT layer, table_name AS tableName, rule_code AS ruleCode, "
            + "rule_desc AS ruleDesc, hit_rows AS hitRows, run_time AS runTime "
            + "FROM dqc_clean_audit ORDER BY hit_rows DESC, rule_code")
    List<Map<String, Object>> dqAuditRows();

    /* DQC 汇总：规则条数 / 命中总行数 / 涉及来源表数 */
    @Select("SELECT COUNT(*) AS ruleCnt, IFNULL(SUM(hit_rows),0) AS hitTotal, "
            + "COUNT(DISTINCT table_name) AS tableCnt, MAX(run_time) AS runTime "
            + "FROM dqc_clean_audit")
    Map<String, Object> dqAuditSummary();
}
