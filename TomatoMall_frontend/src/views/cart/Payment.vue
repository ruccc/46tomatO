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

        <!-- 支付表单显示区域 - 修改了位置，确保优先渲染 -->
        <div v-if="paymentForm" class="payment-form-container">
          <h3>请完成支付</h3>
          <div v-html="paymentForm" class="payment-form"></div>
          <el-alert
            v-if="!paymentFormRendered"
            title="支付表单加载中..."
            type="info"
            show-icon
          />
        </div>

        <!-- 支付信息 -->
        <div v-if="!loading && orderInfo && !paymentForm" class="payment-info">
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

        <!-- 订单不存在 -->
        <div v-if="!loading && !orderInfo && !paymentForm" class="empty-payment">
          <el-empty description="订单不存在或已被取消" />
          <el-button type="primary" @click="$router.push('/cart')">返回购物车</el-button>
        </div>
      </el-card>
    </el-main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getOrderDetail, initPayment } from '../../api/Book/orders'
import type { Order, PaymentInfo } from '../../api/Book/orders'
// 导入会员卡工具函数
import { isMembershipCard, getMemberLevelByProductId } from '../../utils/membershipUtils'
import { axios } from '../../utils/request'

const route = useRoute()
const router = useRouter()
const orderId = route.params.orderId as string
const loading = ref(true)
const paying = ref(false)
const orderInfo = ref<Order | null>(null)
const paymentForm = ref<string | null>(null)
const paymentFormRendered = ref(false)

// 添加会员等级
const memberLevel = ref(0)

// 监听paymentForm变化，处理表单渲染
watch(paymentForm, async (newValue) => {
  if (newValue) {
    // 等待DOM更新后执行
    await nextTick()
    
    // 给表单一点时间渲染
    setTimeout(() => {
      const formElement = document.querySelector('.payment-form form')
      if (formElement) {
        paymentFormRendered.value = true
        console.log('支付表单已渲染')
        
        // 手动提交表单，解决表单内嵌script不执行的问题
        try {
          (formElement as HTMLFormElement).submit()
          console.log('表单已手动提交')
        } catch (error) {
          console.error('表单提交失败:', error)
        }
      } else {
        console.warn('支付表单元素未找到')
      }
    }, 500)
  }
})

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

// 获取当前会员等级
const fetchMemberLevel = () => {
  const level = localStorage.getItem('memberLevel')
  memberLevel.value = level ? parseInt(level) : 0
}

// 检查会员卡并更新会员等级
const checkMembershipAfterPayment = async () => {
  try {
    if (!orderInfo.value || orderInfo.value.status !== 'SUCCESS') return
    
    // 检查订单中是否包含会员卡商品
    const orderDetails = orderInfo.value.orderItems || []
    const membershipCards = orderDetails.filter(item => isMembershipCard(item.productId))
    
    if (membershipCards.length > 0) {
      // 找出最高等级的会员卡
      const highestLevel = Math.max(
        ...membershipCards.map(item => getMemberLevelByProductId(item.productId))
      )
      
      // 如果购买的会员卡等级比当前等级高，则更新会员等级
      if (highestLevel > memberLevel.value) {
        // 调用后端API更新会员等级
        try {
          const response = await axios.post('/api/accounts/update-member-level', {
            level: highestLevel
          }, {
            headers: {
              'Content-Type': 'application/json',
              token: localStorage.getItem('token')
            }
          })
          
          if (response.data && response.data.code === 200) {
            // 更新本地存储的会员等级
            localStorage.setItem('memberLevel', highestLevel.toString())
            memberLevel.value = highestLevel
            ElMessage.success(`恭喜您成为${getMemberLevelName(highestLevel)}！`)
          }
        } catch (error) {
          console.error('更新会员等级失败:', error)
        }
      }
    }
  } catch (error) {
    console.error('检查会员卡失败:', error)
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
      console.log('支付表单内容:', paymentInfo.paymentForm)
      
      if (!paymentInfo.paymentForm) {
        ElMessage.error('获取到的支付表单为空')
        return
      }
      
      // 创建一个临时div来放置支付表单
      const div = document.createElement('div')
      div.innerHTML = paymentInfo.paymentForm
      document.body.appendChild(div)
      
      // 找到表单元素
      const form = div.querySelector('form')
      if (form) {
        // 直接在当前页面跳转，不设置target属性
        // 确保所有需要的参数都已包含在表单中
        const hiddenFields = form.querySelectorAll('input[type="hidden"]')
        console.log(`表单中包含 ${hiddenFields.length} 个隐藏字段`)
        
        // 手动提交表单，直接在当前页面跳转
        form.submit()
        
        ElMessage.success('正在跳转到支付页面...')
      } else {
        console.error('无法在返回的HTML中找到表单')
        ElMessage.error('初始化支付失败，无法获取支付表单')
        
        // 清理临时元素
        document.body.removeChild(div)
      }
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

// 修改发起支付成功后的处理逻辑
const handlePaymentSuccess = async () => {
  // 刷新订单信息
  await fetchOrderInfo()
  
  // 检查会员卡并更新会员等级
  await checkMembershipAfterPayment()
}

// 将会员等级判断功能添加到onMounted
onMounted(() => {
  fetchOrderInfo()
  fetchMemberLevel()
  
  // 定期检查支付状态，如果发现支付成功，则处理会员卡逻辑
  const checkInterval = setInterval(async () => {
    if (orderInfo.value && orderInfo.value.status === 'PENDING') {
      // 刷新订单信息
      await fetchOrderInfo()
      
      // 如果支付成功，处理会员卡
      if (orderInfo.value && orderInfo.value.status === 'SUCCESS') {
        clearInterval(checkInterval)
        await checkMembershipAfterPayment()
      }
    } else {
      clearInterval(checkInterval)
    }
  }, 5000) // 每5秒检查一次
  
  // 组件卸载时清除定时器
  return () => clearInterval(checkInterval)
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
  min-height: 200px;
}

/* 确保支付表单内部内容正确显示 */
.payment-form :deep(form) {
  display: block !important;
  text-align: center !important;
}

.payment-form :deep(iframe) {
  width: 100% !important;
  min-height: 300px !important;
  border: none !important;
}

.payment-form :deep(button), 
.payment-form :deep(input[type="submit"]) {
  padding: 8px 20px !important;
  background-color: #1890ff !important;
  color: #fff !important;
  border: none !important;
  border-radius: 4px !important;
  cursor: pointer !important;
  margin-top: 10px !important;
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