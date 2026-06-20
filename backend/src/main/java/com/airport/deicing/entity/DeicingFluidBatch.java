package com.airport.deicing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("deicing_fluid_batch")
public class DeicingFluidBatch implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String batchNo;

    private String fluidType;

    private String fluidName;

    private String manufacturer;

    private LocalDate productionDate;

    private LocalDate expiryDate;

    private BigDecimal totalVolume;

    private BigDecimal usedVolume;

    private BigDecimal remainingVolume;

    private BigDecimal minValidTemperature;

    private BigDecimal maxValidTemperature;

    private BigDecimal concentration;

    private String batchStatus;

    private String storageLocation;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private String remark;

    @TableLogic
    private Integer deleted;
}
