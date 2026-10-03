<template>
  <div class="vab-avatar-wrap">
    <el-dropdown @command="handleCommand" trigger="click">
      <div
        class="avatar-container"
        :class="{ 'horizontal-layout': isHorizontalLayout }"
      >
        <div class="avatar-wrapper">
          <img :src="displayAvatar" alt="用户头像" class="user-avatar" />
        </div>
        <div class="user-info">
          <div class="username">{{ username }}</div>
          <div class="user-role">{{ roleDisplayName }}</div>
        </div>
        <!-- 直接使用图标组件 -->
        <ArrowDown class="avatar-dropdown-icon" />
      </div>

      <template #dropdown>
        <el-dropdown-menu class="custom-dropdown">
          <div class="dropdown-header">
            <img :src="displayAvatar" alt="用户头像" class="header-avatar" />
            <div class="header-info">
              <div class="header-username">{{ username }}</div>
            </div>
          </div>

          <el-dropdown-item command="changeAvatar" class="dropdown-item">
            <Camera class="dropdown-icon" />
            <span>换头像</span>
          </el-dropdown-item>

          <el-divider></el-divider>

          <el-dropdown-item command="logout" class="dropdown-item logout-item">
            <!-- 直接使用图标组件 -->
            <SwitchButton class="dropdown-icon" />
            <span>退出登录</span>
          </el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
    <input
      ref="avatarInputRef"
      type="file"
      accept="image/jpeg,image/png"
      class="hidden-input"
      @change="onAvatarFileChange"
    />
  </div>
</template>

<script setup>
import { computed, ref, nextTick } from "vue";
import { useStore } from "vuex";
import { useRouter, useRoute } from "vue-router";
import { ElMessage } from "element-plus";
import { recordRoute } from "@/config";
import axios from "axios";
import {
  ArrowDown,
  SwitchButton,
  Camera,
} from "@element-plus/icons-vue";
import { updateMyAvatar } from "@/api/admin";
import { baseURL } from "@/config";

defineOptions({
  name: "VabAvatar",
});

const store = useStore();
const router = useRouter();
const route = useRoute();
const avatarInputRef = ref(null);

// 图片基础地址（与接口同源）
const getImageBase = () => (baseURL || "").replace(/\/api\/?$/, "") || "http://localhost:8080";

// 计算属性
const avatar = computed(() => store.getters["user/avatar"]);
const displayAvatar = computed(() => {
  const raw = avatar.value;
  if (!raw || typeof raw !== "string") return getImageBase() + "/static/images/garfield-default-avatar.png";
  const s = raw.trim();
  if (!s) return getImageBase() + "/static/images/garfield-default-avatar.png";
  if (s.startsWith("http://") || s.startsWith("https://")) return s;
  if (s.startsWith("/")) return getImageBase() + s;
  return getImageBase() + "/" + s.replace(/^\/+/, "");
});
const username = computed(() => store.getters["user/username"]);
const role = computed(() => store.getters["user/role"] || "");
const roleDisplayName = computed(() => {
  const map = { super_admin: "超级管理员", admin: "管理员", staff: "员工" };
  return map[role.value] || "管理员";
});
const layout = computed(() => store.getters["settings/layout"]);
const isHorizontalLayout = computed(() => layout.value === "horizontal");

// 方法
const handleCommand = (command) => {
  switch (command) {
    case "changeAvatar":
      // 下拉会先关闭，nextTick + setTimeout(0) 确保在下一轮再触发 input，避免被下拉关闭拦截
      nextTick(() => {
        setTimeout(() => {
          const el = avatarInputRef.value;
          if (el && typeof el.click === "function") {
            el.value = "";
            el.click();
          }
        }, 0);
      });
      break;
    case "logout":
      logout();
      break;
  }
};

const onAvatarFileChange = async (e) => {
  const file = e.target?.files?.[0];
  if (!file) return;
  if (file.type !== "image/jpeg" && file.type !== "image/png") {
    ElMessage.error("请选择 JPG 或 PNG 图片");
    e.target.value = "";
    return;
  }
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.error("图片大小不能超过 10MB");
    e.target.value = "";
    return;
  }
  const formData = new FormData();
  formData.append("file", file);
  try {
    const base = getImageBase();
    const token = store.getters["user/accessToken"];
    const headers = { "Content-Type": "multipart/form-data" };
    if (token) headers["Authorization"] = "Bearer " + token;
    const res = await axios.post(base + "/api/upload/image", formData, { headers });
    const data = res.data;
    const avatarUrl = (data && typeof data.data === "string") ? data.data : (data && data.url) ? data.url : "";
    if (!avatarUrl || (data && data.code !== 0 && data.code !== 200)) {
      ElMessage.error(data?.msg || "上传失败");
      e.target.value = "";
      return;
    }
    const updateRes = await updateMyAvatar(avatarUrl);
    if (updateRes && (updateRes.code === 0 || updateRes.code === 200)) {
      const newAvatar = (updateRes.data && updateRes.data.avatar) ? updateRes.data.avatar : avatarUrl;
      store.commit("user/setAvatar", newAvatar);
      ElMessage.success("头像已更新，全系统同步");
    } else {
      ElMessage.error(updateRes?.msg || "保存失败");
    }
  } catch (err) {
    console.error(err);
    ElMessage.error(err?.response?.data?.msg || err?.message || "上传失败");
  }
  e.target.value = "";
};

const logout = () => {
  store.dispatch("user/logout");
  if (recordRoute) {
    const fullPath = route.fullPath;
    router.push(`/login?redirect=${fullPath}`);
  }
};
</script>

<style lang="scss" scoped>
.vab-avatar-wrap {
  display: inline-block;
}

.avatar-container {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  border-radius: 8px;
  cursor: pointer;

  &.horizontal-layout {
    .user-info {
      .username,
      .user-role {
        color: rgba(255, 255, 255, 0.9) !important;
      }
    }

    .avatar-dropdown-icon {
      color: rgba(255, 255, 255, 0.9) !important;
    }
  }

  .avatar-wrapper {
    position: relative;
    margin-right: 12px;

    .user-avatar {
      width: 40px;
      height: 40px;
      border-radius: 50%;
      object-fit: cover;
      border: 2px solid rgba(255, 255, 255, 0.3);
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    }
  }

  .user-info {
    flex: 1;
    min-width: 0;

    .username {
      font-size: 14px;
      font-weight: 600;
      color: #333;
      margin-bottom: 2px;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .user-role {
      font-size: 12px;
      color: #666;
      opacity: 0.8;
    }
  }

  .avatar-dropdown-icon {
    margin-left: 5px;
    color: #666;
  }
}

.custom-dropdown {
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.95);
  padding: 0;
  min-width: 220px;

  .dropdown-header {
    display: flex;
    align-items: center;
    padding: 16px;
    border-bottom: 1px solid rgba(0, 0, 0, 0.06);
    background: linear-gradient(135deg, #409EFF 0%, #69C0FF 100%);
    border-radius: 12px 12px 0 0;
    color: white;

    .header-avatar {
      width: 48px;
      height: 48px;
      border-radius: 50%;
      border: 2px solid rgba(255, 255, 255, 0.3);
      margin-right: 12px;
      object-fit: cover;
    }

    .header-info {
      flex: 1;

      .header-username {
        font-size: 16px;
        font-weight: 600;
      }
    }
  }

  .dropdown-item {
    display: flex;
    align-items: center;
    padding: 6px 12px;
    transition: background-color 0.2s;

    &.logout-item {
      color: #f56c6c;
    }
  }

  .el-divider {
    margin: 8px 0;
  }
}

// 响应式设计
@media (max-width: 768px) {
  .avatar-container {
    padding: 6px 8px;

    .user-info {
      display: none;
    }

    .dropdown-icon {
      display: none;
    }
  }

  .custom-dropdown {
    min-width: 200px;
  }
}

// 隐藏下拉菜单箭头
:deep() .popper__arrow {
  display: none !important;
}

.hidden-input {
  position: absolute;
  opacity: 0;
  width: 0;
  height: 0;
  pointer-events: none;
}
</style>
