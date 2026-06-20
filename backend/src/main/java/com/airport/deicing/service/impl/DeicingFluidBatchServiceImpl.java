package com.airport.deicing.service.impl;

import com.airport.deicing.common.PageResult;
import com.airport.deicing.common.ResultCode;
import com.airport.deicing.entity.DeicingFluidBatch;
import com.airport.deicing.exception.BusinessException;
import com.airport.deicing.mapper.DeicingFluidBatchMapper;
import com.airport.deicing.service.DeicingFluidBatchService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeicingFluidBatchServiceImpl extends ServiceImpl<DeicingFluidBatchMapper, DeicingFluidBatch> implements DeicingFluidBatchService {

    @Override
    public PageResult<DeicingFluidBatch> getBatchPage(Long pageNum, Long pageSize, String batchNo, String fluidType, String batchStatus) {
        LambdaQueryWrapper<DeicingFluidBatch> wrapper = new LambdaQueryWrapper<>();
        if (batchNo != null && !batchNo.isEmpty()) {
            wrapper.like(DeicingFluidBatch::getBatchNo, batchNo);
        }
        if (fluidType != null && !fluidType.isEmpty()) {
            wrapper.eq(DeicingFluidBatch::getFluidType, fluidType);
        }
        if (batchStatus != null && !batchStatus.isEmpty()) {
            wrapper.eq(DeicingFluidBatch::getBatchStatus, batchStatus);
        }
        wrapper.orderByDesc(DeicingFluidBatch::getCreateTime);

        IPage<DeicingFluidBatch> page = this.page(new Page<>(pageNum, pageSize), wrapper);
        return new PageResult<>(page);
    }

    @Override
    public DeicingFluidBatch getBatchById(Long id) {
        DeicingFluidBatch batch = this.getById(id);
        if (batch == null) {
            throw new BusinessException(ResultCode.BATCH_NOT_FOUND);
        }
        return batch;
    }

    @Override
    public DeicingFluidBatch getBatchByNo(String batchNo) {
        LambdaQueryWrapper<DeicingFluidBatch> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DeicingFluidBatch::getBatchNo, batchNo);
        return this.getOne(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addBatch(DeicingFluidBatch batch) {
        batch.setCreateTime(LocalDateTime.now());
        batch.setUpdateTime(LocalDateTime.now());
        if (batch.getBatchStatus() == null) {
            batch.setBatchStatus("AVAILABLE");
        }
        if (batch.getUsedVolume() == null) {
            batch.setUsedVolume(BigDecimal.ZERO);
        }
        if (batch.getRemainingVolume() == null) {
            batch.setRemainingVolume(batch.getTotalVolume());
        }
        return this.save(batch);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateBatch(DeicingFluidBatch batch) {
        DeicingFluidBatch exist = this.getById(batch.getId());
        if (exist == null) {
            throw new BusinessException(ResultCode.BATCH_NOT_FOUND);
        }
        batch.setUpdateTime(LocalDateTime.now());
        return this.updateById(batch);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteBatch(Long id) {
        DeicingFluidBatch exist = this.getById(id);
        if (exist == null) {
            throw new BusinessException(ResultCode.BATCH_NOT_FOUND);
        }
        return this.removeById(id);
    }

    @Override
    public List<DeicingFluidBatch> getAvailableBatches() {
        LambdaQueryWrapper<DeicingFluidBatch> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DeicingFluidBatch::getBatchStatus, "AVAILABLE")
               .gt(DeicingFluidBatch::getRemainingVolume, BigDecimal.ZERO)
               .orderByAsc(DeicingFluidBatch::getBatchNo);
        return this.list(wrapper);
    }

    @Override
    public List<DeicingFluidBatch> getAvailableBatchesByType(String fluidType) {
        LambdaQueryWrapper<DeicingFluidBatch> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DeicingFluidBatch::getFluidType, fluidType)
               .eq(DeicingFluidBatch::getBatchStatus, "AVAILABLE")
               .gt(DeicingFluidBatch::getRemainingVolume, BigDecimal.ZERO)
               .orderByAsc(DeicingFluidBatch::getBatchNo);
        return this.list(wrapper);
    }

    @Override
    public Map<String, Object> checkBatchAvailability(Long batchId, BigDecimal ambientTemperature) {
        DeicingFluidBatch batch = this.getBatchById(batchId);
        Map<String, Object> result = new HashMap<>();
        result.put("batch", batch);

        boolean available = true;
        String message = "";

        if (!"AVAILABLE".equals(batch.getBatchStatus()) && !"IN_USE".equals(batch.getBatchStatus())) {
            available = false;
            message = "除冰液批次状态不可用";
        }

        if (batch.getRemainingVolume().compareTo(BigDecimal.ZERO) <= 0) {
            available = false;
            message = "除冰液剩余量不足";
        }

        if (batch.getExpiryDate() != null && batch.getExpiryDate().isBefore(LocalDate.now())) {
            available = false;
            message = "除冰液已过期";
        }

        boolean temperatureValid = true;
        if (ambientTemperature != null && batch.getMinValidTemperature() != null && batch.getMaxValidTemperature() != null) {
            if (ambientTemperature.compareTo(batch.getMinValidTemperature()) < 0
                || ambientTemperature.compareTo(batch.getMaxValidTemperature()) > 0) {
                available = false;
                temperatureValid = false;
                message = "环境温度超出除冰液有效温度区间（" + batch.getMinValidTemperature() + "℃ ~ " + batch.getMaxValidTemperature() + "℃）";
            }
        }

        result.put("available", available);
        result.put("temperatureValid", temperatureValid);
        result.put("message", message);
        result.put("minTemp", batch.getMinValidTemperature());
        result.put("maxTemp", batch.getMaxValidTemperature());
        result.put("currentTemp", ambientTemperature);

        return result;
    }

    @Override
    public boolean validateTemperatureRange(Long batchId, BigDecimal ambientTemperature) {
        DeicingFluidBatch batch = this.getBatchById(batchId);

        if (batch.getMinValidTemperature() == null || batch.getMaxValidTemperature() == null) {
            return true;
        }

        if (ambientTemperature == null) {
            return true;
        }

        boolean valid = ambientTemperature.compareTo(batch.getMinValidTemperature()) >= 0
            && ambientTemperature.compareTo(batch.getMaxValidTemperature()) <= 0;

        if (!valid) {
            throw new BusinessException(ResultCode.BATCH_TEMPERATURE_OUT_OF_RANGE,
                "环境温度" + ambientTemperature + "℃超出除冰液有效温度区间（"
                + batch.getMinValidTemperature() + "℃ ~ " + batch.getMaxValidTemperature() + "℃）");
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean consumeBatchVolume(Long batchId, BigDecimal volume) {
        DeicingFluidBatch batch = this.getBatchById(batchId);

        if (batch.getRemainingVolume().compareTo(volume) < 0) {
            throw new BusinessException(ResultCode.BATCH_INSUFFICIENT_VOLUME);
        }

        BigDecimal newRemaining = batch.getRemainingVolume().subtract(volume);
        BigDecimal newUsed = batch.getUsedVolume().add(volume);

        batch.setRemainingVolume(newRemaining);
        batch.setUsedVolume(newUsed);
        batch.setUpdateTime(LocalDateTime.now());

        if (newRemaining.compareTo(BigDecimal.ZERO) <= 0) {
            batch.setBatchStatus("EMPTY");
        } else if ("AVAILABLE".equals(batch.getBatchStatus())) {
            batch.setBatchStatus("IN_USE");
        }

        return this.updateById(batch);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean returnBatchVolume(Long batchId, BigDecimal volume) {
        DeicingFluidBatch batch = this.getBatchById(batchId);

        BigDecimal newRemaining = batch.getRemainingVolume().add(volume);
        BigDecimal newUsed = batch.getUsedVolume().subtract(volume);
        if (newUsed.compareTo(BigDecimal.ZERO) < 0) {
            newUsed = BigDecimal.ZERO;
        }

        batch.setRemainingVolume(newRemaining);
        batch.setUsedVolume(newUsed);
        batch.setUpdateTime(LocalDateTime.now());

        if ("EMPTY".equals(batch.getBatchStatus()) && newRemaining.compareTo(BigDecimal.ZERO) > 0) {
            batch.setBatchStatus("AVAILABLE");
        }

        return this.updateById(batch);
    }
}
