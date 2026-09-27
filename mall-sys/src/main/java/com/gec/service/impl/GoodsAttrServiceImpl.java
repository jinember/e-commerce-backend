package com.gec.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gec.dao.GoodsAttrMapper;
import com.gec.domain.bo.GoodsAttrBO;
import com.gec.domain.entity.GoodsAttr;
import com.gec.domain.search.GoodsAttrSearch;
import com.gec.domain.vo.GoodsAttrVO;
import com.gec.service.IGoodsAttrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/*
   {1}流的使用示例..
     List<GoodsAttrBO> boList = list.stream().map(
        ....
     ).collect( Collectors.toList() );

   {2}调用了映射器的转换方法。
      GoodsAttrBO attrBO = GoodsAttrMap
            .INSTANCE.convertBO(attr);

   {3}注意: 如果只是以下调用是不会迭代集合的。
      boList.stream().map( ... );
 */

@Service
public class GoodsAttrServiceImpl
    extends ServiceImpl<GoodsAttrMapper, GoodsAttr>
    implements IGoodsAttrService {
    @Autowired
    private GoodsAttrMapper goodsAttrMapper;

    @Override
    public IPage<GoodsAttrBO> listGoodsAttr(
            Page page, GoodsAttrSearch attrSearch) {
        /* 分页+搜索查询属性列表(带所属分组名)。 */
        return goodsAttrMapper.getGoodsAttrList(page, attrSearch);
    }

    @Override
    public void addGoodsAttr(GoodsAttrVO attrVO) {
        /* 1.先插入属性定义。 */
        GoodsAttr attr = getEntity(attrVO);
        goodsAttrMapper.insert(attr);   // 自增主键回填到 attr.id
        /* 2.再建立属性与分组的关联。 */
        if( attrVO.getAttrGroupId() != null ){
            goodsAttrMapper.addGroupAttrRelation(
                attrVO.getAttrGroupId(), attr.getId());
        }
    }

    @Override
    public void updateGoodsAttr(GoodsAttrVO attrVO) {
        /* 1.更新属性定义。 */
        GoodsAttr attr = getEntity(attrVO);
        goodsAttrMapper.updateById(attr);
        /* 2.更新属性与分组的关联(先删后建, 保证分组一致)。 */
        goodsAttrMapper.removeGroupAttrRelation(attrVO.getId());
        if( attrVO.getAttrGroupId() != null ){
            goodsAttrMapper.addGroupAttrRelation(
                attrVO.getAttrGroupId(), attrVO.getId());
        }
    }

    @Override
    public void deleteGoodsAttr(Integer id) {
        /* 1.先删除属性与分组的关联。 */
        goodsAttrMapper.removeGroupAttrRelation(id);
        /* 2.再删除属性定义。 */
        goodsAttrMapper.deleteById(id);
    }

    @Override
    public List<GoodsAttr> queryGoodsAttrByCategory(
        Integer categoryId, Integer attrType) {
        /*
        * ★根据类别ID，查询 tbl_goods_attr 表。
        * 锁定条件：类别ID，属性类型=1
        */
        QueryWrapper<GoodsAttr> QW = new QueryWrapper<>();
        QW.eq("category_id",categoryId)
            .eq("attr_type",attrType);
        return goodsAttrMapper.selectList( QW );
    }

    private GoodsAttr getEntity(GoodsAttrVO attrVO) {
        GoodsAttr attr = new GoodsAttr();
        attr.setId( attrVO.getId() );
        attr.setAttrName( attrVO.getAttrName() );
        attr.setCategoryId( attrVO.getCategoryId() );
        attr.setAttrType( attrVO.getAttrType() );
        attr.setValueType( attrVO.getValueType() );
        attr.setAttrValue( attrVO.getAttrValue() );
        attr.setEnable( attrVO.getEnable() );
        attr.setSearchEnable( attrVO.getSearchEnable() );
        return attr;
    }
}
