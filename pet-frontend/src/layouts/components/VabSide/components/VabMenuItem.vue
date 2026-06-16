<template>
  <el-menu-item :index="handlePath(routeChildren.path)" @click="handleLink">
    <img
      v-if="isImageIcon(routeChildren.meta?.icon) && !failedIconMap[routeChildren.meta?.icon]"
      :src="routeChildren.meta.icon"
      class="vab-menu-image-icon"
      alt="menu-icon"
      @error="markIconError(routeChildren.meta?.icon)"
    />
    <el-icon
      v-if="routeChildren.meta && routeChildren.meta.icon && (!isImageIcon(routeChildren.meta?.icon) || failedIconMap[routeChildren.meta?.icon])"
      class="vab-fas-icon"
    >
      <component :is="getIconComponent(routeChildren.meta.icon)" />
    </el-icon>
    <span>{{ routeChildren.meta?.title || '未知菜单' }}</span>
  </el-menu-item>
</template>

<script setup>
import { isExternal } from "@/utils/validate";
import path from "path";
import { faToElIcon } from "@/utils/vab";
import { useRouter, useRoute } from "vue-router";
import { reactive } from "vue";

defineOptions({
  name: "VabMenuItem",
});

const router = useRouter();
const route = useRoute();
const failedIconMap = reactive({});

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

const handlePath = (routePath) => {
  if (isExternal(routePath)) {
    return routePath;
  }
  if (isExternal(props.fullPath)) {
    return props.fullPath;
  }
  return path.resolve(props.fullPath, routePath);
};

const handleLink = () => {
  const routePath = props.routeChildren.path;
  const target = props.routeChildren.meta.target;

  if (target === "_blank") {
    if (isExternal(routePath)) {
      window.open(routePath);
    } else if (isExternal(props.fullPath)) {
      window.open(props.fullPath);
    } else if (route.path !== path.resolve(props.fullPath, routePath)) {
      let routeData = router.resolve(path.resolve(props.fullPath, routePath));
      window.open(routeData.href);
    }
  } else {
    if (isExternal(routePath)) {
      window.location.href = routePath;
    } else if (isExternal(props.fullPath)) {
      window.location.href = props.fullPath;
    } else if (route.path !== path.resolve(props.fullPath, routePath)) {
      router.push(path.resolve(props.fullPath, routePath));
    }
  }
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
