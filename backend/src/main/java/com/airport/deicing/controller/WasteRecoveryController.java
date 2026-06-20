package com.airport.deicing.controller;

import com.airport.deicing.common.PageResult;
import com.airport.deicing.common.Result;
import com.airport.deicing.entity.WasteRecovery;
import com.airport.deicing.service.WasteRecoveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Tag(name = "废液回收管理", description = "废液回收记录管理接口")
@RestController
@RequestMapping("/waste-recovery")
@RequiredArgsConstructor
public class WasteRecoveryController {

    private final WasteRecoveryService wasteRecoveryService;

    @Operation(summary = "分页查询废液回收记录列表")
    @GetMapping("/page")
    public Result<PageResult<WasteRecovery>> getRecoveryPage(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize,
            @RequestParam(required = false) String recoveryNo,
            @RequestParam(required = false) Long flightId,
            @RequestParam(required = false) String recoveryStatus) {
        return Result.success(wasteRecoveryService.getRecoveryPage(pageNum, pageSize, recoveryNo, flightId, recoveryStatus));
    }

    @Operation(summary = "根据ID获取废液回收详情")
    @GetMapping("/{id}")
    public Result<WasteRecovery> getRecoveryById(@PathVariable Long id) {
        return Result.success(wasteRecoveryService.getRecoveryById(id));
    }

    @Operation(summary = "根据回收单号获取记录")
    @GetMapping("/no/{recoveryNo}")
    public Result<WasteRecovery> getRecoveryByNo(@PathVariable String recoveryNo) {
        return Result.success(wasteRecoveryService.getRecoveryByNo(recoveryNo));
    }

    @Operation(summary = "新增废液回收记录")
    @PostMapping
    public Result<Void> addRecovery(@RequestBody WasteRecovery recovery) {
        wasteRecoveryService.addRecovery(recovery);
        return Result.success();
    }

    @Operation(summary = "更新废液回收记录")
    @PutMapping
    public Result<Void> updateRecovery(@RequestBody WasteRecovery recovery) {
        wasteRecoveryService.updateRecovery(recovery);
        return Result.success();
    }

    @Operation(summary = "删除废液回收记录")
    @DeleteMapping("/{id}")
    public Result<Void> deleteRecovery(@PathVariable Long id) {
        wasteRecoveryService.deleteRecovery(id);
        return Result.success();
    }

    @Operation(summary = "检测回收液")
    @PostMapping("/{id}/test")
    public Result<Void> testRecovery(
            @PathVariable Long id,
            @RequestParam BigDecimal concentration,
            @RequestParam BigDecimal phValue,
            @RequestParam String contaminationLevel) {
        wasteRecoveryService.testRecovery(id, concentration, phValue, contaminationLevel);
        return Result.success();
    }

    @Operation(summary = "判定回收液合格")
    @PostMapping("/{id}/qualify")
    public Result<Void> qualifyRecovery(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "TREATMENT") String disposalMethod) {
        wasteRecoveryService.qualifyRecovery(id, disposalMethod);
        return Result.success();
    }

    @Operation(summary = "判定回收液不合格")
    @PostMapping("/{id}/unqualify")
    public Result<Void> unqualifyRecovery(@PathVariable Long id) {
        wasteRecoveryService.unqualifyRecovery(id);
        return Result.success();
    }

    @Operation(summary = "获取今日回收记录")
    @GetMapping("/today")
    public Result<List<WasteRecovery>> getTodayRecoveries() {
        return Result.success(wasteRecoveryService.getTodayRecoveries());
    }

    @Operation(summary = "获取待检测回收记录")
    @GetMapping("/pending")
    public Result<List<WasteRecovery>> getPendingRecoveries() {
        return Result.success(wasteRecoveryService.getPendingRecoveries());
    }
}
