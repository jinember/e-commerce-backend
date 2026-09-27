package com.gec.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gec.dao.GoodsDraftMapper;
import com.gec.domain.entity.GoodsAttrValue;
import com.gec.domain.entity.GoodsDraft;
import com.gec.domain.vo.GoodsBaseInfoVO;
import com.gec.domain.vo.GoodsDraftDataVO;
import com.gec.domain.vo.SkuLineVO;
import com.gec.service.IGoodsDetailService;
import com.gec.service.IGoodsDraftService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class GoodsDraftServiceImpl
        extends ServiceImpl<GoodsDraftMapper, GoodsDraft>
        implements IGoodsDraftService {

    @Autowired
    @Qualifier("myJacksonTemp")
    private RedisTemplate<String, Object> jacksonTemp;

    @Autowired
    private IGoodsDetailService detailService;

    /* Spring Boot 自动配置的 Jackson，用来把快照对象转 JSON 存库。 */
    @Autowired
    private ObjectMapper objectMapper;

    /* 生成一把 16 位大写 pubKey（与发布流程规则保持一致）。 */
    private String newPubKey() {
        String key = UUID.randomUUID().toString().toUpperCase()
                .replace("-", "");
        return key.substring(0, 16);
    }

    /* ---------------- 保存草稿：给 Redis 四段缓存拍快照 --------------- */
    @Override
    public Integer saveFromCache(String pubKey, String draftName, Integer step) {
        if (pubKey == null || pubKey.isEmpty()) {
            throw new RuntimeException("保存草稿失败：缺少发布缓存钥匙 pubKey");
        }
        /* 1.从 Redis 读出四段数据。 */
        GoodsBaseInfoVO baseInfo = detailService.getBaseInfoCache(pubKey);
        List<GoodsAttrValue> goodsAttr = detailService.getGoodsAttrValCache(pubKey);
        List<GoodsAttrValue> saleAttr = detailService.getGoodsSaleAttrCache(pubKey);
        List<SkuLineVO> skuList = detailService.getSkuInfoCache(pubKey);

        if (baseInfo == null || baseInfo.getGoodsDetail() == null) {
            throw new RuntimeException("保存草稿失败：基本信息还没填写，请先完成第一步");
        }

        /* 2.没传草稿名时，用商品名兜底。 */
        if (draftName == null || draftName.isEmpty()) {
            draftName = baseInfo.getGoodsDetail().getGoodsName();
        }
        if (draftName == null || draftName.isEmpty()) {
            draftName = "草稿_" + System.currentTimeMillis();
        }

        /* 3.打包成快照并序列化成 JSON。 */
        GoodsDraftDataVO data = new GoodsDraftDataVO();
        data.setBaseInfo(baseInfo);
        data.setGoodsAttr(goodsAttr);
        data.setSaleAttr(saleAttr);
        data.setSkuList(skuList);
        String json;
        try {
            json = objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            throw new RuntimeException("保存草稿失败：序列化出错 " + e.getMessage());
        }

        /* 4.每次保存都生成一条独立的草稿快照，不再按 pubKey 覆盖，
              便于保留同一商品在不同填写阶段的多个版本。 */
        GoodsDraft draft = new GoodsDraft();
        draft.setDraftName(draftName);
        draft.setSpuForm(json);
        draft.setPubKey(pubKey);
        draft.setStep(step == null ? 1 : step);
        draft.setStatus(0);
        this.save(draft);
        return draft.getId();
    }

    /* ---------------- 继续编辑：把草稿快照灌回 Redis --------------- */
    @Override
    public String resume(Integer draftId) {
        GoodsDraft draft = this.getById(draftId);
        if (draft == null) {
            throw new RuntimeException("草稿不存在或已被删除");
        }
        GoodsDraftDataVO data = parseSnapshot(draft.getSpuForm());

        /* 沿用草稿原来的 pubKey，没有才新生成。 */
        String pubKey = draft.getPubKey();
        if (pubKey == null || pubKey.isEmpty()) {
            pubKey = newPubKey();
        }
        restoreToCache(pubKey, data);
        return pubKey;
    }

    /* ---------------- 立即发布草稿 --------------- */
    @Override
    public Integer publishNow(Integer draftId) {
        GoodsDraft draft = this.getById(draftId);
        if (draft == null) {
            throw new RuntimeException("草稿不存在或已被删除");
        }
        if (draft.getStatus() != null && draft.getStatus() == 2) {
            throw new RuntimeException("该草稿已经发布过了");
        }
        GoodsDraftDataVO data = parseSnapshot(draft.getSpuForm());

        String pubKey = draft.getPubKey();
        if (pubKey == null || pubKey.isEmpty()) {
            pubKey = newPubKey();
        }
        /* 1.把快照灌回 Redis。 */
        restoreToCache(pubKey, data);
        /* 2.走原有发布逻辑写库并清缓存。 */
        Integer spuId = detailService.publishGoods(pubKey);
        /* 3.标记草稿为已发布。 */
        draft.setStatus(2);
        draft.setPublishTime(new Date());
        this.updateById(draft);
        return spuId;
    }

    /* ---------------- 设置定时发布 --------------- */
    @Override
    public void schedule(Integer draftId, Date publishTime) {
        GoodsDraft draft = this.getById(draftId);
        if (draft == null) {
            throw new RuntimeException("草稿不存在或已被删除");
        }
        if (publishTime.before(new Date())) {
            throw new RuntimeException("定时发布时间必须晚于当前时间");
        }
        draft.setPublishTime(publishTime);
        draft.setStatus(1);
        this.updateById(draft);
    }

    /* ---------------- 定时任务：发布所有到点的草稿 --------------- */
    @Override
    public int publishDueDrafts() {
        Date now = new Date();
        List<GoodsDraft> dueList = this.lambdaQuery()
                .eq(GoodsDraft::getStatus, 1)
                .isNotNull(GoodsDraft::getPublishTime)
                .le(GoodsDraft::getPublishTime, now)
                .list();
        int count = 0;
        for (GoodsDraft draft : dueList) {
            try {
                publishNow(draft.getId());
                count++;
                System.out.println("【定时发布】草稿[" + draft.getId() + "] "
                        + draft.getDraftName() + " 已自动发布");
            } catch (Exception e) {
                System.out.println("【定时发布】草稿[" + draft.getId()
                        + "] 发布失败：" + e.getMessage());
            }
        }
        return count;
    }

    /* ---------------- 公共：JSON -> 快照对象 --------------- */
    private GoodsDraftDataVO parseSnapshot(String json) {
        if (json == null || json.isEmpty()) {
            throw new RuntimeException("草稿数据为空");
        }
        try {
            return objectMapper.readValue(json, GoodsDraftDataVO.class);
        } catch (Exception e) {
            throw new RuntimeException("读取草稿失败：反序列化出错 " + e.getMessage());
        }
    }

    /* ---------------- 公共：把快照四段数据写回 Redis --------------- */
    private void restoreToCache(String pubKey, GoodsDraftDataVO data) {
        ValueOperations<String, Object> OP = jacksonTemp.opsForValue();
        if (data.getBaseInfo() != null) {
            data.getBaseInfo().setPubKey(pubKey);
            OP.set(pubKey + "-base", data.getBaseInfo());
        }
        if (data.getGoodsAttr() != null) {
            OP.set(pubKey + "-goods-attr", data.getGoodsAttr());
        }
        if (data.getSaleAttr() != null) {
            OP.set(pubKey + "-sale-attr", data.getSaleAttr());
        }
        if (data.getSkuList() != null) {
            OP.set(pubKey + "-sku", data.getSkuList());
        }
    }
}
