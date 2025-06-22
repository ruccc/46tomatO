<template>
  <div class="checkout-container">
    <el-header class="header">
      <div class="logo">番茄书城 - 订单结算</div>
      <div class="nav-buttons">
        <el-button type="default" @click="$router.push('/cart')">返回购物车</el-button>
      </div>
    </el-header>

    <el-main class="main-content">
      <el-card class="checkout-card" v-loading="loading">
        <template #header>
          <div class="card-header">
            <h2>确认订单</h2>
          </div>
        </template>

        <!-- 订单商品列表 -->
        <div class="section-title">商品信息</div>
        <div v-if="checkoutItems.length === 0" class="empty-checkout">
          <el-empty description="没有选择商品" />
          <el-button type="primary" @click="$router.push('/cart')">返回购物车</el-button>
        </div>
        <div v-else class="checkout-items">
          <el-table :data="checkoutItems" style="width: 100%">
            <el-table-column label="商品信息" min-width="350">
              <template #default="{ row }">
                <div class="product-info">
                  <el-image 
                    :src="row.cover || defaultCover" 
                    fit="cover" 
                    class="product-image"
                  />
                  <div class="product-details">
                    <div class="product-title">{{ row.title }}</div>
                    <div class="product-desc">{{ row.description }}</div>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="单价" width="120">
              <template #default="{ row }">
                <span class="price">¥{{ row.price.toFixed(2) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="数量" width="100" prop="quantity" />
            <el-table-column label="金额" width="120">
              <template #default="{ row }">
                <span class="subtotal">¥{{ (row.price * row.quantity).toFixed(2) }}</span>
              </template>
            </el-table-column>
          </el-table>

          <!-- 收货地址表单 -->
          <div class="section-title" v-if="!isOnlyMembershipCards">收货信息</div>
          <div class="membership-notice" v-else>
            <el-alert
              title="购买会员卡无需填写收货信息"
              type="info"
              :closable="false"
              show-icon>
              <template #default>
                会员卡为虚拟商品，购买后将自动为您的账户开通会员服务
              </template>
            </el-alert>
          </div>
          <el-form 
            v-if="!isOnlyMembershipCards"
            ref="addressFormRef"
            :model="addressForm"
            :rules="addressRules"
            label-width="100px"
            class="address-form"
          >
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="收货人" prop="name">
                  <el-input v-model="addressForm.name" placeholder="请输入收货人姓名" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="手机号码" prop="phone">
                  <el-input v-model="addressForm.phone" placeholder="请输入手机号码" />
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="省份" prop="province">
                  <el-input v-model="addressForm.province" placeholder="请输入省份" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="城市" prop="city">
                  <el-input v-model="addressForm.city" placeholder="请输入城市" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="区县" prop="district">
                  <el-input v-model="addressForm.district" placeholder="请输入区县" />
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-form-item label="详细地址" prop="address">
              <el-input 
                v-model="addressForm.address" 
                type="textarea" 
                placeholder="请输入详细地址"
              />
            </el-form-item>
            
            <el-form-item label="邮政编码" prop="zipCode">
              <el-input v-model="addressForm.zipCode" placeholder="请输入邮政编码" />
            </el-form-item>
          </el-form>

          <!-- 支付方式 -->
          <div class="section-title">支付方式</div>
          <el-radio-group v-model="paymentMethod">
            <el-radio label="ALIPAY">支付宝支付</el-radio>
          </el-radio-group>

          <!-- 订单金额 -->
          <div class="order-summary">
            <div class="order-amount-details">
              <div class="amount-row">
                <span>商品总额：</span>
                <span>¥{{ originalTotal.toFixed(2) }}</span>
              </div>
              
              <!-- 会员折扣信息 -->
              <div class="amount-row discount-row" v-if="memberLevel > 0">
                <span>会员折扣 ({{ getMemberLevelName(memberLevel) }})：</span>
                <span class="discount-amount">-¥{{ discountAmount.toFixed(2) }}</span>
                <el-tag size="small" type="danger" class="discount-tag">
                  {{ getMemberLevelName(memberLevel) }} {{ getDiscountByLevel(memberLevel) }}折
                </el-tag>
              </div>
              
              <div class="amount-row final-amount">
                <span>实付金额：</span>
                <span class="total-price">¥{{ finalAmount.toFixed(2) }}</span>
              </div>
            </div>
            <el-button type="primary" @click="submitOrder" :loading="submitting">
              提交订单
            </el-button>
          </div>
        </div>
      </el-card>
    </el-main>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCartItems, checkout } from '../../api/Book/cart'
import type { CartItem, ShippingAddress } from '../../api/Book/cart'
import defaultCover from '../../assets/tomato@1x-1.0s-200px-200px.svg'
import { getDiscountRateByLevel, getMemberLevelName, isMembershipCard } from '../../utils/membershipUtils'
import { axios } from '../../utils/request'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const loading = ref(true)
const submitting = ref(false)
const checkoutItems = ref<CartItem[]>([])
const addressFormRef = ref<FormInstance>()
const paymentMethod = ref('ALIPAY')

// 会员等级和折扣相关
const memberLevel = ref(0)

// 收货地址表单
const addressForm = reactive<ShippingAddress>({
  name: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  address: '',
  zipCode: ''
})

// 表单验证规则
const addressRules = reactive<FormRules>({
  name: [{ required: true, message: '请输入收货人姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  province: [{ required: true, message: '请输入省份', trigger: 'blur' }],
  city: [{ required: true, message: '请输入城市', trigger: 'blur' }],
  district: [{ required: true, message: '请输入区县', trigger: 'blur' }],
  address: [{ required: true, message: '请输入详细地址', trigger: 'blur' }],
  zipCode: [
    { required: true, message: '请输入邮政编码', trigger: 'blur' },
    { pattern: /^\d{6}$/, message: '请输入正确的邮政编码', trigger: 'blur' }
  ]
})

// 获取会员等级信息
const fetchUserMemberInfo = async () => {
  // 首先尝试从本地存储获取会员等级
  const storedLevel = localStorage.getItem('memberLevel')
  if (storedLevel) {
    memberLevel.value = parseInt(storedLevel)
    console.log('从localStorage获取会员等级:', memberLevel.value)
    return
  }
  
  // 如果localStorage中没有，则从后端API获取
  try {
    const username = localStorage.getItem('username')
    const token = localStorage.getItem('token')
    
    if (!username || !token) {
      memberLevel.value = 0
      return
    }
    
    const encodedUsername = encodeURIComponent(username)
    const response = await axios.get(`/api/accounts/${encodedUsername}`, {
      headers: { token }
    })
    
    if (response.data && response.data.code === 200) {
      const userData = response.data.data
      if (userData.memberLevel !== undefined && userData.memberLevel !== null) {
        memberLevel.value = parseInt(userData.memberLevel) || 0
        // 同步到localStorage供后续使用
        localStorage.setItem('memberLevel', memberLevel.value.toString())
        console.log('从API获取并存储会员等级:', memberLevel.value)
      } else {
        memberLevel.value = 0
        localStorage.removeItem('memberLevel')
      }
    } else {
      memberLevel.value = 0
    }
  } catch (error) {
    console.error('获取用户会员信息失败:', error)
    memberLevel.value = 0
  }
}

// 计算原价总额
const originalTotal = computed(() => {
  return checkoutItems.value.reduce((sum, item) => {
    return sum + item.price * item.quantity
  }, 0)
})

// 计算折扣金额
const discountAmount = computed(() => {
  if (memberLevel.value === 0) return 0
  return originalTotal.value * (1 - getDiscountRateByLevel(memberLevel.value))
})

// 计算最终金额（应用折扣后）
const finalAmount = computed(() => {
  return originalTotal.value * getDiscountRateByLevel(memberLevel.value)
})

// 检查是否只有会员卡商品
const isOnlyMembershipCards = computed(() => {
  return checkoutItems.value.length > 0 && checkoutItems.value.every(item => isMembershipCard(item.productId))
})

// 获取折扣显示
const getDiscountByLevel = (level: number) => {
  return Math.round(getDiscountRateByLevel(level) * 10)
}

// 获取结算商品数据
const fetchCheckoutItems = async () => {
  loading.value = true
  try {
    // 从localStorage获取选中的购物车商品ID列表
    const checkoutItemsStr = localStorage.getItem('checkoutItems')
    if (!checkoutItemsStr) {
      ElMessage.error('未选择商品，请返回购物车选择商品')
      checkoutItems.value = []
      loading.value = false
      return
    }

    const selectedIds = JSON.parse(checkoutItemsStr) as string[]
    if (!selectedIds || selectedIds.length === 0) {
      ElMessage.error('未选择商品，请返回购物车选择商品')
      checkoutItems.value = []
      loading.value = false
      return
    }

    // 获取购物车所有商品
    const res = await getCartItems()
    if (res.data && res.data.code === 200) {
      const allItems = res.data.data.items || []
      // 过滤出选中的商品
      checkoutItems.value = allItems.filter((item: CartItem) => selectedIds.includes(item.cartItemId))
    } else {
      ElMessage.error(res.data?.msg || '获取结算商品数据失败')
    }

    // 获取会员信息
    const memberInfoStr = localStorage.getItem('checkoutMemberInfo')
    if (memberInfoStr) {
      try {
        const memberInfo = JSON.parse(memberInfoStr)
        memberLevel.value = memberInfo.memberLevel || 0
      } catch (e) {
        console.error('解析会员信息失败:', e)
      }
    } else {
      // 如果没有从购物车传递会员信息，尝试从localStorage直接读取
      const storedMemberLevel = localStorage.getItem('memberLevel')
      memberLevel.value = storedMemberLevel ? parseInt(storedMemberLevel) : 0
    }
  } catch (error) {
    console.error('获取结算商品数据时发生错误:', error)
    ElMessage.error('获取结算商品数据失败')
    checkoutItems.value = []
  } finally {
    loading.value = false
  }
}

// 处理订单创建的核心逻辑
const processOrder = async () => {
  submitting.value = true
  try {
    const checkoutRequest: any = {
      cartItemIds: checkoutItems.value.map(item => item.cartItemId),
      payment_method: paymentMethod.value,
      // 确保传递会员折扣相关信息
      memberLevel: memberLevel.value,
      originalAmount: originalTotal.value,
      discountAmount: discountAmount.value,
      finalAmount: finalAmount.value
    }

    // 只有非会员卡商品才需要收货地址
    if (!isOnlyMembershipCards.value) {
      checkoutRequest.shipping_address = addressForm
    }

    const res = await checkout(checkoutRequest)
    if (res.data && res.data.code === 200) {
      const orderId = res.data.data.orderId
      ElMessage.success('订单创建成功，正在前往支付')
      
      // 清除localStorage中的结算商品数据和折扣信息
      localStorage.removeItem('checkoutItems')
      localStorage.removeItem('checkoutMemberInfo')
      
      // 跳转到支付页面
      router.push(`/pay/${orderId}`)
    } else {
      ElMessage.error(res.data?.msg || '提交订单失败')
    }
  } catch (error) {
    console.error('提交订单时发生错误:', error)
    ElMessage.error('提交订单失败')
  } finally {
    submitting.value = false
  }
}

// 提交订单 - 确保会员折扣信息被正确传递给后端API
const submitOrder = async () => {
  if (checkoutItems.value.length === 0) {
    ElMessage.warning('没有选择商品，请返回购物车选择商品')
    return
  }

  // 如果只购买会员卡，跳过地址验证
  if (isOnlyMembershipCards.value) {
    await processOrder()
    return
  }

  // 表单验证（非会员卡商品需要填写收货地址）
  if (!addressFormRef.value) return
  await addressFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      await processOrder()
    } else {
      ElMessage.error('请填写完整的收货信息')
    }
  })
}

onMounted(() => {
  fetchCheckoutItems()
  fetchUserMemberInfo()
})
</script>

<style scoped>
.checkout-container {
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

.checkout-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-title {
  font-size: 16px;
  font-weight: bold;
  margin: 20px 0;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.empty-checkout {
  padding: 40px 0;
  text-align: center;
}

.product-info {
  display: flex;
  align-items: center;
}

.product-image {
  width: 60px;
  height: 60px;
  border-radius: 4px;
  margin-right: 15px;
  object-fit: cover;
}

.product-details {
  flex: 1;
}

.product-title {
  font-weight: bold;
  margin-bottom: 8px;
  color: #333;
}

.product-desc {
  color: #999;
  font-size: 12px;
}

.price, .subtotal {
  color: #ff4400;
  font-weight: bold;
}

.subtotal {
  font-size: 16px;
}

.address-form {
  margin: 20px 0;
}

/* 订单金额相关样式 */
.order-summary {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-top: 30px;
}

.order-amount-details {
  margin-right: 20px;
}

.amount-row {
  margin-bottom: 5px;
}

.discount-row {
  color: #ff4400;
}

.discount-amount {
  margin-right: 8px;
}

.discount-tag {
  margin-left: 5px;
}

.final-amount {
  margin-top: 10px;
  font-weight: bold;
}

.order-amount {
  margin-right: 20px;
  font-size: 16px;
}

.total-price {
  font-size: 20px;
  color: #ff4400;
  font-weight: bold;
}

/* 会员卡购买提示样式 */
.membership-notice {
  margin: 20px 0;
}

.membership-notice .el-alert {
  border: 1px solid #e1f3ff;
  background-color: #f4f9ff;
}
</style>