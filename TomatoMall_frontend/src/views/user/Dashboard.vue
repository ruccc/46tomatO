<script setup lang="ts">
import {ref, computed} from 'vue'
import {getUserDetail, updateUserInfo} from '../../api/user'
import {parseRole} from "../../utils"
import {router} from '../../router'

import {ElMessage, ElMessageBox} from "element-plus";
import axios from 'axios' // 需安装：npm install axios

const role = localStorage.getItem("role") || ''
const username = localStorage.getItem("username") || ''
const token  = localStorage.getItem("token") || ''

// 定义显示左侧个人信息需要的字段
const name = ref('')
const telephone = ref('')
const email = ref('')
const location = ref('')
const avatar = ref('') // 初始值为默认头像

// 定义个人信息修改时用的字段（仅限于个人信息修改，不含密码）
const newName = ref('')
const newAvatarFile = ref<File | null>(null)//新增 newAvatarFile 存储用户选择的图片文件

// 定义密码修改相关字段，仅在密码修改页面使用
const password = ref('')
const confirmPassword = ref('')

/*// 固定头像 URL（所有地方均使用此 URL）
const avatarUrl = "../../assets/tomato@1x-1.0s-200px-200px.svg"*/

// 邮箱格式校验相关
const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/
const emailLegal = computed(() => emailRegex.test(email.value))

// 控制显示的页面：true 显示“修改个人信息”页面，false 显示“修改密码”页面
const displayInfoCard = ref(false)

// 密码校验：当有输入确认密码时判断两次输入是否一致
const hasConfirmPasswordInput = computed(() => confirmPassword.value != '')
const isPasswordIdentical = computed(() => password.value == confirmPassword.value)
//const changeDisabled = computed(() => !(hasConfirmPasswordInput.value && isPasswordIdentical.value))

// 获取用户详细信息，更新左侧显示的个人信息及修改用的初始数据
function getUserInfo() {
  getUserDetail(username, token).then(res => {
    if (res.code ==='200'){
      name.value = res.data.name
      newName.value = res.data.name
      telephone.value = res.data.telephone
      email.value = res.data.email
      location.value = res.data.location
      avatar.value = res.data.avatar || '../../assets/tomato@1x-1.0s-200px-200px.svg' // 从后端获取头像
    }
  }).catch(error => {
    ElMessage.error(error.msg || "获取信息失败")
  })
}

// 修改点 6：新增 handleAvatarChange 处理文件选择和预览
function handleAvatarChange(event: Event) {
  const target = event.target as HTMLInputElement
  if (target.files && target.files[0]) {
    newAvatarFile.value = target.files[0]
    avatar.value = URL.createObjectURL(newAvatarFile.value) // 本地预览
  }
}

// 修改点 7：新增 uploadAvatar 上传头像到后端
async function uploadAvatar() {
  if (!newAvatarFile.value) return avatar.value
  console.log('开始上传头像') // 调试
  const formData = new FormData()
  formData.append('file', newAvatarFile.value)
  try {
    const res = await axios.post('/upload-avatar', formData, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    const newUrl = res.data.code === '200' ? res.data.data.avatarUrl : avatar.value
    console.log('上传成功，返回的头像链接:', newUrl) // 调试
    return newUrl
  } catch (error) {
    console.log('上传失败:', error) // 调试
    return avatar.value // 即使失败也返回当前值，避免卡住
  }
}

// 修改点 8：修改 updateInfo，支持新头像上传
async function updateInfo() {
  console.log('点击更新，开始执行 updateInfo') // 调试
  if (!emailLegal.value) {
    ElMessage.error("邮箱格式不正确")
    return
  }
  const newAvatarUrl = await uploadAvatar()
  console.log('获取到 newAvatarUrl:', newAvatarUrl) // 调试
  const updateData = {
    username: username,
    name: newName.value,
    avatar: newAvatarUrl,
    role: role,
    telephone: telephone.value,
    email: email.value,
    location: location.value,
  }
  console.log('准备发送 updateData:', updateData) // 调试
  try {
    const res = await updateUserInfo(updateData, token)
    console.log('后端响应:', res) // 调试
    if (res.code === '200') {
      ElMessage.success('更新成功')
      avatar.value = newAvatarUrl
      getUserInfo()
    } else {
      ElMessage.error(res.msg || "更新失败")
    }
  } catch (error) {
    ElMessage.error("请求失败")
    console.log('updateUserInfo 错误:', error) // 调试
  }
}

// 修改点 9：调整 updatePassword，使用动态 avatar
function updatePassword() {
  if (!hasConfirmPasswordInput.value || !isPasswordIdentical.value) {
    ElMessage.error("密码不一致或未完整输入")
    return
  }
  const updateData = {
    username: username,
    password: password.value,
    name: name.value,
    avatar: avatar.value, // 使用动态头像
    role: role,
    telephone: telephone.value,
    email: email.value,
    location: location.value,
  }
  updateUserInfo(updateData, token).then(res => {
    if (res.code === '200'){
      ElMessageBox.confirm('成功修改密码，请重新登录', '提示', {
        confirmButtonText: '重新登录',
        cancelButtonText: '取消',
        type: 'success',
      }).then(() => {
        localStorage.clear()
        router.push("/login")
      })
    }
    else {
      ElMessage.error(res.msg || "密码修改失败")
    }
  }).catch(error => {
    ElMessage.error(error.msg || "请求失败")
  })
}


// 初始化时获取用户信息
getUserInfo()
</script>


<template>
  <el-main class="main-container">
    <el-card class="aside-card">
      <div class="avatar-area">
        <!-- 修改点 10：改为动态显示 avatar -->
        <el-avatar :src="avatar" :size="80" /> <!-- 原 :icon="UserFilled" -->
<!--        <el-avatar :src="UserFilled" size="80"></el-avatar>-->
        <span class="avatar-text"> 欢迎您，{{ username }}</span>
      </div>

      <el-divider></el-divider>

      <el-descriptions :column="1" border title="个人信息">
        <template #extra>
          <el-button type="primary" @click="displayInfoCard = !displayInfoCard;" class="custom-primary">
            <span v-if="displayInfoCard">修改密码</span>
            <span v-else>修改个人信息</span>
          </el-button>
        </template>
        <el-descriptions-item label="真实姓名">
          {{ name }}
        </el-descriptions-item>
        <el-descriptions-item label="身份">
          <el-tag>{{ parseRole(role) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="联系电话">
          {{ telephone }}
        </el-descriptions-item>
        <el-descriptions-item label="邮箱">
          {{ email }}
        </el-descriptions-item>
        <el-descriptions-item label="地址">
          {{ location }}
        </el-descriptions-item>

      </el-descriptions>
    </el-card>

    <el-card v-if="displayInfoCard" class="change-card">
      <template #header>
        <div class="card-header">
          <span>个人信息更新</span>
          <el-button type="primary" @click="updateInfo">更新</el-button>
        </div>
      </template>

      <el-form>
        <el-form-item>
          <label>用户名</label>
          <el-input v-model="username" disabled />
        </el-form-item>
        <!-- 修改点 11：添加头像显示和上传控件 -->
        <el-form-item>
          <label>头像</label>
          <el-avatar :src="avatar" :size="50" />
          <input type="file" accept="image/*" @change="handleAvatarChange" />
        </el-form-item>

        <el-form-item>
          <label for="name">真实姓名</label>
          <el-input type="text" id="name" v-model="newName"/>
        </el-form-item>

        <el-form-item>
          <label for="telephone">手机号</label>
          <el-input id="telephone" v-model="telephone"/>
        </el-form-item>

        <el-form-item>
            <label for="email">邮箱</label>
          <el-input id="email" v-model="email"/>
          <!-- 邮箱校验提示 -->
          <span v-if="!emailLegal" class="error-warn">邮箱格式不正确</span>
        </el-form-item>

        <el-form-item>
          <label for="location">地址</label>
          <el-input
              id="location"
              type="textarea"
              rows="4"
              v-model="location"
              placeholder="请输入地址">
          </el-input>
        </el-form-item>

      </el-form>
    </el-card>

    <el-card v-else class="change-card">
      <template #header>
        <div class="card-header">
          <span>修改密码</span>
          <el-button type="primary" @click="updatePassword">修改</el-button>
        </div>
      </template>

      <el-form>
        <el-form-item>
          <label for="password">新密码</label>
          <el-input
              type="password"
              id="password"
              show-password
              v-model="password"
              placeholder="•••••••••" required/>
        </el-form-item>
        <el-form-item>
          <label v-if="!hasConfirmPasswordInput" for="confirm_password">确认密码</label>
          <label v-else-if="!isPasswordIdentical" for="confirm_password" class="error-warn">密码不一致</label>
          <label v-else for="confirm_password">确认密码</label>

          <el-input type="password" id="confirm_password" v-model="confirmPassword"
                    :class="{'error-warn-input' :(hasConfirmPasswordInput && !isPasswordIdentical)}"
                    placeholder="•••••••••" required/>
        </el-form-item>
      </el-form>

    </el-card>
  </el-main>

</template>


<style scoped>

.custom-primary {
  background-color: #FFB200;
  border-color: #FFB200;
  /* 如果需要调整文字颜色 */
}


.error-warn {
  color: red;
}

.error-warn-input {
  --el-input-focus-border-color: red;
}

.main-container {
  display: flex;
  flex-direction: row;
  padding: 15px;
  gap: 5px;
  justify-content: center;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.change-card {
  width: 66%;
}

.avatar-area {
  display: flex;
  justify-content: space-around;
  align-items: center;
  gap: 30px;
}

.avatar-text {
  font-size: x-large;
  font-weight: bolder;
  padding-right: 40px;
}


</style>
