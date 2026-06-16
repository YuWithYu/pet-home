<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-form-container">
        <div class="logo-container">
          <h2 class="welcome-text">注册账号</h2>
          <h3 class="system-title">{{ title }}</h3>
        </div>

        <el-form
          ref="registerFormRef"
          :model="form"
          :rules="rules"
          class="login-form"
        >
          <el-form-item prop="phone">
            <el-input
              v-model="form.phone"
              placeholder="请输入手机号"
              maxlength="11"
              tabindex="1"
              type="text"
            >
              <template #prefix>
                <el-icon><Iphone /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item prop="smsCode">
            <div class="sms-row">
              <el-input
                v-model="form.smsCode"
                placeholder="短信验证码"
                maxlength="6"
                tabindex="2"
                type="text"
              />
              <el-button
                type="primary"
                plain
                :disabled="smsSending || !canSendSms"
                @click="handleSendSms"
              >
                {{ smsSending ? `${smsCountdown}s` : "获取验证码" }}
              </el-button>
            </div>
          </el-form-item>

          <el-form-item prop="nickname">
            <el-input
              v-model="form.nickname"
              placeholder="请输入昵称"
              tabindex="3"
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
              ref="passwordRef"
              v-model="form.password"
              :type="passwordType"
              tabindex="4"
              placeholder="请设置密码（至少6位）"
              @keyup.enter="handleRegister"
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
            <span class="show-pwd" @click="showPwd">
              <el-icon v-if="passwordType === 'password'"><Hide /></el-icon>
              <el-icon v-else><View /></el-icon>
            </span>
          </el-form-item>

          <el-form-item prop="confirmPassword">
            <el-input
              v-model="form.confirmPassword"
              :type="passwordType"
              tabindex="5"
              placeholder="请再次输入密码"
              @keyup.enter="handleRegister"
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <div class="form-actions">
            <router-link class="forgot-password" to="/login">已有账号？去登录</router-link>
          </div>

          <el-button
            :loading="loading"
            type="primary"
            class="login-button"
            @click.prevent="handleRegister"
          >
            注册
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
import { reactive, ref, toRefs, nextTick, computed } from "vue";
import { useRouter } from "vue-router";
import { title } from "@/config";
import { isPassword } from "@/utils/validate";
import { register, sendSmsCode } from "@/api/user";
import { ElMessage } from "element-plus";
import { Hide, View, User, Lock, Iphone } from "@element-plus/icons-vue";

const router = useRouter();
const registerFormRef = ref(null);
const passwordRef = ref(null);
const smsSending = ref(false);
const smsCountdown = ref(0);

const state = reactive({
  form: {
    phone: "",
    smsCode: "",
    nickname: "",
    password: "",
    confirmPassword: "",
  },
  rules: {
    phone: [
      { required: true, trigger: "blur", message: "请输入手机号" },
      {
        validator: (rule, value, callback) => {
          if (!/^\d{11}$/.test((value || "").trim())) {
            callback(new Error("请输入11位手机号"));
          } else {
            callback();
          }
        },
        trigger: "blur",
      },
    ],
    smsCode: [
      { required: true, trigger: "blur", message: "请输入短信验证码" },
      {
        validator: (rule, value, callback) => {
          if (!/^\d{4,6}$/.test((value || "").trim())) {
            callback(new Error("请输入4～6位数字验证码"));
          } else {
            callback();
          }
        },
        trigger: "blur",
      },
    ],
    nickname: [{ required: true, trigger: "blur", message: "请输入昵称" }],
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
    confirmPassword: [
      { required: true, trigger: "blur", message: "请再次输入密码" },
      {
        validator: (rule, value, callback) => {
          if (value !== state.form.password) {
            callback(new Error("两次输入的密码不一致"));
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
});

const showPwd = () => {
  state.passwordType = state.passwordType === "password" ? "" : "password";
  nextTick(() => passwordRef.value?.focus());
};

const canSendSms = computed(() => /^\d{11}$/.test((state.form.phone || "").trim()));

const handleSendSms = async () => {
  const phone = (state.form.phone || "").trim();
  if (!/^\d{11}$/.test(phone)) {
    ElMessage.warning("请先填写11位手机号");
    return;
  }
  if (smsSending.value) return;
  try {
    const res = await sendSmsCode(phone);
    if (res.code === 200 || res.code === 0) {
      ElMessage.success("验证码已发送");
      smsSending.value = true;
      smsCountdown.value = 60;
      const t = setInterval(() => {
        smsCountdown.value--;
        if (smsCountdown.value <= 0) {
          clearInterval(t);
          smsSending.value = false;
        }
      }, 1000);
    } else {
      ElMessage.error(res.msg || "发送失败");
    }
  } catch (e) {
    ElMessage.error(e?.response?.data?.msg || e?.message || "发送失败");
  }
};

const handleRegister = () => {
  registerFormRef.value?.validate(async (valid) => {
    if (!valid) return;
    if (!isPassword(state.form.password)) {
      ElMessage.error("密码长度必须大于等于6位");
      return;
    }
    state.loading = true;
    try {
      const res = await register({
        phone: state.form.phone.trim(),
        password: state.form.password,
        nickname: state.form.nickname.trim(),
        smsCode: state.form.smsCode.trim(),
      });
      if (res.code === 200 || res.code === 0) {
        ElMessage.success(res.msg || "注册成功，请登录");
        router.replace("/login");
      } else {
        ElMessage.error(res.msg || "注册失败");
      }
    } catch (e) {
      ElMessage.error(e?.response?.data?.msg || e?.message || "注册失败");
    } finally {
      state.loading = false;
    }
  });
};

const { form, rules, loading, passwordType } = toRefs(state);
</script>

<style lang="scss" scoped>
.login-container {
  height: 100vh;
  width: 100vw;
  display: flex;
  justify-content: center;
  align-items: center;
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
    margin-bottom: 20px;
    position: relative;
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

  .sms-row {
    display: flex;
    gap: 10px;
    width: 100%;
    align-items: center;
    .el-input {
      flex: 1;
    }
    .el-button {
      flex-shrink: 0;
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

.form-actions {
  display: flex;
  justify-content: flex-end;
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
  background: url("~@/assets/login_images/background.png") center center no-repeat;
  background-color: #2c5a7a;
  background-size: cover;

  .overlay {
    position: absolute;
    inset: 0;
    background: rgba(0, 0, 0, 0.4);
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    padding: 40px;
  }
}

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
</style>
