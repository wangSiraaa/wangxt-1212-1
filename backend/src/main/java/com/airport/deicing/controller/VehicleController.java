package com.airport.deicing.controller;

import com.airport.deicing.common.PageResult;
import com.airport.deicing.common.Result;
import com.airport.deicing.entity.Vehicle;
import com.airport.deicing.service.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Tag(name = "车辆管理", description = "除冰车车辆管理接口")
@RestController
@RequestMapping("/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    @Operation(summary = "分页查询车辆列表")
    @GetMapping("/page")
    public Result<PageResult<Vehicle>> getVehiclePage(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize,
            @RequestParam(required = false) String vehicleNo,
            @RequestParam(required = false) String vehicleStatus,
            @RequestParam(required = false) String vehicleType) {
        return Result.success(vehicleService.getVehiclePage(pageNum, pageSize, vehicleNo, vehicleStatus, vehicleType));
    }

    @Operation(summary = "根据ID获取车辆详情")
    @GetMapping("/{id}")
    public Result<Vehicle> getVehicleById(@PathVariable Long id) {
        return Result.success(vehicleService.getVehicleById(id));
    }

    @Operation(summary = "根据车辆编号获取车辆")
    @GetMapping("/no/{vehicleNo}")
    public Result<Vehicle> getVehicleByNo(@PathVariable String vehicleNo) {
        return Result.success(vehicleService.getVehicleByNo(vehicleNo));
    }

    @Operation(summary = "新增车辆")
    @PostMapping
    public Result<Void> addVehicle(@RequestBody Vehicle vehicle) {
        vehicleService.addVehicle(vehicle);
        return Result.success();
    }

    @Operation(summary = "更新车辆")
    @PutMapping
    public Result<Void> updateVehicle(@RequestBody Vehicle vehicle) {
        vehicleService.updateVehicle(vehicle);
        return Result.success();
    }

    @Operation(summary = "删除车辆")
    @DeleteMapping("/{id}")
    public Result<Void> deleteVehicle(@PathVariable Long id) {
        vehicleService.deleteVehicle(id);
        return Result.success();
    }

    @Operation(summary = "获取可用车辆列表")
    @GetMapping("/available")
    public Result<List<Vehicle>> getAvailableVehicles() {
        return Result.success(vehicleService.getAvailableVehicles());
    }

    @Operation(summary = "更新车辆状态")
    @PostMapping("/{id}/status")
    public Result<Void> updateVehicleStatus(@PathVariable Long id, @RequestParam String status) {
        vehicleService.updateVehicleStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "更新车辆液体量")
    @PostMapping("/{id}/fluid-volume")
    public Result<Void> updateFluidVolume(@PathVariable Long id, @RequestParam BigDecimal volume) {
        vehicleService.updateFluidVolume(id, volume);
        return Result.success();
    }
}
