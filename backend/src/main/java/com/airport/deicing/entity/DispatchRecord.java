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
@TableName("dispatch_record")
public class DispatchRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String dispatchNo;

    private Long flightId;

    private String flightNo;

    private Long vehicleId;

    private String vehicleNo;

    private Long batchId;

    private String batchNo;

    private String dispatcherName;

    private LocalDateTime dispatchTime;

    private BigDecimal estimatedSprayVolume;

    private String dispatchStatus;

    private String standNo;

    private BigDecimal ambientTemperature;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private String remark;

    @TableLogic
    private Integer deleted;
}
