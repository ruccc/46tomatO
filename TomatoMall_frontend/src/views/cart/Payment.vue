<template>
  <div class="payment-container">
    <el-header class="header">
      <div class="logo">番茄书城 - 订单支付</div>
      <div class="nav-buttons">
        <el-button type="default" @click="$router.push('/orders')">查看订单</el-button>
      </div>
    </el-header>

    <el-main class="main-content">
      <el-card class="payment-card" v-loading="loading">
        <template #header>
          <div class="card-header">
            <h2>订单支付</h2>
          </div>
        </template>

        <!-- 支付信息 -->
        <div v-if="!loading && orderInfo" class="payment-info">
          <div class="payment-summary">
            <div class="info-row">
              <span class="info-label">订单号：</span>
              <span class="info-value">{{ orderInfo.orderId }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">订单金额：</span>
              <span class="info-value price">¥{{ orderInfo.totalAmount.toFixed(2) }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">支付方式：</span>
              <span class="info-value">{{ paymentMethodLabel }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">订单状态：</span>
              <span :class="'status-' + orderInfo.status.toLowerCase()">{{ orderStatusLabel }}</span>
            </div>
          </div>

          <!-- 支付按钮 -->
          <div class="payment-actions" v-if="orderInfo.status === 'PENDING'">
            <el-button type="primary" @click="pay" :loading="paying">去支付</el-button>
            <el-button @click="$router.push('/products')">继续购物</el-button>
          </div>

          <!-- 支付成功提示 -->
          <div class="payment-result" v-else-if="orderInfo.status === 'SUCCESS'">
            <el-result
              icon="success"
              title="支付成功"
              sub-title="您的订单已支付成功"
            >
              <template #extra>
                <div class="result-buttons">
                  <el-button type="primary" @click="$router.push('/orders')">查看订单</el-button>
                  <el-button @click="$router.push('/products')">继续购物</el-button>
                </div>
              </template>
            </el-result>
          </div>

          <!-- 支付失败提示 -->
          <div class="payment-result" v-else>
            <el-result
              icon="error"
              title="支付失败"
              sub-title="订单支付出现问题，请稍后重试"
            >
              <template #extra>
                <div class="result-buttons">
                  <el-button type="primary" @click="pay">重新支付</el-button>
                  <el-button @click="$router.push('/orders')">查看订单</el-button>
                </div>
              </template>
            </el-result>
          </div>
        </div>

        <!-- 支付表单显示区域 -->
        <div v-if="paymentForm" class="payment-form-container">
          <h3>请完成支付</h3>
          <div v-html="paymentForm" class="payment-form"></div>
        </div>

        <!-- 订单不存在 -->
        <div v-if="!loading && !orderInfo" class="empty-payment">
          <el-empty description="订单不存在或已被取消" />
          <el-button type="primary" @click="$router.push('/cart')">返回购物车</el-button>
        </div>
      </el-card>
    </el-main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getOrderDetail, initPayment } from '../../api/Book/orders'
import type { Order, PaymentInfo } from '../../api/Book/orders' // 修改为仅类型导入

const route = useRoute()
const router = useRouter() // router变量确实被使用了，在goToOrders和其他函数中
const orderId = route.params.orderId as string
const loading = ref(true)
const paying = ref(false)
const orderInfo = ref<Order | null>(null)
const paymentForm = ref<string | null>(null)

// 格式化支付方式
const paymentMethodLabel = computed(() => {
  if (!orderInfo.value) return ''
  switch (orderInfo.value.paymentMethod) {
    case 'ALIPAY':
      return '支付宝'
    default:
      return orderInfo.value.paymentMethod
  }
})

// 格式化订单状态
const orderStatusLabel = computed(() => {
  if (!orderInfo.value) return ''
  switch (orderInfo.value.status) {
    case 'PENDING':
      return '待支付'
    case 'SUCCESS':
      return '支付成功'
    case 'FAILED':
      return '支付失败'
    case 'TIMEOUT':
      return '支付超时'
    default:
      return orderInfo.value.status
  }
})

// 获取订单信息
const fetchOrderInfo = async () => {
  loading.value = true
  try {
    const res = await getOrderDetail(orderId)
    if (res.data && res.data.code === 200) {
      orderInfo.value = res.data.data
    } else {
      ElMessage.error(res.data?.msg || '获取订单信息失败')
      orderInfo.value = null
    }
  } catch (error) {
    console.error('获取订单信息时发生错误:', error)
    ElMessage.error('获取订单信息失败')
    orderInfo.value = null
  } finally {
    loading.value = false
  }
}

// 发起支付
const pay = async () => {
  if (!orderInfo.value) {
    ElMessage.error('订单信息不存在')
    return
  }

  paying.value = true
  try {
    const res = await initPayment(orderId)
    if (res.data && res.data.code === 200) {
      const paymentInfo = res.data.data as PaymentInfo
      paymentForm.value = paymentInfo.paymentForm
      // 设置自动刷新，检查支付状态（实际项目中可以使用轮询或WebSocket）
      setTimeout(() => {
        fetchOrderInfo()
      }, 30000) // 30秒后刷新订单状态
    } else {
      ElMessage.error(res.data?.msg || '发起支付失败')
    }
  } catch (error) {
    console.error('发起支付时发生错误:', error)
    ElMessage.error('发起支付失败')
  } finally {
    paying.value = false
  }
}

onMounted(() => {
  fetchOrderInfo()
})
</script>

<style scoped>
.payment-container {
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
  background-color: #f5f5f5;
}

.payment-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.payment-info {
  padding: 20px;
}

.payment-summary {
  background-color: #f9f9f9;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 30px;
}

.info-row {
  display: flex;
  margin-bottom: 15px;
}

.info-label {
  width: 100px;
  color: #666;
}

.info-value {
  font-weight: 500;
  color: #333;
}

.price {
  color: #ff4400;
  font-size: 18px;
  font-weight: bold;
}

.payment-actions {
  text-align: center;
  margin-top: 30px;
}

.empty-payment {
  padding: 40px 0;
  text-align: center;
}

.payment-form-container {
  margin-top: 20px;
  padding: 20px;
  background-color: #fff;
  border-radius: 8px;
  border: 1px solid #eee;
}

.payment-form {
  margin-top: 15px;
}

.payment-form :deep(form) {
  text-align: center;
}

.payment-result {
  margin-top: 30px;
}

.result-buttons {
  display: flex;
  justify-content: center;
  gap: 10px;
}

.status-pending {
  color: #e6a23c;
}

.status-success {
  color: #67c23a;
}

.status-failed, .status-timeout {
  color: #f56c6c;
}
</style>