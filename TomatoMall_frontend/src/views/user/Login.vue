<script setup lang="ts">
import {ElForm, ElFormItem, ElMessage} from "element-plus"
import {ref, computed} from 'vue'
import {router} from '../../router'
import {getUserDetail, userLogin} from "../../api/user.ts"

// 输入框值（需要在前端拦截不合法输入：是否为空+额外规则）
const username = ref('')
const password = ref('')

// username 是否为空
const hasUsernameInput = computed(() => username.value.trim() !== '')
const hasPasswordInput = computed(() => password.value.trim() !== '')
// 密码不设置特殊规则
// 登录按钮可用性
const loginDisabled = computed(() => {
  return !(hasUsernameInput.value && hasPasswordInput.value)
})

// 登录按钮触发
function handleLogin() {
  userLogin({
    username: username.value,
    password: password.value,
  }).then(res => {
    if (res.code === '200') {
      ElMessage.success("登录成功！")
      const token = res.data
      localStorage.setItem('token', token)
      localStorage.setItem('username', username.value)

      getUserDetail(username.value, token).then(userRes => {
        localStorage.setItem('name', userRes.data.name)
        localStorage.setItem('role', userRes.data.role)
        router.push({path: "/main"})
      })
    }
    else {
      ElMessage.error(res.msg || "登录失败")
      password.value = ''
    }
  }).catch(error => {
    ElMessage.error(error.message || "请求失败")
  })
}
</script>


<template>
  <el-main class="main-frame bgimage">
    <el-card class="login-card">
      <div>
        <h1>登入您的账户</h1>
        <el-form>
          <el-form-item>
            <label for="username">用户名</label>
            <el-input id="username" type="text" v-model="username"
                      required
                      placeholder="请输入唯一用户名"/>
          </el-form-item>

          <el-form-item>
            <label for="password">账户密码</label>
            <el-input id="password" type="password" v-model="password"
                      required
                      placeholder="••••••••"/>
          </el-form-item>

          <span class="button-group">
              <el-button @click.prevent="handleLogin" :disabled="loginDisabled"
                         type="primary">登录</el-button>
              <router-link to="/register" v-slot="{navigate}">
                <el-button @click="navigate">去注册</el-button>
              </router-link>
          </span>
        </el-form>
      </div>
    </el-card>
  </el-main>
</template>


<style scoped>
.main-frame {
  width: 100%;
  height: 100%;

  display: flex;
  align-items: center;
  justify-content: center;
}

.bgimage {
  background-image: url("../../assets/tomato@1x-1.0s-200px-200px.svg");
}

.login-card {
  width: 60%;
  padding: 10px;
}

.error-warn {
  color: red;
}

.error-warn-input {
  --el-input-focus-border-color: red;
}

.button-group {
  padding-top: 10px;
  display: flex;
  flex-direction: row;
  gap: 30px;
  align-items: center;
  justify-content: right;
}
</style>
