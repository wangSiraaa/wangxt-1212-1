package com.airport.deicing.service;

import com.airport.deicing.entity.DispatchRecord;
import com.airport.deicing.common.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface DispatchRecordService extends IService<DispatchRecord> {

    PageResult<DispatchRecord> getDispatchPage(Long pageNum, Long pageSize, String dispatchNo, Long flightId, Long vehicleId, String dispatchStatus);

    DispatchRecord getDispatchById(Long id);

    DispatchRecord getDispatchByNo(String dispatchNo);

    boolean addDispatch(DispatchRecord dispatch);

    boolean updateDispatch(DispatchRecord dispatch);

    boolean deleteDispatch(Long id);

    boolean startDispatch(Long id);

    boolean completeDispatch(Long id);

    boolean cancelDispatch(Long id);

    List<DispatchRecord> getTodayDispatches();

    List<DispatchRecord> getPendingDispatches();
}
