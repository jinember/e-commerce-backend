package com.gec.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gec.dao.OrderInfoMapper;
import com.gec.domain.entity.OrderInfo;
import com.gec.domain.search.OrderSearch;
import com.gec.service.IOrderInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderInfoServiceImpl
        extends ServiceImpl<OrderInfoMapper, OrderInfo>
        implements IOrderInfoService {

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Override
    public IPage<OrderInfo> listOrder(Page page, OrderSearch param) {
        /*1.创建条件查询器*/
        QueryWrapper<OrderInfo> QW = new QueryWrapper<>();
        /*2.动态设置查询条件*/
        if (param.getOrderNo() != null) {
            QW.like("order_no", param.getOrderNo());
        }
        if (param.getUserName() != null) {
            QW.like("user_name", param.getUserName());
        }
        if (param.getStatus() != null) {
            QW.eq("status", param.getStatus());
        }
        /*3.按下单时间倒序*/
        QW.orderByDesc("create_date");
        /*4.分页查询*/
        return orderInfoMapper.selectPage(page, QW);
    }

    @Override
    public void updateStatus(OrderInfo orderInfo) {
        /*1.按ID修改状态*/
        UpdateWrapper<OrderInfo> UW = new UpdateWrapper<>();
        UW.eq("id", orderInfo.getId())
          .set("status", orderInfo.getStatus());
        int rows = orderInfoMapper.update(null, UW);
        /*2.失败则抛出业务异常*/
        if (rows == 0) {
            throw new RuntimeException("订单状态更新失败");
        }
    }
}
