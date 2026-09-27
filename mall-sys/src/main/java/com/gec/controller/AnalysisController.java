package com.gec.controller;

import com.gec.components.FileTemplate;
import com.gec.dao.AnalysisMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 阶段二 大数据分析结果展示接口（只读）
 * 数据来源：ads_* 结果表 与 tbl_etl_job_log，由离线 ETL 产出；表为空时返回空集合。
 */
@RestController
@RequestMapping("/Analysis")
public class AnalysisController extends BaseController {

    @Autowired
    private AnalysisMapper analysisMapper;
    @Autowired
    private FileTemplate fileTemplate;

    @Override
    protected FileTemplate getFileTemplate() { return fileTemplate; }

    /* 1.模块1 用户全链路行为漏斗 */
    @GetMapping("/funnel")
    public R funnel() {
        Map<String, Object> ret = new HashMap<>();
        List<Map<String, Object>> rows = analysisMapper.funnelRows();
        ret.put("rows", rows);
        ret.put("total", analysisMapper.funnelTotal());
        return R.ok(ret);
    }

    /* 2.模块2 订单数据分析 */
    @GetMapping("/order")
    public R order() {
        Map<String, Object> ret = new HashMap<>();
        ret.put("rows", analysisMapper.orderRows());
        return R.ok(ret);
    }

    /* 3.模块3 用户评价分析 */
    @GetMapping("/comment")
    public R comment() {
        Map<String, Object> ret = new HashMap<>();
        ret.put("rows", analysisMapper.commentRows());
        return R.ok(ret);
    }

    /* 4.模块4 用户与商家交流分析 */
    @GetMapping("/chat")
    public R chat() {
        Map<String, Object> ret = new HashMap<>();
        ret.put("rows", analysisMapper.chatRows());
        return R.ok(ret);
    }

    /* 5.ETL 任务执行日志（展示脚本运行结果） */
    @GetMapping("/jobLog")
    public R jobLog() {
        Map<String, Object> ret = new HashMap<>();
        ret.put("rows", analysisMapper.jobLogRows());
        return R.ok(ret);
    }

    /* 6.模块5 会员画像 */
    @GetMapping("/memberProfile")
    public R memberProfile() {
        Map<String, Object> ret = new HashMap<>();
        ret.put("rows", analysisMapper.memberProfileRows());
        return R.ok(ret);
    }

    /* 7.模块6 地域分布 */
    @GetMapping("/region")
    public R region() {
        Map<String, Object> ret = new HashMap<>();
        ret.put("rows", analysisMapper.regionRows());
        return R.ok(ret);
    }

    /* 8.模块7 商品分析 */
    @GetMapping("/product")
    public R product() {
        Map<String, Object> ret = new HashMap<>();
        ret.put("rows", analysisMapper.productRows());
        return R.ok(ret);
    }

    /* 9.需求补齐：时段分析（行为/订单/咨询 三模块共用，支撑热力图） */
    @GetMapping("/hourly")
    public R hourly() {
        Map<String, Object> ret = new HashMap<>();
        ret.put("rows", analysisMapper.hourlyRows());
        return R.ok(ret);
    }

    /* 10.需求补齐：搜索行为分析（热搜词 TOP50 + 无结果占比） */
    @GetMapping("/search")
    public R search() {
        Map<String, Object> ret = new HashMap<>();
        ret.put("rows", analysisMapper.searchRows());
        return R.ok(ret);
    }

    /* 11.需求补齐：浏览/收藏指标 + 热门商品·品类榜单 */
    @GetMapping("/behaviorSummary")
    public R behaviorSummary() {
        Map<String, Object> ret = new HashMap<>();
        ret.put("rows", analysisMapper.behaviorSummaryRows());
        return R.ok(ret);
    }

    /* 12.需求补齐：订单流失分析 + 客单价分布 */
    @GetMapping("/orderLoss")
    public R orderLoss() {
        Map<String, Object> ret = new HashMap<>();
        ret.put("rows", analysisMapper.orderLossRows());
        return R.ok(ret);
    }

    /* 13.需求补齐：商品口碑 TOP 榜（含好/中/差三分类汇总） */
    @GetMapping("/goodsRating")
    public R goodsRating() {
        Map<String, Object> ret = new HashMap<>();
        ret.put("rows", analysisMapper.goodsRatingRows());
        ret.put("levels", analysisMapper.goodsRatingLevel());
        return R.ok(ret);
    }

    /* 14.需求补齐：客服服务质量 + 热门咨询商品 */
    @GetMapping("/chatQuality")
    public R chatQuality() {
        Map<String, Object> ret = new HashMap<>();
        ret.put("rows", analysisMapper.chatQualityRows());
        return R.ok(ret);
    }

    /* =======================================================================
     * 15.清洗前后 A/B 对照（2026-09-25 新增）
     * -----------------------------------------------------------------------
     * 「清洗到底有没有用」用数据回答：同一批 ODS 原始数据、同一套聚合口径，
     * 对照组（ads_noclean_*）跳过 DWD 清洗直接聚合，实验组（ads_*）走完整清洗。
     * 12 项关键指标并排返回，前端算差值。
     * ======================================================================= */
    @GetMapping("/compare")
    public R compare() {
        Map<String, Object> ret = new HashMap<>();
        ret.put("ready", analysisMapper.nocleanReady() > 0);
        ret.put("metrics", analysisMapper.compareMetrics());
        ret.put("entryPages", analysisMapper.compareEntryPages());
        ret.put("categories", analysisMapper.compareCategories());
        ret.put("memberProfile", analysisMapper.compareMemberProfile());
        return R.ok(ret);
    }

    /* 16.数据质量稽核（DQC）：每类清洗动作命中了多少行 */
    @GetMapping("/dqAudit")
    public R dqAudit() {
        Map<String, Object> ret = new HashMap<>();
        ret.put("rows", analysisMapper.dqAuditRows());
        ret.put("summary", analysisMapper.dqAuditSummary());
        return R.ok(ret);
    }
}
