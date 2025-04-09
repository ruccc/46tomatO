<template>
  <div class="stockpile-container">
    <el-header class="header">
      <div class="logo">番茄书城</div>
      <div class="nav-buttons">
        <el-button type="default" @click="$router.push(`/products/${id}`)">返回详情</el-button>
      </div>
    </el-header>

    <el-main class="main-content">
      <el-card class="stockpile-card" v-loading="loading">
        <template #header>
          <div class="card-header">
            <h2>库存管理</h2>
            <h3 v-if="bookInfo.title">《{{ bookInfo.title }}》</h3>
          </div>
        </template>

        <el-descriptions v-if="!loading && stockInfo" :column="1" border>
          <el-descriptions-item label="可售库存">{{ stockInfo.amount }}</el-descriptions-item>
          <el-descriptions-item label="冻结库存">{{ stockInfo.frozen }}</el-descriptions-item>
          <el-descriptions-item label="总库存">{{ stockInfo.amount + stockInfo.frozen }}</el-descriptions-item>
        </el-descriptions>

        <div v-if="!loading && !stockInfo" class="no-stock">
          <el-empty description="该商品暂无库存记录"></el-empty>
        </div>

        <el-divider>调整库存</el-divider>

        <el-form :model="stockForm" label-width="120px" :rules="rules" ref="stockFormRef">
          <el-form-item label="调整方式" prop="adjustType">
            <el-radio-group v-model="stockForm.adjustType">
              <el-radio :label="'add'">增加库存</el-radio>
              <el-radio :label="'reduce'">减少库存</el-radio>
              <el-radio :label="'set'">设置库存</el-radio>
            </el-radio-group>
          </el-form-item>
          
          <el-form-item label="调整数量" prop="amount">
            <el-input-number 
              v-model="stockForm.amount" 
              :min="0" 
              :max="stockForm.adjustType === 'reduce' ? stockInfo?.amount || 0 : undefined"
            ></el-input-number>
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="submitForm" :disabled="loading">提交调整</el-button>
            <el-button @click="resetForm" :disabled="loading">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </el-main>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { getInfo } from '../../api/Book/products'
import { getStockpileInfo, adjustStockpile } from '../../api/Book/stockpiles'

const route = useRoute()
const router = useRouter()
const id = route.params.id as string
const stockFormRef = ref<FormInstance>()
const loading = ref(true)

// 商品信息
const bookInfo = ref({
  id: '',
  title: '',
})

// 库存信息
const stockInfo = ref<{
  id: string;
  amount: number;
  frozen: number;
  productId: string;
} | null>(null)

// 表单数据
const stockForm = reactive({
  adjustType: 'add', // add, reduce, set
  amount: 0
})

// 表单验证规则
const rules = {
  amount: [
    { required: true, message: '请输入调整数量', trigger: 'blur' },
    { type: 'number', min: 0, message: '数量必须大于等于0', trigger: 'blur' }
  ],
  adjustType: [
    { required: true, message: '请选择调整方式', trigger: 'change' }
  ]
}

// 获取商品信息
const fetchBookInfo = async () => {
  try {
    const res = await getInfo(id)
    if (res.data && res.data.code === 200) {
      bookInfo.value = {
        id: res.data.data.id,
        title: res.data.data.title
      }
    } else {
      ElMessage.error('获取商品信息失败')
      router.push('/products')
    }
  } catch (error) {
    ElMessage.error('获取商品信息失败')
    router.push('/products')
  }
}

// 获取库存信息
const fetchStockInfo = async () => {
  try {
    const res = await getStockpileInfo(id)
    if (res.data && res.data.code === 200) {
      stockInfo.value = res.data.data
    } else if (res.data.code === 400 && res.data.msg === '商品不存在') {
      ElMessage.error('商品不存在')
      router.push('/products')
    } else {
      // 商品可能存在但没有库存记录
      stockInfo.value = null
    }
  } catch (error) {
    // 某些错误可能表示商品存在但没有库存记录
    stockInfo.value = null
  }
}

// 提交表单
const submitForm = async () => {
  if (!stockFormRef.value) return
  
  await stockFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        // 根据调整方式计算实际的调整值
        let adjustedAmount = stockForm.amount
        
        // 如果是减少库存
        if (stockForm.adjustType === 'reduce') {
          adjustedAmount = -adjustedAmount
        } 
        // 如果是设置库存
        else if (stockForm.adjustType === 'set') {
          if (stockInfo.value) {
            // 计算差值
            adjustedAmount = stockForm.amount - stockInfo.value.amount
          }
        }
        
        const res = await adjustStockpile(id, adjustedAmount)
        if (res.data && res.data.code === 200) {
          ElMessage.success('库存调整成功')
          // 重新获取库存信息
          await fetchStockInfo()
          // 重置表单
          stockForm.amount = 0
        } else {
          ElMessage.error(res.data.msg || '库存调整失败')
        }
      } catch (error) {
        ElMessage.error('库存调整失败')
      } finally {
        loading.value = false
      }
    }
  })
}

// 重置表单
const resetForm = () => {
  if (stockFormRef.value) {
    stockFormRef.value.resetFields()
  }
}

// 初始化加载
const initData = async () => {
  loading.value = true
  try {
    await fetchBookInfo()
    await fetchStockInfo()
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  initData()
})
</script>

<style scoped>
.stockpile-container {
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

.stockpile-card {
  max-width: 600px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.card-header h2 {
  margin-bottom: 10px;
}

.card-header h3 {
  margin-top: 0;
  color: #666;
}

.no-stock {
  padding: 20px;
  text-align: center;
}
</style>
