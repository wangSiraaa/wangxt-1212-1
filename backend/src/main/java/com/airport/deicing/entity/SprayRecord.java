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
@TableName("spray_record")
public class SprayRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String sprayNo;

    private Long dispatchId;

    private String dispatchNo;

    private Long flightId;

    private String flightNo;

    private Long vehicleId;

    private String vehicleNo;

    private Long batchId;

    private String batchNo;

    private String driverName;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private BigDecimal sprayVolume;

    private String sprayType;

    private Boolean wingSprayed;

    private Boolean fuselageSprayed;

    private Boolean tailSprayed;

    private BigDecimal fluidTemperature;

    private BigDecimal ambientTemperature;

    private String sprayStatus;

    private String deicingEffect;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private String remark;

    @TableLogic
    private Integer deleted;
}
