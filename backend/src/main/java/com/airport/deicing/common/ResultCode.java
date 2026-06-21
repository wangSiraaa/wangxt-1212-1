package com.airport.deicing.common;

import lombok.Getter;

@Getter
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    FAILURE(500, "操作失败"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),

    FLIGHT_NOT_FOUND(1001, "航班不存在"),
    FLIGHT_DEICING_NOT_COMPLETED(1002, "航班除冰未完成，存在风险"),
    FLIGHT_ALREADY_DEPARTED(1003, "航班已起飞"),
    DEICING_ALREADY_COMPLETED(1004, "航班已完成除冰，无需标记风险"),
    RISK_REMARK_NOT_FOUND(1005, "风险备注不存在"),

    VEHICLE_NOT_FOUND(2001, "车辆不存在"),
    VEHICLE_NOT_AVAILABLE(2002, "车辆不可用"),
    VEHICLE_INSUFFICIENT_FLUID(2003, "车辆液体不足"),

    BATCH_NOT_FOUND(3001, "除冰液批次不存在"),
    BATCH_NOT_AVAILABLE(3002, "除冰液批次不可用"),
    BATCH_TEMPERATURE_OUT_OF_RANGE(3003, "环境温度超出除冰液有效温度区间"),
    BATCH_INSUFFICIENT_VOLUME(3004, "除冰液剩余量不足"),
    BATCH_EXPIRED(3005, "除冰液已过期"),

    DISPATCH_NOT_FOUND(4001, "调度记录不存在"),
    DISPATCH_STATUS_ERROR(4002, "调度状态异常"),

    SPRAY_NOT_FOUND(5001, "喷洒记录不存在"),
    SPRAY_STATUS_ERROR(5002, "喷洒状态异常"),

    WASTE_NOT_FOUND(6001, "废液回收记录不存在"),
    WASTE_STATUS_ERROR(6002, "回收状态异常"),

    ENV_CHECK_NOT_FOUND(7001, "环保检查不存在"),
    ENV_CHECK_UNQUALIFIED(7002, "环保检查未达标，不能关闭"),
    ENV_CHECK_STATUS_ERROR(7003, "环保检查状态异常"),

    DASHBOARD_ERROR(8001, "看板数据获取失败");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
