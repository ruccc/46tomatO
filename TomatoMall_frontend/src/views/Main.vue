<template>
  <div class="main-container">
    <!-- 顶部导航 -->
    <el-header class="header">
      <div class="logo">番茄书城</div>
      <div class="nav-buttons">
        <el-button type="primary" @click="$router.push('/products')">书籍列表</el-button>
        <el-button type="primary" @click="$router.push('/dashboard')">个人中心</el-button>
      </div>
    </el-header>

    <!-- 主要内容区 -->
    <el-main class="main-content">
      <!-- 欢迎标语 -->
      <div class="welcome-section">
        <el-carousel height="400px">
          <el-carousel-item>
            <div class="welcome-text">
              <h1>多彩世界，尽在番茄书城</h1>
            </div>
          </el-carousel-item>
        </el-carousel>
      </div>
    </el-main>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const username = ref(sessionStorage.getItem("username") || '')
</script>

<style scoped>
.main-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  background-color: #ff4400;
  padding: 1rem 2rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.logo {
  color: white;
  font-size: 1.5rem;
  font-weight: bold;
}

.nav-buttons {
  display: flex;
  gap: 1rem;
}

.main-content {
  flex: 1;
  padding: 0;
  background-color: #faf3e0; /* 添加米色背景 */
}

.welcome-text {
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: flex-end;
  padding-bottom: 50px; /* 减小底部内边距，让文字更靠下 */
  background: #faf3e0;
}

.welcome-text h1 {
  font-size: 2.5rem;
  color: #ff6b6b;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.08);
  animation: fadeIn 1.5s ease-in-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>