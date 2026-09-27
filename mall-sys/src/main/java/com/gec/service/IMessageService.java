package com.gec.service;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gec.domain.entity.Message;

public interface IMessageService extends IService<Message> {
    IPage<Message> listMessage(Page<Message> page);
}