package com.pethome.config;

import com.pethome.common.BusinessException;
import com.pethome.common.Result;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 锁定全局异常出口的契约：
 * 1. 业务异常的具体文案可以给前端看（用户需要知道"为什么失败"）；
 * 2. 参数类异常返回 400；
 * 3. 运行时异常的内部细节绝不能外泄（避免泄露 SQL / 类名 / 堆栈）。
 *
 * 控制器不再手写 try/catch 之后，这三个断言就是对外错误语义的护栏。
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void businessExceptionKeepsMessageAndCode() {
        Result<String> result = handler.handleBusiness(new BusinessException(400, "积分不足"));
        assertEquals(400, result.getCode());
        assertEquals("积分不足", result.getMsg());
    }

    @Test
    void illegalArgumentBecomesBadRequest() {
        Result<String> result = handler.handleIllegalArgumentException(new IllegalArgumentException("库存为 0"));
        assertEquals(400, result.getCode());
        assertTrue(result.getMsg().contains("库存为 0"));
    }

    @Test
    void runtimeExceptionHidesInternalDetail() {
        Result<String> result = handler.handleRuntimeException(new RuntimeException("DataIntegrityViolationException: dup key"));
        assertEquals(500, result.getCode());
        assertEquals("系统繁忙，请稍后重试", result.getMsg());
        assertNull(result.getData());
    }
}
