package com.pethome.common;

import lombok.Getter;

/**
 * 业务异常
 * 用于在 Service 层主动抛出可预期的业务错误，由 GlobalExceptionHandler 捕获后返回给前端
 */
@Getter
public class BusinessException extends RuntimeException {
    private final int code;

    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
