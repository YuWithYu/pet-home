/* 公共引入,勿随意修改,修改时需经过确认 */
import "./support";
import "@/styles/vab.scss";
import "@/config/permission";
// 不再导入vab-icon
import VabPermissions from "layouts/Permissions";
import Vab from "@/utils/vab";
// 导入 Element Plus 但不包含日期选择器组件
import ElementPlus from "element-plus";
import "element-plus/dist/index.css";
import zhCnLocale from "@/locale/element-plus-zh-cn";

// 禁用日期选择器组件以避免 dayjs 错误
if (ElementPlus.ElDatePicker) {
  delete ElementPlus.ElDatePicker;
}

// 确保 dayjs 在 Element Plus 加载前可用
import dayjs from "@/utils/dayjs";
if (typeof window !== 'undefined') {
  window.dayjs = dayjs;
}
import * as ElementPlusIconsVue from "@element-plus/icons-vue";
import { faToElIcon } from "@/utils/vab";
import { h } from "vue";

// 创建全局VabIcon组件，用于替换之前的vab-icon
const VabIcon = {
  name: "VabIcon",
  props: {
    icon: {
      type: [String, Array],
      required: true,
    },
  },
  setup(props) {
    const isImageIcon = (icon) => {
      if (typeof icon !== "string") return false;
      const val = icon.trim();
      if (!val) return false;
      if (val.startsWith("/") || val.startsWith("http://") || val.startsWith("https://")) {
        return true;
      }
      return /\.(png|jpg|jpeg|gif|webp|svg)$/i.test(val);
    };
    return () => {
      if (isImageIcon(props.icon)) {
        return h("img", {
          src: props.icon,
          alt: "menu-icon",
          style: {
            width: "16px",
            height: "16px",
            display: "inline-block",
            objectFit: "contain",
            verticalAlign: "middle",
          },
        });
      }
      const iconName = faToElIcon(props.icon);
      return h("el-icon", {}, [h(iconName)]);
    };
  },
};

export default (app) => {
  // 注册Element Plus，配置 ResizeObserver
  app.use(ElementPlus, {
    size: 'default',
    zIndex: 3000,
    locale: zhCnLocale
  });
  
  // 修复 dayjs 问题 - 确保 Element Plus 使用正确的 dayjs 实例
  if (typeof window !== 'undefined' && window.dayjs) {
    // 将配置好的 dayjs 实例传递给 Vue 应用
    app.config.globalProperties.$dayjs = window.dayjs;
  }
  

  // 注册所有Element Plus图标
  for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component);
  }

  // 注册VabIcon组件，替代之前的vab-icon
  app.component("VabIcon", VabIcon);

  // 注册自定义插件
  app.use(Vab);
  app.use(VabPermissions);
};
