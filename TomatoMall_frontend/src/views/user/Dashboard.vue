<script setup lang="ts">
import { ref, computed } from 'vue';
import { userInfo, userInfoUpdate } from '../../api/user.ts';
import { router } from '../../router';
import { UserFilled } from "@element-plus/icons-vue";
import { ElMessage, ElMessageBox } from 'element-plus';

const username = ref(''); // 添加 username
const name = ref('');
const telephone = ref(''); // 修改为 telephone
const email = ref(''); // 添加 email
const location = ref(''); // 修改为 location
const avatar = ref(''); // 添加 avatar

const newName = ref('');
const newLocation = ref('');

const displayInfoCard = ref(false);

const password = ref('');
const confirmPassword = ref('');

const hasConfirmPasswordInput = computed(() => confirmPassword.value != '');
const isPasswordIdentical = computed(() => password.value == confirmPassword.value);
const changeDisabled = computed(() => {
  return !(hasConfirmPasswordInput.value && isPasswordIdentical.value);
});

getUserInfo();

function getUserInfo() {
  // 获取 username
  username.value = sessionStorage.getItem('username') || '';

  userInfo().then(res => {
    if (res.data.code === '200') {
      name.value = res.data.data.name;
      telephone.value = res.data.data.telephone;
      email.value = res.data.data.email;
      location.value = res.data.data.location;
      avatar.value = res.data.data.avatar;

      newName.value = name.value;
      newLocation.value = location.value;
    } else {
      ElMessage.error(res.data.msg);
    }
  });
}

function updateInfo() {
  userInfoUpdate({
    username: username.value,
    name: newName.value,
    location: newLocation.value,
    avatar: avatar.value,
    telephone: telephone.value,
    email: email.value
  }).then(res => {
    if (res.data.code === '200') {
      ElMessage.success('更新成功！');
      getUserInfo();
    } else {
      ElMessage.error(res.data.msg);
    }
  });
}

function updatePassword() {
  userInfoUpdate({
    username: username.value,
    password: password.value,
  }).then(res => {
    if (res.data.code === '200') {
      password.value = '';
      confirmPassword.value = '';
      ElMessageBox.alert(
          `请重新登录`,
          '修改成功',
          {
            confirmButtonText: '跳转到登录',
            type: "success",
          }).then(() => router.push({ path: "/login" }));
    } else {
      ElMessage.error(res.data.msg);
      password.value = '';
      confirmPassword.value = '';
    }
  });
}
</script>

<template>
  <el-main class="main-container">
    <el-card class="aside-card">
      <div class="avatar-area">
        <el-avatar :icon="UserFilled" :size="80"></el-avatar>
        <span class="avatar-text"> 欢迎您，{{ name }}</span>
      </div>

      <el-divider></el-divider>

      <el-descriptions :column="1" border title="个人信息">
        <template #extra>
          <el-button type="primary" @click="displayInfoCard = !displayInfoCard;">
            <span v-if="displayInfoCard">修改密码</span>
            <span v-else>修改个人信息</span>
          </el-button>
        </template>

        <el-descriptions-item label="联系电话">
          {{ telephone }}
        </el-descriptions-item>

        <el-descriptions-item label="地址">
          {{ location }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card v-if="displayInfoCard" class="change-card">
      <template #header>
        <div class="card-header">
          <span>个人信息</span>
          <el-button @click="updateInfo">更新</el-button>
        </div>
      </template>

      <el-form>
        <el-form-item label="昵称">
          <el-input v-model="newName" />
        </el-form-item>

        <el-form-item label="手机号">
          <el-input v-model="telephone" disabled />
        </el-form-item>

        <el-form-item label="收货地址">
          <el-input type="textarea" rows="4" v-model="newLocation" placeholder="中华门" />
        </el-form-item>
      </el-form>
    </el-card>

    <el-card v-else class="change-card">
      <template #header>
        <div class="card-header">
          <span>修改密码</span>
          <el-button @click="updatePassword" :disabled="changeDisabled">修改</el-button>
        </div>
      </template>

      <el-form>
        <el-form-item label="密码">
          <el-input type="password" v-model="password" placeholder="•••••••••" />
        </el-form-item>
        <el-form-item>
          <label v-if="!hasConfirmPasswordInput">确认密码</label>
          <label v-else-if="!isPasswordIdentical" class="error-warn">密码不一致</label>
          <label v-else>确认密码</label>
          <el-input type="password" v-model="confirmPassword"
                    :class="{ 'error-warn-input': !isPasswordIdentical }" placeholder="•••••••••" />
        </el-form-item>
      </el-form>
    </el-card>
  </el-main>
</template>

<style scoped>
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