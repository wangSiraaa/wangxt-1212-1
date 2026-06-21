package com.airport.deicing;

import com.airport.deicing.entity.DeicingFluidBatch;
import com.airport.deicing.entity.EnvironmentalCheck;
import com.airport.deicing.entity.Flight;
import com.airport.deicing.exception.BusinessException;
import com.airport.deicing.service.DeicingFluidBatchService;
import com.airport.deicing.service.EnvironmentalCheckService;
import com.airport.deicing.service.FlightService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DisplayName("三条核心业务规则测试")
class BusinessRulesTest {

    @Autowired
    private FlightService flightService;

    @Autowired
    private DeicingFluidBatchService batchService;

    @Autowired
    private EnvironmentalCheckService envCheckService;

    private Flight testFlight;
    private DeicingFluidBatch testBatch;
    private EnvironmentalCheck testEnvCheck;

    @BeforeEach
    void setUp() {
        testFlight = new Flight();
        testFlight.setFlightNo("TEST-FLIGHT-001");
        testFlight.setAirline("测试航空");
        testFlight.setScheduledDepartureTime(LocalDateTime.now().plusHours(2));
        testFlight.setStandNo("A12");
        testFlight.setDeicingRequired(true);
        testFlight.setDeicingCompleted(false);
        testFlight.setFlightStatus("SCHEDULED");

        testBatch = new DeicingFluidBatch();
        testBatch.setBatchNo("TEST-BATCH-001");
        testBatch.setFluidType("TYPE1");
        testBatch.setTotalVolume(new BigDecimal("5000.00"));
        testBatch.setUsedVolume(BigDecimal.ZERO);
        testBatch.setRemainingVolume(new BigDecimal("5000.00"));
        testBatch.setMinValidTemperature(new BigDecimal("-20"));
        testBatch.setMaxValidTemperature(new BigDecimal("5"));
        testBatch.setBatchStatus("AVAILABLE");

        testEnvCheck = new EnvironmentalCheck();
        testEnvCheck.setCheckNo("TEST-ENV-001");
        testEnvCheck.setPoolNo("回收池A");
        testEnvCheck.setCheckStatus("OPEN");
        testEnvCheck.setCheckResult("PENDING");
    }

    @Test
    @DisplayName("规则1：航班起飞前缺少除冰完成记录要提示风险")
    void testFlightDeicingRiskCheck() {
        flightService.addFlight(testFlight);
        assertNotNull(testFlight.getId());

        Map<String, Object> status = flightService.checkFlightDeicingStatus(testFlight.getId());
        assertNotNull(status);
        assertTrue((Boolean) status.get("hasRisk"), "需要除冰但未完成的航班应该提示风险");
        assertTrue(status.get("riskMessage").toString().contains("缺少除冰完成记录"));

        testFlight.setFlightStatus("DEPARTED");
        flightService.updateFlight(testFlight);

        Map<String, Object> departedStatus = flightService.checkFlightDeicingStatus(testFlight.getId());
        assertTrue((Boolean) departedStatus.get("hasRisk"), "已起飞但未除冰的航班应该有严重风险");
        assertTrue(departedStatus.get("riskMessage").toString().contains("严重警告"));

        flightService.deleteFlight(testFlight.getId());
    }

    @Test
    @DisplayName("规则1：完成除冰后不应该有风险")
    void testFlightCompletedDeicingNoRisk() {
        testFlight.setScheduledDepartureTime(LocalDateTime.now().plusHours(2));
        flightService.addFlight(testFlight);
        assertNotNull(testFlight.getId());

        flightService.completeDeicing(testFlight.getId());

        Map<String, Object> status = flightService.checkFlightDeicingStatus(testFlight.getId());
        assertNotNull(status);
        assertFalse((Boolean) status.get("hasRisk"), "已完成除冰的航班不应有风险");
        assertTrue(((Flight) status.get("flight")).getDeicingCompleted());
        assertEquals("DEICED", ((Flight) status.get("flight")).getFlightStatus());

        flightService.deleteFlight(testFlight.getId());
    }

    @Test
    @DisplayName("规则1：不需要除冰的航班不应有风险")
    void testFlightNoDeicingRequiredNoRisk() {
        testFlight.setDeicingRequired(false);
        testFlight.setDeicingCompleted(false);
        testFlight.setFlightStatus("SCHEDULED");
        flightService.addFlight(testFlight);
        assertNotNull(testFlight.getId());

        Map<String, Object> status = flightService.checkFlightDeicingStatus(testFlight.getId());
        assertNotNull(status);
        assertFalse((Boolean) status.get("hasRisk"), "不需要除冰的航班不应有风险");
        assertFalse((Boolean) status.get("deicingRequired"));

        flightService.deleteFlight(testFlight.getId());
    }

    @Test
    @DisplayName("规则1：已起飞但未完成除冰标记为风险航班")
    void testDepartedWithoutDeicingMarkedAsRisk() {
        testFlight.setScheduledDepartureTime(LocalDateTime.now().minusHours(1));
        testFlight.setFlightStatus("DEPARTED");
        testFlight.setDeicingRequired(true);
        testFlight.setDeicingCompleted(false);
        flightService.addFlight(testFlight);
        assertNotNull(testFlight.getId());

        Map<String, Object> status = flightService.checkFlightDeicingStatus(testFlight.getId());
        assertTrue((Boolean) status.get("hasRisk"));
        assertTrue(status.get("riskMessage").toString().contains("已起飞但未完成除冰"));

        long riskCount = flightService.getTodayFlights().stream()
            .filter(f -> Boolean.TRUE.equals(f.getDeicingRequired()) 
                && Boolean.FALSE.equals(f.getDeicingCompleted())
                && "DEPARTED".equals(f.getFlightStatus()))
            .count();
        assertTrue(riskCount > 0, "看板统计中应该能识别到风险航班");

        flightService.deleteFlight(testFlight.getId());
    }

    @Test
    @DisplayName("规则2：环境温度在有效区间内可以领用")
    void testTemperatureWithinRangeAllowed() {
        batchService.addBatch(testBatch);
        assertNotNull(testBatch.getId());

        Map<String, Object> result = batchService.checkBatchAvailability(
            testBatch.getId(), new BigDecimal("-10"));
        assertTrue((Boolean) result.get("available"), "-10℃在-20℃~5℃区间内，应该可用");
        assertTrue((Boolean) result.get("temperatureValid"), "温度应该有效");

        boolean validateResult = batchService.validateTemperatureRange(
            testBatch.getId(), new BigDecimal("0"));
        assertTrue(validateResult, "0℃应该通过校验");

        batchService.deleteBatch(testBatch.getId());
    }

    @Test
    @DisplayName("规则2：环境温度低于最小值禁止领用")
    void testTemperatureBelowMinimumRejected() {
        batchService.addBatch(testBatch);
        assertNotNull(testBatch.getId());

        Map<String, Object> result = batchService.checkBatchAvailability(
            testBatch.getId(), new BigDecimal("-25"));
        assertFalse((Boolean) result.get("available"), "-25℃低于-20℃最小值，应该不可用");
        assertFalse((Boolean) result.get("temperatureValid"), "温度应该无效");
        assertTrue(result.get("message").toString().contains("超出除冰液有效温度区间"));

        BusinessException exception = assertThrows(BusinessException.class, () ->
            batchService.validateTemperatureRange(testBatch.getId(), new BigDecimal("-30"))
        );
        assertTrue(exception.getMessage().contains("超出除冰液有效温度区间"));
        assertEquals(3003, exception.getCode(), "错误码应该是3003");

        batchService.deleteBatch(testBatch.getId());
    }

    @Test
    @DisplayName("规则2：环境温度高于最大值禁止领用")
    void testTemperatureAboveMaximumRejected() {
        batchService.addBatch(testBatch);
        assertNotNull(testBatch.getId());

        Map<String, Object> result = batchService.checkBatchAvailability(
            testBatch.getId(), new BigDecimal("10"));
        assertFalse((Boolean) result.get("available"), "10℃高于5℃最大值，应该不可用");
        assertFalse((Boolean) result.get("temperatureValid"), "温度应该无效");

        BusinessException exception = assertThrows(BusinessException.class, () ->
            batchService.validateTemperatureRange(testBatch.getId(), new BigDecimal("15"))
        );
        assertTrue(exception.getMessage().contains("超出除冰液有效温度区间"));

        batchService.deleteBatch(testBatch.getId());
    }

    @Test
    @DisplayName("规则2：边界温度值应该允许")
    void testBoundaryTemperatureAllowed() {
        batchService.addBatch(testBatch);
        assertNotNull(testBatch.getId());

        boolean minResult = batchService.validateTemperatureRange(
            testBatch.getId(), new BigDecimal("-20"));
        assertTrue(minResult, "最小值-20℃应该允许");

        boolean maxResult = batchService.validateTemperatureRange(
            testBatch.getId(), new BigDecimal("5"));
        assertTrue(maxResult, "最大值5℃应该允许");

        batchService.deleteBatch(testBatch.getId());
    }

    @Test
    @DisplayName("规则2：空温度值跳过校验")
    void testNullTemperatureSkipValidation() {
        batchService.addBatch(testBatch);
        assertNotNull(testBatch.getId());

        boolean result = batchService.validateTemperatureRange(testBatch.getId(), null);
        assertTrue(result, "空温度值应该跳过校验");

        batchService.deleteBatch(testBatch.getId());
    }

    @Test
    @DisplayName("规则3：浓度未达标（>30%）不能关闭环保检查")
    void testConcentrationOverThresholdCannotClose() {
        testEnvCheck.setConcentration(new BigDecimal("35"));
        testEnvCheck.setPhValue(new BigDecimal("7.0"));
        testEnvCheck.setCodValue(new BigDecimal("300"));
        testEnvCheck.setBodValue(new BigDecimal("100"));
        testEnvCheck.setCheckResult("UNQUALIFIED");
        testEnvCheck.setRecheckRequired(true);
        envCheckService.addCheck(testEnvCheck);
        assertNotNull(testEnvCheck.getId());

        Map<String, Object> qualResult = envCheckService.checkEnvironmentalQualification(testEnvCheck.getId());
        assertFalse((Boolean) qualResult.get("qualified"), "浓度35%>30%，应该不合格");
        assertTrue(qualResult.get("failReasons").toString().contains("浓度超标"));

        BusinessException exception = assertThrows(BusinessException.class, () ->
            envCheckService.closeCheck(testEnvCheck.getId())
        );
        assertTrue(exception.getMessage().contains("未达标，不能关闭检查"));
        assertEquals(7002, exception.getCode(), "错误码应该是7002");

        envCheckService.deleteCheck(testEnvCheck.getId());
    }

    @Test
    @DisplayName("规则3：pH值超出6-9区间不能关闭")
    void testPhOutOfRangeCannotClose() {
        testEnvCheck.setConcentration(new BigDecimal("25"));
        testEnvCheck.setPhValue(new BigDecimal("4.0"));
        testEnvCheck.setCodValue(new BigDecimal("300"));
        testEnvCheck.setBodValue(new BigDecimal("100"));
        testEnvCheck.setCheckResult("UNQUALIFIED");
        testEnvCheck.setRecheckRequired(true);
        envCheckService.addCheck(testEnvCheck);
        assertNotNull(testEnvCheck.getId());

        Map<String, Object> qualResult = envCheckService.checkEnvironmentalQualification(testEnvCheck.getId());
        assertFalse((Boolean) qualResult.get("qualified"), "pH=4.0，应该不合格");
        assertTrue(qualResult.get("failReasons").toString().contains("pH值超标"));

        BusinessException exception = assertThrows(BusinessException.class, () ->
            envCheckService.closeCheck(testEnvCheck.getId())
        );
        assertTrue(exception.getMessage().contains("未达标"));

        envCheckService.deleteCheck(testEnvCheck.getId());
    }

    @Test
    @DisplayName("规则3：COD超标不能关闭")
    void testCodOverThresholdCannotClose() {
        testEnvCheck.setConcentration(new BigDecimal("25"));
        testEnvCheck.setPhValue(new BigDecimal("7.0"));
        testEnvCheck.setCodValue(new BigDecimal("600"));
        testEnvCheck.setBodValue(new BigDecimal("100"));
        testEnvCheck.setCheckResult("UNQUALIFIED");
        testEnvCheck.setRecheckRequired(true);
        envCheckService.addCheck(testEnvCheck);
        assertNotNull(testEnvCheck.getId());

        Map<String, Object> qualResult = envCheckService.checkEnvironmentalQualification(testEnvCheck.getId());
        assertFalse((Boolean) qualResult.get("qualified"), "COD=600>500，应该不合格");
        assertTrue(qualResult.get("failReasons").toString().contains("COD超标"));

        envCheckService.deleteCheck(testEnvCheck.getId());
    }

    @Test
    @DisplayName("规则3：BOD超标不能关闭")
    void testBodOverThresholdCannotClose() {
        testEnvCheck.setConcentration(new BigDecimal("25"));
        testEnvCheck.setPhValue(new BigDecimal("7.0"));
        testEnvCheck.setCodValue(new BigDecimal("300"));
        testEnvCheck.setBodValue(new BigDecimal("300"));
        testEnvCheck.setCheckResult("UNQUALIFIED");
        testEnvCheck.setRecheckRequired(true);
        envCheckService.addCheck(testEnvCheck);
        assertNotNull(testEnvCheck.getId());

        Map<String, Object> qualResult = envCheckService.checkEnvironmentalQualification(testEnvCheck.getId());
        assertFalse((Boolean) qualResult.get("qualified"), "BOD=300>200，应该不合格");
        assertTrue(qualResult.get("failReasons").toString().contains("BOD超标"));

        envCheckService.deleteCheck(testEnvCheck.getId());
    }

    @Test
    @DisplayName("规则3：全部指标达标可以关闭环保检查")
    void testAllIndicatorsQualifiedCanClose() {
        envCheckService.addCheck(testEnvCheck);
        assertNotNull(testEnvCheck.getId());

        envCheckService.performCheck(
            testEnvCheck.getId(),
            new BigDecimal("20"),
            new BigDecimal("7.5"),
            new BigDecimal("200"),
            new BigDecimal("100")
        );

        EnvironmentalCheck updated = envCheckService.getCheckById(testEnvCheck.getId());
        assertEquals("QUALIFIED", updated.getCheckResult(), "所有指标达标，结果应为合格");
        assertFalse(updated.getRecheckRequired(), "合格不需要复检");

        Map<String, Object> qualResult = envCheckService.checkEnvironmentalQualification(testEnvCheck.getId());
        assertTrue((Boolean) qualResult.get("qualified"), "应该判定为合格");
        assertEquals("", qualResult.get("failReasons"), "不应该有失败原因");

        boolean closed = envCheckService.closeCheck(testEnvCheck.getId());
        assertTrue(closed, "合格的检查应该可以关闭");

        EnvironmentalCheck closedCheck = envCheckService.getCheckById(testEnvCheck.getId());
        assertEquals("CLOSED", closedCheck.getCheckStatus(), "检查状态应为已关闭");

        envCheckService.deleteCheck(testEnvCheck.getId());
    }

    @Test
    @DisplayName("规则3：未进行检测不能关闭")
    void testPendingCheckCannotClose() {
        testEnvCheck.setCheckResult("PENDING");
        envCheckService.addCheck(testEnvCheck);
        assertNotNull(testEnvCheck.getId());

        BusinessException exception = assertThrows(BusinessException.class, () ->
            envCheckService.closeCheck(testEnvCheck.getId())
        );
        assertTrue(exception.getMessage().contains("未进行环保检测"));

        envCheckService.deleteCheck(testEnvCheck.getId());
    }

    @Test
    @DisplayName("规则3：边界值测试 - 刚好达标可以关闭")
    void testBoundaryValuesQualifiedCanClose() {
        envCheckService.addCheck(testEnvCheck);
        assertNotNull(testEnvCheck.getId());

        envCheckService.performCheck(
            testEnvCheck.getId(),
            new BigDecimal("30"),
            new BigDecimal("6.0"),
            new BigDecimal("500"),
            new BigDecimal("200")
        );

        EnvironmentalCheck updated = envCheckService.getCheckById(testEnvCheck.getId());
        assertEquals("QUALIFIED", updated.getCheckResult(), "刚好达标应判定为合格");

        boolean closed = envCheckService.closeCheck(testEnvCheck.getId());
        assertTrue(closed, "刚好达标应该可以关闭");

        envCheckService.deleteCheck(testEnvCheck.getId());
    }

    @Test
    @DisplayName("规则3：边界值测试 - pH=9.0刚好达标")
    void testPhMaxBoundaryQualified() {
        envCheckService.addCheck(testEnvCheck);
        assertNotNull(testEnvCheck.getId());

        envCheckService.performCheck(
            testEnvCheck.getId(),
            new BigDecimal("25"),
            new BigDecimal("9.0"),
            new BigDecimal("300"),
            new BigDecimal("100")
        );

        EnvironmentalCheck updated = envCheckService.getCheckById(testEnvCheck.getId());
        assertEquals("QUALIFIED", updated.getCheckResult(), "pH=9.0刚好达标应判定为合格");

        envCheckService.deleteCheck(testEnvCheck.getId());
    }
}
