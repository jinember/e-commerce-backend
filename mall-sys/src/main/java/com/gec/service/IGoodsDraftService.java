package com.gec.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gec.domain.entity.GoodsDraft;

import java.util.Date;

public interface IGoodsDraftService extends IService<GoodsDraft> {

    /* 从 Redis 缓存拍快照保存草稿，返回草稿ID。 */
    Integer saveFromCache(String pubKey, String draftName, Integer step);

    /* 继续编辑：把草稿快照灌回 Redis，返回 pubKey。 */
    String resume(Integer draftId);

    /* 立即发布草稿，返回新商品 spuId。 */
    Integer publishNow(Integer draftId);

    /* 设置定时发布。 */
    void schedule(Integer draftId, Date publishTime);

    /* 定时任务调用：发布所有到点的定时草稿，返回发布条数。 */
    int publishDueDrafts();
}
