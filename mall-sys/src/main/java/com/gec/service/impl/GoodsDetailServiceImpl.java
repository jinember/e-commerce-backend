package com.gec.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gec.dao.GoodsDetailMapper;
import com.gec.dao.SkuInfoMapper;
import com.gec.dao.SkuSaleAttrValueMapper;
import com.gec.dao.SpuAlbumMapper;
import com.gec.dao.SpuGoodsAttrMapper;
import com.gec.domain.entity.GoodsAttrValue;
import com.gec.domain.entity.GoodsDetail;
import com.gec.domain.entity.SkuInfo;
import com.gec.domain.entity.SkuSaleAttrValue;
import com.gec.domain.entity.SpuAlbum;
import com.gec.domain.entity.SpuGoodsAttr;
import com.gec.domain.vo.GoodsAttrValuesVO;
import com.gec.domain.vo.GoodsBaseInfoVO;
import com.gec.domain.vo.SkuAttrItem;
import com.gec.domain.vo.SkuInfoVO;
import com.gec.domain.vo.SkuLineVO;
import com.gec.service.IGoodsDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class GoodsDetailServiceImpl
    extends ServiceImpl<GoodsDetailMapper, GoodsDetail>
    implements IGoodsDetailService {
    @Autowired
    @Qualifier("myJacksonTemp")
    private RedisTemplate<String,Object> jacksonTemp;

    @Autowired
    private SpuGoodsAttrMapper spuGoodsAttrMapper;

    @Autowired
    private SkuInfoMapper skuInfoMapper;

    @Autowired
    private SpuAlbumMapper spuAlbumMapper;

    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    /*
        KEY:"0107AX-base"       [STEP01-基本信息]
        KEY:"0107AX-goods-attr" [STEP02-规格属性]
        KEY:"0107AX-sale-attr"  [STEP03-销售属性]
        KEY:"0107AX-sku"        [STEP04-SKU信息]
    */
    /* -----------------【01】保存基础信息缓存【START】------------------ */
    @Override
    public String saveBaseInfoCache(GoodsBaseInfoVO biVO) {
        ValueOperations<String, Object> OP =
            jacksonTemp.opsForValue();
        /* 前端带 pubKey 说明是流程中返回修改, 沿用旧钥匙, 避免后续缓存全丢。 */
        String pubKey = biVO.getPubKey();
        if( pubKey == null || pubKey.isEmpty() ){
            pubKey = UUID.randomUUID()
                .toString().toUpperCase();
            pubKey = pubKey.replace("-","").substring(0,16);
        }
        String KEY = pubKey + "-base";
        OP.set(KEY, biVO);
        printMARK(KEY, pubKey);
        return pubKey;
    }
    /* -----------------【01】保存基础信息缓存【END】------------------ */

    /* -----------------【02】保存规格属性缓存【START】------------------ */
    @Override
    public String saveGoodsAttrValCache(
        GoodsAttrValuesVO gavVO) {
        ValueOperations<String, Object> OP =
            jacksonTemp.opsForValue();
        String pubKey = gavVO.getPubKey();
        String KEY = pubKey +"-goods-attr";
        OP.set(KEY, gavVO.getAttrList());
        printMARK(KEY, pubKey);
        return pubKey;
    }
    /* -----------------【02】保存规格属性缓存【END】------------------ */

    /* -----------------【03】读取基础信息缓存【START】------------------ */
    @Override
    public GoodsBaseInfoVO getBaseInfoCache(String pubKey) {
        String KEY = pubKey + "-base";
        ValueOperations<String, Object> OP =
            jacksonTemp.opsForValue();
        GoodsBaseInfoVO biVO =
            (GoodsBaseInfoVO) OP.get(KEY);
        printMARK(KEY, pubKey);
        return biVO;
    }
    /* -----------------【03】读取基础信息缓存【END】------------------ */

    /* -----------------【04】读取规格属性缓存【START】------------------ */
    @Override
    @SuppressWarnings("unchecked")
    public List<GoodsAttrValue> getGoodsAttrValCache(String pubKey) {
        String KEY = pubKey + "-goods-attr";
        ValueOperations<String, Object> OP =
            jacksonTemp.opsForValue();
        List<GoodsAttrValue> list =
            (List<GoodsAttrValue>) OP.get(KEY);
        printMARK(KEY, pubKey);
        return list;
    }
    /* -----------------【04】读取规格属性缓存【END】------------------ */

    /* -----------------【05】保存销售属性缓存【START】------------------ */
    @Override
    public String saveGoodsSaleAttrCache(
        GoodsAttrValuesVO gavVO) {
        ValueOperations<String, Object> OP =
            jacksonTemp.opsForValue();
        String pubKey = gavVO.getPubKey();
        String KEY = pubKey +"-sale-attr";
        OP.set(KEY, gavVO.getAttrList());
        printMARK(KEY, pubKey);
        return pubKey;
    }
    /* -----------------【05】保存销售属性缓存【END】------------------ */

    /* -----------------【06】读取销售属性缓存【START】------------------ */
    @Override
    @SuppressWarnings("unchecked")
    public List<GoodsAttrValue> getGoodsSaleAttrCache(String pubKey) {
        String KEY = pubKey + "-sale-attr";
        ValueOperations<String, Object> OP =
            jacksonTemp.opsForValue();
        List<GoodsAttrValue> list =
            (List<GoodsAttrValue>) OP.get(KEY);
        printMARK(KEY, pubKey);
        return list;
    }
    /* -----------------【06】读取销售属性缓存【END】------------------ */

    /* -----------------【07】保存SKU缓存【START】------------------ */
    @Override
    public String saveSkuInfoCache(SkuInfoVO skuVO) {
        ValueOperations<String, Object> OP =
            jacksonTemp.opsForValue();
        String pubKey = skuVO.getPubKey();
        String KEY = pubKey + "-sku";
        OP.set(KEY, skuVO.getSkuList());
        printMARK(KEY, pubKey);
        return pubKey;
    }
    /* -----------------【07】保存SKU缓存【END】------------------ */

    /* -----------------【08】读取SKU缓存【START】------------------ */
    @Override
    @SuppressWarnings("unchecked")
    public List<SkuLineVO> getSkuInfoCache(String pubKey) {
        String KEY = pubKey + "-sku";
        ValueOperations<String, Object> OP =
            jacksonTemp.opsForValue();
        List<SkuLineVO> list =
            (List<SkuLineVO>) OP.get(KEY);
        printMARK(KEY, pubKey);
        return list;
    }
    /* -----------------【08】读取SKU缓存【END】------------------ */

    /* -----------------【09】发布商品：写库+清缓存【START】------------------ */
    @Override
    public Integer publishGoods(String pubKey) {
        /* 1.读取四段缓存。 */
        GoodsBaseInfoVO biVO = getBaseInfoCache(pubKey);
        if( biVO == null || biVO.getGoodsDetail() == null ){
            throw new RuntimeException("发布失败：基本信息缓存缺失，请从第一步重新填写");
        }
        List<GoodsAttrValue> attrList =
            getGoodsAttrValCache(pubKey);
        List<SkuLineVO> skuList =
            getSkuInfoCache(pubKey);
        String now = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss").format(new Date());

        /* 2.写入 SPU 主表 tbl_spu_detail。 */
        GoodsDetail gd = biVO.getGoodsDetail();
        gd.setStatus(1);                 // 1=发布
        gd.setCreateDate(now);
        gd.setUpdateDate(now);
        this.save(gd);
        Integer spuId = gd.getId();

        /* 3.写入 SPU 规格参数表 tbl_spu_goods_attr。 */
        if( attrList != null ){
            int sort = 1;
            for( GoodsAttrValue gav : attrList ){
                SpuGoodsAttr sga = new SpuGoodsAttr();
                sga.setSpuId(spuId);
                sga.setAttrId(gav.getAttrId());
                sga.setAttrName(gav.getAttrName());
                sga.setAttrValue(gav.getAttrValue());
                sga.setAttrSort(sort++);
                spuGoodsAttrMapper.insert(sga);
            }
        }

        /* 4.写入 SKU 三张表。 */
        if( skuList != null ){
            for( SkuLineVO line : skuList ){
                /* 4.0 只保存勾选中的组合。 */
                if( line.getSelected() == null
                    || !line.getSelected() ){
                    continue;
                }
                /* 4.1 tbl_sku_info。 */
                SkuInfo sku = new SkuInfo();
                sku.setSpuId(spuId);
                sku.setSkuName(line.getSkuName());
                sku.setSkuTitle(line.getSkuTitle());
                sku.setSkuSubtitle(line.getSkuSubtitle());
                sku.setPrice(line.getPrice());
                sku.setCategoryId(gd.getCategoryId());
                sku.setBrandId(gd.getBrandId());
                sku.setSaleCount(0);
                skuInfoMapper.insert(sku);
                Integer skuId = sku.getSkuId();

                /* 4.2 tbl_sku_album 商品图片集。 */
                if( line.getImages() != null
                    && !line.getImages().isEmpty() ){
                    SpuAlbum album = new SpuAlbum();
                                        album.setSkuId(String.valueOf(skuId));
                    album.setImages(line.getImages());
                    album.setDefaultImage(line.getDefaultImage());
                    album.setCreateTime(now);
                    album.setUpdateTime(now);
                    spuAlbumMapper.insert(album);
                }

                /* 4.3 tbl_sku_sale_attr_value 销售属性值。 */
                if( line.getAttrList() != null ){
                    int sort = 1;
                    for( SkuAttrItem item : line.getAttrList() ){
                        SkuSaleAttrValue ssav =
                            new SkuSaleAttrValue();
                        ssav.setSpuId(spuId);
                        ssav.setSkuId(skuId);
                        ssav.setAttrId(item.getAttrId());
                        ssav.setAttrName(item.getAttrName());
                        ssav.setAttrValue(item.getAttrValue());
                        ssav.setAttrSort(sort++);
                        skuSaleAttrValueMapper.insert(ssav);
                    }
                }
            }
        }

        /* 5.发布完成，清理 Redis 缓存。 */
        jacksonTemp.delete(pubKey + "-base");
        jacksonTemp.delete(pubKey + "-goods-attr");
        jacksonTemp.delete(pubKey + "-sale-attr");
        jacksonTemp.delete(pubKey + "-sku");

        return spuId;
    }
    /* -----------------【09】发布商品：写库+清缓存【END】------------------ */

    void printMARK(String KEY, String pubKey){
        System.out.println("+-----------------[REDIS跟踪器]-------------------+");
        System.out.println("【REDIS】写入 REDIS:..");
        System.out.println("【REDIS】KEY:"+ KEY);
        System.out.println("【REDIS】pubKey:"+ pubKey);
        System.out.println("+------------------------------------------------+");
    }
}
