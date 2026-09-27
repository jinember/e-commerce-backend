package com.gec.domain.entity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName(value = "tbl_invoice")
@EqualsAndHashCode(callSuper = false)
public class Invoice extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer orderId;
    private Integer memberId;
    private String type;
    private String title;
    private String taxNo;
    private Double amount;
    private String status;
    private java.util.Date issueDate;   //开票时间
    private String invoiceFile;        //发票PDF文件名

    @TableField(exist = false)
    private String nickname;    //关联查询：用户昵称
}