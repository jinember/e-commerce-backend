package com.gec.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gec.dao.CategoryMapper;
import com.gec.domain.bo.CategoryBO;
import com.gec.domain.entity.Category;
import com.gec.domain.entity.Node;
import com.gec.domain.vo.BrandVO;
import com.gec.domain.vo.CategoryBrandVO;
import com.gec.service.IBaseService;
import com.gec.service.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CategoryServiceImpl
        extends ServiceImpl<CategoryMapper, Category>
        implements ICategoryService, IBaseService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public List<Node> listCategory() {
        QueryWrapper<Category> QW = new QueryWrapper<>();
        List<Category> list = categoryMapper.selectList(QW);
        List<Node> nodes = convertNodeBO(list);
        return nodes;
    }

    @Override
    public Node copyObj(Node node) {
        Category C = (Category) node;
        String pIds = C.getPIds();
        CategoryBO BO = new CategoryBO(C);
        String[] sp = pIds.split(",");
        int LEVEL = sp.length;
        BO.setLevel(LEVEL);
        return BO;
    }

    @Override
    public Integer[] getPidsArr(Integer id) {
        String pids = categoryMapper.getPids(id);
        pids = pids.replaceAll("^0,", "") + "," + id;
        String[] arr = pids.split(",");
        Integer[] i_arr = new Integer[arr.length];
        for (int j = 0; j < arr.length; j++) {
            i_arr[j] = Integer.valueOf(arr[j]);
        }
        return i_arr;
    }

    @Override
    public String getPids(Integer id) {
        return categoryMapper.getPids(id);
    }

    @Override
    public void associateBrand(CategoryBrandVO cbVO) {
        Integer categoryId = cbVO.getCategoryId();
        List<Integer> brandIds = cbVO.getDeleteIds();
        if (brandIds != null && brandIds.size() > 0) {
            categoryMapper.removeAssociate(categoryId, brandIds);
        }
        int SIZE = cbVO.getBrands().size();
        if (SIZE > 0) {
            int cnt = categoryMapper.associateBrand(cbVO);
            if (cnt != SIZE) {
                throw new RuntimeException("设置类别对品牌关联失败");
            }
        }
    }

    @Override
    public IPage<BrandVO> getListByBrand(Page page, Map data) {
        return null;
    }
}
