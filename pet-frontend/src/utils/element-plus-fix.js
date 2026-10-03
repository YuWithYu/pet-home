/**
 * Element Plus 已知运行时噪音过滤
 *
 * 仅在事件层面拦截库内部已知的非致命错误，避免开发控制台被刷屏。
 * 不覆盖 console、不覆盖全局 Error 构造函数，保证 instanceof Error 等
 * 基础语言行为不受影响。
 */
const IGNORED_ERROR_PATTERNS = [
  // Element Plus 日期组件旧版本与 dayjs 的兼容问题（升级组件库后可移除）
  "defaultTimeDValue.hour is not a function",
  // Element Plus Transition 在快速切换时的内部竞态警告
  "Cannot read properties of null (reading 'nextSibling')",
  "Cannot read property 'nextSibling' of null",
];

const isIgnoredError = (message) =>
  IGNORED_ERROR_PATTERNS.some((pattern) =>
    String(message || "").includes(pattern)
  );

window.addEventListener(
  "error",
  (event) => {
    if (isIgnoredError(event.message)) {
      event.preventDefault();
    }
  },
  true
);

window.addEventListener("unhandledrejection", (event) => {
  if (isIgnoredError(event.reason?.message)) {
    event.preventDefault();
  }
});
