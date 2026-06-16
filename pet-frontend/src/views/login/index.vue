<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-form-container">
        <div class="logo-container">
          <h2 class="welcome-text">欢迎回来</h2>
          <h3 class="system-title">{{ title }}</h3>
        </div>

        <el-form
          ref="loginForm"
          :model="form"
          :rules="rules"
          class="login-form"
        >
          <el-form-item prop="loginType">
            <el-radio-group v-model="form.loginType" class="login-type-group">
              <el-radio-button label="admin">管理员</el-radio-button>
              <el-radio-button label="staff">服务人员</el-radio-button>
            </el-radio-group>
          </el-form-item>

          <el-form-item prop="username">
            <el-input
              v-model="form.username"
              placeholder="请输入用户名"
              tabindex="1"
              type="text"
            >
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              :key="passwordType"
              ref="password"
              v-model="form.password"
              :type="passwordType"
              tabindex="2"
              placeholder="请输入密码"
              @keyup.enter="handleLogin"
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
            <span class="show-pwd" @click="showPwd">
              <el-icon v-if="passwordType === 'password'">
                <Hide />
              </el-icon>
              <el-icon v-else>
                <View />
              </el-icon>
            </span>
          </el-form-item>

          <div class="form-actions">
            <el-checkbox v-model="rememberMe">记住我</el-checkbox>
            <a href="javascript:;" class="forgot-password" @click="handleForgotPassword">忘记密码?</a>
          </div>

          <el-button
            :loading="loading"
            type="primary"
            class="login-button"
            @click.prevent="handleLogin"
          >
            登录
          </el-button>

        </el-form>
      </div>

      <div class="login-image">
        <div class="overlay">
          <h2 class="slogan"></h2>
          <p class="description"></p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, toRefs, onMounted, computed, nextTick } from "vue";
import { useRouter } from "vue-router";
import { useStore } from "vuex";
import { title } from "@/config";
import { isPassword } from "@/utils/validate";
import { ElMessage } from "element-plus";
import { Hide, View, User, Lock } from "@element-plus/icons-vue";

// 创建路由实例
const router = useRouter();
const store = useStore();

// 响应式状态
const state = reactive({
  form: {
    loginType: "admin",
    username: "",
    password: "",
  },
  rules: {
    loginType: [{ required: true, trigger: "change", message: "请选择登录类型" }],
    username: [{ required: true, trigger: "blur", message: "请输入用户名" }],
    password: [
      { required: true, trigger: "blur", message: "请输入密码" },
      {
        validator: (rule, value, callback) => {
          if (!isPassword(value)) {
            callback(new Error("密码长度必须大于等于6位"));
          } else {
            callback();
          }
        },
        trigger: "blur",
      },
    ],
  },
  loading: false,
  passwordType: "password",
  redirect: undefined,
});

// 使用refs获取表单DOM引用
const loginForm = ref(null);
const password = ref(null);
const rememberMe = ref(false);

// 计算属性
const otherQuery = computed(() => {
  return Object.keys(router.currentRoute.value.query).reduce((acc, cur) => {
    if (cur !== "redirect") {
      acc[cur] = router.currentRoute.value.query[cur];
    }
    return acc;
  }, {});
});

// 忘记密码：管理员后台需联系系统管理员重置
const handleForgotPassword = () => {
  ElMessage.info("忘记密码请联系系统管理员重置");
};

// 显示/隐藏密码
const showPwd = () => {
  state.passwordType = state.passwordType === "password" ? "" : "password";
  // 等待DOM更新后聚焦
  nextTick(() => {
    password.value?.focus();
  });
};

// 处理登录
const handleLogin = () => {
  loginForm.value?.validate(async (valid) => {
    if (valid) {
      if (!isPassword(state.form.password)) {
        ElMessage.error("密码长度必须大于等于6位");
        return;
      }

      state.loading = true;
      try {
        // 使用命名空间调用login action
        await store.dispatch("user/login", state.form);

        // 登录成功后，获取用户信息（确保 role 和 department 已保存）
        const userInfoResult = await store.dispatch("user/getUserInfo");
        
        // 如果获取用户信息失败，停止执行
        if (userInfoResult === false) {
          state.loading = false;
          return;
        }
        
        // 等待 store 更新完成，确保权限已设置
        await nextTick();
        
        // 不再为空权限注入 admin；零权限账号仅保留 getUserInfo 已写入的列表（可为 []）
        
        // 根据用户角色和登录类型，做入口约束 + 跳转
        const userRole = store.getters["user/role"];
        const userDepartment = store.getters["user/department"];
        const selectedLoginType = state.form.loginType;
        const isAdminRole = userRole === "admin" || userRole === "store_admin";
        const isStaffRole = userRole === "staff";

        if (selectedLoginType === "admin" && !isAdminRole) {
          ElMessage.error("该账号不是管理员，请选择“服务人员”登录");
          await store.dispatch("user/logout");
          state.loading = false;
          return;
        }
        if (selectedLoginType === "staff" && !isStaffRole) {
          ElMessage.error("该账号不是服务人员，请选择“管理员”登录");
          await store.dispatch("user/logout");
          state.loading = false;
          return;
        }
        
        console.log('登录后用户信息 - role:', userRole, 'department:', userDepartment, 'permissions:', store.getters["user/permissions"]);
        
        // 保存到 localStorage（用于路由设置）
        if (userDepartment) {
          localStorage.setItem('department', userDepartment);
        }
        
        // 登录后统一进入首页；首页对所有账号放行，不需要单独在权限管理中配置
        const targetPath = "/index";
        console.log("登录成功，跳转目标:", targetPath, "用户角色:", userRole, "用户部门:", userDepartment);
        
        // 先关闭loading状态
        state.loading = false;
        
        // 清除路由守卫的加载标记，避免重复调用getUserInfo
        sessionStorage.removeItem('isLoadingRoutes');
        sessionStorage.removeItem('lastRoutePath');
        sessionStorage.removeItem('lastGetUserInfoTime');
        
        // 等待一下确保状态已更新
        await new Promise(resolve => setTimeout(resolve, 100));
        
        // 直接跳转
        router.push(targetPath).catch((err) => {
          console.error('路由跳转失败:', err);
          // 如果路由跳转失败，使用window.location强制跳转
          window.location.href = targetPath;
        });
      } catch (error) {
        console.error("登录失败:", error);
        ElMessage.error(error.message || "登录失败，请检查用户名和密码");
        state.loading = false;
      }
    }
  });
};

// 生命周期钩子
onMounted(() => {
  if (router.currentRoute.value.query.redirect) {
    state.redirect = router.currentRoute.value.query.redirect;
  }
});

// 暴露给模板的变量
const { form, rules, loading, passwordType, redirect } = toRefs(state);
</script>

<style lang="scss" scoped>
/* 背景替换说明：
 * 1. 页面外围背景：下方 .login-container 的 background，可改为渐变色或 background-image: url()
 * 2. 右侧图背景：下方 .login-image 的 background，替换 public/static/login-bg.jpg 即可换图
 */
.login-container {
  height: 100vh;
  width: 100vw;
  display: flex;
  justify-content: center;
  align-items: center;
  /* 页面背景：渐变可改为 linear-gradient(角度, 色1, 色2)，或 background-image: url('/static/xxx.jpg') */
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.login-box {
  width: 80%;
  max-width: 1000px;
  height: 700px;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  display: flex;
  background-color: #fff;
}

.login-form-container {
  width: 50%;
  padding: 50px;
  display: flex;
  flex-direction: column;
}

.logo-container {
  margin-bottom: 40px;
  text-align: center;

  .welcome-text {
    font-size: 28px;
    color: #333;
    margin-bottom: 10px;
    font-weight: 600;
  }

  .system-title {
    font-size: 18px;
    color: #666;
    font-weight: 400;
  }
}

.login-form {
  flex: 1;

  .el-form-item {
    margin-bottom: 24px;
  }

  .el-input {
    height: 50px;

    :deep(.el-input__wrapper) {
      padding-left: 15px;
      box-shadow: 0 0 0 1px #dcdfe6 inset;
    }

    :deep(.el-input__prefix) {
      color: #909399;
      font-size: 18px;
    }
  }

  .show-pwd {
    position: absolute;
    right: 15px;
    top: 14px;
    font-size: 16px;
    color: #889aa4;
    cursor: pointer;
    user-select: none;
  }
}

.login-type-group {
  width: 100%;
  display: flex;

  :deep(.el-radio-button) {
    flex: 1;
  }

  :deep(.el-radio-button__inner) {
    width: 100%;
    text-align: center;
  }
}

.form-actions {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;

  .forgot-password {
    color: #409eff;
    text-decoration: none;

    &:hover {
      text-decoration: underline;
    }
  }
}

.login-button {
  width: 100%;
  height: 50px;
  border-radius: 25px;
  font-size: 16px;
  font-weight: 500;
  letter-spacing: 1px;
  background: linear-gradient(90deg, #409eff 0%, #007aff 100%);
  border: none;
  margin-top: 10px;

  &:hover {
    background: linear-gradient(90deg, #007aff 0%, #409eff 100%);
  }
}

.login-image {
  width: 50%;
  position: relative;
  background: url("../../../public/login-pet-cover.png") center center no-repeat;
  background-color: #2c5a7a;
  background-size: cover;

  .overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0);
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    padding: 40px;

    .slogan {
      color: #fff;
      font-size: 32px;
      font-weight: 600;
      margin-bottom: 20px;
      text-align: center;
    }

    .description {
      color: rgba(255, 255, 255, 0.9);
      font-size: 16px;
      text-align: center;
    }
  }
}

// 响应式设计
@media screen and (max-width: 992px) {
  .login-box {
    width: 100%;
    max-width: 100%;
    flex-direction: column;
    height: auto;
    max-height: 90vh;
    overflow-y: auto;
  }

  .login-form-container,
  .login-image {
    width: 100%;
  }

  .login-image {
    height: 200px;
    order: -1;
  }
}

@media screen and (max-width: 576px) {
  .login-container {
    padding: 0;
    height: 100%;
    background: #fff;
  }

  .login-box {
    width: 100%;
    max-width: 100%;
    height: 100%;
    border-radius: 0;
    box-shadow: none;
  }

  .login-form-container {
    padding: 20px;
    width: 100%;
    box-sizing: border-box;
  }

  .logo-container {
    margin-bottom: 20px;

    .welcome-text {
      font-size: 24px;
    }

    .system-title {
      font-size: 16px;
    }
  }

  .login-form {
    .el-form-item {
      margin-bottom: 15px;
      width: 100%;
    }

    :deep(.el-input) {
      width: 100%;

      .el-input__wrapper {
        width: 100%;
        box-sizing: border-box;
      }
    }
  }

  .login-button {
    height: 45px;
    font-size: 15px;
    width: 100%;
  }

  .form-actions {
    flex-direction: column;
    align-items: flex-start;
    width: 100%;

    .forgot-password {
      margin-top: 8px;
    }
  }
}

// 添加额外的小屏幕适配
@media screen and (max-width: 375px) {
  .login-form-container {
    padding: 15px 10px;
  }

  .login-image {
    height: 150px;
  }
}
</style>
