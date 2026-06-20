package com.airport.deicing.controller;

import com.airport.deicing.common.PageResult;
import com.airport.deicing.common.Result;
import com.airport.deicing.entity.DispatchRecord;
import com.airport.deicing.service.DispatchRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "调度记录管理", description = "除冰车调度管理接口")
@RestController
@RequestMapping("/dispatches")
@RequiredArgsConstructor
public class DispatchRecordController {

    private final DispatchRecordService dispatchService;

    @Operation(summary = "分页查询调度记录列表")
    @GetMapping("/page")
    public Result<PageResult<DispatchRecord>> getDispatchPage(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize,
            @RequestParam(required = false) String dispatchNo,
            @RequestParam(required = false) Long flightId,
            @RequestParam(required = false) Long vehicleId,
            @RequestParam(required = false) String dispatchStatus) {
        return Result.success(dispatchService.getDispatchPage(pageNum, pageSize, dispatchNo, flightId, vehicleId, dispatchStatus));
    }

    @Operation(summary = "根据ID获取调度记录详情")
    @GetMapping("/{id}")
    public Result<DispatchRecord> getDispatchById(@PathVariable Long id) {
        return Result.success(dispatchService.getDispatchById(id));
    }

    @Operation(summary = "根据调度单号获取调度记录")
    @GetMapping("/no/{dispatchNo}")
    public Result<DispatchRecord> getDispatchByNo(@PathVariable String dispatchNo) {
        return Result.success(dispatchService.getDispatchByNo(dispatchNo));
    }

    @Operation(summary = "新增调度记录")
    @PostMapping
    public Result<Void> addDispatch(@RequestBody DispatchRecord dispatch) {
        dispatchService.addDispatch(dispatch);
        return Result.success();
    }

    @Operation(summary = "更新调度记录")
    @PutMapping
    public Result<Void> updateDispatch(@RequestBody DispatchRecord dispatch) {
        dispatchService.updateDispatch(dispatch);
        return Result.success();
    }

    @Operation(summary = "删除调度记录")
    @DeleteMapping("/{id}")
    public Result<Void> deleteDispatch(@PathVariable Long id) {
        dispatchService.deleteDispatch(id);
        return Result.success();
    }

    @Operation(summary = "开始执行调度")
    @PostMapping("/{id}/start")
    public Result<Void> startDispatch(@PathVariable Long id) {
        dispatchService.startDispatch(id);
        return Result.success();
    }

    @Operation(summary = "完成调度")
    @PostMapping("/{id}/complete")
    public Result<Void> completeDispatch(@PathVariable Long id) {
        dispatchService.completeDispatch(id);
        return Result.success();
    }

    @Operation(summary = "取消调度")
    @PostMapping("/{id}/cancel")
    public Result<Void> cancelDispatch(@PathVariable Long id) {
        dispatchService.cancelDispatch(id);
        return Result.success();
    }

    @Operation(summary = "获取今日调度列表")
    @GetMapping("/today")
    public Result<List<DispatchRecord>> getTodayDispatches() {
        return Result.success(dispatchService.getTodayDispatches());
    }

    @Operation(summary = "获取待执行调度列表")
    @GetMapping("/pending")
    public Result<List<DispatchRecord>> getPendingDispatches() {
        return Result.success(dispatchService.getPendingDispatches());
    }
}
