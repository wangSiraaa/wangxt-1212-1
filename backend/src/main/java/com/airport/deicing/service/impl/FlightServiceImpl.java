package com.airport.deicing.service.impl;

import com.airport.deicing.common.PageResult;
import com.airport.deicing.common.ResultCode;
import com.airport.deicing.entity.Flight;
import com.airport.deicing.exception.BusinessException;
import com.airport.deicing.mapper.FlightMapper;
import com.airport.deicing.service.FlightService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlightServiceImpl extends ServiceImpl<FlightMapper, Flight> implements FlightService {

    @Override
    public PageResult<Flight> getFlightPage(Long pageNum, Long pageSize, String flightNo, String flightStatus, String airline) {
        LambdaQueryWrapper<Flight> wrapper = new LambdaQueryWrapper<>();
        if (flightNo != null && !flightNo.isEmpty()) {
            wrapper.like(Flight::getFlightNo, flightNo);
        }
        if (flightStatus != null && !flightStatus.isEmpty()) {
            wrapper.eq(Flight::getFlightStatus, flightStatus);
        }
        if (airline != null && !airline.isEmpty()) {
            wrapper.like(Flight::getAirline, airline);
        }
        wrapper.orderByDesc(Flight::getScheduledDepartureTime);

        IPage<Flight> page = this.page(new Page<>(pageNum, pageSize), wrapper);
        return new PageResult<>(page);
    }

    @Override
    public Flight getFlightById(Long id) {
        Flight flight = this.getById(id);
        if (flight == null) {
            throw new BusinessException(ResultCode.FLIGHT_NOT_FOUND);
        }
        return flight;
    }

    @Override
    public Flight getFlightByNo(String flightNo) {
        LambdaQueryWrapper<Flight> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Flight::getFlightNo, flightNo);
        return this.getOne(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addFlight(Flight flight) {
        flight.setCreateTime(LocalDateTime.now());
        flight.setUpdateTime(LocalDateTime.now());
        if (flight.getDeicingCompleted() == null) {
            flight.setDeicingCompleted(false);
        }
        if (flight.getDeicingRequired() == null) {
            flight.setDeicingRequired(true);
        }
        if (flight.getFlightStatus() == null) {
            flight.setFlightStatus("SCHEDULED");
        }
        return this.save(flight);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateFlight(Flight flight) {
        Flight exist = this.getById(flight.getId());
        if (exist == null) {
            throw new BusinessException(ResultCode.FLIGHT_NOT_FOUND);
        }
        flight.setUpdateTime(LocalDateTime.now());
        return this.updateById(flight);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteFlight(Long id) {
        Flight exist = this.getById(id);
        if (exist == null) {
            throw new BusinessException(ResultCode.FLIGHT_NOT_FOUND);
        }
        return this.removeById(id);
    }

    @Override
    public List<Flight> getPendingDeicingFlights() {
        LambdaQueryWrapper<Flight> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Flight::getDeicingRequired, true)
               .eq(Flight::getDeicingCompleted, false)
               .ne(Flight::getFlightStatus, "DEPARTED")
               .ne(Flight::getFlightStatus, "CANCELLED")
               .orderByAsc(Flight::getScheduledDepartureTime);
        return this.list(wrapper);
    }

    @Override
    public List<Flight> getTodayFlights() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        LambdaQueryWrapper<Flight> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(Flight::getScheduledDepartureTime, startOfDay, endOfDay)
               .orderByAsc(Flight::getScheduledDepartureTime);
        return this.list(wrapper);
    }

    @Override
    public Map<String, Object> checkFlightDeicingStatus(Long flightId) {
        Flight flight = this.getFlightById(flightId);
        Map<String, Object> result = new HashMap<>();
        result.put("flight", flight);
        result.put("deicingRequired", flight.getDeicingRequired());
        result.put("deicingCompleted", flight.getDeicingCompleted());

        boolean hasRisk = false;
        String riskMessage = "";

        if (flight.getDeicingRequired() && !flight.getDeicingCompleted()) {
            hasRisk = true;
            riskMessage = "航班起飞前缺少除冰完成记录，存在安全风险！";
        }

        if ("DEPARTED".equals(flight.getFlightStatus()) && flight.getDeicingRequired() && !flight.getDeicingCompleted()) {
            hasRisk = true;
            riskMessage = "严重警告：航班已起飞但未完成除冰操作！";
        }

        result.put("hasRisk", hasRisk);
        result.put("riskMessage", riskMessage);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean completeDeicing(Long flightId) {
        Flight flight = this.getFlightById(flightId);
        if ("DEPARTED".equals(flight.getFlightStatus())) {
            throw new BusinessException(ResultCode.FLIGHT_ALREADY_DEPARTED);
        }
        flight.setDeicingCompleted(true);
        flight.setFlightStatus("DEICED");
        flight.setUpdateTime(LocalDateTime.now());
        return this.updateById(flight);
    }
}
