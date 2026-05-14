<template>
  <el-sub-menu
    ref="subMenu"
    :index="handlePath(item.path)"
    :popper-append-to-body="false"
  >
    <template #title>
      <img
        v-if="isImageIcon(item.meta?.icon) && !failedIconMap[item.meta?.icon]"
        :src="item.meta.icon"
        class="vab-menu-image-icon"
        alt="menu-icon"
        @error="markIconError(item.meta?.icon)"
      />
      <el-icon
        v-if="item.meta && item.meta.icon && (!isImageIcon(item.meta?.icon) || failedIconMap[item.meta?.icon])"
        class="vab-fas-icon"
      >
        <component :is="getIconComponent(item.meta.icon)" />
      </el-icon>
      <span>{{ item.meta?.title || item.title || '未命名菜单' }}</span>
    </template>
    <slot />
  </el-sub-menu>
</template>

<script setup>
import { isExternal } from "@/utils/validate";
import path from "path";
import { faToElIcon } from "@/utils/vab";
import { reactive } from "vue";

defineOptions({
  name: "VabSubmenu",
});

const props = defineProps({
  routeChildren: {
    type: Object,
    default: () => null,
  },
  item: {
    type: Object,
    default: () => null,
  },
  fullPath: {
    type: String,
    default: "",
  },
});
const failedIconMap = reactive({});

const handlePath = (routePath) => {
  if (isExternal(routePath)) {
    return routePath;
  }
  if (isExternal(props.fullPath)) {
    return props.fullPath;
  }
  return path.resolve(props.fullPath, routePath);
};

// 将路由中的icon名称转换为Element Plus图标组件
const getIconComponent = (iconName) => {
  // 直接使用导入的faToElIcon函数
  return faToElIcon(iconName);
};

const isImageIcon = (icon) => {
  if (!icon || typeof icon !== "string") return false;
  const cleanIcon = icon.split("?")[0].split("#")[0];
  return /\.(png|jpe?g|gif|webp|svg)$/i.test(cleanIcon) || icon.startsWith("data:image/");
};

const markIconError = (icon) => {
  if (!icon) return;
  failedIconMap[icon] = true;
};
</script>

<style scoped>
.vab-menu-image-icon {
  width: 14px;
  height: 14px;
  margin-right: 10px;
  object-fit: contain;
}
</style>
