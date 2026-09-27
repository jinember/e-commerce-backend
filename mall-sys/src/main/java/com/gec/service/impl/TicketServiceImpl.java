package com.gec.service.impl;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gec.dao.TicketDao;
import com.gec.domain.entity.Ticket;
import com.gec.service.ITicketService;
import org.springframework.stereotype.Service;

@Service
public class TicketServiceImpl extends ServiceImpl<TicketDao, Ticket> implements ITicketService {

    @Override
    public IPage<Ticket> listTicket(Page<Ticket> page) {
        return baseMapper.selectPageWithMember(page);
    }
}