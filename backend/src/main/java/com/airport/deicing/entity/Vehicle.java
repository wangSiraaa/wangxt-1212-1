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
@TableName("vehicle")
public class Vehicle implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String vehicleNo;

    private String vehicleName;

    private String vehicleType;

    private String driverName;

    private String driverPhone;

    private Long currentBatchId;

    private BigDecimal currentFluidVolume;

    private String vehicleStatus;

    private String currentStand;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private String remark;

    @TableLogic
    private Integer deleted;
}
