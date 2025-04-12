<template>
  <div class="detail-container">
    <el-header class="header">
      <div class="logo">番茄书城</div>
      <div class="nav-buttons">
        <el-button type="default" @click="$router.push('/products')">返回列表</el-button>
      </div>
    </el-header>

    <el-main class="main-content">
      <el-card class="book-detail">
        <div class="book-layout">
          <!-- 左侧封面 -->
          <div class="book-cover">
            <img :src="bookInfo.cover || defaultCover" alt="book cover">
          </div>

          <!-- 右侧信息 -->
          <div class="book-info">
            <h2>{{ bookInfo.title }}</h2>
            <el-descriptions :column="1" border>
              <el-descriptions-item label="价格">¥{{ bookInfo.price }}</el-descriptions-item>
              <el-descriptions-item label="评分">{{ bookInfo.rate }}</el-descriptions-item>
              <el-descriptions-item label="库存">
                <span v-if="stockInfo">{{ stockInfo.amount }}</span>
                <span v-else>暂无库存信息</span>
              </el-descriptions-item>
              <el-descriptions-item label="描述">{{ bookInfo.description }}</el-descriptions-item>
              <el-descriptions-item label="详细信息">{{ bookInfo.detail }}</el-descriptions-item>
              
              <!-- 规格信息显示 -->
              <el-descriptions-item label="规格信息">
                <div v-if="bookInfo.specifications && bookInfo.specifications.length > 0">
                  <el-tag 
                    v-for="(spec, index) in bookInfo.specifications" 
                    :key="index"
                    class="spec-tag"
                    type="info"
                  >
                    {{ spec.item }}: {{ spec.value }}
                  </el-tag>
                </div>
                <span v-else>暂无规格信息</span>
              </el-descriptions-item>
            </el-descriptions>

            <!-- 所有用户可见的操作按钮 -->
            <div class="user-actions">
              <!-- 添加数量选择器 -->
              <el-input-number 
                v-model="quantity" 
                :min="1" 
                :max="stockInfo ? stockInfo.amount : 1"
                size="small"
                :disabled="!stockInfo || stockInfo.amount <= 0"
              />
              <el-button type="primary" @click="handleAddToCart" :disabled="!stockInfo || stockInfo.amount <= 0">
                加入购物车
              </el-button>
            </div>

            <!-- 管理员操作按钮 -->
            <div v-if="role === 'admin'" class="admin-actions">
              <el-button type="primary" @click="$router.push(`/products/${id}/stockpile`)">
                调整库存
              </el-button>
              <el-button type="warning" @click="$router.push(`/products/${id}/update`)">
                更新信息
              </el-button>
              <el-button type="danger" @click="handleDelete">
                删除商品
              </el-button>
            </div>
          </div>
        </div>
      </el-card>
    </el-main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getInfo, deleteInfo } from '../../api/Book/products'
import { getStockpileInfo } from '../../api/Book/stockpiles'
import { addToCart as cartApi } from '../../api/Book/cart'
import defaultCover from '../../assets/tomato@1x-1.0s-200px-200px.svg'

const route = useRoute()
const router = useRouter()
const id = route.params.id as string
const role = ref(localStorage.getItem('role') || '')
const bookInfo = ref({
  id: '',
  title: '',
  price: 0,
  rate: 0,
  description: '',
  cover: '',
  detail: '',
  stockpile: 0,
  specifications: [] as { item: string, value: string }[]
})
const stockInfo = ref<{ id: string; amount: number; frozen: number; productId: string; } | null>(null)
const quantity = ref(1) // 购买数量，默认为1

// 获取书籍详情
const fetchBookInfo = async () => {
  try {
    const res = await getInfo(id)
    if (res.data && res.data.code === 200) {
      bookInfo.value = res.data.data
    } else {
      ElMessage.error('获取书籍信息失败')
    }
  } catch (error) {
    ElMessage.error('获取书籍信息失败')
  }
}

// 获取库存信息
const fetchStockInfo = async () => {
  try {
    const res = await getStockpileInfo(id)
    if (res.data && res.data.code === 200) {
      stockInfo.value = res.data.data
    } else {
      stockInfo.value = null
    }
  } catch (error) {
    stockInfo.value = null
  }
}

// 删除书籍
const handleDelete = () => {
  ElMessageBox.confirm('确定要删除这本书吗？此操作不可恢复', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteInfo(id)
      if (res.data && res.data.code === 200) {
        ElMessage.success('删除成功')
        router.push('/products')
      } else {
        ElMessage.error(res.data.msg || '删除失败')
      }
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {
    ElMessage.info('已取消删除')
  })
}

// 添加到购物车（重命名以避免冲突）
const handleAddToCart = async () => {
  try {
    if (!stockInfo.value || stockInfo.value.amount <= 0) {
      ElMessage.warning('商品库存不足，无法加入购物车')
      return
    }
    
    if (quantity.value > stockInfo.value.amount) {
      ElMessage.warning(`当前可用库存仅${stockInfo.value.amount}，请减少购买数量`)
      return
    }

    const res = await cartApi(id, quantity.value)
    if (res.data && res.data.code === 200) {
      ElMessage.success(`成功将《${bookInfo.value.title}》添加到购物车`)
    } else {
      ElMessage.error(res.data?.msg || '添加到购物车失败')
    }
  } catch (error) {
    console.error('添加到购物车时发生错误:', error)
    ElMessage.error('添加到购物车失败')
  }
}

onMounted(() => {
  fetchBookInfo()
  fetchStockInfo()
})
</script>

<style scoped>
.detail-container {
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
  gap: 10px;
}

.main-content {
  flex: 1;
  padding: 20px;
  background-color: #faf3e0;
}

.book-detail {
  max-width: 1200px;
  margin: 0 auto;
}

.book-layout {
  display: flex;
  gap: 40px;
  padding: 20px;
}

.book-cover {
  flex-shrink: 0;
  width: 300px;
  height: 400px;
}

.book-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 8px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.book-info {
  flex: 1;
}

.book-info h2 {
  margin-bottom: 20px;
  color: #333;
}

.admin-actions {
  margin-top: 20px;
  display: flex;
  gap: 10px;
}

.user-actions {
  margin-top: 20px;
  display: flex;
  gap: 15px;
  align-items: center;
}

.spec-tag {
  margin-right: 5px;
}
</style>