package com.gec.domain.entity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName(value = "tbl_browse_history")
@EqualsAndHashCode(callSuper = false)
public class BrowseHistory extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer memberId;
    private Integer spuId;
    private String browseTime;
    private Integer stayDuration;

    @TableField(exist = false)
    private String nickname;    //关联查询：用户昵称
}