<script setup lang="ts">
import {ref, computed} from 'vue'
import {router} from '../../router'
import {createUser} from "../../api/user.ts"
import {ElMessage} from "element-plus";
//import DEFAULT_AVATAR from "../../assets/tomato@1x-1.0s-200px-200px.svg"

const DEFAULT_AVATAR = "../../assets/tomato@1x-1.0s-200px-200px.svg"

// 输入框值（需要在前端拦截不合法输入：是否为空+额外规则）
const username = ref('')
const password = ref('')
const confirmPassword = ref('')
const name = ref('')
//avatar待完成
const role = ref('')
const telephone = ref('')
const email = ref('')
const location = ref('')


// 各输入项的校验
const hasUsernameInput = computed(() => username.value.trim() !== '')
const hasPasswordInput = computed(() => password.value != '')
const hasConfirmPasswordInput = computed(() => confirmPassword.value != '')
const hasNameInput = computed(() => name.value.trim() !== '')
//avatar待实现
const hasRoleChosen = computed(() => role.value != '')
const hasTelephoneInput = computed(() => telephone.value != '')
const hasEmailInput = computed(() => email.value != '')
const hasLocationInput = computed(() => location.value !== '')

// 电话号码的规则
const chinaMobileRegex = /^1\d{10}$/  // 1XXXXXXXXXX（总11位）
const telLegal = computed(() => chinaMobileRegex.test(telephone.value))
// 邮箱格式的规则
const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/
const emailLegal = computed(() => emailRegex.test(email.value))
// 重复密码的规则
const isPasswordIdentical = computed(() => password.value == confirmPassword.value)

// 注册按钮可用性
const registerDisabled = computed(() => {
  return !(
      hasUsernameInput.value &&
      hasPasswordInput.value &&
      hasConfirmPasswordInput.value &&
      hasNameInput.value &&
      hasRoleChosen.value &&
      hasTelephoneInput.value&&
      hasEmailInput.value &&
      hasLocationInput.value &&

      telLegal.value &&
      emailLegal.value&&
      isPasswordIdentical.value
  )
})

// 注册按钮触发
function handleRegister() {
  createUser({
    username: username.value,
    password: password.value,
    name: name.value,
    role: role.value,
    telephone: telephone.value,
    location: location.value,
    email: email.value,
    avatar:DEFAULT_AVATAR,
  }).then(res => {
    if (res.code === '200') {  // 直接访问res.code（因响应结构为BaseResponse）
      ElMessage.success("注册成功，请登录账号")
      router.push("/login")
    }
    else {
      ElMessage.error(res.msg || "注册失败")
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
        <h1>创建一个新的账户</h1>

        <el-form>
          <el-row>
            <el-col :span="8">
              <el-form-item>
                <label for="username">用户名</label>
                <el-input id="username"
                          v-model="username"
                          placeholder="请输入唯一用户名"/>
              </el-form-item>
            </el-col>

            <el-col :span="1"></el-col>

            <el-col :span="15">
              <el-form-item>
                <label v-if="!hasEmailInput" for="email">
                  电子邮箱
                </label>
                <label v-else-if="!emailLegal && hasEmailInput" for="email" class="error-warn">
                  邮箱格式错误
                </label>
                <label v-else for="email">
                  电子邮箱
                </label>
                <el-input id="email"
                          v-model="email"
                          :class="{'error-warn-input' :(hasEmailInput && !emailLegal)}"
                          placeholder="user@example.com"/>
              </el-form-item>
            </el-col>

            </el-row>
          <el-row>
            <el-col :span="15">
              <el-form-item>
                <label for="name">真实姓名</label>
                <el-input id="name"
                          v-model="name"
                          placeholder="请输入真实姓名"/>
              </el-form-item>
            </el-col>

            <el-col :span="1"></el-col>

            <el-col :span="8">
              <el-form-item>
                <label for="identity">身份</label>
                <el-select id="identity"
                           v-model="role"
                           placeholder="请选择"
                           style="width: 100%;">
                  <el-option value="customer" label="顾客"/>
                  <el-option value="admin" label="管理员"/>
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row>
            <el-col :span="8">
              <el-form-item>
                <!-- 手机号标签：根据输入状态显示不同提示 -->
                <label v-if="!hasTelephoneInput" for="tel">
                  注册手机号
                </label>
                <label v-else-if="!telLegal && hasTelephoneInput" for="tel" class="error-warn">
                  手机号不合法
                </label>
                <label v-else for="tel">
                  注册手机号
                </label>

                <el-input id="tel"
                          v-model="telephone"
                          :class="{'error-warn-input' :(hasTelephoneInput && !telLegal)}"
                          placeholder="请输入手机号"/>
              </el-form-item>
            </el-col>

            <el-col :span="1"></el-col>

            <el-col :span="15">
              <el-form-item>
                <label for="location">地址</label>
                <el-input id="location"
                          v-model="location"
                          placeholder="请输入地址"/>
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item>
            <label for="password">密码</label>
            <el-input type="password" id="password"
                      v-model="password"
                      placeholder="••••••••"/>
          </el-form-item>

          <el-form-item>
            <label v-if="!hasConfirmPasswordInput" for="confirm-password">
              确认密码
            </label>
            <label v-else-if="hasConfirmPasswordInput && !isPasswordIdentical" class="error-warn">
              密码不一致
            </label>
            <label v-else for="confirm-password">
              确认密码
            </label>

            <el-input type="password" id="confirm-password"
                      v-model="confirmPassword"
                      :class="{'error-warn-input' :(hasConfirmPasswordInput && !isPasswordIdentical)}"
                      placeholder="••••••••"/>
          </el-form-item>

          <span class="button-group">
            <el-button @click.prevent="handleRegister"
                       :disabled="registerDisabled"
                       type="primary">
              创建账户
            </el-button>

            <router-link to="/login" v-slot="{navigate}">
              <el-button @click="navigate">
                去登录
              </el-button>
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
