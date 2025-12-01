package com.lfc.yundocker.common;

/**
 * 自定义错误码
 *
 * @author laifucheng
 */
public enum ErrorCode {

    SUCCESS(200, "ok"),
    PARAMS_ERROR(40000, "请求参数错误"),
    NOT_LOGIN_ERROR(40100, "未登录"),
    NO_AUTH_ERROR(40101, "无权限"),
    NOT_FOUND_ERROR(40400, "请求数据不存在"),
    FORBIDDEN_ERROR(40300, "禁止访问"),
    SYSTEM_ERROR(50000, "系统内部异常"),

    HOST_ERROR(50003, "计算节点异常"),
    DOCKER_ERROR(50002, "计算节点Docker异常"),
    OPERATION_ERROR(50001, "操作失败"),

    INSUFFICIENT_BALANCE(50003, "余额不足"),
    ORDER_STATUS_ERROR(50004, "订单状态异常");

    /**
     * 状态码
     */
    private final int code;

    /**
     * 信息
     */
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
