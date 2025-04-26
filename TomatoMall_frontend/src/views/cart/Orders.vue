<template>
  <div class="orders-container">
    <el-header class="header">
      <div class="logo">番茄书城 - 我的订单</div>
      <div class="nav-buttons">
        <el-button type="default" @click="$router.push('/products')">继续购物</el-button>
        <el-button type="default" @click="$router.push('/cart')">查看购物车</el-button>
      </div>
    </el-header>

    <el-main class="main-content">
      <el-card class="orders-card" v-loading="loading">
        <template #header>
          <div class="card-header">
            <h2>订单列表</h2>
            <el-button size="small" @click="fetchOrders">
              刷新
            </el-button>
          </div>
        </template>

        <!-- 订单为空 -->
        <div v-if="!loading && orders.length === 0" class="empty-orders">
          <el-empty description="暂无订单记录">
            <el-button type="primary" @click="$router.push('/products')">去购物</el-button>
          </el-empty>
        </div>

        <!-- 订单列表 -->
        <div v-else-if="!loading" class="orders-list">
          <el-collapse accordion>
            <el-collapse-item v-for="order in orders" :key="order.orderId">
              <template #title>
                <div class="order-header">
                  <div class="order-id">订单号: {{ order.orderId }}</div>
                  <div class="order-info">
                    <span class="order-amount">¥{{ order.totalAmount.toFixed(2) }}</span>
                    <span :class="'order-status status-' + order.status.toLowerCase()">{{ getOrderStatusLabel(order.status) }}</span>
                    <span class="order-time">{{ formatTime(order.createTime) }}</span>
                  </div>
                </div>
              </template>
              
              <div class="order-detail">
                <div class="order-section">
                  <div class="section-title">订单信息</div>
                  <div class="info-item">
                    <span class="info-label">订单号：</span>
                    <span class="info-value">{{ order.orderId }}</span>
                  </div>
                  <div class="info-item">
                    <span class="info-label">创建时间：</span>
                    <span class="info-value">{{ formatTime(order.createTime) }}</span>
                  </div>
                  <div class="info-item">
                    <span class="info-label">支付方式：</span>
                    <span class="info-value">{{ getPaymentMethodLabel(order.paymentMethod) }}</span>
                  </div>
                  <div class="info-item">
                    <span class="info-label">订单状态：</span>
                    <span :class="'info-value status-' + order.status.toLowerCase()">{{ getOrderStatusLabel(order.status) }}</span>
                  </div>
                  <div class="info-item">
                    <span class="info-label">订单金额：</span>
                    <span class="info-value price">¥{{ order.totalAmount.toFixed(2) }}</span>
                  </div>
                </div>

                <!-- 订单操作 -->
                <div class="order-actions">
                  <el-button 
                    v-if="order.status === 'PENDING'" 
                    type="primary" 
                    @click="goToPay(order.orderId)"
                  >
                    去支付
                  </el-button>
                  <el-button 
                    v-else-if="order.status === 'SUCCESS'" 
                    type="success"
                    disabled
                  >
                    已完成
                  </el-button>
                  <el-button 
                    v-else 
                    type="danger"
                    disabled
                  >
                    支付失败
                  </el-button>
                </div>
              </div>
            </el-collapse-item>
          </el-collapse>
        </div>
      </el-card>
    </el-main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getOrders } from '../../api/Book/orders'
import type { Order } from '../../api/Book/orders' // 修改为仅类型导入

const router = useRouter()
const loading = ref(true)
const orders = ref<Order[]>([])

// 格式化时间
const formatTime = (timeStr: string) => {
  if (!timeStr) return ''
  try {
    const date = new Date(timeStr)
    return date.toLocaleString()
  } catch (error) {
    return timeStr
  }
}

// 获取订单状态标签
const getOrderStatusLabel = (status: string) => {
  switch (status) {
    case 'PENDING':
      return '待支付'
    case 'SUCCESS':
      return '支付成功'
    case 'FAILED':
      return '支付失败'
    case 'TIMEOUT':
      return '支付超时'
    default:
      return status
  }
}

// 获取支付方式标签
const getPaymentMethodLabel = (method: string) => {
  switch (method) {
    case 'ALIPAY':
      return '支付宝'
    default:
      return method
  }
}

// 获取订单数据
const fetchOrders = async () => {
  loading.value = true
  try {
    const res = await getOrders()
    if (res.data && res.data.code === 200) {
      orders.value = res.data.data || []
      // 按创建时间倒序排列
      orders.value.sort((a, b) => new Date(b.createTime).getTime() - new Date(a.createTime).getTime())
    } else {
      ElMessage.error(res.data?.msg || '获取订单数据失败')
    }
  } catch (error) {
    console.error('获取订单数据时发生错误:', error)
    ElMessage.error('获取订单数据失败')
  } finally {
    loading.value = false
  }
}

// 前往支付页面
const goToPay = (orderId: string) => {
  router.push(`/pay/${orderId}`)
}

onMounted(() => {
  fetchOrders()
})
</script>

<style scoped>
.orders-container {
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

.orders-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.empty-orders {
  padding: 40px 0;
  text-align: center;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.order-id {
  font-weight: bold;
  color: #333;
}

.order-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.order-amount {
  color: #ff4400;
  font-weight: bold;
}

.order-time {
  color: #999;
  font-size: 0.9em;
}

.order-detail {
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 4px;
}

.order-section {
  margin-bottom: 20px;
}

.section-title {
  font-weight: bold;
  margin-bottom: 10px;
  padding-bottom: 5px;
  border-bottom: 1px solid #eee;
}

.info-item {
  display: flex;
  margin-bottom: 8px;
}

.info-label {
  width: 100px;
  color: #666;
}

.info-value {
  font-weight: 500;
}

.price {
  color: #ff4400;
  font-weight: bold;
}

.order-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
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