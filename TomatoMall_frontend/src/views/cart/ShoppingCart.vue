<template>
  <div class="cart-container">
    <el-header class="header">
      <div class="logo">番茄书城 - 购物车</div>
      <div class="nav-buttons">
        <el-button type="default" @click="$router.push('/products')">继续购物</el-button>
        <el-button type="primary" @click="goToCheckout" :disabled="selectedItems.length === 0">
          结算 ({{ selectedItems.length }})
        </el-button>
      </div>
    </el-header>

    <el-main class="main-content">
      <el-card class="cart-card" v-loading="loading">
        <template #header>
          <div class="card-header">
            <h2>我的购物车</h2>
            <div class="cart-summary" v-if="!loading && cartItems.length > 0">
              总共 {{ cartItems.length }} 件商品，已选择 {{ selectedItems.length }} 件
            </div>
          </div>
        </template>

        <!-- 购物车为空 -->
        <div v-if="!loading && cartItems.length === 0" class="empty-cart">
          <el-empty description="购物车空空如也，快去挑选心仪的书籍吧！">
            <el-button type="primary" @click="$router.push('/products')">去购物</el-button>
          </el-empty>
        </div>

        <!-- 购物车列表 -->
        <div v-else-if="!loading" class="cart-list">
          <el-table :data="cartItems" style="width: 100%" @selection-change="handleSelectionChange">
            <el-table-column type="selection" width="55" />
            <el-table-column label="商品信息" min-width="350">
              <template #default="{ row }">
                <div class="product-info">
                  <el-image 
                    :src="row.cover || defaultCover" 
                    fit="cover" 
                    class="product-image"
                    @click="$router.push(`/products/${row.productId}`)"
                  />
                  <div class="product-details">
                    <div class="product-title" @click="$router.push(`/products/${row.productId}`)">
                      {{ row.title }}
                    </div>
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
            <el-table-column label="数量" width="150">
              <template #default="{ row }">
                <el-input-number 
                  v-model="row.quantity" 
                  :min="1" 
                  :max="100"
                  size="small"
                  @change="(val: number) => updateQuantity(row.cartItemId, val)"
                />
              </template>
            </el-table-column>
            <el-table-column label="金额" width="120">
              <template #default="{ row }">
                <span class="subtotal">¥{{ (row.price * row.quantity).toFixed(2) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button 
                  type="danger" 
                  size="small" 
                  @click="removeItem(row.cartItemId)"
                  :icon="Delete"
                  circle
                />
              </template>
            </el-table-column>
          </el-table>

          <!-- 购物车底部结算区 -->
          <div class="cart-footer">
            <div class="cart-total">
              <span>已选商品 {{ selectedItems.length }} 件</span>
              <span class="total-amount">总计：¥{{ totalAmount.toFixed(2) }}</span>
            </div>
            <el-button type="primary" @click="goToCheckout" :disabled="selectedItems.length === 0">
              去结算
            </el-button>
          </div>
        </div>
      </el-card>
    </el-main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'
import { getCartItems, removeFromCart, updateCartQuantity } from '../../api/Book/cart'
import type { CartItem } from '../../api/Book/cart' // 修改为仅类型导入
import defaultCover from '../../assets/tomato@1x-1.0s-200px-200px.svg'

const router = useRouter()
const loading = ref(true)
const cartItems = ref<CartItem[]>([])
const selectedItems = ref<CartItem[]>([])
const totalAmount = computed(() => {
  return selectedItems.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
})

// 获取购物车数据
const fetchCartItems = async () => {
  loading.value = true
  try {
    const res = await getCartItems()
    if (res.data && res.data.code === 200) {
      cartItems.value = res.data.data.items || []
      // 初次加载时选中所有商品
      if (cartItems.value.length > 0) {
        setTimeout(() => {
          const table = document.querySelector('.el-table__header-wrapper th.el-table-column--selection .el-checkbox')
          if (table) {
            (table as HTMLElement).click()
          }
        }, 100)
      }
    } else {
      ElMessage.error(res.data?.msg || '获取购物车数据失败')
    }
  } catch (error) {
    console.error('获取购物车数据时发生错误:', error)
    ElMessage.error('获取购物车数据失败')
  } finally {
    loading.value = false
  }
}

// 移除购物车商品
const removeItem = async (cartItemId: string) => {
  try {
    await ElMessageBox.confirm('确定要从购物车中删除该商品吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await removeFromCart(cartItemId)
    if (res.data && res.data.code === 200) {
      ElMessage.success('删除成功')
      // 更新商品列表
      cartItems.value = cartItems.value.filter(item => item.cartItemId !== cartItemId)
      // 更新选中商品列表
      selectedItems.value = selectedItems.value.filter(item => item.cartItemId !== cartItemId)
    } else {
      // 显示具体的错误信息
      const errorMsg = res.data?.msg || '删除失败'
      ElMessage.error(errorMsg)
      console.error('删除购物车商品失败:', errorMsg)
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      // 显示更详细的错误信息
      let errorMsg = '删除失败'
      if (error.response && error.response.data) {
        errorMsg = error.response.data.msg || errorMsg
      }
      console.error('删除购物车商品时发生错误:', error)
      ElMessage.error(errorMsg)
    }
  }
}

// 更新商品数量
const updateQuantity = async (cartItemId: string, quantity: number) => {
  try {
    const res = await updateCartQuantity(cartItemId, quantity)
    if (res.data && res.data.code === 200) {
      ElMessage.success('数量更新成功')
      // 更新选中商品列表中的数量
      const selectedItem = selectedItems.value.find(item => item.cartItemId === cartItemId)
      if (selectedItem) {
        selectedItem.quantity = quantity
      }
    } else {
      ElMessage.error(res.data?.msg || '更新数量失败')
      // 恢复原来的数量
      await fetchCartItems()
    }
  } catch (error) {
    console.error('更新购物车商品数量时发生错误:', error)
    ElMessage.error('更新数量失败')
    // 恢复原来的数量
    await fetchCartItems()
  }
}

// 处理选择变更
const handleSelectionChange = (val: CartItem[]) => {
  selectedItems.value = val
}

// 前往结算页面
const goToCheckout = () => {
  if (selectedItems.value.length === 0) {
    ElMessage.warning('请至少选择一件商品进行结算')
    return
  }
  
  // 将选中的商品ID存储到localStorage中，用于结算页面获取
  localStorage.setItem('checkoutItems', JSON.stringify(selectedItems.value.map(item => item.cartItemId)))
  router.push('/checkout')
}

onMounted(() => {
  fetchCartItems()
})
</script>

<style scoped>
.cart-container {
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

.cart-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.empty-cart {
  padding: 40px 0;
  text-align: center;
}

.product-info {
  display: flex;
  align-items: center;
}

.product-image {
  width: 80px;
  height: 80px;
  border-radius: 4px;
  margin-right: 15px;
  cursor: pointer;
  object-fit: cover;
}

.product-details {
  flex: 1;
}

.product-title {
  font-weight: bold;
  margin-bottom: 8px;
  cursor: pointer;
  color: #333;
}

.product-title:hover {
  color: #ff4400;
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

.cart-footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-top: 20px;
  padding: 10px 20px;
  background: #f9f9f9;
  border-radius: 4px;
}

.cart-total {
  margin-right: 20px;
}

.total-amount {
  font-size: 18px;
  color: #ff4400;
  font-weight: bold;
  margin-left: 10px;
}
</style>