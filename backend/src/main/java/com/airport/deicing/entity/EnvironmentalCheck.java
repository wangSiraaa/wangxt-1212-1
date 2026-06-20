package com.airport.deicing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("environmental_check")
public class EnvironmentalCheck implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String checkNo;

    private Long recoveryId;

    private String recoveryNo;

    private String inspectorName;

    private LocalDateTime checkTime;

    private String poolNo;

    private BigDecimal concentration;

    private BigDecimal standardConcentration;

    private BigDecimal phValue;

    private BigDecimal codValue;

    private BigDecimal bodValue;

    private String checkResult;

    private String checkStatus;

    private Boolean recheckRequired;

    private LocalDateTime recheckTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private String remark;

    @TableLogic
    private Integer deleted;
}
