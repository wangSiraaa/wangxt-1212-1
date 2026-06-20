package com.airport.deicing.controller;

import com.airport.deicing.common.PageResult;
import com.airport.deicing.common.Result;
import com.airport.deicing.entity.Flight;
import com.airport.deicing.service.FlightService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "航班管理", description = "航班信息管理接口")
@RestController
@RequestMapping("/flights")
@RequiredArgsConstructor
public class FlightController {

    private final FlightService flightService;

    @Operation(summary = "分页查询航班列表")
    @GetMapping("/page")
    public Result<PageResult<Flight>> getFlightPage(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize,
            @RequestParam(required = false) String flightNo,
            @RequestParam(required = false) String flightStatus,
            @RequestParam(required = false) String airline) {
        return Result.success(flightService.getFlightPage(pageNum, pageSize, flightNo, flightStatus, airline));
    }

    @Operation(summary = "根据ID获取航班详情")
    @GetMapping("/{id}")
    public Result<Flight> getFlightById(@PathVariable Long id) {
        return Result.success(flightService.getFlightById(id));
    }

    @Operation(summary = "根据航班号获取航班")
    @GetMapping("/no/{flightNo}")
    public Result<Flight> getFlightByNo(@PathVariable String flightNo) {
        return Result.success(flightService.getFlightByNo(flightNo));
    }

    @Operation(summary = "新增航班")
    @PostMapping
    public Result<Void> addFlight(@RequestBody Flight flight) {
        flightService.addFlight(flight);
        return Result.success();
    }

    @Operation(summary = "更新航班")
    @PutMapping
    public Result<Void> updateFlight(@RequestBody Flight flight) {
        flightService.updateFlight(flight);
        return Result.success();
    }

    @Operation(summary = "删除航班")
    @DeleteMapping("/{id}")
    public Result<Void> deleteFlight(@PathVariable Long id) {
        flightService.deleteFlight(id);
        return Result.success();
    }

    @Operation(summary = "获取待除冰航班列表")
    @GetMapping("/pending-deicing")
    public Result<List<Flight>> getPendingDeicingFlights() {
        return Result.success(flightService.getPendingDeicingFlights());
    }

    @Operation(summary = "获取今日航班列表")
    @GetMapping("/today")
    public Result<List<Flight>> getTodayFlights() {
        return Result.success(flightService.getTodayFlights());
    }

    @Operation(summary = "检查航班除冰状态")
    @GetMapping("/{id}/deicing-status")
    public Result<Map<String, Object>> checkFlightDeicingStatus(@PathVariable Long id) {
        return Result.success(flightService.checkFlightDeicingStatus(id));
    }

    @Operation(summary = "标记航班除冰完成")
    @PostMapping("/{id}/complete-deicing")
    public Result<Void> completeDeicing(@PathVariable Long id) {
        flightService.completeDeicing(id);
        return Result.success();
    }
}
