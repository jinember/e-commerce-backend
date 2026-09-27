package com.gec.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gec.domain.entity.PointLog;

public interface IPointLogService extends IService<PointLog> {
    IPage<PointLog> listPointLog(Page page, PointLog param);
}

