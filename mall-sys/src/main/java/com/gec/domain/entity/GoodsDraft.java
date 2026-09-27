package com.gec.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("tbl_goods_draft")
public class GoodsDraft {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String draftName;
    private String spuForm;
    /* 发布流程在 Redis 中的缓存钥匙。 */
    private String pubKey;
    /* 保存草稿时所在的步骤：1基本信息 2规格参数 3销售属性 4SKU设置。 */
    private Integer step;
    /* 0=普通草稿 1=定时待发布 2=已发布。 */
    private Integer status;
    /* 定时发布时间（为空表示普通草稿）。 */
    private Date publishTime;
    private Date createTime;
    private Date updateTime;
}
