package com.gec.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gec.dao.MemberMapper;
import com.gec.dao.OrderInfoMapper;
import com.gec.domain.entity.Member;
import com.gec.service.IMemberService;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MemberServiceImpl
        extends ServiceImpl<MemberMapper, Member>
        implements IMemberService {
    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Override
    public IPage<Member> listMember(Page page, Member param) {
        QueryWrapper<Member> QW = new QueryWrapper<>();
        if (param.getId() != null) {
            QW.like("id", param.getId());
        }
        if (param.getNickname() != null && !param.getNickname().isEmpty()) {
            QW.like("nickname", param.getNickname());
        }
        if (param.getPhone() != null && !param.getPhone().isEmpty()) {
            QW.like("phone", param.getPhone());
        }
        IPage<Member> result = baseMapper.selectPage(page, QW);
        List<Map<String,Object>> stats = orderInfoMapper.statByMember();
        java.util.Map<Integer, Map<String,Object>> statMap = new HashMap<>();
        for (Map<String,Object> row : stats) {
            Object midObj = row.get("member_id");
            if (midObj == null) continue;  //跳过member_id为null的记录
            Integer mid = ((Number) midObj).intValue();
            statMap.put(mid, row);
        }
        for (Member m : result.getRecords()) {
            Map<String,Object> s = statMap.get(m.getId());
            if (s != null) {
                m.setOrderCount(((Number) s.get("orderCount")).intValue());
                m.setTotalSpend(((Number) s.get("totalSpend")).doubleValue());
            } else {
                m.setOrderCount(0);
                m.setTotalSpend(0.0);
            }
        }
        return result;
    }

    @Override
    public Map<String, Object> statMember() {
        Map<String, Object> map = new HashMap<>();
        map.put("total", baseMapper.selectCount(null));
        map.put("blackCount", baseMapper.selectCount(
                new QueryWrapper<Member>().eq("status", 0)));
        map.put("normalCount", baseMapper.selectCount(
                new QueryWrapper<Member>().eq("level", "普通会员")));
        map.put("goldCount", baseMapper.selectCount(
                new QueryWrapper<Member>().eq("level", "黄金会员")));
        return map;
    }
}
