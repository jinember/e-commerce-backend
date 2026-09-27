package com.gec.service;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gec.domain.entity.Invoice;

public interface IInvoiceService extends IService<Invoice> {
    IPage<Invoice> listInvoice(Page<Invoice> page);

    /** 开具发票：校验关联订单状态，更新发票信息并置为已开票 */
    Invoice issue(Invoice invoice);
}