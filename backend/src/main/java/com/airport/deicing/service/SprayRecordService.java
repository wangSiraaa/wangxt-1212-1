package com.airport.deicing.service;

import com.airport.deicing.entity.SprayRecord;
import com.airport.deicing.common.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;
import java.util.List;

public interface SprayRecordService extends IService<SprayRecord> {

    PageResult<SprayRecord> getSprayPage(Long pageNum, Long pageSize, String sprayNo, Long flightId, Long vehicleId, String sprayStatus);

    SprayRecord getSprayById(Long id);

    SprayRecord getSprayByNo(String sprayNo);

    boolean addSpray(SprayRecord spray);

    boolean updateSpray(SprayRecord spray);

    boolean deleteSpray(Long id);

    boolean startSpray(Long id);

    boolean completeSpray(Long id, BigDecimal sprayVolume, String deicingEffect);

    boolean cancelSpray(Long id);

    List<SprayRecord> getTodaySprays();

    List<SprayRecord> getInProgressSprays();
}
