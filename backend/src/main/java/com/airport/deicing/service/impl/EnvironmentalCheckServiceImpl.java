package com.airport.deicing.service.impl;

import com.airport.deicing.common.PageResult;
import com.airport.deicing.common.ResultCode;
import com.airport.deicing.entity.EnvironmentalCheck;
import com.airport.deicing.entity.WasteRecovery;
import com.airport.deicing.exception.BusinessException;
import com.airport.deicing.mapper.EnvironmentalCheckMapper;
import com.airport.deicing.service.EnvironmentalCheckService;
import com.airport.deicing.service.WasteRecoveryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnvironmentalCheckServiceImpl extends ServiceImpl<EnvironmentalCheckMapper, EnvironmentalCheck> implements EnvironmentalCheckService {

    private final WasteRecoveryService wasteRecoveryService;

    private static final BigDecimal STANDARD_CONCENTRATION = new BigDecimal("30.0");
    private static final BigDecimal STANDARD_PH_MIN = new BigDecimal("6.0");
    private static final BigDecimal STANDARD_PH_MAX = new BigDecimal("9.0");
    private static final BigDecimal STANDARD_COD = new BigDecimal("500.0");
    private static final BigDecimal STANDARD_BOD = new BigDecimal("200.0");

    @Override
    public PageResult<EnvironmentalCheck> getCheckPage(Long pageNum, Long pageSize, String checkNo, Long recoveryId, String checkResult, String checkStatus) {
        LambdaQueryWrapper<EnvironmentalCheck> wrapper = new LambdaQueryWrapper<>();
        if (checkNo != null && !checkNo.isEmpty()) {
            wrapper.like(EnvironmentalCheck::getCheckNo, checkNo);
        }
        if (recoveryId != null) {
            wrapper.eq(EnvironmentalCheck::getRecoveryId, recoveryId);
        }
        if (checkResult != null && !checkResult.isEmpty()) {
            wrapper.eq(EnvironmentalCheck::getCheckResult, checkResult);
        }
        if (checkStatus != null && !checkStatus.isEmpty()) {
            wrapper.eq(EnvironmentalCheck::getCheckStatus, checkStatus);
        }
        wrapper.orderByDesc(EnvironmentalCheck::getCheckTime);

        IPage<EnvironmentalCheck> page = this.page(new Page<>(pageNum, pageSize), wrapper);
        return new PageResult<>(page);
    }

    @Override
    public EnvironmentalCheck getCheckById(Long id) {
        EnvironmentalCheck check = this.getById(id);
        if (check == null) {
            throw new BusinessException(ResultCode.ENV_CHECK_NOT_FOUND);
        }
        return check;
    }

    @Override
    public EnvironmentalCheck getCheckByNo(String checkNo) {
        LambdaQueryWrapper<EnvironmentalCheck> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EnvironmentalCheck::getCheckNo, checkNo);
        return this.getOne(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addCheck(EnvironmentalCheck check) {
        if (check.getCheckTime() == null) {
            check.setCheckTime(LocalDateTime.now());
        }
        check.setCreateTime(LocalDateTime.now());
        check.setUpdateTime(LocalDateTime.now());
        if (check.getCheckResult() == null) {
            check.setCheckResult("PENDING");
        }
        if (check.getCheckStatus() == null) {
            check.setCheckStatus("OPEN");
        }
        if (check.getStandardConcentration() == null) {
            check.setStandardConcentration(STANDARD_CONCENTRATION);
        }
        return this.save(check);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateCheck(EnvironmentalCheck check) {
        EnvironmentalCheck exist = this.getById(check.getId());
        if (exist == null) {
            throw new BusinessException(ResultCode.ENV_CHECK_NOT_FOUND);
        }
        check.setUpdateTime(LocalDateTime.now());
        return this.updateById(check);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteCheck(Long id) {
        EnvironmentalCheck exist = this.getById(id);
        if (exist == null) {
            throw new BusinessException(ResultCode.ENV_CHECK_NOT_FOUND);
        }
        return this.removeById(id);
    }

    @Override
    public Map<String, Object> checkEnvironmentalQualification(Long checkId) {
        EnvironmentalCheck check = this.getCheckById(checkId);
        Map<String, Object> result = new HashMap<>();
        result.put("check", check);

        boolean qualified = true;
        StringBuilder failReasons = new StringBuilder();
        Map<String, Boolean> itemResults = new HashMap<>();

        if (check.getConcentration() != null) {
            boolean concentrationOk = check.getConcentration().compareTo(STANDARD_CONCENTRATION) <= 0;
            itemResults.put("concentration", concentrationOk);
            if (!concentrationOk) {
                qualified = false;
                failReasons.append("浓度超标;");
            }
        }

        if (check.getPhValue() != null) {
            boolean phOk = check.getPhValue().compareTo(STANDARD_PH_MIN) >= 0
                && check.getPhValue().compareTo(STANDARD_PH_MAX) <= 0;
            itemResults.put("phValue", phOk);
            if (!phOk) {
                qualified = false;
                failReasons.append("pH值超标;");
            }
        }

        if (check.getCodValue() != null) {
            boolean codOk = check.getCodValue().compareTo(STANDARD_COD) <= 0;
            itemResults.put("codValue", codOk);
            if (!codOk) {
                qualified = false;
                failReasons.append("COD超标;");
            }
        }

        if (check.getBodValue() != null) {
            boolean bodOk = check.getBodValue().compareTo(STANDARD_BOD) <= 0;
            itemResults.put("bodValue", bodOk);
            if (!bodOk) {
                qualified = false;
                failReasons.append("BOD超标;");
            }
        }

        result.put("qualified", qualified);
        result.put("failReasons", failReasons.toString());
        result.put("itemResults", itemResults);
        result.put("standards", Map.of(
            "concentration", STANDARD_CONCENTRATION,
            "phMin", STANDARD_PH_MIN,
            "phMax", STANDARD_PH_MAX,
            "cod", STANDARD_COD,
            "bod", STANDARD_BOD
        ));

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean performCheck(Long checkId, BigDecimal concentration, BigDecimal phValue, BigDecimal codValue, BigDecimal bodValue) {
        EnvironmentalCheck check = this.getCheckById(checkId);

        if (!"OPEN".equals(check.getCheckStatus())) {
            throw new BusinessException(ResultCode.ENV_CHECK_STATUS_ERROR, "环保检查状态异常，无法进行检测");
        }

        check.setConcentration(concentration);
        check.setPhValue(phValue);
        check.setCodValue(codValue);
        check.setBodValue(bodValue);
        check.setCheckTime(LocalDateTime.now());
        check.setUpdateTime(LocalDateTime.now());

        boolean concentrationOk = concentration.compareTo(STANDARD_CONCENTRATION) <= 0;
        boolean phOk = phValue.compareTo(STANDARD_PH_MIN) >= 0 && phValue.compareTo(STANDARD_PH_MAX) <= 0;
        boolean codOk = codValue.compareTo(STANDARD_COD) <= 0;
        boolean bodOk = bodValue.compareTo(STANDARD_BOD) <= 0;

        boolean allQualified = concentrationOk && phOk && codOk && bodOk;
        check.setCheckResult(allQualified ? "QUALIFIED" : "UNQUALIFIED");
        check.setRecheckRequired(!allQualified);

        if (check.getRecoveryId() != null) {
            WasteRecovery recovery = wasteRecoveryService.getRecoveryById(check.getRecoveryId());
            if (allQualified) {
                wasteRecoveryService.qualifyRecovery(check.getRecoveryId(), "TREATMENT");
            } else {
                wasteRecoveryService.unqualifyRecovery(check.getRecoveryId());
            }
        }

        return this.updateById(check);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean closeCheck(Long checkId) {
        EnvironmentalCheck check = this.getCheckById(checkId);

        if (!"OPEN".equals(check.getCheckStatus())) {
            throw new BusinessException(ResultCode.ENV_CHECK_STATUS_ERROR, "环保检查状态异常，无法关闭");
        }

        if ("PENDING".equals(check.getCheckResult())) {
            throw new BusinessException(ResultCode.ENV_CHECK_UNQUALIFIED, "未进行环保检测，不能关闭");
        }

        if ("UNQUALIFIED".equals(check.getCheckResult())) {
            throw new BusinessException(ResultCode.ENV_CHECK_UNQUALIFIED, "环保检查未达标，不能关闭检查");
        }

        check.setCheckStatus("CLOSED");
        check.setUpdateTime(LocalDateTime.now());
        return this.updateById(check);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean recheck(Long checkId) {
        EnvironmentalCheck check = this.getCheckById(checkId);

        if (!"OPEN".equals(check.getCheckStatus())) {
            throw new BusinessException(ResultCode.ENV_CHECK_STATUS_ERROR, "环保检查状态异常，无法复检");
        }

        check.setRecheckRequired(false);
        check.setRecheckTime(LocalDateTime.now());
        check.setCheckResult("PENDING");
        check.setUpdateTime(LocalDateTime.now());
        return this.updateById(check);
    }

    @Override
    public List<EnvironmentalCheck> getTodayChecks() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        LambdaQueryWrapper<EnvironmentalCheck> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(EnvironmentalCheck::getCheckTime, startOfDay, endOfDay)
               .orderByDesc(EnvironmentalCheck::getCheckTime);
        return this.list(wrapper);
    }

    @Override
    public List<EnvironmentalCheck> getOpenChecks() {
        LambdaQueryWrapper<EnvironmentalCheck> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EnvironmentalCheck::getCheckStatus, "OPEN")
               .orderByAsc(EnvironmentalCheck::getCheckTime);
        return this.list(wrapper);
    }
}
