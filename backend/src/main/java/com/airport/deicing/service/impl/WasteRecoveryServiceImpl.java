package com.airport.deicing.service.impl;

import com.airport.deicing.common.PageResult;
import com.airport.deicing.common.ResultCode;
import com.airport.deicing.entity.WasteRecovery;
import com.airport.deicing.exception.BusinessException;
import com.airport.deicing.mapper.WasteRecoveryMapper;
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
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WasteRecoveryServiceImpl extends ServiceImpl<WasteRecoveryMapper, WasteRecovery> implements WasteRecoveryService {

    @Override
    public PageResult<WasteRecovery> getRecoveryPage(Long pageNum, Long pageSize, String recoveryNo, Long flightId, String recoveryStatus) {
        LambdaQueryWrapper<WasteRecovery> wrapper = new LambdaQueryWrapper<>();
        if (recoveryNo != null && !recoveryNo.isEmpty()) {
            wrapper.like(WasteRecovery::getRecoveryNo, recoveryNo);
        }
        if (flightId != null) {
            wrapper.eq(WasteRecovery::getFlightId, flightId);
        }
        if (recoveryStatus != null && !recoveryStatus.isEmpty()) {
            wrapper.eq(WasteRecovery::getRecoveryStatus, recoveryStatus);
        }
        wrapper.orderByDesc(WasteRecovery::getRecoveryTime);

        IPage<WasteRecovery> page = this.page(new Page<>(pageNum, pageSize), wrapper);
        return new PageResult<>(page);
    }

    @Override
    public WasteRecovery getRecoveryById(Long id) {
        WasteRecovery recovery = this.getById(id);
        if (recovery == null) {
            throw new BusinessException(ResultCode.WASTE_NOT_FOUND);
        }
        return recovery;
    }

    @Override
    public WasteRecovery getRecoveryByNo(String recoveryNo) {
        LambdaQueryWrapper<WasteRecovery> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WasteRecovery::getRecoveryNo, recoveryNo);
        return this.getOne(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addRecovery(WasteRecovery recovery) {
        if (recovery.getRecoveryTime() == null) {
            recovery.setRecoveryTime(LocalDateTime.now());
        }
        recovery.setCreateTime(LocalDateTime.now());
        recovery.setUpdateTime(LocalDateTime.now());
        if (recovery.getRecoveryStatus() == null) {
            recovery.setRecoveryStatus("PENDING");
        }
        return this.save(recovery);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRecovery(WasteRecovery recovery) {
        WasteRecovery exist = this.getById(recovery.getId());
        if (exist == null) {
            throw new BusinessException(ResultCode.WASTE_NOT_FOUND);
        }
        recovery.setUpdateTime(LocalDateTime.now());
        return this.updateById(recovery);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRecovery(Long id) {
        WasteRecovery exist = this.getById(id);
        if (exist == null) {
            throw new BusinessException(ResultCode.WASTE_NOT_FOUND);
        }
        return this.removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean testRecovery(Long id, BigDecimal concentration, BigDecimal phValue, String contaminationLevel) {
        WasteRecovery recovery = this.getRecoveryById(id);

        if (!"PENDING".equals(recovery.getRecoveryStatus()) && !"TESTED".equals(recovery.getRecoveryStatus())) {
            throw new BusinessException(ResultCode.WASTE_STATUS_ERROR, "回收状态异常，无法检测");
        }

        recovery.setConcentration(concentration);
        recovery.setPhValue(phValue);
        recovery.setContaminationLevel(contaminationLevel);
        recovery.setRecoveryStatus("TESTED");
        recovery.setUpdateTime(LocalDateTime.now());
        return this.updateById(recovery);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean qualifyRecovery(Long id, String disposalMethod) {
        WasteRecovery recovery = this.getRecoveryById(id);

        if (!"TESTED".equals(recovery.getRecoveryStatus())) {
            throw new BusinessException(ResultCode.WASTE_STATUS_ERROR, "回收状态异常，无法判定合格");
        }

        recovery.setDisposalMethod(disposalMethod);
        recovery.setRecoveryStatus("QUALIFIED");
        recovery.setDisposalTime(LocalDateTime.now());
        recovery.setUpdateTime(LocalDateTime.now());
        return this.updateById(recovery);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unqualifyRecovery(Long id) {
        WasteRecovery recovery = this.getRecoveryById(id);

        if (!"TESTED".equals(recovery.getRecoveryStatus())) {
            throw new BusinessException(ResultCode.WASTE_STATUS_ERROR, "回收状态异常，无法判定不合格");
        }

        recovery.setRecoveryStatus("UNQUALIFIED");
        recovery.setUpdateTime(LocalDateTime.now());
        return this.updateById(recovery);
    }

    @Override
    public List<WasteRecovery> getTodayRecoveries() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        LambdaQueryWrapper<WasteRecovery> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(WasteRecovery::getRecoveryTime, startOfDay, endOfDay)
               .orderByDesc(WasteRecovery::getRecoveryTime);
        return this.list(wrapper);
    }

    @Override
    public List<WasteRecovery> getPendingRecoveries() {
        LambdaQueryWrapper<WasteRecovery> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WasteRecovery::getRecoveryStatus, "PENDING")
               .orderByAsc(WasteRecovery::getRecoveryTime);
        return this.list(wrapper);
    }
}
