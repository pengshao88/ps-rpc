package cn.pengshao.rpc.core.api;

/**
 * Description:
 *
 * @Author: yezp
 * @date 2024/3/28 23:32
 */
public class RpcException extends RuntimeException {

    private String errCode;

    public RpcException() {
    }

    public RpcException(Throwable cause, String errCode) {
        super(cause);
        this.errCode = errCode;
    }

    public RpcException(String message, String errCode) {
        super(message);
        this.errCode = errCode;
    }

    public RpcException(String message) {
        super(message);
    }

    public RpcException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcException(Throwable cause) {
        super(cause);
    }
}
