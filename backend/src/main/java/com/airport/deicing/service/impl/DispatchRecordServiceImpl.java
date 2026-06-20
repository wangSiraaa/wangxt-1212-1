package com.airport.deicing.service.impl;

import com.airport.deicing.common.PageResult;
import com.airport.deicing.common.ResultCode;
import com.airport.deicing.entity.*;
import com.airport.deicing.exception.BusinessException;
import com.airport.deicing.mapper.DispatchRecordMapper;
import com.airport.deicing.mapper.FlightMapper;
import com.airport.deicing.mapper.VehicleMapper;
import com.airport.deicing.mapper.DeicingFluidBatchMapper;
import com.airport.deicing.service.DeicingFluidBatchService;
import com.airport.deicing.service.DispatchRecordService;
import com.airport.deicing.service.FlightService;
import com.airport.deicing.service.VehicleService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DispatchRecordServiceImpl extends ServiceImpl<DispatchRecordMapper, DispatchRecord> implements DispatchRecordService {

    private final FlightService flightService;
    private final VehicleService vehicleService;
    private final DeicingFluidBatchService batchService;

    @Override
    public PageResult<DispatchRecord> getDispatchPage(Long pageNum, Long pageSize, String dispatchNo, Long flightId, Long vehicleId, String dispatchStatus) {
        LambdaQueryWrapper<DispatchRecord> wrapper = new LambdaQueryWrapper<>();
        if (dispatchNo != null && !dispatchNo.isEmpty()) {
            wrapper.like(DispatchRecord::getDispatchNo, dispatchNo);
        }
        if (flightId != null) {
            wrapper.eq(DispatchRecord::getFlightId, flightId);
        }
        if (vehicleId != null) {
            wrapper.eq(DispatchRecord::getVehicleId, vehicleId);
        }
        if (dispatchStatus != null && !dispatchStatus.isEmpty()) {
            wrapper.eq(DispatchRecord::getDispatchStatus, dispatchStatus);
        }
        wrapper.orderByDesc(DispatchRecord::getDispatchTime);

        IPage<DispatchRecord> page = this.page(new Page<>(pageNum, pageSize), wrapper);
        return new PageResult<>(page);
    }

    @Override
    public DispatchRecord getDispatchById(Long id) {
        DispatchRecord dispatch = this.getById(id);
        if (dispatch == null) {
            throw new BusinessException(ResultCode.DISPATCH_NOT_FOUND);
        }
        return dispatch;
    }

    @Override
    public DispatchRecord getDispatchByNo(String dispatchNo) {
        LambdaQueryWrapper<DispatchRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DispatchRecord::getDispatchNo, dispatchNo);
        return this.getOne(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addDispatch(DispatchRecord dispatch) {
        Flight flight = flightService.getFlightById(dispatch.getFlightId());
        Vehicle vehicle = vehicleService.getVehicleById(dispatch.getVehicleId());
        DeicingFluidBatch batch = batchService.getBatchById(dispatch.getBatchId());

        if (!"IDLE".equals(vehicle.getVehicleStatus())) {
            throw new BusinessException(ResultCode.VEHICLE_NOT_AVAILABLE);
        }

        batchService.validateTemperatureRange(dispatch.getBatchId(), dispatch.getAmbientTemperature());

        dispatch.setDispatchTime(LocalDateTime.now());
        dispatch.setCreateTime(LocalDateTime.now());
        dispatch.setUpdateTime(LocalDateTime.now());
        if (dispatch.getDispatchStatus() == null) {
            dispatch.setDispatchStatus("PENDING");
        }

        dispatch.setFlightNo(flight.getFlightNo());
        dispatch.setVehicleNo(vehicle.getVehicleNo());
        dispatch.setBatchNo(batch.getBatchNo());
        if (dispatch.getStandNo() == null) {
            dispatch.setStandNo(flight.getStandNo());
        }

        vehicle.setVehicleStatus("DISPATCHED");
        vehicleService.updateById(vehicle);

        flight.setFlightStatus("DEICING");
        flightService.updateById(flight);

        return this.save(dispatch);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateDispatch(DispatchRecord dispatch) {
        DispatchRecord exist = this.getById(dispatch.getId());
        if (exist == null) {
            throw new BusinessException(ResultCode.DISPATCH_NOT_FOUND);
        }
        dispatch.setUpdateTime(LocalDateTime.now());
        return this.updateById(dispatch);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteDispatch(Long id) {
        DispatchRecord exist = this.getById(id);
        if (exist == null) {
            throw new BusinessException(ResultCode.DISPATCH_NOT_FOUND);
        }
        return this.removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean startDispatch(Long id) {
        DispatchRecord dispatch = this.getDispatchById(id);

        if (!"PENDING".equals(dispatch.getDispatchStatus())) {
            throw new BusinessException(ResultCode.DISPATCH_STATUS_ERROR, "调度状态异常，无法开始");
        }

        Vehicle vehicle = vehicleService.getVehicleById(dispatch.getVehicleId());
        vehicle.setVehicleStatus("SPRAYING");
        vehicleService.updateById(vehicle);

        dispatch.setDispatchStatus("IN_PROGRESS");
        dispatch.setUpdateTime(LocalDateTime.now());
        return this.updateById(dispatch);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean completeDispatch(Long id) {
        DispatchRecord dispatch = this.getDispatchById(id);

        if (!"IN_PROGRESS".equals(dispatch.getDispatchStatus())) {
            throw new BusinessException(ResultCode.DISPATCH_STATUS_ERROR, "调度状态异常，无法完成");
        }

        Vehicle vehicle = vehicleService.getVehicleById(dispatch.getVehicleId());
        vehicle.setVehicleStatus("IDLE");
        vehicleService.updateById(vehicle);

        dispatch.setDispatchStatus("COMPLETED");
        dispatch.setUpdateTime(LocalDateTime.now());
        return this.updateById(dispatch);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelDispatch(Long id) {
        DispatchRecord dispatch = this.getDispatchById(id);

        if ("COMPLETED".equals(dispatch.getDispatchStatus())) {
            throw new BusinessException(ResultCode.DISPATCH_STATUS_ERROR, "已完成的调度无法取消");
        }

        Vehicle vehicle = vehicleService.getVehicleById(dispatch.getVehicleId());
        vehicle.setVehicleStatus("IDLE");
        vehicleService.updateById(vehicle);

        Flight flight = flightService.getFlightById(dispatch.getFlightId());
        flight.setFlightStatus("SCHEDULED");
        flightService.updateById(flight);

        dispatch.setDispatchStatus("CANCELLED");
        dispatch.setUpdateTime(LocalDateTime.now());
        return this.updateById(dispatch);
    }

    @Override
    public List<DispatchRecord> getTodayDispatches() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        LambdaQueryWrapper<DispatchRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(DispatchRecord::getDispatchTime, startOfDay, endOfDay)
               .orderByDesc(DispatchRecord::getDispatchTime);
        return this.list(wrapper);
    }

    @Override
    public List<DispatchRecord> getPendingDispatches() {
        LambdaQueryWrapper<DispatchRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DispatchRecord::getDispatchStatus, "PENDING")
               .orderByAsc(DispatchRecord::getDispatchTime);
        return this.list(wrapper);
    }
}
