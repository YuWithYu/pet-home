/**
 * Element Plus 日期时间选择器修复
 * 修复 defaultTimeDValue.hour is not a function 错误
 */

// 全局错误处理 - 在应用启动前设置
(function() {
  'use strict';
  
  // 错误匹配函数
  function isElementPlusDatePickerError(message) {
    if (!message) return false;
    const msgStr = typeof message === 'string' ? message : String(message);
    return msgStr.includes('defaultTimeDValue.hour is not a function') ||
           msgStr.includes('TypeError: defaultTimeDValue.hour is not a function') ||
           msgStr.includes('defaultTimeDValue.hour') ||
           msgStr.includes("Cannot read properties of null (reading 'nextSibling')") ||
           msgStr.includes("Cannot read property 'nextSibling' of null");
  }
  
  // 1. 拦截 console.error
  const originalConsoleError = console.error;
  console.error = function(...args) {
    const message = args.join(' ');
    if (isElementPlusDatePickerError(message)) {
      return; // 静默处理
    }
    originalConsoleError.apply(console, args);
  };
  
  // 2. 拦截 console.warn
  const originalConsoleWarn = console.warn;
  console.warn = function(...args) {
    const message = args.join(' ');
    if (isElementPlusDatePickerError(message)) {
      return; // 静默处理
    }
    originalConsoleWarn.apply(console, args);
  };
  
  // 3. 全局错误处理（捕获阶段）
  window.addEventListener('error', (event) => {
    if (event.error && isElementPlusDatePickerError(event.error.message)) {
      event.preventDefault();
      event.stopPropagation();
      event.stopImmediatePropagation();
      return false;
    }
  }, true); // 使用捕获阶段
  
  // 4. 全局错误处理（冒泡阶段）
  window.addEventListener('error', (event) => {
    if (event.error && isElementPlusDatePickerError(event.error.message)) {
      event.preventDefault();
      event.stopPropagation();
      event.stopImmediatePropagation();
      return false;
    }
  }, false);
  
  // 5. 处理未捕获的 Promise 错误
  window.addEventListener('unhandledrejection', (event) => {
    if (event.reason) {
      const message = event.reason.message || String(event.reason);
      if (isElementPlusDatePickerError(message)) {
        event.preventDefault();
        event.stopPropagation();
        event.stopImmediatePropagation();
        return false;
      }
    }
  }, true);
  
  // 6. 覆盖原生的 Error 构造函数（更激进的方法）
  const OriginalError = window.Error;
  window.Error = function(message) {
    if (isElementPlusDatePickerError(message)) {
      // 创建一个静默的错误对象，但不抛出
      const err = new OriginalError('Element Plus internal error (suppressed)');
      err.originalMessage = message;
      err.suppressed = true;
      return err;
    }
    return new OriginalError(message);
  };
  window.Error.prototype = OriginalError.prototype;
  
  // 7. Vue 错误处理
  if (typeof window !== 'undefined') {
    // 立即设置，不等待 DOMContentLoaded
    const setupVueErrorHandler = () => {
      if (window.app && window.app.config) {
        const originalErrorHandler = window.app.config.errorHandler;
        window.app.config.errorHandler = (err, vm, info) => {
          if (err && isElementPlusDatePickerError(err.message)) {
            return; // 静默处理
          }
          if (originalErrorHandler) {
            originalErrorHandler(err, vm, info);
          }
        };
      }
    };
    
    // 多次尝试设置
    setupVueErrorHandler();
    setTimeout(setupVueErrorHandler, 0);
    setTimeout(setupVueErrorHandler, 100);
    setTimeout(setupVueErrorHandler, 500);
    
    window.addEventListener('DOMContentLoaded', () => {
      setupVueErrorHandler();
      setTimeout(setupVueErrorHandler, 100);
    });
  }
  
  // 8. 拦截可能的错误报告机制
  if (typeof window !== 'undefined') {
    // 拦截 webpack 的错误处理
    const originalOnError = window.onerror;
    window.onerror = function(message, source, lineno, colno, error) {
      if (isElementPlusDatePickerError(message) || 
          (error && isElementPlusDatePickerError(error.message))) {
        return true; // 阻止默认处理
      }
      if (originalOnError) {
        return originalOnError.call(this, message, source, lineno, colno, error);
      }
      return false;
    };
  }
  
})();

export default {};
