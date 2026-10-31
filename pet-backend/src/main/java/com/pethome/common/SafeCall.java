package com.pethome.common;

import org.slf4j.Logger;

/**
 * 控制器安全调用模板。
 *
 * <p>把「执行 → 记录日志 → 兜底返回」这段在每个接口里重复手写的 try/catch 收敛到一处，
 * 让 Controller 只表达"调用什么、失败时给用户什么提示"，异常处理不再散落在表现层。</p>
 *
 * <p>约定：
 * <ul>
 *   <li>{@link BusinessException} 原样向上抛出，由 GlobalExceptionHandler 统一返回可读文案；</li>
 *   <li>其他异常（含 IOException 等受检异常）记录堆栈后返回统一的兜底 Result，避免泄露内部细节。</li>
 * </ul>
 */
public final class SafeCall {

    private SafeCall() {
    }

    /** 允许抛出受检异常的动作，避免上传类接口被迫在 Controller 里再写一层 try/catch */
    @FunctionalInterface
    public interface Action<T> {
        T run() throws Exception;
    }

    public static <T> Result<T> of(Action<Result<T>> action, String failMessage, Logger log) {
        try {
            return action.run();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error(failMessage, e);
            return Result.error(failMessage);
        }
    }
}
