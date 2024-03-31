package cn.pengshao.rpc.core.enums;

import lombok.Data;
import lombok.Getter;

/**
 * Description:
 *
 * @Author: yezp
 * @date 2024/3/28 23:41
 */
public enum ErrorCodeEnum {

    /**
     * 超时
     */
    SOCKET_TIME_OUT_EX("X", "001", "http invoke timeout"),

    /**
     * 方法不存在
     */
    NO_SUCH_METHOD("X", "002", "no such method"),

    INTERRUPTED_EXCEPTION("X", "003", "interrupted exception"),

    /**
     * 未知错误
     */
    UNKNOWN_ERROR("Z", "003", "unknown error"),;

    @Getter
    private final String errorType;

    @Getter
    private final String errorCode;

    private final String errorMsg;

    ErrorCodeEnum(String errorType, String errorCode, String errorMsg) {
        this.errorType = errorType;
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public String getErrorMsg() {
        return errorType + errorCode + "-" + errorMsg;
    }
}
