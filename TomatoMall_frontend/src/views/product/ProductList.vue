<template>
  <div class="product-container">
    <!-- 顶部导航 -->
    <el-header class="header">
      <div class="logo">番茄书城</div>
      <div class="nav-buttons">
        <el-button 
          v-if="role === 'admin'" 
          type="primary" 
          @click="$router.push('/products/create')"
        >
          新建书籍
        </el-button>
      </div>
    </el-header>

    <!-- 主要内容区 -->
    <el-main class="main-content">
      <div class="book-grid">
        <div v-for="book in books" :key="book.id" class="book-item">
          <div class="book-cover">
            <img :src="book.cover || defaultCover" alt="book cover">
            <div class="cover-overlay" @click="goToDetail(book.id)"></div>
          </div>
          <div class="book-title">{{ book.title }}</div>
        </div>
      </div>
    </el-main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getListInfo, type Specification } from '../../api/Book/products'
import defaultCover from '../../assets/tomato@1x-1.0s-200px-200px.svg'

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

const goToDetail = (id: string) => {
  router.push(`/products/${id}`)
}

onMounted(async () => {
  try {
    const res = await getListInfo()
    // 需要检查 res.code 是否为 200
    if (res.data && res.data.code === 200) {
      books.value = res.data.data
    } else {
      ElMessage.error(res.data.msg || '获取书籍列表失败')
    }
  } catch (error) {
    ElMessage.error('获取书籍列表失败')
  }
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
</style>