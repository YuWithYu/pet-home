/**
 * ResizeObserver 错误修复工具
 * 解决 "ResizeObserver loop completed with undelivered notifications" 错误
 */

// 保存原始的 ResizeObserver
const OriginalResizeObserver = window.ResizeObserver;

// 创建一个包装的 ResizeObserver
class PatchedResizeObserver extends OriginalResizeObserver {
  constructor(callback) {
    // 包装回调函数，添加错误处理
    const wrappedCallback = (entries, observer) => {
      try {
        callback(entries, observer);
      } catch (error) {
        // 忽略 ResizeObserver 相关的错误
        if (error.message && error.message.includes('ResizeObserver')) {
          return;
        }
        throw error;
      }
    };
    
    super(wrappedCallback);
  }
}

// 替换全局的 ResizeObserver
window.ResizeObserver = PatchedResizeObserver;

// 处理未捕获的错误
const handleError = (event) => {
  if (event.message && event.message.includes('ResizeObserver')) {
    event.preventDefault();
    event.stopImmediatePropagation();
    return false;
  }
};

// 处理未捕获的 Promise 错误
const handleUnhandledRejection = (event) => {
  if (event.reason && event.reason.message && event.reason.message.includes('ResizeObserver')) {
    event.preventDefault();
    return false;
  }
};

// 添加事件监听器
window.addEventListener('error', handleError);
window.addEventListener('unhandledrejection', handleUnhandledRejection);

// 重写 console.error 来过滤 ResizeObserver 错误
const originalConsoleError = console.error;
console.error = function(...args) {
  const message = args[0];
  if (typeof message === 'string' && message.includes('ResizeObserver')) {
    return;
  }
  originalConsoleError.apply(console, args);
};

// 重写 console.warn 来过滤 ResizeObserver 警告
const originalConsoleWarn = console.warn;
console.warn = function(...args) {
  const message = args[0];
  if (typeof message === 'string' && message.includes('ResizeObserver')) {
    return;
  }
  originalConsoleWarn.apply(console, args);
};

export default {
  OriginalResizeObserver,
  PatchedResizeObserver
};
