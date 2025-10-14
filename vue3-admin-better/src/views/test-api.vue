<template>
  <div class="test-api-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>API连接测试</span>
        </div>
      </template>
      
      <div class="test-section">
        <h3>后端连接测试</h3>
        <el-button type="primary" @click="testConnection" :loading="testing">
          测试后端连接
        </el-button>
        <div v-if="connectionResult" class="result">
          <el-alert 
            :title="connectionResult.message" 
            :type="connectionResult.success ? 'success' : 'error'"
            show-icon
          />
        </div>
      </div>

      <div class="test-section">
        <h3>用户登录测试</h3>
        <el-form :model="loginForm" label-width="80px">
          <el-form-item label="用户名">
            <el-input v-model="loginForm.username" placeholder="请输入用户名"></el-input>
          </el-form-item>
          <el-form-item label="密码">
            <el-input v-model="loginForm.password" type="password" placeholder="请输入密码"></el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="testLogin" :loading="loginTesting">
              测试登录
            </el-button>
          </el-form-item>
        </el-form>
        <div v-if="loginResult" class="result">
          <el-alert 
            :title="loginResult.message" 
            :type="loginResult.success ? 'success' : 'error'"
            show-icon
          />
          <div v-if="loginResult.success && loginResult.data" class="login-data">
            <p><strong>用户ID:</strong> {{ loginResult.data.userId }}</p>
            <p><strong>用户名:</strong> {{ loginResult.data.username }}</p>
            <p><strong>昵称:</strong> {{ loginResult.data.nickname }}</p>
            <p><strong>角色:</strong> {{ loginResult.data.role }}</p>
            <p><strong>Token:</strong> {{ loginResult.data.token.substring(0, 50) }}...</p>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import { login } from "@/api/user";
import request from "@/utils/request";

export default {
  name: "TestApi",
  data() {
    return {
      testing: false,
      loginTesting: false,
      connectionResult: null,
      loginResult: null,
      loginForm: {
        username: "",
        password: ""
      }
    };
  },
  methods: {
    async testConnection() {
      this.testing = true;
      this.connectionResult = null;
      
      try {
        // 测试一个简单的API调用
        const response = await request({
          url: "/api/users/check/username/test",
          method: "get"
        });
        
        this.connectionResult = {
          success: true,
          message: `连接成功！后端响应: ${JSON.stringify(response)}`
        };
      } catch (error) {
        console.error("连接测试失败:", error);
        this.connectionResult = {
          success: false,
          message: `连接失败: ${error.message || error}`
        };
      } finally {
        this.testing = false;
      }
    },
    
    async testLogin() {
      if (!this.loginForm.username || !this.loginForm.password) {
        this.$message.warning("请输入用户名和密码");
        return;
      }
      
      this.loginTesting = true;
      this.loginResult = null;
      
      try {
        const response = await login(this.loginForm);
        console.log("登录响应:", response);
        
        if (response.code === 200) {
          this.loginResult = {
            success: true,
            message: "登录成功！",
            data: response.data
          };
        } else {
          this.loginResult = {
            success: false,
            message: `登录失败: ${response.msg}`
          };
        }
      } catch (error) {
        console.error("登录测试失败:", error);
        this.loginResult = {
          success: false,
          message: `登录失败: ${error.message || error}`
        };
      } finally {
        this.loginTesting = false;
      }
    }
  }
};
</script>

<style scoped>
.test-api-container {
  padding: 20px;
}

.box-card {
  max-width: 800px;
  margin: 0 auto;
}

.test-section {
  margin-bottom: 30px;
}

.test-section h3 {
  margin-bottom: 15px;
  color: #409eff;
}

.result {
  margin-top: 15px;
}

.login-data {
  margin-top: 15px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.login-data p {
  margin: 5px 0;
  font-size: 14px;
}
</style>
