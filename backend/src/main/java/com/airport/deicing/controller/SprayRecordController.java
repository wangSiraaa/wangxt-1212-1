package com.airport.deicing.controller;

import com.airport.deicing.common.PageResult;
import com.airport.deicing.common.Result;
import com.airport.deicing.entity.SprayRecord;
import com.airport.deicing.service.SprayRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Tag(name = "喷洒记录管理", description = "除冰液喷洒记录管理接口")
@RestController
@RequestMapping("/sprays")
@RequiredArgsConstructor
public class SprayRecordController {

    private final SprayRecordService sprayService;

    @Operation(summary = "分页查询喷洒记录列表")
    @GetMapping("/page")
    public Result<PageResult<SprayRecord>> getSprayPage(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize,
            @RequestParam(required = false) String sprayNo,
            @RequestParam(required = false) Long flightId,
            @RequestParam(required = false) Long vehicleId,
            @RequestParam(required = false) String sprayStatus) {
        return Result.success(sprayService.getSprayPage(pageNum, pageSize, sprayNo, flightId, vehicleId, sprayStatus));
    }

    @Operation(summary = "根据ID获取喷洒记录详情")
    @GetMapping("/{id}")
    public Result<SprayRecord> getSprayById(@PathVariable Long id) {
        return Result.success(sprayService.getSprayById(id));
    }

    @Operation(summary = "根据喷洒记录单号获取记录")
    @GetMapping("/no/{sprayNo}")
    public Result<SprayRecord> getSprayByNo(@PathVariable String sprayNo) {
        return Result.success(sprayService.getSprayByNo(sprayNo));
    }

    @Operation(summary = "新增喷洒记录")
    @PostMapping
    public Result<Void> addSpray(@RequestBody SprayRecord spray) {
        sprayService.addSpray(spray);
        return Result.success();
    }

    @Operation(summary = "更新喷洒记录")
    @PutMapping
    public Result<Void> updateSpray(@RequestBody SprayRecord spray) {
        sprayService.updateSpray(spray);
        return Result.success();
    }

    @Operation(summary = "删除喷洒记录")
    @DeleteMapping("/{id}")
    public Result<Void> deleteSpray(@PathVariable Long id) {
        sprayService.deleteSpray(id);
        return Result.success();
    }

    @Operation(summary = "开始喷洒")
    @PostMapping("/{id}/start")
    public Result<Void> startSpray(@PathVariable Long id) {
        sprayService.startSpray(id);
        return Result.success();
    }

    @Operation(summary = "完成喷洒")
    @PostMapping("/{id}/complete")
    public Result<Void> completeSpray(
            @PathVariable Long id,
            @RequestParam(required = false) BigDecimal sprayVolume,
            @RequestParam(required = false, defaultValue = "GOOD") String deicingEffect) {
        sprayService.completeSpray(id, sprayVolume, deicingEffect);
        return Result.success();
    }

    @Operation(summary = "取消喷洒")
    @PostMapping("/{id}/cancel")
    public Result<Void> cancelSpray(@PathVariable Long id) {
        sprayService.cancelSpray(id);
        return Result.success();
    }

    @Operation(summary = "获取今日喷洒记录")
    @GetMapping("/today")
    public Result<List<SprayRecord>> getTodaySprays() {
        return Result.success(sprayService.getTodaySprays());
    }

    @Operation(summary = "获取进行中的喷洒记录")
    @GetMapping("/in-progress")
    public Result<List<SprayRecord>> getInProgressSprays() {
        return Result.success(sprayService.getInProgressSprays());
    }
}
