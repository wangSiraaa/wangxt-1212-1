package com.airport.deicing.service.impl;

import com.airport.deicing.common.PageResult;
import com.airport.deicing.common.ResultCode;
import com.airport.deicing.entity.Vehicle;
import com.airport.deicing.exception.BusinessException;
import com.airport.deicing.mapper.VehicleMapper;
import com.airport.deicing.service.VehicleService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleServiceImpl extends ServiceImpl<VehicleMapper, Vehicle> implements VehicleService {

    @Override
    public PageResult<Vehicle> getVehiclePage(Long pageNum, Long pageSize, String vehicleNo, String vehicleStatus, String vehicleType) {
        LambdaQueryWrapper<Vehicle> wrapper = new LambdaQueryWrapper<>();
        if (vehicleNo != null && !vehicleNo.isEmpty()) {
            wrapper.like(Vehicle::getVehicleNo, vehicleNo);
        }
        if (vehicleStatus != null && !vehicleStatus.isEmpty()) {
            wrapper.eq(Vehicle::getVehicleStatus, vehicleStatus);
        }
        if (vehicleType != null && !vehicleType.isEmpty()) {
            wrapper.eq(Vehicle::getVehicleType, vehicleType);
        }
        wrapper.orderByAsc(Vehicle::getVehicleNo);

        IPage<Vehicle> page = this.page(new Page<>(pageNum, pageSize), wrapper);
        return new PageResult<>(page);
    }

    @Override
    public Vehicle getVehicleById(Long id) {
        Vehicle vehicle = this.getById(id);
        if (vehicle == null) {
            throw new BusinessException(ResultCode.VEHICLE_NOT_FOUND);
        }
        return vehicle;
    }

    @Override
    public Vehicle getVehicleByNo(String vehicleNo) {
        LambdaQueryWrapper<Vehicle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Vehicle::getVehicleNo, vehicleNo);
        return this.getOne(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addVehicle(Vehicle vehicle) {
        vehicle.setCreateTime(LocalDateTime.now());
        vehicle.setUpdateTime(LocalDateTime.now());
        if (vehicle.getVehicleStatus() == null) {
            vehicle.setVehicleStatus("IDLE");
        }
        if (vehicle.getCurrentFluidVolume() == null) {
            vehicle.setCurrentFluidVolume(BigDecimal.ZERO);
        }
        return this.save(vehicle);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateVehicle(Vehicle vehicle) {
        Vehicle exist = this.getById(vehicle.getId());
        if (exist == null) {
            throw new BusinessException(ResultCode.VEHICLE_NOT_FOUND);
        }
        vehicle.setUpdateTime(LocalDateTime.now());
        return this.updateById(vehicle);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteVehicle(Long id) {
        Vehicle exist = this.getById(id);
        if (exist == null) {
            throw new BusinessException(ResultCode.VEHICLE_NOT_FOUND);
        }
        return this.removeById(id);
    }

    @Override
    public List<Vehicle> getAvailableVehicles() {
        LambdaQueryWrapper<Vehicle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Vehicle::getVehicleStatus, "IDLE")
               .gt(Vehicle::getCurrentFluidVolume, BigDecimal.ZERO)
               .orderByAsc(Vehicle::getVehicleNo);
        return this.list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateVehicleStatus(Long id, String status) {
        Vehicle vehicle = this.getVehicleById(id);
        vehicle.setVehicleStatus(status);
        vehicle.setUpdateTime(LocalDateTime.now());
        return this.updateById(vehicle);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateFluidVolume(Long id, BigDecimal volume) {
        Vehicle vehicle = this.getVehicleById(id);
        vehicle.setCurrentFluidVolume(volume);
        vehicle.setUpdateTime(LocalDateTime.now());
        return this.updateById(vehicle);
    }
}
