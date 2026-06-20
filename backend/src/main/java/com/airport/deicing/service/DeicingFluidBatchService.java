package com.airport.deicing.service;

import com.airport.deicing.entity.DeicingFluidBatch;
import com.airport.deicing.common.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface DeicingFluidBatchService extends IService<DeicingFluidBatch> {

    PageResult<DeicingFluidBatch> getBatchPage(Long pageNum, Long pageSize, String batchNo, String fluidType, String batchStatus);

    DeicingFluidBatch getBatchById(Long id);

    DeicingFluidBatch getBatchByNo(String batchNo);

    boolean addBatch(DeicingFluidBatch batch);

    boolean updateBatch(DeicingFluidBatch batch);

    boolean deleteBatch(Long id);

    List<DeicingFluidBatch> getAvailableBatches();

    List<DeicingFluidBatch> getAvailableBatchesByType(String fluidType);

    Map<String, Object> checkBatchAvailability(Long batchId, BigDecimal ambientTemperature);

    boolean validateTemperatureRange(Long batchId, BigDecimal ambientTemperature);

    boolean consumeBatchVolume(Long batchId, BigDecimal volume);

    boolean returnBatchVolume(Long batchId, BigDecimal volume);
}
