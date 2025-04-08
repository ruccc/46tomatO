<script setup lang="ts">
import {onMounted, ref} from "vue";
import {router} from '../router'
import {parseRole} from "../utils"
import {User, SwitchButton} from "@element-plus/icons-vue"   //图标
import {getUserDetail} from "../api/user.ts";

const role = ref('');    //登录的时候插入的

// 初始化时获取用户角色
onMounted(async () => {
  const token = localStorage.getItem("token");
  const username = localStorage.getItem("username");
  if (token && username) {
    const res = await getUserDetail(username, token);
    role.value = res.data.role;
    localStorage.setItem("role", res.data.role);
  }
});

//退出登录
function logout() {
  localStorage.removeItem("token");
  localStorage.removeItem("username");
  localStorage.removeItem("role");
  router.push({ path: "/login" });
}
</script>


<template>
  <el-header class="custom-header" height="20">
    <el-row :gutter="10">

      <el-col :span="3" class="header-icon">
        <router-link to="/dashboard" v-slot="{navigate}" class="no-link">
          <h1 @click="navigate" class="header-text"> 番茄阅读</h1>
        </router-link>
      </el-col>

      <el-col :span="2">
        <el-tag
            class="role-tag"
            color="white"
            size="large">{{ parseRole(role) }}版</el-tag>
      </el-col>

      <el-col :span="16">
      </el-col>

      <el-col :span="1" class="header-icon">
        <router-link to="/dashboard" v-slot="{navigate}">
          <el-icon @click="navigate" :size="35" color="white" ><User /></el-icon>
        </router-link>
      </el-col>

      <el-col :span="1" class="header-icon">
        <a @click="logout">
          <el-icon :size="35" color="white" ><SwitchButton /></el-icon>
        </a>
      </el-col>
    </el-row>
  </el-header>
</template>


<style scoped>
.custom-header {
  background-color: #FF8989;
  border-bottom-left-radius: 20px;
  border-bottom-right-radius: 20px;

  display: flex;
  flex-direction: column;
}

.no-link {
  text-decoration: none;
}

:deep(.role-tag) {
  color: #fdab9e !important;
  border-color: #fdab9e33;
  background-color: white;
  font-weight: 500;
}
.role-tag {
  margin-top: 20px;
  font-size: 20px;
}

.header-text {
  color:white;
  font-size: x-large;
  min-width: max-content;
  margin-top: 15px;
  margin-bottom: 15px;
}

.header-icon {
  display: flex;
  flex-direction: column;
  align-items:center;
  justify-content: center;
}
</style>

