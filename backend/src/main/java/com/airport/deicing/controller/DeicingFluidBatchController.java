package com.airport.deicing.controller;

import com.airport.deicing.common.PageResult;
import com.airport.deicing.common.Result;
import com.airport.deicing.entity.DeicingFluidBatch;
import com.airport.deicing.service.DeicingFluidBatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Tag(name = "除冰液批次管理", description = "除冰液批次管理接口")
@RestController
@RequestMapping("/batches")
@RequiredArgsConstructor
public class DeicingFluidBatchController {

    private final DeicingFluidBatchService batchService;

    @Operation(summary = "分页查询批次列表")
    @GetMapping("/page")
    public Result<PageResult<DeicingFluidBatch>> getBatchPage(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize,
            @RequestParam(required = false) String batchNo,
            @RequestParam(required = false) String fluidType,
            @RequestParam(required = false) String batchStatus) {
        return Result.success(batchService.getBatchPage(pageNum, pageSize, batchNo, fluidType, batchStatus));
    }

    @Operation(summary = "根据ID获取批次详情")
    @GetMapping("/{id}")
    public Result<DeicingFluidBatch> getBatchById(@PathVariable Long id) {
        return Result.success(batchService.getBatchById(id));
    }

    @Operation(summary = "根据批次编号获取批次")
    @GetMapping("/no/{batchNo}")
    public Result<DeicingFluidBatch> getBatchByNo(@PathVariable String batchNo) {
        return Result.success(batchService.getBatchByNo(batchNo));
    }

    @Operation(summary = "新增批次")
    @PostMapping
    public Result<Void> addBatch(@RequestBody DeicingFluidBatch batch) {
        batchService.addBatch(batch);
        return Result.success();
    }

    @Operation(summary = "更新批次")
    @PutMapping
    public Result<Void> updateBatch(@RequestBody DeicingFluidBatch batch) {
        batchService.updateBatch(batch);
        return Result.success();
    }

    @Operation(summary = "删除批次")
    @DeleteMapping("/{id}")
    public Result<Void> deleteBatch(@PathVariable Long id) {
        batchService.deleteBatch(id);
        return Result.success();
    }

    @Operation(summary = "获取可用批次列表")
    @GetMapping("/available")
    public Result<List<DeicingFluidBatch>> getAvailableBatches() {
        return Result.success(batchService.getAvailableBatches());
    }

    @Operation(summary = "按类型获取可用批次列表")
    @GetMapping("/available/type/{fluidType}")
    public Result<List<DeicingFluidBatch>> getAvailableBatchesByType(@PathVariable String fluidType) {
        return Result.success(batchService.getAvailableBatchesByType(fluidType));
    }

    @Operation(summary = "检查批次可用性（含温度校验）")
    @GetMapping("/{id}/availability")
    public Result<Map<String, Object>> checkBatchAvailability(
            @PathVariable Long id,
            @RequestParam(required = false) BigDecimal ambientTemperature) {
        return Result.success(batchService.checkBatchAvailability(id, ambientTemperature));
    }

    @Operation(summary = "验证温度区间")
    @PostMapping("/{id}/validate-temperature")
    public Result<Boolean> validateTemperatureRange(
            @PathVariable Long id,
            @RequestParam BigDecimal ambientTemperature) {
        return Result.success(batchService.validateTemperatureRange(id, ambientTemperature));
    }

    @Operation(summary = "消耗批次液体量")
    @PostMapping("/{id}/consume")
    public Result<Void> consumeBatchVolume(
            @PathVariable Long id,
            @RequestParam BigDecimal volume) {
        batchService.consumeBatchVolume(id, volume);
        return Result.success();
    }

    @Operation(summary = "退回批次液体量")
    @PostMapping("/{id}/return")
    public Result<Void> returnBatchVolume(
            @PathVariable Long id,
            @RequestParam BigDecimal volume) {
        batchService.returnBatchVolume(id, volume);
        return Result.success();
    }
}
