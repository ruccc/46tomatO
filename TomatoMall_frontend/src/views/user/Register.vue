<script setup lang="ts">
import { ref, computed } from 'vue';
import { router } from '../../router';
import { userRegister } from "../../api/user.ts";
import { ElMessage } from 'element-plus';

// 输入框值
const username = ref('');
const password = ref('');
const confirmPassword = ref('');
const name = ref('');
const avatar = ref('');
const telephone = ref('');
const email = ref('');
const location = ref('');

// 电话号码的规则
const chinaMobileRegex = /^1(3[0-9]|4[579]|5[0-35-9]|6[2567]|7[0-8]|8[0-9]|9[189])\d{8}$/;
const telLegal = computed(() => chinaMobileRegex.test(telephone.value));
// 重复密码的规则
const isPasswordIdentical = computed(() => password.value == confirmPassword.value);
// 注册按钮可用性
const registerDisabled = computed(() => {
  return !(username.value && password.value && name.value &&
      telLegal.value && isPasswordIdentical.value);
});

// 注册按钮触发
function handleRegister() {
  userRegister({
    username: username.value,
    password: password.value,
    name: name.value,
    avatar: avatar.value,
    telephone: telephone.value,
    email: email.value,
    location: location.value,
  }).then(res => {
    if (res.data.code === '200') {
      ElMessage({
        message: "注册成功！请登录账号",
        type: 'success',
        center: true,
      });
      router.push({ path: "/login" });
    } else if (res.data.code === '400') {
      ElMessage({
        message: res.data.msg,
        type: 'error',
        center: true,
      });
    }
  });
}
</script>

<template>
  <el-main class="main-frame bgimage">
    <el-card class="login-card">
      <div>
        <h1>创建一个新的账户</h1>

        <el-form>
          <el-form-item>
            <label for="username">用户名</label>
            <el-input id="username" v-model="username" placeholder="请输入用户名" />
          </el-form-item>

          <el-form-item>
            <label for="password">密码</label>
            <el-input type="password" id="password" v-model="password" placeholder="••••••••" />
          </el-form-item>

          <el-form-item>
            <label v-if="!isPasswordIdentical">确认密码</label>
            <label v-else class="error-warn">密码不一致</label>
            <el-input type="password" id="confirm-password" v-model="confirmPassword"
                      :class="{ 'error-warn-input': !isPasswordIdentical }" placeholder="••••••••" />
          </el-form-item>

          <el-form-item>
            <label for="name">真实姓名</label>
            <el-input id="name" v-model="name" placeholder="请输入真实姓名" />
          </el-form-item>

          <el-form-item>
            <label for="avatar">头像 URL</label>
            <el-input id="avatar" v-model="avatar" placeholder="请输入头像 URL" />
          </el-form-item>

          <el-form-item>
            <label v-if="!telLegal">手机号</label>
            <label v-else class="error-warn">手机号不合法</label>
            <el-input id="telephone" v-model="telephone"
                      :class="{ 'error-warn-input': !telLegal }" placeholder="请输入手机号" />
          </el-form-item>

          <el-form-item>
            <label for="email">邮箱</label>
            <el-input id="email" v-model="email" placeholder="请输入邮箱" />
          </el-form-item>

          <el-form-item>
            <label for="location">位置</label>
            <el-input id="location" v-model="location" placeholder="请输入位置" />
          </el-form-item>

          <span class="button-group">
                        <el-button @click.prevent="handleRegister" :disabled="registerDisabled"
                                   type="primary">创建账户</el-button>

                        <router-link to="/login" v-slot="{ navigate }">
                            <el-button @click="navigate">去登录</el-button>
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
  background-image: url("../../assets/vue.svg");
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