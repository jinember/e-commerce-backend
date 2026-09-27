package com.gec.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@TableName(value = "tbl_order_op_log")
@EqualsAndHashCode(callSuper = false)
public class OrderOpLog implements Serializable {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer orderId;       //order_id
    private String operator;       //operator 操作人
    private String opType;         //op_type 操作类型
    private Integer fromStatus;    //from_status
    private Integer toStatus;       //to_status
    private String remark;         //remark
    private String createTime;     //create_time
}
