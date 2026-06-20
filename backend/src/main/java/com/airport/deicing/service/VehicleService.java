package com.airport.deicing.service;

import com.airport.deicing.entity.Vehicle;
import com.airport.deicing.common.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;
import java.util.List;

public interface VehicleService extends IService<Vehicle> {

    PageResult<Vehicle> getVehiclePage(Long pageNum, Long pageSize, String vehicleNo, String vehicleStatus, String vehicleType);

    Vehicle getVehicleById(Long id);

    Vehicle getVehicleByNo(String vehicleNo);

    boolean addVehicle(Vehicle vehicle);

    boolean updateVehicle(Vehicle vehicle);

    boolean deleteVehicle(Long id);

    List<Vehicle> getAvailableVehicles();

    boolean updateVehicleStatus(Long id, String status);

    boolean updateFluidVolume(Long id, BigDecimal volume);
}
