package com.gec.service;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gec.domain.entity.Ticket;

public interface ITicketService extends IService<Ticket> {
    IPage<Ticket> listTicket(Page<Ticket> page);
}