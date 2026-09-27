package com.gec.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gec.dao.PointLogMapper;
import com.gec.domain.entity.PointLog;
import com.gec.service.IPointLogService;
import org.springframework.stereotype.Service;

@Service
public class PointLogServiceImpl
        extends ServiceImpl<PointLogMapper, PointLog>
        implements IPointLogService {

    @Override
    public IPage<PointLog> listPointLog(Page page, PointLog param) {
        return baseMapper.selectPageWithMember(page);
    }
}

