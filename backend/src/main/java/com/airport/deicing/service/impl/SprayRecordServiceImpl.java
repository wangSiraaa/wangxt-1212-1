package com.airport.deicing.service.impl;

import com.airport.deicing.common.PageResult;
import com.airport.deicing.common.ResultCode;
import com.airport.deicing.entity.*;
import com.airport.deicing.exception.BusinessException;
import com.airport.deicing.mapper.SprayRecordMapper;
import com.airport.deicing.service.*;
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
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SprayRecordServiceImpl extends ServiceImpl<SprayRecordMapper, SprayRecord> implements SprayRecordService {

    private final FlightService flightService;
    private final VehicleService vehicleService;
    private final DeicingFluidBatchService batchService;
    private final DispatchRecordService dispatchService;

    @Override
    public PageResult<SprayRecord> getSprayPage(Long pageNum, Long pageSize, String sprayNo, Long flightId, Long vehicleId, String sprayStatus) {
        LambdaQueryWrapper<SprayRecord> wrapper = new LambdaQueryWrapper<>();
        if (sprayNo != null && !sprayNo.isEmpty()) {
            wrapper.like(SprayRecord::getSprayNo, sprayNo);
        }
        if (flightId != null) {
            wrapper.eq(SprayRecord::getFlightId, flightId);
        }
        if (vehicleId != null) {
            wrapper.eq(SprayRecord::getVehicleId, vehicleId);
        }
        if (sprayStatus != null && !sprayStatus.isEmpty()) {
            wrapper.eq(SprayRecord::getSprayStatus, sprayStatus);
        }
        wrapper.orderByDesc(SprayRecord::getStartTime);

        IPage<SprayRecord> page = this.page(new Page<>(pageNum, pageSize), wrapper);
        return new PageResult<>(page);
    }

    @Override
    public SprayRecord getSprayById(Long id) {
        SprayRecord spray = this.getById(id);
        if (spray == null) {
            throw new BusinessException(ResultCode.SPRAY_NOT_FOUND);
        }
        return spray;
    }

    @Override
    public SprayRecord getSprayByNo(String sprayNo) {
        LambdaQueryWrapper<SprayRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SprayRecord::getSprayNo, sprayNo);
        return this.getOne(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addSpray(SprayRecord spray) {
        Flight flight = flightService.getFlightById(spray.getFlightId());
        Vehicle vehicle = vehicleService.getVehicleById(spray.getVehicleId());
        DeicingFluidBatch batch = batchService.getBatchById(spray.getBatchId());

        batchService.validateTemperatureRange(spray.getBatchId(), spray.getAmbientTemperature());

        if (spray.getStartTime() == null) {
            spray.setStartTime(LocalDateTime.now());
        }
        spray.setCreateTime(LocalDateTime.now());
        spray.setUpdateTime(LocalDateTime.now());
        if (spray.getSprayStatus() == null) {
            spray.setSprayStatus("IN_PROGRESS");
        }

        spray.setFlightNo(flight.getFlightNo());
        spray.setVehicleNo(vehicle.getVehicleNo());
        spray.setBatchNo(batch.getBatchNo());

        if (spray.getDispatchId() != null) {
            DispatchRecord dispatch = dispatchService.getDispatchById(spray.getDispatchId());
            spray.setDispatchNo(dispatch.getDispatchNo());
        }

        vehicle.setVehicleStatus("SPRAYING");
        vehicleService.updateById(vehicle);

        flight.setFlightStatus("DEICING");
        flightService.updateById(flight);

        return this.save(spray);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateSpray(SprayRecord spray) {
        SprayRecord exist = this.getById(spray.getId());
        if (exist == null) {
            throw new BusinessException(ResultCode.SPRAY_NOT_FOUND);
        }
        spray.setUpdateTime(LocalDateTime.now());
        return this.updateById(spray);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteSpray(Long id) {
        SprayRecord exist = this.getById(id);
        if (exist == null) {
            throw new BusinessException(ResultCode.SPRAY_NOT_FOUND);
        }
        return this.removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean startSpray(Long id) {
        SprayRecord spray = this.getSprayById(id);

        if (!"IN_PROGRESS".equals(spray.getSprayStatus())) {
            throw new BusinessException(ResultCode.SPRAY_STATUS_ERROR, "喷洒状态异常，无法开始");
        }

        spray.setStartTime(LocalDateTime.now());
        spray.setUpdateTime(LocalDateTime.now());
        return this.updateById(spray);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean completeSpray(Long id, BigDecimal sprayVolume, String deicingEffect) {
        SprayRecord spray = this.getSprayById(id);

        if (!"IN_PROGRESS".equals(spray.getSprayStatus())) {
            throw new BusinessException(ResultCode.SPRAY_STATUS_ERROR, "喷洒状态异常，无法完成");
        }

        BigDecimal actualVolume = sprayVolume != null ? sprayVolume : spray.getSprayVolume();
        if (actualVolume == null || actualVolume.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ResultCode.SPRAY_STATUS_ERROR, "喷洒量不能为空或小于等于0");
        }

        batchService.consumeBatchVolume(spray.getBatchId(), actualVolume);

        Vehicle vehicle = vehicleService.getVehicleById(spray.getVehicleId());
        BigDecimal newVolume = vehicle.getCurrentFluidVolume().subtract(actualVolume);
        if (newVolume.compareTo(BigDecimal.ZERO) < 0) {
            newVolume = BigDecimal.ZERO;
        }
        vehicle.setCurrentFluidVolume(newVolume);
        vehicle.setVehicleStatus("IDLE");
        vehicleService.updateById(vehicle);

        spray.setSprayVolume(actualVolume);
        spray.setEndTime(LocalDateTime.now());
        spray.setSprayStatus("COMPLETED");
        spray.setDeicingEffect(deicingEffect);
        spray.setUpdateTime(LocalDateTime.now());
        boolean result = this.updateById(spray);

        if (spray.getDispatchId() != null) {
            dispatchService.completeDispatch(spray.getDispatchId());
        }

        flightService.completeDeicing(spray.getFlightId());

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelSpray(Long id) {
        SprayRecord spray = this.getSprayById(id);

        if ("COMPLETED".equals(spray.getSprayStatus())) {
            throw new BusinessException(ResultCode.SPRAY_STATUS_ERROR, "已完成的喷洒无法取消");
        }

        Vehicle vehicle = vehicleService.getVehicleById(spray.getVehicleId());
        vehicle.setVehicleStatus("IDLE");
        vehicleService.updateById(vehicle);

        spray.setSprayStatus("CANCELLED");
        spray.setUpdateTime(LocalDateTime.now());
        return this.updateById(spray);
    }

    @Override
    public List<SprayRecord> getTodaySprays() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        LambdaQueryWrapper<SprayRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(SprayRecord::getStartTime, startOfDay, endOfDay)
               .orderByDesc(SprayRecord::getStartTime);
        return this.list(wrapper);
    }

    @Override
    public List<SprayRecord> getInProgressSprays() {
        LambdaQueryWrapper<SprayRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SprayRecord::getSprayStatus, "IN_PROGRESS")
               .orderByAsc(SprayRecord::getStartTime);
        return this.list(wrapper);
    }
}
