<template>
  <div class="main-container">
    <!-- 顶部导航 -->
    <el-header class="header">
      <div class="logo">番茄书城 - 主页</div>      <div class="nav-buttons">
        <router-link to="/products" class="nav-button">书籍列表</router-link>
        <router-link to="/advertisement/list" class="nav-button">广告列表</router-link>
        <router-link to="/dashboard" class="nav-button">个人中心</router-link>
        <!-- 添加我的订单按钮 -->
        <el-button type="info" @click="$router.push('/orders')">我的订单</el-button>
        <!-- 添加消息中心按钮 -->
        <el-badge :value="unreadMessageCount" :max="99" :hidden="unreadMessageCount === 0">
          <el-button type="primary" @click="$router.push('/messages')">消息中心</el-button>
        </el-badge>
        <el-button type="warning" @click="handleLogout">退出登录</el-button>
      </div>
    </el-header>

    <!-- 主要内容区 -->
    <el-container>
      <el-main class="main-content">
        <router-view v-if="$route.path !== '/main'"></router-view>
        <template v-else>
          <!-- 明显的主页标识 -->
          <div class="page-indicator">
            <h1>这是主页面</h1>
            <p>欢迎您，{{ username }}！</p>
          </div>
          
          <!-- 欢迎标语 -->
          <div class="welcome-section">
            <el-carousel height="400px">
              <el-carousel-item>
                <div class="welcome-text">
                  <h1>多彩世界，尽在番茄书城</h1>
                  <p class="sub-text">当前时间: {{ currentTime }}</p>
                </div>
              </el-carousel-item>
              <el-carousel-item>
                <div class="welcome-text" style="background-color: #ffedcc;">
                  <h1>探索知识，从这里开始</h1>
                  <p class="sub-text">点击"书籍列表"浏览所有书籍</p>
                </div>
              </el-carousel-item>
            </el-carousel>
          </div>
          
          <!-- 功能导航卡片 -->
          <div class="feature-cards">
            <el-card class="feature-card" shadow="hover" @click="$router.push('/products')">
              <div class="card-content">
                <i class="el-icon-collection"></i>
                <h3>浏览书籍</h3>
                <p>查看我们丰富的书籍收藏</p>
              </div>
            </el-card>
              <el-card class="feature-card" shadow="hover" @click="$router.push('/dashboard')">
              <div class="card-content">
                <i class="el-icon-user"></i>
                <h3>个人中心</h3>
                <p>管理您的个人信息和设置</p>
              </div>
            </el-card>
            
            <el-card class="feature-card" shadow="hover" @click="$router.push('/messages')">
              <div class="card-content">
                <i class="el-icon-chat-dot-round"></i>
                <h3>消息中心</h3>
                <p>查看和发送私信消息</p>
                <el-badge v-if="unreadMessageCount > 0" :value="unreadMessageCount" class="message-badge" />
              </div>
            </el-card>
          </div>
        </template>
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'
import { getUnreadCount } from '../api/messages'

const router = useRouter()
const username = ref(localStorage.getItem('username') || '访客')
const currentTime = ref(new Date().toLocaleString())
const unreadMessageCount = ref(0)

// 获取当前用户ID
const getCurrentUserId = () => {
  // 先尝试从userInfo获取
  const userInfo = localStorage.getItem('userInfo')
  if (userInfo) {
    try {
      const parsed = JSON.parse(userInfo)
      if (parsed && parsed.id) {
        return parsed.id
      }
    } catch (e) {
      console.error('解析userInfo失败:', e)
    }
  }
  
  // 再尝试从userId获取
  const userId = localStorage.getItem('userId')
  if (userId && userId !== 'null' && userId !== 'undefined') {
    return parseInt(userId)
  }
  
  // 如果都没有，说明用户未登录
  console.warn('用户未登录，无法获取用户ID')
  return null
}

// 加载未读消息数
const loadUnreadMessageCount = async () => {
  try {
    const userId = getCurrentUserId()
    if (!userId) {
      console.error('无法获取用户ID，无法加载未读消息数')
      return
    }
    const response = await getUnreadCount(userId)
    
    if (response.data && response.data.code === 200) {
      unreadMessageCount.value = response.data.data
    }
  } catch (error) {
    console.error('获取未读消息数失败:', error)
  }
}

// 更新时间的函数
const updateTime = () => {
  currentTime.value = new Date().toLocaleString()
}

// 组件挂载时
onMounted(async () => {
  console.log('=== Main组件调试信息 ===')
  console.log('当前用户:', username.value)
  console.log('当前路由:', router.currentRoute.value.path)
  console.log('localStorage.token:', localStorage.getItem('token'))
  console.log('localStorage.username:', localStorage.getItem('username'))
  console.log('localStorage.userInfo:', localStorage.getItem('userInfo'))
  console.log('localStorage.userId:', localStorage.getItem('userId'))
  console.log('getCurrentUserId():', getCurrentUserId())
  console.log('========================')
  
  // 如果没有token，跳转到登录页
  if (!localStorage.getItem('token')) {
    console.log('未检测到token，跳转至登录页')
    ElMessage.error('您尚未登录，请先登录')
    router.replace('/login')
    return
  }
  
  // 定时更新时间
  setInterval(updateTime, 1000)
  
  // 加载未读消息数
  await loadUnreadMessageCount()
  
  // 定时刷新未读消息数（每30秒）
  setInterval(loadUnreadMessageCount, 30000)
})

const handleLogout = () => {
  // 清除所有用户相关信息
  localStorage.removeItem('token')
  localStorage.removeItem('role')
  localStorage.removeItem('username')
  localStorage.removeItem('name')
  localStorage.removeItem('userId')
  localStorage.removeItem('userInfo')
  sessionStorage.clear()
  
  console.log('已清除所有用户数据')
  
  ElMessage.success('退出登录成功')
  // 跳转到登录页
  router.push('/login')
}
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
  align-items: center;
}

.nav-button {
  padding: 0.5rem 1rem;
  color: white;
  text-decoration: none;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.nav-button:hover {
  background-color: rgba(255, 255, 255, 0.2);
}

.main-content {
  flex: 1;
  padding: 20px;
  background-color: #faf3e0; /* 添加米色背景 */
}

/* 新增明显的页面标识样式 */
.page-indicator {
  background-color: #ff4400;
  color: white;
  padding: 10px 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  text-align: center;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% { box-shadow: 0 0 0 0 rgba(255, 68, 0, 0.7); }
  70% { box-shadow: 0 0 0 10px rgba(255, 68, 0, 0); }
  100% { box-shadow: 0 0 0 0 rgba(255, 68, 0, 0); }
}

.welcome-text {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: #faf3e0;
}

.welcome-text h1 {
  font-size: 2.5rem;
  color: #ff6b6b;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.08);
  animation: fadeIn 1.5s ease-in-out;
}

.sub-text {
  font-size: 1.2rem;
  color: #555;
  margin-top: 10px;
}

/* 功能卡片样式 */
.feature-cards {
  display: flex;
  gap: 20px;
  margin-top: 30px;
}

.feature-card {
  flex: 1;
  cursor: pointer;
  transition: transform 0.3s;
}

.feature-card:hover {
  transform: translateY(-5px);
}

.card-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  position: relative;
}

.card-content i {
  font-size: 2rem;
  color: #ff4400;
  margin-bottom: 15px;
}

.card-content h3 {
  margin: 10px 0;
  color: #333;
}

.card-content p {
  color: #666;
  text-align: center;
}

.message-badge {
  position: absolute;
  top: 10px;
  right: 10px;
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