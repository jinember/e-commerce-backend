package com.gec.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gec.domain.entity.*;
import com.gec.service.*;
import com.gec.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Calendar;
import org.springframework.transaction.annotation.Transactional;

/**
 * 【C端商城】面向消费者的接口
 * 商品列表 / 商品详情(含SKU) / 提交订单 / 我的订单
 */
@RestController
@RequestMapping("/shop")
public class ShopController {
    /* 统一积分入账：写积分流水 + 回写会员累计积分；balance 一律记为「变更后的累计余额」 */
    private void grantPoints(Integer memberId, int delta, Integer orderId, String remark) {
        Member mb = memberService.getById(memberId);
        int oldPts = (mb == null || mb.getPoints() == null) ? 0 : mb.getPoints();
        int newPts = Math.max(0, oldPts + delta);
        PointLog log = new PointLog();
        log.setMemberId(memberId);
        log.setOrderId(orderId == null ? 0 : orderId);
        log.setChangeType(delta >= 0 ? "add" : "subtract");
        log.setPoints(Math.abs(delta));
        log.setBalance(newPts);
        log.setRemark(remark);
        pointLogService.save(log);
        if (mb != null) {
            mb.setPoints(newPts);
            memberService.updateById(mb);
        }
    }

    /* 行为会话号：同一会员 30 分钟内的行为归为一次会话（与埋点分析口径一致） */
    private String sessId(Integer mid) {
        long bucket = System.currentTimeMillis() / (30L * 60 * 1000);
        return "S" + (mid == null ? 0 : mid) + "_T" + bucket;
    }

    /* 埋点的入口页：沿用同一会话已记录的入口页，没有则兜底「首页」。
       作用：让实时产生的埋点也能参与「入口追踪」分析（历史数据该列已全量回填） */
    private String entryPageOf(Integer mid, String sessionId) {
        if (mid == null || sessionId == null || sessionId.isEmpty()) return "首页";
        try {
            List<Map<String, Object>> r = jdbcTemplate.queryForList(
                    "SELECT entry_page FROM tbl_user_behavior WHERE member_id=? AND session_id=? "
                  + "AND entry_page IS NOT NULL AND entry_page<>\'\' ORDER BY create_date DESC LIMIT 1",
                    mid, sessionId);
            if (!r.isEmpty() && r.get(0).get("entry_page") != null) {
                return String.valueOf(r.get(0).get("entry_page"));
            }
        } catch (Exception e) {
            /* 查询失败不影响埋点写入 */
        }
        return "首页";
    }


    @Autowired
    private CacheService cacheService;
    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;
    @Autowired
    private ISpuDetailService spuDetailService;
    @Autowired
    private ISkuInfoService skuInfoService;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;
    @Autowired
    private IOrderInfoService orderInfoService;
    @Autowired
    private IMemberService memberService;
    @Autowired
    private IStockService stockService;
    @Autowired
    private IStockLogService stockLogService;
    @Autowired
    private IPaymentService paymentService;
    @Autowired
    private ILogisticsService logisticsService;
    @Autowired
    private IUserBehaviorService userBehaviorService;
    @Autowired
    private ICommentService commentService;
    @Autowired
    private IPointLogService pointLogService;
    @Autowired
    private IRefundService refundService;
    @Autowired
    private IBrowseHistoryService browseHistoryService;
    @Autowired
    private IMessageService messageService;
    @Autowired
    private ISignInService signInService;
    @Autowired
    private com.gec.dao.SignRuleMapper signRuleMapper;
    @Autowired
    private com.gec.dao.CategoryMapper categoryMapper;
    @Autowired
    private IChatSessionService chatSessionService;
    @Autowired
    private IChatMessageService chatMessageService;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private Random random = new Random();

    /* ==================================================================
       【营销联动】活动 / 优惠券
       后台配置的活动（tbl_promotion）和优惠券（tbl_coupon）从这里真正作用到 C 端：
         - activePromotion(spuId)：找出当前对该商品生效的活动
         - 活动价 = 原价 × 折扣率
         - 下单时先按活动价算商品金额，再叠加优惠券抵扣
       ================================================================== */
    @Autowired
    private IPromotionService promotionService;

    /**
     * 找出当前对某个商品生效的活动；命中多个时取折扣力度最大的那个。
     * 规则：status=1（启用）、当前时间在 start~end 之间、spu_id 为空(全场)或等于该商品。
     * discount 存的是折扣率，0.62 = 6.2 折，所以取最小值＝最优惠。
     */
    private Promotion activePromotion(Integer spuId) {
        Promotion best = null;
        double bestDiscount = 1.0;
        for (Promotion p : loadPromotionPool()) {
            Promotion hit = matchPromotion(p, spuId);
            if (hit == null) continue;
            double d = (hit.getDiscount() == null || hit.getDiscount() <= 0) ? 1.0 : hit.getDiscount();
            if (d < bestDiscount) { bestDiscount = d; best = hit; }
        }
        return best;
    }

    /**
     * 一次性把活动读出来（表里只有几十条，量极小）。
     * 列表页要给每个商品算活动价，如果放在循环里逐条查会变成 N 次 SQL，所以先读一次再复用。
     */
    private List<Promotion> loadPromotionPool() {
        try {
            return promotionService.list();
        } catch (Exception e) {
            return java.util.Collections.emptyList();   /* 活动表出问题也不能拖垮商品浏览 */
        }
    }

    /** 判断某条活动当前是否对某个商品生效：命中返回该活动，不命中返回 null */
    private Promotion matchPromotion(Promotion p, Integer spuId) {
        if (p == null || spuId == null) return null;
        if (p.getStatus() == null || p.getStatus() != 1) return null;
        String now = sdf.format(new Date());
        if (p.getStartTime() != null && now.compareTo(p.getStartTime()) < 0) return null;   /* 还没开始 */
        if (p.getEndTime() != null && now.compareTo(p.getEndTime()) > 0) return null;       /* 已结束 */
        Integer sid = p.getSpuId();
        if (sid != null && !sid.equals(spuId)) return null;                                  /* 指定给别人了 */
        return p;
    }

    /** 活动价：原价乘折扣率，保留两位小数 */
    private double promoPrice(double origin, Promotion p) {
        if (p == null || p.getDiscount() == null || p.getDiscount() <= 0) return origin;
        return Math.round(origin * p.getDiscount() * 100.0) / 100.0;
    }

    /* 1.C端商品列表(只查上架 status=1)
       商品列表是典型的读多写少，这里加 Redis 缓存；商品改动时由
       SpuController / PublishGoodsController 负责清掉 shop: 前缀的缓存。
       注意：详情页 detail 不能整体缓存 —— 它有浏览量+1、写浏览行为等副作用。 */
    @GetMapping("/list")
    public R list() {
        final String CK = "shop:list";
        List<Map<String, Object>> cached = cacheService.get(CK, List.class);
        if (cached != null) {
            return R.ok().put("data", cached);
        }

        LambdaQueryWrapper<GoodsDetail> qw = new LambdaQueryWrapper<>();
        qw.eq(GoodsDetail::getStatus, 1);
        qw.orderByDesc(GoodsDetail::getCreateDate);
        List<GoodsDetail> list = spuDetailService.list(qw);
        List<Category> cats = categoryMapper.selectList(null);
        Map<Integer,String> catMap = new HashMap<>();
        for(Category c : cats){ catMap.put(c.getId(), c.getCategoryName()); }
        for(GoodsDetail g : list){ g.setCategoryName(catMap.get(g.getCategoryId())); }

        // 填充每个SPU下SKU最低价
        java.util.List<SkuInfo> allSkus = skuInfoService.list();
        java.util.Map<Integer,Double> priceMap = new java.util.HashMap<>();
        for(SkuInfo sku : allSkus){
            if(sku.getSpuId()==null || sku.getPrice()==null) continue;
            Double cur = priceMap.get(sku.getSpuId());
            if(cur==null || sku.getPrice()<cur) priceMap.put(sku.getSpuId(), sku.getPrice().doubleValue());
        }
        for(GoodsDetail g : list){ g.setMinPrice(priceMap.get(g.getId())); }

        /* 【营销联动】给每个商品填上当前生效的活动：活动名 + 折后最低价 */
        for(GoodsDetail g : list){
            Promotion p = activePromotion(g.getId());
            if(p != null){
                g.setPromoName(p.getName());
                g.setPromoType(p.getType());
                g.setPromoDiscount(p.getDiscount());
                if(g.getMinPrice() != null){
                    g.setPromoMinPrice(promoPrice(g.getMinPrice(), p));
                }
            }
        }

        cacheService.set(CK, objectMapper.convertValue(
                list, new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {}));
        return R.ok().put("data", list);
    }

    /* 2.商品详情(SPU信息 + 该商品的所有SKU) */
    @GetMapping("/detail/{spuId}")
    public R detail(@PathVariable("spuId") Integer spuId,
                    @RequestParam(required=false, defaultValue="1") Integer memberId) {
        GoodsDetail spu = spuDetailService.getById(spuId);
        LambdaQueryWrapper<SkuInfo> qw = new LambdaQueryWrapper<>();
        qw.eq(SkuInfo::getSpuId, spuId);
        List<SkuInfo> skuList = skuInfoService.list(qw);

        /* 【营销联动】这个商品当前命中了哪个活动（没有则为 null） */
        Promotion promo = activePromotion(spuId);

        /* 把SKU转成Map，加上defaultImage字段 */
        List<java.util.Map<String,Object>> skuMapList = new java.util.ArrayList<>();
        for(SkuInfo sku : skuList){
            java.util.Map<String,Object> m = new java.util.HashMap<>();
            m.put("skuId", sku.getSkuId());
            m.put("skuName", sku.getSkuName());
            m.put("price", sku.getPrice());
            m.put("saleCount", sku.getSaleCount());
            /* 活动价：原价 × 折扣率，前端用来显示划线价 */
            m.put("promoPrice", promoPrice(sku.getPrice() == null ? 0d : sku.getPrice(), promo));
            try {
                String img = jdbcTemplate.queryForObject(
                    "SELECT default_image FROM tbl_sku_album WHERE sku_id = ? LIMIT 1",
                    String.class, sku.getSkuId()
                );
                m.put("defaultImage", img);
            } catch (Exception e) {
                m.put("defaultImage", "");
            }
            skuMapList.add(m);
        }

        Map<String, Object> ret = new HashMap<>();
        ret.put("spu", spu);
        ret.put("skuList", skuMapList);

        /* 【营销联动】活动信息回给前端（无活动则传 null） */
        if(promo != null){
            Map<String, Object> pm = new HashMap<>();
            pm.put("id", promo.getId());
            pm.put("name", promo.getName());
            pm.put("type", promo.getType());
            pm.put("discount", promo.getDiscount());
            pm.put("endTime", promo.getEndTime());
            ret.put("promotion", pm);
        } else {
            ret.put("promotion", null);
        }

        /* 【联动】浏览量+1 */
        spu.setViewCount(spu.getViewCount() == null ? 1 : spu.getViewCount() + 1);
        spuDetailService.updateById(spu);

        /* 记录浏览行为 */
        UserBehavior behavior = new UserBehavior();
        behavior.setMemberId(memberId);
        behavior.setSpuId(spuId);
        behavior.setBehaviorType("浏览");
        behavior.setPageUrl("/shopDetail/" + spuId);
        behavior.setStayTime(10 + random.nextInt(60));
        behavior.setDevice(Math.random() < 0.6 ? "Android" : "iOS");
        behavior.setChannel("APP");
        behavior.setSessionId(sessId(behavior.getMemberId()));
        behavior.setEntryPage(entryPageOf(behavior.getMemberId(), behavior.getSessionId()));
        userBehaviorService.save(behavior);

        /* 【联动】写浏览历史 */
        BrowseHistory history = new BrowseHistory();
        history.setMemberId(memberId);
        history.setSpuId(spuId);
        history.setBrowseTime(sdf.format(new Date()));
        history.setStayDuration(10 + random.nextInt(60));
        browseHistoryService.save(history);

        return R.ok().put("data", ret);
    }

    /* ===== 【营销联动】优惠券：可领列表 / 领券 / 我的券 =====
       都放在 /shop/** 下，属于免登录白名单，C 端会员可以不登录后台直接访问。 */

    /** 当前可领取的优惠券（启用 + 在有效期内 + 还有库存），并标出当前会员是否已领 */
    @GetMapping("/couponList")
    public R couponList(@RequestParam(required = false, defaultValue = "1") Integer memberId) {
        String now = sdf.format(new Date());
        List<Coupon> all = couponService.list();
        List<Map<String, Object>> got = java.util.Collections.emptyList();
        try {
            got = jdbcTemplate.queryForList(
                "SELECT coupon_id FROM tbl_coupon_record WHERE member_id=? AND status='unused'", memberId);
        } catch (Exception e) { /* 领券表异常时按未领取处理，不影响列表展示 */ }

        java.util.Set<Object> owned = new java.util.HashSet<>();
        for (Map<String, Object> r : got) owned.add(r.get("coupon_id"));

        List<Map<String, Object>> data = new java.util.ArrayList<>();
        for (Coupon c : all) {
            if (c.getStatus() == null || c.getStatus() != 1) continue;
            if (c.getStartTime() != null && now.compareTo(c.getStartTime()) < 0) continue;
            if (c.getEndTime() != null && now.compareTo(c.getEndTime()) > 0) continue;
            int used = c.getUsedCount() == null ? 0 : c.getUsedCount();
            int stock = c.getTotalCount() == null ? 0 : c.getTotalCount();
            Map<String, Object> m = new HashMap<>();
            m.put("couponId", c.getId());
            m.put("name", c.getName());
            m.put("type", c.getType());
            m.put("value", c.getValue());
            m.put("minAmount", c.getMinAmount());
            m.put("remain", stock - used);
            m.put("endTime", c.getEndTime());
            m.put("owned", owned.contains(c.getId()));
            data.add(m);
        }
        return R.ok().put("data", data);
    }

    /** 领券：写入 tbl_coupon_record。同一会员同一张券只能有一条未使用的记录 */
    @PostMapping("/receiveCoupon")
    public R receiveCoupon(@RequestBody Map<String, Integer> body) {
        Integer memberId = body.getOrDefault("memberId", 1);
        Integer couponId = body.get("couponId");
        if (couponId == null) {
            return R.err(new RuntimeException("缺少券ID"));
        }
        Coupon c = couponService.getById(couponId);
        String now = sdf.format(new Date());
        if (c == null || c.getStatus() == null || c.getStatus() != 1) {
            return R.err(new RuntimeException("该优惠券不存在或已下架"));
        }
        if (c.getEndTime() != null && now.compareTo(c.getEndTime()) > 0) {
            return R.err(new RuntimeException("该优惠券已过期"));
        }
        int used = c.getUsedCount() == null ? 0 : c.getUsedCount();
        int stock = c.getTotalCount() == null ? 0 : c.getTotalCount();
        if (used >= stock) {
            return R.err(new RuntimeException("该优惠券已被领完"));
        }
        try {
            Integer dup = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM tbl_coupon_record WHERE member_id=? AND coupon_id=? AND status='unused'",
                Integer.class, memberId, couponId);
            if (dup != null && dup > 0) {
                return R.err(new RuntimeException("你已经领过这张券了"));
            }
            jdbcTemplate.update(
                "INSERT INTO tbl_coupon_record(member_id, coupon_id, status, receive_time, create_date) "
              + "VALUES(?, ?, 'unused', NOW(), NOW())", memberId, couponId);
        } catch (Exception e) {
            return R.err(new RuntimeException("领券失败：" + e.getMessage()));
        }
        return R.ok().put("msg", "领券成功");
    }

    /** 我的优惠券：未使用的券 */
    @GetMapping("/myCoupons")
    public R myCoupons(@RequestParam(required = false, defaultValue = "1") Integer memberId) {
        List<Map<String, Object>> data;
        try {
            data = jdbcTemplate.queryForList(
                "SELECT r.id AS recordId, c.id AS couponId, c.name, c.type, c.`value`, c.min_amount AS minAmount, "
              + "c.end_time AS endTime, r.receive_time AS receiveTime "
              + "FROM tbl_coupon_record r JOIN tbl_coupon c ON c.id = r.coupon_id "
              + "WHERE r.member_id = ? AND r.status = 'unused' AND c.status = 1 "
              + "ORDER BY c.`value` DESC", memberId);
        } catch (Exception e) {
            data = new java.util.ArrayList<>();
        }
        return R.ok().put("data", data);
    }

    /* 3.提交订单 */
    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/submitOrder")
    public R submitOrder(@RequestBody OrderInfo order) {
        /* 1.根据SKUID查出SKU,回填商品名称/单价 */
        SkuInfo sku = skuInfoService.getById(order.getSkuId());
        if (sku == null) {
            throw new RuntimeException("SKU不存在");
        }
        order.setGoodsId(sku.getSpuId());
        order.setGoodsName(sku.getSkuName());
        order.setSkuName(sku.getSkuName());
        /* 【营销联动】命中活动的话，单价按活动价来算（和详情页展示的价格保持一致） */
        Promotion hitPromo = activePromotion(sku.getSpuId());
        double unitPrice = (sku.getPrice() == null ? 0d : sku.getPrice());
        if (hitPromo != null) {
            unitPrice = promoPrice(unitPrice, hitPromo);
            /* remark 里追加活动说明，便于订单追溯是哪个活动成交的 */
            String pmTag = "promo:" + hitPromo.getId();
            if (order.getRemark() == null || order.getRemark().isEmpty()) {
                order.setRemark(pmTag);
            } else {
                order.setRemark(order.getRemark() + "|" + pmTag);
            }
        }
        order.setPrice(BigDecimal.valueOf(unitPrice));
        /* 2.计算总价 */
        int cnt = order.getCount() == null ? 1 : order.getCount();
        double totalPrice = unitPrice * cnt;

        /* 【营销联动】优惠券抵扣：前端下单时把 coupon:<券ID> 拼进 remark。
           校验顺序：券存在 → 启用 → 在有效期内 → 达到使用门槛 → 还有库存；
           五关全过才真正减钱，并把表里的已用数量+1。 */
        Integer usedCouponId = null;
        if (order.getRemark() != null) {
            java.util.regex.Matcher cm =
                java.util.regex.Pattern.compile("coupon:(\\d+)").matcher(order.getRemark());
            if (cm.find()) {
                Integer couponId = Integer.parseInt(cm.group(1));
                Coupon coupon = couponService.getById(couponId);
                String nowCoupon = sdf.format(new Date());
                int used = coupon == null || coupon.getUsedCount() == null ? 0 : coupon.getUsedCount();
                int stock = coupon == null || coupon.getTotalCount() == null ? 0 : coupon.getTotalCount();
                boolean usable = coupon != null
                        && coupon.getStatus() != null && coupon.getStatus() == 1
                        && (coupon.getStartTime() == null || nowCoupon.compareTo(coupon.getStartTime()) >= 0)
                        && (coupon.getEndTime() == null || nowCoupon.compareTo(coupon.getEndTime()) <= 0)
                        && totalPrice >= (coupon.getMinAmount() == null ? 0d : coupon.getMinAmount())
                        && used < stock;
                if (usable) {
                    totalPrice -= (coupon.getValue() == null ? 0d : coupon.getValue());
                    if (totalPrice < 0) totalPrice = 0;
                    coupon.setUsedCount(used + 1);
                    couponService.updateById(coupon);
                    usedCouponId = couponId;
                }
            }
        }
        order.setTotalPrice(BigDecimal.valueOf(totalPrice));

        /* 3.生成订单号 */
        String orderNo = "DD" + System.currentTimeMillis();
        order.setOrderNo(orderNo);
        /* 4.状态:0=待付款 */
        order.setStatus(0);
        boolean ret = orderInfoService.save(order);
        if (!ret) {
            throw new RuntimeException("提交订单失败");
        }

        /* 【营销联动】核销会员领过的那张券：领券记录置为「已使用」并关联订单号，
           这样后台能看到这张券是谁在哪一单用掉的（可追溯）。 */
        if (usedCouponId != null && order.getMemberId() != null) {
            try {
                jdbcTemplate.update(
                    "UPDATE tbl_coupon_record SET status='used', use_time=NOW(), order_id=? "
                  + "WHERE member_id=? AND coupon_id=? AND status='unused' ORDER BY id LIMIT 1",
                    order.getId(), order.getMemberId(), usedCouponId);
            } catch (Exception e) {
                /* 领券记录核销失败不应阻断下单主流程 */
            }
        }

        /* 【联动】写行为日志(下单行为) */
        UserBehavior behavior = new UserBehavior();
        behavior.setMemberId(order.getMemberId() == null ? 1 : order.getMemberId());
        behavior.setSpuId(sku.getSpuId());
        behavior.setBehaviorType("购买");
        behavior.setPageUrl("/shopOrder");
        behavior.setStayTime(0);
        behavior.setDevice(Math.random() < 0.6 ? "Android" : "iOS");
        behavior.setChannel("APP");
        behavior.setSessionId(sessId(behavior.getMemberId()));
        behavior.setEntryPage(entryPageOf(behavior.getMemberId(), behavior.getSessionId()));
        userBehaviorService.save(behavior);

        /* 【联动】站内消息：订单已提交 */
        int mid = order.getMemberId() == null ? 1 : order.getMemberId();
        Message msg1 = new Message();
        msg1.setMemberId(mid);
        msg1.setTitle("订单已提交");
        msg1.setContent("您的订单 " + orderNo + " 已提交，请尽快完成支付。");
        msg1.setType("order");
        msg1.setIsRead(0);
        msg1.setSendTime(sdf.format(new Date()));
        messageService.save(msg1);

        return R.ok().put("orderNo", orderNo);
    }

    /* ===== 【新增】4.用户付款 ===== */
    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/payOrder/{orderId}")
    public R payOrder(@PathVariable("orderId") Integer orderId) {
        OrderInfo order = orderInfoService.getById(orderId);
        if (order == null) throw new RuntimeException("订单不存在");
        if (order.getStatus() != 0) throw new RuntimeException("订单状态不正确");

        /* 【联动】扣库存 + 写出库流水（数据库原子操作，防并发超卖） */
        LambdaQueryWrapper<Stock> stockQw = new LambdaQueryWrapper<>();
        stockQw.eq(Stock::getSkuId, order.getSkuId());
        Stock stock = stockService.getOne(stockQw);
        int cnt = order.getCount() == null ? 1 : order.getCount();
        if (stock == null) {
            throw new RuntimeException("该商品未配置库存，无法下单");
        }
        /* 原子扣减：UPDATE stock SET stock = stock - cnt WHERE id=? AND stock >= cnt */
        com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Stock> uw =
            new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<>();
        uw.eq(Stock::getId, stock.getId())
          .ge(Stock::getStock, cnt)
          .setSql("stock = stock - " + cnt);
        boolean ok = stockService.update(uw);
        if (!ok) {
            int nowStock = stock.getStock() == null ? 0 : stock.getStock();
            throw new RuntimeException("库存不足：【" + stock.getGoodsName() + "】当前库存" + nowStock + "件，无法购买" + cnt + "件");
        }
        /* 扣完后重新查一次，拿真实库存数（并发下准确） */
        Stock after = stockService.getById(stock.getId());
        int newStock = after.getStock() == null ? 0 : after.getStock();

        StockLog log = new StockLog();
        log.setStockId(stock.getId());
        log.setChangeType("out");
        log.setQuantity(cnt);
        log.setRemark("订单出库: " + order.getOrderNo());
        log.setOperator(order.getUserName());
        stockLogService.save(log);

        /* 库存预警（safeStock 可能为 null，默认不预警；<= 安全线就提醒） */
        int safe = after.getSafeStock() == null ? 0 : after.getSafeStock();
        if (safe > 0 && newStock <= safe) {
            Message warnMsg = new Message();
            warnMsg.setMemberId(0);
            warnMsg.setTitle("库存预警：" + stock.getGoodsName());
            warnMsg.setContent("商品【" + stock.getGoodsName() + "】库存仅剩 " + newStock + " 件，低于安全库存 " + safe + " 件，请及时补货！");
            warnMsg.setType("system");
            warnMsg.setIsRead(0);
            warnMsg.setSendTime(sdf.format(new Date()));
            messageService.save(warnMsg);
        }

        /* 【联动】更新商品销量 */
        SkuInfo sku = skuInfoService.getById(order.getSkuId());
        if (sku != null) {
            int oldSale = sku.getSaleCount() == null ? 0 : sku.getSaleCount();
            sku.setSaleCount(oldSale + cnt);
            skuInfoService.updateById(sku);
        }

        /* 【联动】写支付流水 */
        Payment payment = new Payment();
        payment.setOrderId(order.getId());
        payment.setPayType(random.nextInt(2) == 0 ? "微信支付" : "支付宝");
        payment.setPayAmount(order.getTotalPrice().doubleValue());
        payment.setPayTime(sdf.format(new Date()));
        payment.setPayStatus(1);
        payment.setTransactionNo("TXN" + System.currentTimeMillis() + random.nextInt(1000));
        paymentService.save(payment);

        /* 订单状态改为已付款 */
        order.setStatus(1);
        orderInfoService.updateById(order);

        /* 【联动】站内消息：支付成功 */
        int mid = order.getMemberId() == null ? 1 : order.getMemberId();
        Message msg = new Message();
        msg.setMemberId(mid);
        msg.setTitle("支付成功");
        msg.setContent("您的订单 " + order.getOrderNo() + " 已支付成功，等待商家发货。");
        msg.setType("order");
        msg.setIsRead(0);
        msg.setSendTime(sdf.format(new Date()));
        messageService.save(msg);

        return R.ok().put("msg", "支付成功");
    }

    /* 4.我的订单列表(按用户名查) */
    @GetMapping("/orderList/{userName}")
    public R orderList(@PathVariable("userName") String userName) {
        LambdaQueryWrapper<OrderInfo> qw = new LambdaQueryWrapper<>();
        qw.eq(OrderInfo::getUserName, userName);
        qw.orderByDesc(OrderInfo::getCreateDate);
        List<OrderInfo> list = orderInfoService.list(qw);
        /* 为每个订单附带最近一条退款记录状态(0待审核/1已同意/2已拒绝)，供 C 端展示"退款被拒绝"标签 */
        for (OrderInfo o : list) {
            LambdaQueryWrapper<Refund> rw = new LambdaQueryWrapper<>();
            rw.eq(Refund::getOrderId, o.getId());
            rw.orderByDesc(Refund::getId);
            rw.last("limit 1");
            Refund r = refundService.getOne(rw);
            o.setRefundStatus(r == null ? null : r.getStatus());
        }
        return R.ok().put("data", list);
    }

    /* ===== 【方案B】C端消息中心：我的消息 / 未读数 / 标记已读 / 全部已读 =====
       严格按 member_id 隔离，只返回当前会员自己的消息；
       readMessage 校验归属，防止把别人的消息标记成已读。 */

    /* B1.我的消息列表(倒序，附未读数) */
    @GetMapping("/myMessages")
    public R myMessages(@RequestParam Integer memberId) {
        LambdaQueryWrapper<Message> qw = new LambdaQueryWrapper<>();
        qw.eq(Message::getMemberId, memberId);
        qw.orderByDesc(Message::getId);
        List<Message> list = messageService.list(qw);
        int unread = 0;
        for (Message m : list) {
            if (m.getIsRead() == null || m.getIsRead() != 1) unread++;
        }
        return R.ok().put("data", list).put("unread", unread);
    }

    /* B2.未读数(列表页头部红点用，轻量接口) */
    @GetMapping("/unreadCount")
    public R unreadCount(@RequestParam Integer memberId) {
        LambdaQueryWrapper<Message> qw = new LambdaQueryWrapper<>();
        qw.eq(Message::getMemberId, memberId).eq(Message::getIsRead, 0);
        return R.ok().put("unread", messageService.count(qw));
    }

    /* B3.标记单条已读(校验归属：只能把自己名下的消息标已读) */
    @PostMapping("/readMessage")
    public R readMessage(@RequestBody Map<String, Integer> body) {
        Integer id = body.get("id");
        Integer mid = body.get("memberId");
        Message m = messageService.getById(id);
        if (m == null) return R.err(new RuntimeException("消息不存在"));
        if (mid == null || !mid.equals(m.getMemberId())) {
            return R.err(new RuntimeException("无权操作该消息"));
        }
        if (m.getIsRead() == null || m.getIsRead() != 1) {
            m.setIsRead(1);
            messageService.updateById(m);
        }
        return R.ok();
    }

    /* B4.全部已读(一键清空未读) */
    @PostMapping("/readAllMessages")
    public R readAllMessages(@RequestBody Map<String, Integer> body) {
        Integer mid = body.get("memberId");
        if (mid == null) return R.err(new RuntimeException("缺少会员ID"));
        jdbcTemplate.update(
            "UPDATE tbl_message SET is_read=1 WHERE member_id=? AND (is_read IS NULL OR is_read=0)", mid);
        return R.ok();
    }

    /* 5.根据昵称/手机号查会员等级 */
    @GetMapping("/checkMember")
    public R checkMember(@RequestParam(required=false) String nickname,
                         @RequestParam(required=false) String phone,
                     @RequestParam(required=false) String password) {
        LambdaQueryWrapper<Member> qw = new LambdaQueryWrapper<>();
        if(nickname != null && nickname.length() > 0){
            qw.eq(Member::getNickname, nickname);
        }
        if(phone != null && phone.length() > 0){
            qw.eq(Member::getPhone, phone);
        }
        Member m = memberService.getOne(qw);
        if (m == null) {
            return R.err(new RuntimeException("该账号未注册"));
        }
        String storedPwd = m.getPassword();
        boolean pwdOk = (password == null || password.length() == 0)
                || (PasswordUtil.isHashed(storedPwd)
                    ? PasswordUtil.matches(password, storedPwd)
                    : password.equals(storedPwd));
        if (!pwdOk) {
            return R.err(new RuntimeException("密码错误"));
        }
        if (m.getStatus() != null && m.getStatus() == 0) {
            return R.err(new RuntimeException("该账号已被禁用，请联系客服"));
        }
        m.setPassword(null);
        return R.ok().put("level", m.getLevel()).put("member", m);
    }

    /* 6.注册会员(昵称+手机号,默认普通会员) */
    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/register")
    public R register(@RequestBody Member member) {
        /* 1.检查昵称/手机号是否已存在 */
        LambdaQueryWrapper<Member> qw = new LambdaQueryWrapper<>();
        qw.eq(Member::getNickname, member.getNickname())
          .or().eq(Member::getPhone, member.getPhone());
        Member exist = memberService.getOne(qw);
        if(exist != null){
            return R.ok().put("level", exist.getLevel()).put("msg","已存在");
        }
        /* 2.新会员默认普通会员 */
        member.setLevel("普通会员");
        member.setStatus(1);
        member.setPoints(0);
        member.setBalance(0.0);
        /* 随机生成用户维度字段（方便做用户画像分析） */
        member.setGender(random.nextBoolean() ? "男" : "女");
        member.setAge(18 + random.nextInt(40));
        String[] cities = {"北京","上海","广州","深圳","杭州","成都","武汉","西安","南京","重庆"};
        member.setCity(cities[random.nextInt(cities.length)]);
        String[] channels = {"自然搜索","广告投放","朋友推荐","社交媒体","直接访问"};
        member.setChannel(channels[random.nextInt(channels.length)]);
        member.setRegisterTime(sdf.format(new Date()));
        /* 密码哈希后再入库，不存明文 */
        if (member.getPassword() != null && member.getPassword().trim().length() > 0
                && !PasswordUtil.isHashed(member.getPassword())) {
            member.setPassword(PasswordUtil.hash(member.getPassword()));
        }
        memberService.save(member);

        /* ===== 【联动】注册送100积分 ===== */
        grantPoints(member.getId(), 100, 0, "注册赠送积分");

        /* 【联动】注册欢迎消息 */
        Message welcome = new Message();
        welcome.setMemberId(member.getId());
        welcome.setTitle("欢迎加入优选商城");
        welcome.setContent("欢迎您，" + member.getNickname() + "！注册成功赠送100积分，祝您购物愉快！");
        welcome.setType("system");
        welcome.setIsRead(0);
        welcome.setSendTime(sdf.format(new Date()));
        messageService.save(welcome);

        return R.ok().put("level", "普通会员").put("msg","注册成功").put("memberId", member.getId());
    }

    /* 7.C端个人信息：查询我的资料 */
    @GetMapping("/myInfo")
    public R myInfo(@RequestParam Integer memberId) {
        Member m = memberService.getById(memberId);
        if (m == null) throw new RuntimeException("用户不存在");
        return R.ok().put("data", m);
    }

    /* 8.C端个人信息：修改昵称/性别/年龄/城市 */
    @PostMapping("/updateInfo")
    public R updateInfo(@RequestBody Member member) {
        Member exist = memberService.getById(member.getId());
        if (exist == null) throw new RuntimeException("用户不存在");
        if (member.getNickname() != null && !member.getNickname().isEmpty()) {
            exist.setNickname(member.getNickname());
        }
        exist.setGender(member.getGender());
        exist.setAge(member.getAge());
        exist.setCity(member.getCity());
        memberService.updateById(exist);
        return R.ok().put("msg", "保存成功").put("data", exist);
    }

    /* ===== 【新增】7.确认收货 ===== */
    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/confirmReceive/{orderId}")
    public R confirmReceive(@PathVariable("orderId") Integer orderId) {
        OrderInfo order = orderInfoService.getById(orderId);
        if (order == null) throw new RuntimeException("订单不存在");
        order.setStatus(3); /* 3=已完成 */
        orderInfoService.updateById(order);

        /* 物流记录更新为已收货 */
        LambdaQueryWrapper<Logistics> lw = new LambdaQueryWrapper<>();
        lw.eq(Logistics::getOrderId, orderId);
        Logistics logi = logisticsService.getOne(lw);
        if (logi != null) {
            logi.setReceiveTime(sdf.format(new Date()));
            logi.setStatus(2);
            logisticsService.updateById(logi);
        }

        /* 送积分(按订单金额1%返积分) */
        int points = (int)(order.getTotalPrice().doubleValue() * 1);
        grantPoints(order.getMemberId() == null ? 1 : order.getMemberId(), points, order.getId(),
                    "购物返积分: " + order.getOrderNo());

        /* 【联动】站内消息：收货成功通知 */
        Message msg = new Message();
        msg.setMemberId(order.getMemberId() == null ? 1 : order.getMemberId());
        msg.setTitle("订单已完成");
        msg.setContent("您的订单 " + order.getOrderNo() + " 已确认收货，获得" + points + "积分奖励。");
        msg.setType("order");
        msg.setIsRead(0);
        msg.setSendTime(sdf.format(new Date()));
        messageService.save(msg);

        return R.ok().put("msg", "收货成功，获得" + points + "积分");
    }

    /* ===== 【新增】8.提交评价 ===== */
    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/submitComment")
    public R submitComment(@RequestBody Comment comment) {
        Integer mid = comment.getMemberId() == null ? 1 : comment.getMemberId();
        comment.setMemberId(mid);
        commentService.save(comment);

        /* 【联动】商品评价数+1，更新好评率 */
        GoodsDetail spu = spuDetailService.getById(comment.getSpuId());
        if (spu != null) {
            int oldCount = spu.getCommentCount() == null ? 0 : spu.getCommentCount();
            double oldRate = spu.getGoodRate() == null ? 5.0 : spu.getGoodRate();
            /* 新的好评率 = (旧总分 + 新评分) / (旧数量 + 1) */
            double newRate = (oldRate * oldCount + comment.getRating()) / (oldCount + 1);
            spu.setCommentCount(oldCount + 1);
            spu.setGoodRate(Math.round(newRate * 10) / 10.0);
            spuDetailService.updateById(spu);
        }

        /* 评价送50积分 */
        grantPoints(mid, 50, comment.getOrderId(), "评价商品送积分");

        /* 记录行为日志 */
        UserBehavior behavior = new UserBehavior();
        behavior.setMemberId(mid);
        behavior.setSpuId(comment.getSpuId());
        behavior.setBehaviorType("评论");
        behavior.setPageUrl("/shopDetail/" + comment.getSpuId());
        behavior.setStayTime(0);
        behavior.setDevice(Math.random() < 0.6 ? "Android" : "iOS");
        behavior.setChannel("APP");
        behavior.setSessionId(sessId(behavior.getMemberId()));
        behavior.setEntryPage(entryPageOf(behavior.getMemberId(), behavior.getSessionId()));
        userBehaviorService.save(behavior);

        return R.ok().put("msg", "评价成功，获得50积分");
    }

    /* ===== 【新增】9.申请退款 ===== */
    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/applyRefund")
    public R applyRefund(@RequestBody Refund refund) {
        /* 幂等校验：同一订单已有退款申请时，不允许重复提交 */
        if (refund.getOrderId() != null) {
            LambdaQueryWrapper<Refund> rqw = new LambdaQueryWrapper<>();
            rqw.eq(Refund::getOrderId, refund.getOrderId());
            rqw.orderByDesc(Refund::getId);
            Refund exist = refundService.getOne(rqw, false);
            if (exist != null && exist.getStatus() != null) {
                if (exist.getStatus() == 0) {
                    return R.ok().put("result", "failed").put("cause", "该订单已提交退款申请，请等待处理");
                }
                if (exist.getStatus() == 1) {
                    return R.ok().put("result", "failed").put("cause", "该订单已完成退款，无需重复申请");
                }
                /* status==2（已拒绝）允许重新申请退款 */
            }
        }
        refund.setStatus(0); /* 0=待审核 */
        refund.setApplyTime(sdf.format(new Date()));
        refundService.save(refund);

        /* 订单状态改为退款中 */
        OrderInfo order = orderInfoService.getById(refund.getOrderId());
        if (order != null) {
            order.setStatus(4); /* 4=退款中 */
            orderInfoService.updateById(order);
        }

        /* 【联动】站内消息：通知管理员处理退款 */
        Message msg = new Message();
        msg.setMemberId(0); /* 0=管理员 */
        msg.setTitle("退款申请：订单 " + (order != null ? order.getOrderNo() : ""));
        msg.setContent("用户申请退款，原因：" + refund.getReason() + "，金额：¥" + refund.getAmount() + "，请及时处理。");
        msg.setType("system");
        msg.setIsRead(0);
        msg.setSendTime(sdf.format(new Date()));
        messageService.save(msg);

        return R.ok().put("msg", "退款申请已提交，等待管理员审核");
    }

    /* ===== 【新增】9.5.升级会员等级 ===== */
    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/upgradeLevel")
    public R upgradeLevel(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        String level = body.get("level");
        LambdaQueryWrapper<Member> qw = new LambdaQueryWrapper<>();
        qw.eq(Member::getPhone, phone);
        Member m = memberService.getOne(qw);
        if (m == null) throw new RuntimeException("用户不存在");
        m.setLevel(level);
        memberService.updateById(m);

        /* 【联动】升级通知 */
        Message upMsg = new Message();
        upMsg.setMemberId(m.getId());
        upMsg.setTitle("恭喜升级为" + level);
        upMsg.setContent("恭喜您成功升级为" + level + "！专属权益已生效，快去体验吧！");
        upMsg.setType("system");
        upMsg.setIsRead(0);
        upMsg.setSendTime(sdf.format(new Date()));
        messageService.save(upMsg);

        return R.ok().put("msg", "升级成功");
    }

    /* ===== 【新增】9.6 每日签到 ===== */
    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/signIn")
    public R signIn(@RequestParam(required=false, defaultValue="1") Integer memberId) {
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        /* 查今天是否已签到 */
        LambdaQueryWrapper<SignIn> qw = new LambdaQueryWrapper<>();
        qw.eq(SignIn::getMemberId, memberId).eq(SignIn::getSignDate, today);
        SignIn exist = signInService.getOne(qw, false);
        if (exist != null) {
            return R.ok().put("msg", "今天已签到").put("points", 0);
        }

        /* 查昨天是否签到，算连续天数 */
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -1);
        String yesterday = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
        LambdaQueryWrapper<SignIn> qw2 = new LambdaQueryWrapper<>();
        qw2.eq(SignIn::getMemberId, memberId).eq(SignIn::getSignDate, yesterday);
        SignIn last = signInService.getOne(qw2, false);
        int continuous = (last != null) ? last.getContinuousDays() + 1 : 1;

        /* 积分规则：从配置表 tbl_sign_rule 读取 */
        com.gec.domain.entity.SignRule rule = signRuleMapper.selectById(1);
        int base = (rule != null && rule.getBasePoints() != null) ? rule.getBasePoints() : 5;
        int bonus = (rule != null && rule.getContinuousBonus() != null) ? rule.getContinuousBonus() : 2;
        int maxP = (rule != null && rule.getMaxPoints() != null) ? rule.getMaxPoints() : 20;
        int points = Math.min(base + (continuous - 1) * bonus, maxP);

        /* 写签到记录 */
        SignIn record = new SignIn();
        record.setMemberId(memberId);
        record.setSignDate(new Date());
        record.setContinuousDays(continuous);
        record.setPoints(points);
        signInService.save(record);

        /* 送积分到积分流水 */
        grantPoints(memberId, points, 0, "每日签到(" + continuous + "天连续)");

        /* 【联动】站内消息：签到成功通知 */
        Message msg = new Message();
        msg.setMemberId(memberId);
        msg.setTitle("签到成功");
        msg.setContent("恭喜您连续签到" + continuous + "天，获得" + points + "积分奖励。继续保持哦！");
        msg.setType("system");
        msg.setIsRead(0);
        msg.setSendTime(sdf.format(new Date()));
        messageService.save(msg);

        return R.ok().put("msg", "签到成功，连续" + continuous + "天，获得" + points + "积分").put("points", points);
    }

    /* ===== 【新增】10.记录搜索行为 ===== */
    @GetMapping("/searchLog")
    public R searchLog(@RequestParam String keyword,
                       @RequestParam(required=false, defaultValue="1") Integer memberId) {
        UserBehavior behavior = new UserBehavior();
        behavior.setMemberId(memberId);
        behavior.setBehaviorType("搜索");
        behavior.setSearchKeyword(keyword);
        behavior.setPageUrl("/shop");
        behavior.setStayTime(0);
        behavior.setDevice(Math.random() < 0.6 ? "Android" : "iOS");
        behavior.setChannel("APP");
        behavior.setSessionId(sessId(behavior.getMemberId()));
        behavior.setEntryPage(entryPageOf(behavior.getMemberId(), behavior.getSessionId()));
        userBehaviorService.save(behavior);
        return R.ok();
    }

    /* ===== 【新增】11.记录收藏行为 ===== */
    @PostMapping("/favoriteLog")
    public R favoriteLog(@RequestBody Map<String, Integer> body) {
        Integer mid = body.getOrDefault("memberId", 1);
        UserBehavior behavior = new UserBehavior();
        behavior.setMemberId(mid);
        behavior.setSpuId(body.get("spuId"));
        behavior.setBehaviorType("收藏");
        behavior.setPageUrl("/shopDetail/" + body.get("spuId"));
        behavior.setStayTime(0);
        behavior.setDevice(Math.random() < 0.6 ? "Android" : "iOS");
        behavior.setChannel("APP");
        behavior.setSessionId(sessId(behavior.getMemberId()));
        behavior.setEntryPage(entryPageOf(behavior.getMemberId(), behavior.getSessionId()));
        userBehaviorService.save(behavior);

        /* 注意：收藏表由 /Favorite/addFavorite 单独写入，此处不再联动，避免一次收藏产生两条记录 */

        return R.ok();
    }

    /* ===== 【新增】12.购物车相关 ===== */
    @Autowired
    private ICartService cartService;
    @Autowired
    private IAddressService addressService;
    @Autowired
    private IInvoiceService invoiceService;
    @Autowired
    private IFavoriteService favoriteService;
    @Autowired
    private ITicketService ticketService;
    @Autowired
    private ICouponService couponService;

    /* 加购物车 */
    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/addToCart")
    public R addToCart(@RequestBody Map<String, Integer> body) {
        Integer mid = body.getOrDefault("memberId", 1);
        Cart cart = new Cart();
        cart.setMemberId(mid);
        cart.setSpuId(body.get("spuId"));
        cart.setSkuId(body.get("skuId"));
        cart.setQuantity(body.get("quantity") == null ? 1 : body.get("quantity"));
        cart.setSelected(1);
        cartService.save(cart);

        /* 【联动】写行为日志(加入购物车) */
        UserBehavior behavior = new UserBehavior();
        behavior.setMemberId(mid);
        behavior.setSpuId(body.get("spuId"));
        behavior.setBehaviorType("加入购物车");
        behavior.setPageUrl("/shopDetail/" + body.get("spuId"));
        behavior.setStayTime(0);
        behavior.setDevice(Math.random() < 0.6 ? "Android" : "iOS");
        behavior.setChannel("APP");
        behavior.setSessionId(sessId(behavior.getMemberId()));
        behavior.setEntryPage(entryPageOf(behavior.getMemberId(), behavior.getSessionId()));
        userBehaviorService.save(behavior);

        return R.ok().put("msg", "已加入购物车");
    }

    /* 购物车列表 */
    @GetMapping("/cartList")
    public R cartList(@RequestParam(required=false, defaultValue="1") Integer memberId) {
        LambdaQueryWrapper<Cart> qw = new LambdaQueryWrapper<>();
        qw.eq(Cart::getMemberId, memberId);
        qw.orderByDesc(Cart::getAddTime);
        List<Cart> list = cartService.list(qw);
        return R.ok().put("data", list);
    }

    /* ===== 【新增】13.收货地址相关 ===== */
    @GetMapping("/addressList")
    public R addressList(@RequestParam(required=false, defaultValue="1") Integer memberId) {
        LambdaQueryWrapper<Address> qw = new LambdaQueryWrapper<>();
        qw.eq(Address::getMemberId, memberId);
        List<Address> list = addressService.list(qw);
        return R.ok().put("data", list);
    }

    /* ===== 【新增】14.申请开发票 ===== */
    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/applyInvoice")
    public R applyInvoice(@RequestBody Invoice invoice) {
        Integer mid = invoice.getMemberId() == null ? 1 : invoice.getMemberId();
        invoice.setMemberId(mid);
        invoice.setStatus("pending");
        invoiceService.save(invoice);

        /* 写站内消息通知 */
        Message msg = new Message();
        msg.setMemberId(mid);
        msg.setTitle("发票申请已提交");
        msg.setContent("您提交的发票申请（订单号：" + invoice.getOrderId() + "）已提交，财务将在3个工作日内为您开具。");
        msg.setType("system");
        msg.setIsRead(0);
        msg.setSendTime(sdf.format(new Date()));
        messageService.save(msg);

        return R.ok().put("msg", "发票申请已提交");
    }

    /* ===== 【新增】15.C端提交客服工单 ===== */
    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/submitTicket")
    public R submitTicket(@RequestBody Map<String, String> body) {
        Integer mid = Integer.parseInt(body.getOrDefault("memberId", "1"));
        Ticket ticket = new Ticket();
        ticket.setMemberId(mid);
        ticket.setTitle(body.get("title"));
        ticket.setContent(body.get("content"));
        ticket.setType(body.getOrDefault("type", "consult"));
        ticket.setStatus("pending");
        ticketService.save(ticket);

        /* 站内消息通知管理员 */
        Message msg = new Message();
        msg.setMemberId(0);
        msg.setTitle("新工单：" + body.get("title"));
        msg.setContent("用户提交了新的客服工单：" + body.get("content") + "，请及时处理。");
        msg.setType("system");
        msg.setIsRead(0);
        msg.setSendTime(sdf.format(new Date()));
        messageService.save(msg);

        return R.ok().put("msg", "工单提交成功，客服会尽快处理");
    }

    /* ==================== C 端客服即时对话 ====================
       口径与 tbl_chat_session / tbl_chat_message 的历史数据完全一致：
         · user_msg_count / merchant_reply_count = 明细表实际条数
         · first_response_sec = start_time → 第一条客服回复
         · session_duration_sec = start_time → end_time
       同一会员 30 分钟内复用同一个会话（与埋点会话号 sessId 同口径）。 */

    private Integer toInt(Object o, int def) {
        if (o == null) return def;
        try { return Integer.parseInt(String.valueOf(o).trim()); } catch (Exception e) { return def; }
    }

    private Date parseTime(String s) {
        try { return sdf.parse(s); } catch (Exception e) { return new Date(); }
    }

    /* 会话编号：CS + 10 位时间戳(秒) + 5 位会话id，与历史数据格式一致（如 CS174064497200001） */
    private String genChatNo(Integer id) {
        return "CS" + (System.currentTimeMillis() / 1000L) + String.format("%05d", id == null ? 0 : id);
    }

    /* 关键词应答：按用户实际输入匹配话术（每类 2~3 个变体，避免同会话重复），杜绝"答非所问" */
    private String replyOf(String text, String chatType) {
        String t = text == null ? "" : text;
        String[] p;
        if (t.contains("快递") || t.contains("物流") || t.contains("发货") || t.contains("运单")
                || t.contains("到哪") || t.contains("更新") || t.contains("没收到")) {
            p = new String[]{"帮您查了，包裹已在派送途中，具体以运单为准～",
                             "这边帮您催一下网点，稍后会有新的物流信息",
                             "包裹目前在中转站，正常 1-2 天送达"};
            return p[random.nextInt(p.length)];
        }
        if (t.contains("破") || t.contains("损") || t.contains("划痕") || t.contains("瑕疵") || t.contains("少件")) {
            p = new String[]{"实在抱歉，麻烦拍张照片发我，这边给您安排补发或换货",
                             "已经记录下来了，可以走售后换新，运费由我们承担"};
            return p[random.nextInt(p.length)];
        }
        if (t.contains("退") || t.contains("换") || t.contains("运费")) {
            p = new String[]{"在订单里申请退款即可，7 天内支持换货，我们及时处理",
                             "可以的，质量问题运费我们承担，非质量问题需要自理"};
            return p[random.nextInt(p.length)];
        }
        if (t.contains("保修") || t.contains("故障") || t.contains("坏") || t.contains("维修")
                || t.contains("充电") || t.contains("不能用")) {
            p = new String[]{"在保修期内可以寄回检测，麻烦先拍个视频发我看看",
                             "这边给您申请售后检测，确认有问题直接换新"};
            return p[random.nextInt(p.length)];
        }
        if (t.contains("优惠") || t.contains("券") || t.contains("便宜") || t.contains("活动") || t.contains("满减")) {
            p = new String[]{"目前有满 300 减 30 的活动，下单时选择券就能抵扣",
                             "已经是活动价了哦，还可以叠加店铺券使用",
                             "这边给您发一张新客券，下单会自动抵扣"};
            return p[random.nextInt(p.length)];
        }
        if (t.contains("投诉") || t.contains("太慢") || t.contains("没人") || t.contains("态度") || t.contains("失望")) {
            p = new String[]{"非常抱歉给您带来不好的体验，这边帮您登记并反馈给主管",
                             "已经帮您加急处理，稍后会有专员跟您联系"};
            return p[random.nextInt(p.length)];
        }
        if (t.contains("颜色") || t.contains("尺寸") || t.contains("重量") || t.contains("参数")
                || t.contains("支持") || t.contains("区别") || t.contains("正品") || t.contains("怎么样")) {
            p = new String[]{"亲，是正品保障的哦，详细参数详情页都有写，也可以帮您对比一下",
                             "这款是支持的，具体参数详情页有写，有其他疑问随时问我"};
            return p[random.nextInt(p.length)];
        }
        if (chatType != null) {
            if (chatType.contains("物流")) return "帮您查了，包裹已在派送途中";
            if (chatType.contains("退换")) return "7 天内支持退换货，在订单里申请即可";
            if (chatType.contains("售后")) return "在保修期内可以寄回检测";
            if (chatType.contains("优惠")) return "目前有满 300 减 30 的活动";
            if (chatType.contains("投诉")) return "非常抱歉，这边帮您登记反馈";
        }
        return "亲，已经看到您的消息了，这边帮您处理～";
    }

    /* 追加一条对话明细，并同步会话的计数 / 首响 / 时长（口径与历史数据一致） */
    private void appendChat(ChatSession cs, String content, String sender) {
        boolean isUser = "user".equals(sender);
        Date now = new Date();
        /* 时间基准 = 会话内最后一条消息，依次向前推进；超过当前时刻就钳到当前时刻。
           这样既能保证「用户问 → 客服答」的先后（同一秒内靠 id 维持），又不会写出未来时间 */
        String lastStr = cs.getEndTime() == null ? cs.getStartTime() : cs.getEndTime();
        Date base = parseTime(lastStr);
        Date floor = new Date(now.getTime() - 90000L);
        if (base.before(floor)) base = floor;
        if (base.after(now)) base = new Date(now.getTime() - 2000L);
        /* 「智能客服」应答快、「人工客服」稍慢，模拟真实节奏 */
        long step = isUser ? (1500L + random.nextInt(2500))
                : ("bot".equals(sender) ? (1200L + random.nextInt(1800)) : (3500L + random.nextInt(6000)));
        Date t = new Date(base.getTime() + step);
        if (t.after(now)) t = now;
        String ts = sdf.format(t);

        ChatMessage m = new ChatMessage();
        m.setSessionId(cs.getId());
        m.setChatNo(cs.getChatNo());
        m.setMemberId(cs.getMemberId());
        m.setSender(sender);
        m.setMsgType("text");
        m.setContent(content == null ? "" : (content.length() > 200 ? content.substring(0, 200) : content));
        m.setSendTime(ts);
        chatMessageService.save(m);

        if (isUser) {
            cs.setUserMsgCount((cs.getUserMsgCount() == null ? 0 : cs.getUserMsgCount()) + 1);
        } else if ("bot".equals(sender)) {
            /* 智能客服应答单独计数：它不代表「商家」行为，不计入 merchant_reply_count */
            cs.setBotReplyCount((cs.getBotReplyCount() == null ? 0 : cs.getBotReplyCount()) + 1);
        } else {
            cs.setMerchantReplyCount((cs.getMerchantReplyCount() == null ? 0 : cs.getMerchantReplyCount()) + 1);
        }
        if (cs.getStartTime() == null) cs.setStartTime(ts);
        cs.setEndTime(ts);
        long start = parseTime(cs.getStartTime()).getTime();
        cs.setSessionDurationSec((int) Math.max(1, (t.getTime() - start) / 1000));
        /* 首响 = 会话开始 → 第一条「人工」回复（智能客服应答不算商家的响应时效） */
        if ("merchant".equals(sender) && (cs.getFirstResponseSec() == null || cs.getFirstResponseSec() <= 0)) {
            cs.setFirstResponseSec((int) Math.max(1, (t.getTime() - start) / 1000));
        }
    }

    /* 1.进入客服：复用 30 分钟内的会话，没有就新建（可带首句问询） */
    @PostMapping("/chatStart")
    @Transactional
    public R chatStart(@RequestBody Map<String, Object> body) {
        Integer mid = toInt(body.get("memberId"), 1);
        Integer spuId = toInt(body.get("spuId"), 0);
        String chatType = body.get("chatType") == null ? "商品咨询" : String.valueOf(body.get("chatType"));
        String opening = body.get("content") == null ? null : String.valueOf(body.get("content")).trim();

        Date nowDate = new Date();
        String nowStr = sdf.format(nowDate);
        /* 新建会话时把开始时间提前 60 秒：给首轮问答留时间窗，保证消息时间都落在过去 */
        String startStr = sdf.format(new Date(nowDate.getTime() - 60000L));
        String half = sdf.format(new Date(nowDate.getTime() - 30 * 60 * 1000L));

        LambdaQueryWrapper<ChatSession> qw = new LambdaQueryWrapper<>();
        qw.eq(ChatSession::getMemberId, mid)
          .ge(ChatSession::getEndTime, half)
          .orderByDesc(ChatSession::getId)
          .last("LIMIT 1");
        ChatSession cs = chatSessionService.getOne(qw, false);

        if (cs == null) {
            cs = new ChatSession();
            cs.setMemberId(mid);
            cs.setSpuId(spuId == 0 ? null : spuId);
            cs.setChatType(chatType);
            cs.setSessionId(sessId(mid));
            cs.setUserMsgCount(0);
            cs.setMerchantReplyCount(0);
            cs.setFirstResponseSec(0);
            cs.setSessionDurationSec(0);
            cs.setIsConverted(0);
            cs.setStartTime(startStr);
            cs.setEndTime(startStr);
            chatSessionService.save(cs);
            /* 编号用自增 id 做序号（save 后才有 id），与历史 00001~15000 的规则一致 */
            cs.setChatNo(genChatNo(cs.getId()));
            chatSessionService.updateById(cs);
        } else if (cs.getStartTime() == null) {
            cs.setStartTime(nowStr);
        }

        /* 会话没有关联商品时补一个：取该会员最近一次浏览的商品。
           新建与复用都可能缺（比如会话由更早版本创建），
           而转化判定需要明确的商品对象（与历史 15000 条会话口径一致） */
        if (cs.getSpuId() == null) {
            List<Map<String, Object>> rec = jdbcTemplate.queryForList(
                    "SELECT spu_id FROM tbl_user_behavior WHERE member_id=? AND behavior_type='浏览' "
                  + "AND spu_id IS NOT NULL ORDER BY create_date DESC LIMIT 1", mid);
            if (!rec.isEmpty() && rec.get(0).get("spu_id") != null) {
                cs.setSpuId(((Number) rec.get(0).get("spu_id")).intValue());
                chatSessionService.updateById(cs);
            }
        }

        if (opening != null && !opening.isEmpty()) {
            appendChat(cs, opening, "user");
            appendChat(cs, replyOf(opening, cs.getChatType()), "bot");
            chatSessionService.updateById(cs);
        }

        Map<String, Object> ret = new HashMap<>();
        ret.put("session", cs);
        ret.put("rows", chatMessageService.listBySession(cs.getId()));
        return R.ok(ret);
    }

    /* 2.发送一条消息：写用户消息 + 客服应答，并同步计数器与时长 */
    @PostMapping("/chatSend")
    @Transactional
    public R chatSend(@RequestBody Map<String, Object> body) {
        Integer sid = toInt(body.get("sessionId"), 0);
        Integer mid = toInt(body.get("memberId"), 0);
        String content = body.get("content") == null ? null : String.valueOf(body.get("content")).trim();
        if (sid <= 0 || content == null || content.isEmpty()) {
            return R.ok().put("result", "failed").put("cause", "会话或消息内容为空");
        }
        ChatSession cs = chatSessionService.getById(sid);
        if (cs == null) return R.ok().put("result", "failed").put("cause", "会话不存在");
        /* 归属校验：会员只能往自己的会话里发消息 */
        if (mid > 0 && !mid.equals(cs.getMemberId())) {
            return R.ok().put("result", "failed").put("cause", "无权操作该会话");
        }

        appendChat(cs, content, "user");
        /* 智能客服即时应答（sender='bot'）：只是自助引导，不计入「商家回复」 */
        appendChat(cs, replyOf(content, cs.getChatType()), "bot");
        chatSessionService.updateById(cs);

        Map<String, Object> ret = new HashMap<>();
        ret.put("session", cs);
        ret.put("rows", chatMessageService.listBySession(sid));
        return R.ok(ret);
    }

    /* 3.拉取某会话的完整对话 */
    @GetMapping("/chatHistory")
    public R chatHistory(@RequestParam Integer sessionId,
                         @RequestParam(required = false) Integer memberId) {
        ChatSession cs = chatSessionService.getById(sessionId);
        if (cs == null) return R.ok().put("rows", java.util.Collections.emptyList());
        /* 归属校验：会员只能查看自己的会话 */
        if (memberId != null && memberId > 0 && !memberId.equals(cs.getMemberId())) {
            return R.ok().put("result", "failed").put("cause", "无权查看该会话");
        }
        Map<String, Object> ret = new HashMap<>();
        ret.put("session", cs);
        ret.put("rows", chatMessageService.listBySession(sessionId));
        return R.ok(ret);
    }

    /* 4.申请转人工：只写一条会员消息，不再触发智能应答，等商家（人工客服）来回复 */
    @PostMapping("/chatTransfer")
    @Transactional
    public R chatTransfer(@RequestBody Map<String, Object> body) {
        Integer sid = toInt(body.get("sessionId"), 0);
        Integer mid = toInt(body.get("memberId"), 0);
        ChatSession cs = chatSessionService.getById(sid);
        if (cs == null) return R.ok().put("result", "failed").put("cause", "会话不存在");
        if (mid > 0 && !mid.equals(cs.getMemberId())) {
            return R.ok().put("result", "failed").put("cause", "无权操作该会话");
        }
        appendChat(cs, "【申请转人工】请人工客服协助", "user");
        chatSessionService.updateById(cs);
        Map<String, Object> ret = new HashMap<>();
        ret.put("session", cs);
        ret.put("rows", chatMessageService.listBySession(sid));
        return R.ok(ret);
    }

    /* 5.该会员最近 5 次客服会话 */
    @GetMapping("/chatSessions")
    public R chatSessions(@RequestParam Integer memberId) {
        LambdaQueryWrapper<ChatSession> qw = new LambdaQueryWrapper<>();
        qw.eq(ChatSession::getMemberId, memberId)
          .orderByDesc(ChatSession::getId)
          .last("LIMIT 5");
        return R.ok().put("rows", chatSessionService.list(qw));
    }

    @ExceptionHandler
    public void exceptionHandler(Exception e, javax.servlet.http.HttpServletResponse resp) {
        R.err(e).write(resp);
    }
}
