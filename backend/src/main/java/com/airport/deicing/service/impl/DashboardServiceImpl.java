package com.airport.deicing.service.impl;

import com.airport.deicing.entity.*;
import com.airport.deicing.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final FlightService flightService;
    private final VehicleService vehicleService;
    private final DeicingFluidBatchService batchService;
    private final SprayRecordService sprayService;
    private final WasteRecoveryService wasteRecoveryService;
    private final EnvironmentalCheckService envCheckService;
    private final DispatchRecordService dispatchService;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String DASHBOARD_KEY = "deicing:dashboard:data";
    private static final long CACHE_EXPIRE_SECONDS = 60;

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> getDashboardData() {
        Map<String, Object> cachedData = (Map<String, Object>) redisTemplate.opsForValue().get(DASHBOARD_KEY);
        if (cachedData != null) {
            return cachedData;
        }
        return buildAndCacheDashboardData();
    }

    @Override
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> dashboardData = getDashboardData();
        return (Map<String, Object>) dashboardData.get("stats");
    }

    @Override
    public Map<String, Object> getRealtimeStatus() {
        Map<String, Object> dashboardData = getDashboardData();
        Map<String, Object> result = new HashMap<>();
        result.put("stats", dashboardData.get("stats"));
        result.put("flights", dashboardData.get("todayFlights"));
        result.put("vehicles", dashboardData.get("vehicleStatus"));
        result.put("dispatches", dashboardData.get("pendingDispatches"));
        result.put("sprays", dashboardData.get("inProgressSprays"));
        result.put("envChecks", dashboardData.get("openEnvChecks"));
        result.put("updateTime", dashboardData.get("updateTime"));
        return result;
    }

    @Override
    public void refreshDashboardCache() {
        buildAndCacheDashboardData();
    }

    @Scheduled(fixedRate = 30000)
    public void scheduledRefresh() {
        try {
            buildAndCacheDashboardData();
            log.debug("Dashboard cache refreshed at {}", LocalDateTime.now());
        } catch (Exception e) {
            log.error("Failed to refresh dashboard cache", e);
        }
    }

    private Map<String, Object> buildAndCacheDashboardData() {
        Map<String, Object> data = new HashMap<>();

        Map<String, Object> stats = buildStats();
        data.put("stats", stats);

        List<Flight> todayFlights = flightService.getTodayFlights();
        data.put("todayFlights", todayFlights);

        List<Flight> pendingFlights = flightService.getPendingDeicingFlights();
        data.put("pendingDeicingFlights", pendingFlights);

        List<Vehicle> allVehicles = vehicleService.list();
        Map<String, Object> vehicleStatus = buildVehicleStatus(allVehicles);
        data.put("vehicleStatus", vehicleStatus);

        List<DeicingFluidBatch> availableBatches = batchService.getAvailableBatches();
        data.put("availableBatches", availableBatches);

        List<DispatchRecord> pendingDispatches = dispatchService.getPendingDispatches();
        data.put("pendingDispatches", pendingDispatches);

        List<SprayRecord> inProgressSprays = sprayService.getInProgressSprays();
        data.put("inProgressSprays", inProgressSprays);

        List<SprayRecord> todaySprays = sprayService.getTodaySprays();
        data.put("todaySprays", todaySprays);

        List<WasteRecovery> todayRecoveries = wasteRecoveryService.getTodayRecoveries();
        data.put("todayRecoveries", todayRecoveries);

        List<EnvironmentalCheck> openEnvChecks = envCheckService.getOpenChecks();
        data.put("openEnvChecks", openEnvChecks);

        data.put("updateTime", LocalDateTime.now());

        redisTemplate.opsForValue().set(DASHBOARD_KEY, data, CACHE_EXPIRE_SECONDS, TimeUnit.SECONDS);

        return data;
    }

    private Map<String, Object> buildStats() {
        Map<String, Object> stats = new HashMap<>();

        List<Flight> todayFlights = flightService.getTodayFlights();
        long totalFlights = todayFlights.size();
        long pendingDeicing = todayFlights.stream()
            .filter(f -> Boolean.TRUE.equals(f.getDeicingRequired()) && Boolean.FALSE.equals(f.getDeicingCompleted()))
            .filter(f -> !"DEPARTED".equals(f.getFlightStatus()) && !"CANCELLED".equals(f.getFlightStatus()))
            .count();
        long deicingInProgress = todayFlights.stream()
            .filter(f -> "DEICING".equals(f.getFlightStatus()))
            .count();
        long completedDeicing = todayFlights.stream()
            .filter(f -> Boolean.TRUE.equals(f.getDeicingCompleted()))
            .count();
        long departedFlights = todayFlights.stream()
            .filter(f -> "DEPARTED".equals(f.getFlightStatus()))
            .count();

        stats.put("totalFlights", totalFlights);
        stats.put("pendingDeicing", pendingDeicing);
        stats.put("deicingInProgress", deicingInProgress);
        stats.put("completedDeicing", completedDeicing);
        stats.put("departedFlights", departedFlights);

        List<Vehicle> vehicles = vehicleService.list();
        long availableVehicles = vehicles.stream()
            .filter(v -> "IDLE".equals(v.getVehicleStatus()))
            .count();
        long busyVehicles = vehicles.stream()
            .filter(v -> "SPRAYING".equals(v.getVehicleStatus()) || "DISPATCHED".equals(v.getVehicleStatus()))
            .count();
        long maintenanceVehicles = vehicles.stream()
            .filter(v -> "MAINTENANCE".equals(v.getVehicleStatus()))
            .count();

        stats.put("totalVehicles", vehicles.size());
        stats.put("availableVehicles", availableVehicles);
        stats.put("busyVehicles", busyVehicles);
        stats.put("maintenanceVehicles", maintenanceVehicles);

        List<DeicingFluidBatch> batches = batchService.list();
        long availableBatches = batches.stream()
            .filter(b -> "AVAILABLE".equals(b.getBatchStatus()) || "IN_USE".equals(b.getBatchStatus()))
            .filter(b -> b.getRemainingVolume() != null && b.getRemainingVolume().compareTo(BigDecimal.ZERO) > 0)
            .count();
        BigDecimal totalRemainingVolume = batches.stream()
            .filter(b -> b.getRemainingVolume() != null)
            .map(DeicingFluidBatch::getRemainingVolume)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        stats.put("totalBatches", batches.size());
        stats.put("availableBatches", availableBatches);
        stats.put("totalRemainingVolume", totalRemainingVolume);

        List<SprayRecord> todaySprays = sprayService.getTodaySprays();
        BigDecimal totalSprayVolume = todaySprays.stream()
            .filter(s -> s.getSprayVolume() != null)
            .map(SprayRecord::getSprayVolume)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        long completedSprays = todaySprays.stream()
            .filter(s -> "COMPLETED".equals(s.getSprayStatus()))
            .count();
        long inProgressSprays = todaySprays.stream()
            .filter(s -> "IN_PROGRESS".equals(s.getSprayStatus()))
            .count();

        stats.put("todaySprayCount", todaySprays.size());
        stats.put("completedSprays", completedSprays);
        stats.put("inProgressSprays", inProgressSprays);
        stats.put("totalSprayVolume", totalSprayVolume);

        List<WasteRecovery> todayRecoveries = wasteRecoveryService.getTodayRecoveries();
        BigDecimal totalRecoveryVolume = todayRecoveries.stream()
            .filter(r -> r.getRecoveryVolume() != null)
            .map(WasteRecovery::getRecoveryVolume)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        stats.put("todayRecoveryCount", todayRecoveries.size());
        stats.put("totalRecoveryVolume", totalRecoveryVolume);

        List<EnvironmentalCheck> todayChecks = envCheckService.getTodayChecks();
        long qualifiedChecks = todayChecks.stream()
            .filter(c -> "QUALIFIED".equals(c.getCheckResult()))
            .count();
        long unqualifiedChecks = todayChecks.stream()
            .filter(c -> "UNQUALIFIED".equals(c.getCheckResult()))
            .count();
        long openChecks = todayChecks.stream()
            .filter(c -> "OPEN".equals(c.getCheckStatus()))
            .count();

        stats.put("todayCheckCount", todayChecks.size());
        stats.put("qualifiedChecks", qualifiedChecks);
        stats.put("unqualifiedChecks", unqualifiedChecks);
        stats.put("openChecks", openChecks);

        long riskFlights = todayFlights.stream()
            .filter(f -> Boolean.TRUE.equals(f.getHasRiskRemark())
                || (Boolean.TRUE.equals(f.getDeicingRequired())
                    && Boolean.FALSE.equals(f.getDeicingCompleted())
                    && "DEPARTED".equals(f.getFlightStatus())))
            .count();
        stats.put("riskFlights", riskFlights);

        return stats;
    }

    private Map<String, Object> buildVehicleStatus(List<Vehicle> vehicles) {
        Map<String, Object> result = new HashMap<>();
        result.put("total", vehicles.size());
        result.put("list", vehicles);

        long idle = vehicles.stream().filter(v -> "IDLE".equals(v.getVehicleStatus())).count();
        long dispatched = vehicles.stream().filter(v -> "DISPATCHED".equals(v.getVehicleStatus())).count();
        long spraying = vehicles.stream().filter(v -> "SPRAYING".equals(v.getVehicleStatus())).count();
        long maintenance = vehicles.stream().filter(v -> "MAINTENANCE".equals(v.getVehicleStatus())).count();

        result.put("idle", idle);
        result.put("dispatched", dispatched);
        result.put("spraying", spraying);
        result.put("maintenance", maintenance);

        return result;
    }
}
