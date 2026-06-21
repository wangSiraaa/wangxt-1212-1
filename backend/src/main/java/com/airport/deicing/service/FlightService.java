package com.airport.deicing.service;

import com.airport.deicing.entity.Flight;
import com.airport.deicing.entity.RiskRemarkHistory;
import com.airport.deicing.common.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

public interface FlightService extends IService<Flight> {

    PageResult<Flight> getFlightPage(Long pageNum, Long pageSize, String flightNo, String flightStatus, String airline);

    Flight getFlightById(Long id);

    Flight getFlightByNo(String flightNo);

    boolean addFlight(Flight flight);

    boolean updateFlight(Flight flight);

    boolean deleteFlight(Long id);

    List<Flight> getPendingDeicingFlights();

    List<Flight> getTodayFlights();

    Map<String, Object> checkFlightDeicingStatus(Long flightId);

    boolean completeDeicing(Long flightId);

    boolean markRiskRemark(Long flightId, String riskReason, String operator);

    boolean clearRiskRemark(Long flightId, String operator, String clearType);

    List<RiskRemarkHistory> getRiskRemarkHistory(Long flightId);
}
