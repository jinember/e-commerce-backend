package com.gec.task;

import com.gec.service.IGoodsDraftService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 商品草稿定时发布任务。
 * 每分钟扫描一次：状态为"定时待发布"(status=1) 且发布时间已到的草稿，自动发布。
 */
@Component
public class GoodsDraftTask {

    @Autowired
    private IGoodsDraftService draftService;

    /* cron：秒 分 时 日 月 周 —— 每分钟第 0 秒执行一次。 */
    @Scheduled(cron = "0 * * * * ?")
    public void autoPublish() {
        int count = draftService.publishDueDrafts();
        if (count > 0) {
            System.out.println("【定时发布】本次自动发布草稿 " + count + " 条");
        }
    }
}
