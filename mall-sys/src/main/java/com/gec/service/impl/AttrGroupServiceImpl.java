package com.gec.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gec.dao.AttrGroupMapper;
import com.gec.domain.entity.GoodsAttrGroup;
import com.gec.service.IAttrGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AttrGroupServiceImpl
    extends ServiceImpl<AttrGroupMapper, GoodsAttrGroup>
    implements IAttrGroupService {
    @Autowired
    private AttrGroupMapper attrGroupMapper;

    /* --请填入代码1--  *///获取属性分组列表
    @Override
    public IPage listAttrGroup(Page page, Integer categoryId) {
        //1.使用lambda表达式条件设置器
            LambdaQueryWrapper<GoodsAttrGroup> QW = new LambdaQueryWrapper<>();
            //2.相当于生成：WHERE category_id = 入参值
        QW.eq(GoodsAttrGroup::getCategoryId, categoryId);
        //3.调用Mybatis-Plus内置方法进行查询
        return attrGroupMapper.selectPage(page, QW);
    }

    /* --请填入代码2--  *///添加属性分组
    @Override
    public void addAttrGroup(GoodsAttrGroup attrGroup) {
        //1.使用mybatis-plus的映射器的内置方法
        int cnt = attrGroupMapper.insert(attrGroup);
        if (cnt != 1) {
            throw new RuntimeException("添加属性分组失败");
        }
    }


    /* --请填入代码3--  *///更新属性分组
    @Override
    public void updateAttrGroup(GoodsAttrGroup attrGroup) {
        //1.使用lambda表达式条件设置器
        LambdaQueryWrapper<GoodsAttrGroup> UW = new LambdaQueryWrapper<>();
        //2.相当于生成：WHERE id = 入参值
        UW.eq(GoodsAttrGroup::getId, attrGroup.getId());
        int cnt = attrGroupMapper.update(attrGroup, UW);
        if (cnt != 1) {
            throw new RuntimeException("更新属性分组失败");
        }
    }


}
