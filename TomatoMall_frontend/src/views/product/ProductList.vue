<template>
  <div class="product-container">
    <!-- 顶部导航 -->
    <el-header class="header">
      <div class="logo">番茄书城 - 商品列表页</div>
      <div class="nav-buttons">
        <el-button 
          v-if="role === 'admin'" 
          type="primary" 
          @click="$router.push('/products/create')"
        >
          新建书籍
        </el-button>
        <el-button 
          v-if="role === 'admin' && books.length > 0" 
          type="danger" 
          @click="clearAllBooks"
        >
          清空书籍
        </el-button>
        <!-- 添加购物车按钮 -->
        <el-button
          type="primary"
          @click="$router.push('/cart')"
        >
          购物车
        </el-button>
        <!-- 添加我的订单按钮 -->
        <el-button
          type="info"
          @click="$router.push('/orders')"
        >
          我的订单
        </el-button>
        <!-- 添加返回主页按钮 -->
        <el-button
          type="success"
          @click="goToMain"
        >
          返回主页
        </el-button>
        <!-- 添加返回登录界面按钮 -->
        <el-button
          type="warning"
          @click="goToLogin"
        >
          退出登录
        </el-button>
      </div>
    </el-header>

    <!-- 主要内容区 -->
    <el-main class="main-content">
      <!-- 清晰标识这是商品列表页 -->
      <h1 class="page-title">商品列表页面</h1>
      
      <!-- 加载状态显示 -->
      <div v-if="loading" class="loading-state">
        <el-icon class="is-loading"><Loading /></el-icon>
        <p>正在加载商品数据...</p>
      </div>
      
      <!-- 空状态显示 -->
      <div v-else-if="books.length === 0" class="empty-state">
        <img :src="defaultCover" alt="empty state" class="empty-icon">
        <p class="empty-text">暂无书籍，请添加新书籍</p>
        <!-- 移除了重复的添加书籍按钮 -->
      </div>
      
      <!-- 书籍列表 -->
      <div v-else class="book-grid">
        <div v-for="book in books" :key="book.id" class="book-item">
          <div class="book-cover">
            <img :src="book.cover || defaultCover" alt="book cover">
            <div class="cover-overlay" @click="goToDetail(book.id)"></div>
          </div>
          <div class="book-title">{{ book.title }}</div>
          <div class="book-actions" v-if="role === 'admin'">
            <el-button type="danger" size="small" @click="deleteBook(book.id)">删除</el-button>
          </div>
        </div>
      </div>
    </el-main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onActivated } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { getListInfo, deleteInfo, type Specification } from '../../api/Book/products'
import defaultCover from '../../assets/tomato@1x-1.0s-200px-200px.svg'

// 添加loading状态
const loading = ref(true)
const router = useRouter()
// UpdateInfo 是用于更新的类型，不适合用作列表数据类型
// 需要定义一个包含 specifications 的完整类型
interface Product {
    id: string;
    title: string;
    price: number;
    rate: number;
    description?: string;
    cover?: string;
    detail?: string;
    specifications?: Specification[];
}

const books = ref<Product[]>([])
const role = ref(localStorage.getItem('role') || '')

// 在组件挂载时输出一些调试信息
console.log('ProductList组件已挂载')
console.log('当前角色:', role.value)
console.log('当前路由:', router.currentRoute.value.path)

const goToDetail = (id: string) => {
  router.push(`/products/${id}`)
}

const goToLogin = () => {
  // 清除登录状态
  localStorage.removeItem('token')
  localStorage.removeItem('role')
  localStorage.removeItem('username')
  
  // 提示用户已登出
  ElMessage.success('已退出登录，正在返回登录界面')
  
  // 跳转到登录页
  router.replace('/login')
}

const goToMain = () => {
  // 跳转到主页
  router.push('/main')
}

const deleteBook = async (id: string) => {
  // 检查ID是否有效
  if (!id || id === 'undefined') {
    ElMessage.error('书籍ID无效，无法删除')
    return
  }

  try {
    await ElMessageBox.confirm('确定要删除这本书吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await deleteInfo(id)
    if (res.data && res.data.code === 200) {
      ElMessage.success('删除成功')
      books.value = books.value.filter(book => book.id !== id)
    } else {
      ElMessage.error(res.data?.msg || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除操作发生错误')
      console.error('删除书籍时发生错误:', error)
    }
  }
}

const clearAllBooks = async () => {
  try {
    await ElMessageBox.confirm('确定要清空所有书籍吗？此操作不可逆！', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 过滤掉没有有效ID的书籍
    const validBooks = books.value.filter(book => book.id && book.id !== 'undefined')
    
    if (validBooks.length === 0) {
      ElMessage.warning('没有有效的书籍可以删除')
      return
    }
    
    // 逐个删除所有书籍
    const deletePromises = validBooks.map(book => {
      console.log('正在删除书籍:', book.id)
      return deleteInfo(book.id)
    })
    
    // 等待所有删除操作完成
    const results = await Promise.allSettled(deletePromises)
    
    // 检查删除结果
    const successCount = results.filter(r => r.status === 'fulfilled').length
    
    // 重新获取书籍列表或清空本地列表
    if (successCount > 0) {
      ElMessage.success(`成功删除了${successCount}本书籍`)
      books.value = []
      // 或者重新获取列表
      // await fetchBooks()
    } else {
      ElMessage.error('删除操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('清空操作发生错误')
      console.error('清空书籍时发生错误:', error)
    }
  }
}

const fetchBooks = async () => {
  loading.value = true
  console.log('开始获取商品列表数据')
  
  // 检查用户是否已登录
  const token = localStorage.getItem('token')
  if (!token) {
    console.log('未检测到token，跳转至登录页')
    ElMessage.error('您尚未登录，请先登录')
    // 使用更可靠的跳转方式
    router.replace({ path: '/login', query: { redirect: router.currentRoute.value.fullPath } })
    setTimeout(() => {
      if (router.currentRoute.value.path !== '/login') {
        window.location.href = '/login'
      }
    }, 100)
    return
  }
  
  try {
    console.log('发起API请求获取商品列表')
    const res = await getListInfo()
    console.log('API返回数据:', res.data) // 打印完整的API返回数据，用于调试
    
    if (res.data && res.data.code === 200) {
      // 检查数据结构，分页数据可能在 res.data.data.content 中
      let booksData = res.data.data
      
      // 处理分页数据结构
      if (booksData && typeof booksData === 'object') {
        if (booksData.content && Array.isArray(booksData.content)) {
          // 如果是分页格式，数据在content属性中
          booksData = booksData.content
        }
      }
      
      // 确保books是数组
      if (Array.isArray(booksData)) {
        // 过滤掉没有有效ID的书籍
        books.value = booksData.filter(book => book && book.id && book.id !== 'undefined')
        console.log('处理后的书籍列表:', books.value)
      } else {
        console.error('API返回的数据不是有效数组:', booksData)
        books.value = []
      }
    } else if (res.data && res.data.code === 401) {
      // 处理未登录情况
      console.log('API返回401未授权错误')
      ElMessage.error('登录已过期，请重新登录')
      localStorage.removeItem('token')
      localStorage.removeItem('role')
      localStorage.removeItem('username')
      router.replace({ path: '/login', query: { redirect: router.currentRoute.value.fullPath } })
    } else {
      console.error('获取书籍列表API返回错误:', res.data)
      ElMessage.error(res.data?.msg || '获取书籍列表失败')
    }
  } catch (error) {
    console.error('获取书籍列表时发生异常:', error)
    // 检查是否为401错误
    if (error.response && error.response.status === 401) {
      ElMessage.error('登录已过期，请重新登录')
      localStorage.removeItem('token')
      localStorage.removeItem('role')
      localStorage.removeItem('username')
      router.replace({ path: '/login', query: { redirect: router.currentRoute.value.fullPath } })
    } else {
      ElMessage.error('获取书籍列表失败')
      console.error('获取书籍列表时发生错误:', error)
    }
  } finally {
    loading.value = false
    console.log('书籍列表数据加载完成')
  }
}

// 组件挂载时加载数据
onMounted(() => {
  console.log('ProductList组件onMounted触发')
  fetchBooks()
})

// 添加 onActivated 生命周期钩子，确保每次进入页面时都重新获取数据
onActivated(() => {
  console.log('ProductList 页面被激活，重新获取数据')
  fetchBooks()
})
</script>

<style scoped>
.product-container {
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

.main-content {
  flex: 1;
  padding: 20px;
  background-color: #faf3e0;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 70vh;
}

.empty-icon {
  width: 120px;
  height: 120px;
  margin-bottom: 20px;
  opacity: 0.5;
}

.empty-text {
  font-size: 1.2rem;
  color: #999;
  margin-bottom: 20px;
}

.book-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
  padding: 20px;
}

.book-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 10px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  transition: transform 0.2s;
}

.book-item:hover {
  transform: translateY(-5px);
}

.book-cover {
  position: relative;
  width: 160px;
  height: 220px;
  margin-bottom: 10px;
}

.book-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 4px;
}

.cover-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  cursor: pointer;
  transition: background-color 0.2s;
}

.cover-overlay:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

.book-title {
  font-size: 1rem;
  color: #333;
  text-align: center;
  margin-top: 8px;
  max-width: 160px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.book-actions {
  margin-top: 10px;
}

/* 添加一些明显的样式区别 */
.page-title {
  color: #ff4400;
  text-align: center;
  margin-bottom: 20px;
  font-size: 2rem;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 50vh;
  font-size: 1.5rem;
}

.loading-state .el-icon {
  font-size: 3rem;
  margin-bottom: 20px;
  color: #ff4400;
}
</style>