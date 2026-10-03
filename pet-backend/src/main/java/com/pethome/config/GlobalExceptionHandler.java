package com.pethome.config;

import com.pethome.common.BusinessException;
import com.pethome.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.ConstraintViolationException;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 设计原则：
 * 1. 业务异常（BusinessException）的 message 可直接返回给前端；
 * 2. 参数校验异常返回 400 并附具体字段提示；
 * 3. 其他异常（含 RuntimeException）一律返回"系统繁忙"，避免泄露内部细节给攻击者；
 * 4. 所有异常必须用 logger 完整记录堆栈，便于排查，不使用 printStackTrace。
 */
@ControllerAdvice
@ResponseBody
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 业务异常：返回具体 message（前端可读）
     */
    @ExceptionHandler(BusinessException.class)
    public Result<String> handleBusiness(BusinessException e) {
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 缺少必需请求参数时返回 400
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Result<String>> handleMissingServletRequestParameter(
            MissingServletRequestParameterException e) {
        String msg = "缺少必要参数: " + e.getParameterName();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Result.error(400, msg));
    }

    /**
     * Bean Validation 参数校验失败（@Valid 触发）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<String> handleValid(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return Result.error(400, msg);
    }

    /**
     * Bean Validation 参数校验失败（@RequestParam 触发）
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<String> handleConstraintViolation(ConstraintViolationException e) {
        return Result.error(400, e.getMessage());
    }

    /**
     * 非法参数异常：返回具体提示
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<String> handleIllegalArgumentException(IllegalArgumentException e) {
        return Result.error(400, "参数错误: " + e.getMessage());
    }

    /**
     * 空指针异常：不返回具体细节，避免泄露内部结构
     */
    @ExceptionHandler(NullPointerException.class)
    public Result<String> handleNullPointerException(NullPointerException e) {
        log.error("空指针异常", e);
        return Result.error(500, "系统内部错误");
    }

    /**
     * 运行时异常：安全修复——不再把 e.getMessage() 返回给前端，
     * 避免泄露 SQL 错误、类名、堆栈片段等内部细节给攻击者。
     */
    @ExceptionHandler(RuntimeException.class)
    public Result<String> handleRuntimeException(RuntimeException e) {
        log.error("未捕获的运行时异常", e);
        return Result.error(500, "系统繁忙，请稍后重试");
    }

    /**
     * 兜底异常
     */
    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error(500, "系统繁忙，请稍后重试");
    }
}
