package com.gec.service.impl;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gec.dao.BrowseHistoryDao;
import com.gec.domain.entity.BrowseHistory;
import com.gec.service.IBrowseHistoryService;
import org.springframework.stereotype.Service;

@Service
public class BrowseHistoryServiceImpl extends ServiceImpl<BrowseHistoryDao, BrowseHistory> implements IBrowseHistoryService {

    @Override
    public IPage<BrowseHistory> listBrowseHistory(Page<BrowseHistory> page) {
        return baseMapper.selectPageWithMember(page);
    }
}