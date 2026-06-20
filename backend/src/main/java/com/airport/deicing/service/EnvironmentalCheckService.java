package com.airport.deicing.service;

import com.airport.deicing.entity.EnvironmentalCheck;
import com.airport.deicing.common.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface EnvironmentalCheckService extends IService<EnvironmentalCheck> {

    PageResult<EnvironmentalCheck> getCheckPage(Long pageNum, Long pageSize, String checkNo, Long recoveryId, String checkResult, String checkStatus);

    EnvironmentalCheck getCheckById(Long id);

    EnvironmentalCheck getCheckByNo(String checkNo);

    boolean addCheck(EnvironmentalCheck check);

    boolean updateCheck(EnvironmentalCheck check);

    boolean deleteCheck(Long id);

    Map<String, Object> checkEnvironmentalQualification(Long checkId);

    boolean performCheck(Long checkId, BigDecimal concentration, BigDecimal phValue, BigDecimal codValue, BigDecimal bodValue);

    boolean closeCheck(Long checkId);

    boolean recheck(Long checkId);

    List<EnvironmentalCheck> getTodayChecks();

    List<EnvironmentalCheck> getOpenChecks();
}
