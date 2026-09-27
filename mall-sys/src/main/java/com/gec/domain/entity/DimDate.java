package com.gec.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName(value = "tbl_dim_date")
public class DimDate {
    @TableId
    private Integer dateId;
    private String date;
    private Integer year;
    private Integer quarter;
    private Integer month;
    private Integer week;
    private Integer day;
    private String weekday;
    private Integer isWeekend;
    private Integer isHoliday;
}
