package com.gec.service.impl;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gec.dao.InvoiceDao;
import com.gec.domain.entity.Invoice;
import com.gec.domain.entity.OrderInfo;
import com.gec.service.IInvoiceService;
import com.gec.service.IOrderInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class InvoiceServiceImpl extends ServiceImpl<InvoiceDao, Invoice> implements IInvoiceService {

    @Autowired
    private IOrderInfoService orderInfoService;

    @Override
    public IPage<Invoice> listInvoice(Page<Invoice> page) {
        return baseMapper.selectPageWithMember(page);
    }

    @Override
    @Transactional
    public Invoice issue(Invoice param) {
        // 1. 查询原发票
        Invoice existing = this.getById(param.getId());
        if (existing == null) {
            throw new RuntimeException("发票不存在");
        }
        if ("issued".equals(existing.getStatus())) {
            throw new RuntimeException("该发票已开具，不能重复开票");
        }

        // 2. 校验关联订单：待支付(0)、已取消(4) 不能开票
        OrderInfo order = orderInfoService.getById(existing.getOrderId());
        if (order == null) {
            throw new RuntimeException("关联订单不存在，无法开票");
        }
        Integer os = order.getStatus();
        // 白名单：仅已支付(1)、已发货(2)、已完成(3) 可开票；待支付/已取消/异常状态均拦截
        if (os == null || (os != 1 && os != 2 && os != 3)) {
            throw new RuntimeException("订单状态不允许开票（仅已支付、已发货、已完成的订单可开票）");
        }

        // 3. 校验发票信息
        String type = param.getType();
        if (type == null || type.trim().isEmpty()) {
            throw new RuntimeException("请选择发票类型");
        }
        if ("company".equals(type)) {
            if (param.getTitle() == null || param.getTitle().trim().isEmpty()) {
                throw new RuntimeException("企业发票必须填写发票抬头");
            }
            if (param.getTaxNo() == null || param.getTaxNo().trim().isEmpty()) {
                throw new RuntimeException("企业发票必须填写纳税人识别号");
            }
        } else {
            // 个人发票抬头默认"个人"
            if (param.getTitle() == null || param.getTitle().trim().isEmpty()) {
                param.setTitle("个人");
            }
        }

        // 4. 更新发票信息，置为已开票
        existing.setType(type);
        existing.setTitle(param.getTitle());
        existing.setTaxNo(param.getTaxNo());
        existing.setStatus("issued");
        existing.setIssueDate(new Date());
        existing.setInvoiceFile(param.getInvoiceFile());
        this.updateById(existing);
        return existing;
    }
}
