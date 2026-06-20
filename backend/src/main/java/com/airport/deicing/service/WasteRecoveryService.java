package com.airport.deicing.service;

import com.airport.deicing.entity.WasteRecovery;
import com.airport.deicing.common.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface WasteRecoveryService extends IService<WasteRecovery> {

    PageResult<WasteRecovery> getRecoveryPage(Long pageNum, Long pageSize, String recoveryNo, Long flightId, String recoveryStatus);

    WasteRecovery getRecoveryById(Long id);

    WasteRecovery getRecoveryByNo(String recoveryNo);

    boolean addRecovery(WasteRecovery recovery);

    boolean updateRecovery(WasteRecovery recovery);

    boolean deleteRecovery(Long id);

    boolean testRecovery(Long id, java.math.BigDecimal concentration, java.math.BigDecimal phValue, String contaminationLevel);

    boolean qualifyRecovery(Long id, String disposalMethod);

    boolean unqualifyRecovery(Long id);

    List<WasteRecovery> getTodayRecoveries();

    List<WasteRecovery> getPendingRecoveries();
}
