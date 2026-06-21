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
@TableName("flight")
public class Flight implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String flightNo;

    private String airline;

    private String aircraftType;

    private String departureAirport;

    private String arrivalAirport;

    private LocalDateTime scheduledDepartureTime;

    private LocalDateTime actualDepartureTime;

    private String standNo;

    private String flightStatus;

    private Boolean deicingRequired;

    private Boolean deicingCompleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private String remark;

    private Boolean hasRiskRemark;

    private String riskReason;

    private String riskMarkedBy;

    private LocalDateTime riskMarkTime;

    private String riskClearBy;

    private LocalDateTime riskClearTime;

    @TableLogic
    private Integer deleted;
}
