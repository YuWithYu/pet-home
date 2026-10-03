import js from "@eslint/js";
import pluginVue from "eslint-plugin-vue";
import globals from "globals";

export default [
  {
    ignores: ["dist/**", "node_modules/**", "public/**", "layouts/**"],
  },
  js.configs.recommended,
  ...pluginVue.configs["flat/essential"],
  {
    files: ["src/**/*.{js,vue}"],
    languageOptions: {
      ecmaVersion: "latest",
      sourceType: "module",
      globals: {
        ...globals.browser,
        // 构建工具注入的 API
        require: "readonly",
        module: "readonly",
        process: "readonly",
      },
    },
    rules: {
      // 生产构建通过 swc pure_funcs 移除 log/info/debug/trace，warn/error 允许保留用于排障
      "no-console": ["warn", { allow: ["warn", "error"] }],
      // 存量代码存在大量预留变量，后续渐进收紧
      "no-unused-vars": "off",
      // 视图组件按「文件夹/index.vue」组织为框架既有约定，暂不强制多词命名
      "vue/multi-word-component-names": "off",
      // 存量 View/Picture 等组件名重命名涉及模板引用，列为渐进项
      "vue/no-reserved-component-names": "off",
      // 视图层存在「加载失败保持空列表」的静默 catch，属既有兜底行为
      "no-empty": ["error", { allowEmptyCatch: true }],
    },
  },
];
