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
@TableName("waste_recovery")
public class WasteRecovery implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String recoveryNo;

    private String recoveryPoolNo;

    private Long sprayId;

    private Long flightId;

    private String flightNo;

    private String recyclerName;

    private LocalDateTime recoveryTime;

    private BigDecimal recoveryVolume;

    private String recoveryMethod;

    private BigDecimal concentration;

    private BigDecimal phValue;

    private String contaminationLevel;

    private String recoveryStatus;

    private String disposalMethod;

    private LocalDateTime disposalTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private String remark;

    @TableLogic
    private Integer deleted;
}
