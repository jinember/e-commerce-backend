package com.gec.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("tbl_sign_rule")
public class SignRule {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer basePoints;      // 基础签到积分
    private Integer continuousBonus; // 连续签到每天额外加成
    private Integer maxPoints;       // 单次上限
}
