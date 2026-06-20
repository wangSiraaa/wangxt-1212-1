package com.airport.deicing.controller;

import com.airport.deicing.common.PageResult;
import com.airport.deicing.common.Result;
import com.airport.deicing.entity.EnvironmentalCheck;
import com.airport.deicing.service.EnvironmentalCheckService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Tag(name = "环保检查管理", description = "环保检查管理接口")
@RestController
@RequestMapping("/env-checks")
@RequiredArgsConstructor
public class EnvironmentalCheckController {

    private final EnvironmentalCheckService envCheckService;

    @Operation(summary = "分页查询环保检查列表")
    @GetMapping("/page")
    public Result<PageResult<EnvironmentalCheck>> getCheckPage(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize,
            @RequestParam(required = false) String checkNo,
            @RequestParam(required = false) Long recoveryId,
            @RequestParam(required = false) String checkResult,
            @RequestParam(required = false) String checkStatus) {
        return Result.success(envCheckService.getCheckPage(pageNum, pageSize, checkNo, recoveryId, checkResult, checkStatus));
    }

    @Operation(summary = "根据ID获取环保检查详情")
    @GetMapping("/{id}")
    public Result<EnvironmentalCheck> getCheckById(@PathVariable Long id) {
        return Result.success(envCheckService.getCheckById(id));
    }

    @Operation(summary = "根据检查单号获取记录")
    @GetMapping("/no/{checkNo}")
    public Result<EnvironmentalCheck> getCheckByNo(@PathVariable String checkNo) {
        return Result.success(envCheckService.getCheckByNo(checkNo));
    }

    @Operation(summary = "新增环保检查")
    @PostMapping
    public Result<Void> addCheck(@RequestBody EnvironmentalCheck check) {
        envCheckService.addCheck(check);
        return Result.success();
    }

    @Operation(summary = "更新环保检查")
    @PutMapping
    public Result<Void> updateCheck(@RequestBody EnvironmentalCheck check) {
        envCheckService.updateCheck(check);
        return Result.success();
    }

    @Operation(summary = "删除环保检查")
    @DeleteMapping("/{id}")
    public Result<Void> deleteCheck(@PathVariable Long id) {
        envCheckService.deleteCheck(id);
        return Result.success();
    }

    @Operation(summary = "检查环保达标情况")
    @GetMapping("/{id}/qualification")
    public Result<Map<String, Object>> checkEnvironmentalQualification(@PathVariable Long id) {
        return Result.success(envCheckService.checkEnvironmentalQualification(id));
    }

    @Operation(summary = "执行环保检测")
    @PostMapping("/{id}/perform")
    public Result<Void> performCheck(
            @PathVariable Long id,
            @RequestParam BigDecimal concentration,
            @RequestParam BigDecimal phValue,
            @RequestParam BigDecimal codValue,
            @RequestParam BigDecimal bodValue) {
        envCheckService.performCheck(id, concentration, phValue, codValue, bodValue);
        return Result.success();
    }

    @Operation(summary = "关闭环保检查")
    @PostMapping("/{id}/close")
    public Result<Void> closeCheck(@PathVariable Long id) {
        envCheckService.closeCheck(id);
        return Result.success();
    }

    @Operation(summary = "复检")
    @PostMapping("/{id}/recheck")
    public Result<Void> recheck(@PathVariable Long id) {
        envCheckService.recheck(id);
        return Result.success();
    }

    @Operation(summary = "获取今日环保检查")
    @GetMapping("/today")
    public Result<List<EnvironmentalCheck>> getTodayChecks() {
        return Result.success(envCheckService.getTodayChecks());
    }

    @Operation(summary = "获取进行中的环保检查")
    @GetMapping("/open")
    public Result<List<EnvironmentalCheck>> getOpenChecks() {
        return Result.success(envCheckService.getOpenChecks());
    }
}
