package com.airport.deicing.service;

import java.util.Map;

public interface DashboardService {

    Map<String, Object> getDashboardData();

    Map<String, Object> getDashboardStats();

    Map<String, Object> getRealtimeStatus();

    void refreshDashboardCache();
}
