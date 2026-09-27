package com.gec.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gec.domain.entity.OrderInfo;
import com.gec.domain.search.OrderSearch;

public interface IOrderInfoService extends IService<OrderInfo> {

    /* 1.订单分页列表(带搜索)。 */
    IPage<OrderInfo> listOrder(Page page, OrderSearch param);

    /* 2.更新订单状态(发货/完成/取消)。 */
    void updateStatus(OrderInfo orderInfo);
}
