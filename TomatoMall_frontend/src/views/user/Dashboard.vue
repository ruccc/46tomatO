<template>
  <div class="dashboard-container">
    <el-row :gutter="20">
      <!-- 用户基本信息卡片 -->
      <el-col :span="8">
        <el-card class="box-card">
          <template #header>
            <div class="clearfix">
              <span>个人信息</span>
              <el-button style="float: right; padding: 3px 0" type="primary" link @click="showEditForm">修改信息</el-button>
            </div>
          </template>
          <div class="user-profile">
            <div class="avatar-container">
              <!-- 点击头像弹出上传框 -->
              <img :src="userInfo.avatar || defaultAvatar" alt="用户头像" class="user-avatar"
                  @click="showAvatarUpload" title="点击更换头像">
              <div class="avatar-edit-overlay" @click="showAvatarUpload">
                <i class="el-icon-camera"></i>
              </div>
            </div>
            <div class="user-info">
              <h3>{{ userInfo.name || '未设置姓名' }}</h3>
              <p>账号: {{ userInfo.username || userInfo.email || '未设置' }}</p>              <p>邮箱: {{ userInfo.email || '未设置' }}</p>
              <p>手机: {{ userInfo.telephone || '未设置' }}</p>
              <!-- 添加地址信息 -->
              <p>地址: {{ userInfo.address || '未设置' }}</p>
              
              <!-- 添加会员身份显示 -->
              <div class="member-identity">
                <span class="identity-label">会员等级:</span>
                <el-tag :type="getMemberLevelTagType(memberLevel)" size="default">
                  {{ getMemberLevelName(memberLevel) }}
                </el-tag>
              </div>
              <p v-if="memberLevel > 0" class="discount-info">
                当前享受 <span class="discount-value">{{ getDiscountByLevel(memberLevel) }}</span> 折优惠
              </p>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <!-- 会员信息卡片 -->
      <el-col :span="16">
        <el-card class="box-card">
          <template #header>
            <div class="clearfix">
              <span>会员信息</span>              <el-button style="float: right; padding: 3px 0; margin-left: 10px;" type="primary" link
                      @click="deleteMemberShip" v-if="memberInfo">取消会员</el-button>
            </div>
          </template>

          <!-- 非会员提示卡片 -->
          <div class="non-member-tip" v-if="!memberInfo">
            <i class="el-icon-star-off"></i>
            <h3>尚未成为会员</h3>
            <p>成为会员即可享受购书折扣、积分累积、等级特权等多重福利</p>
            <el-button type="primary" @click="goToCreateMember">立即成为会员</el-button>
          </div>

          <!-- 会员详细信息 -->
          <div v-else>
            <el-row>
              <el-col :span="24">
                <div class="member-description">
                  <p>您当前是 <strong>{{ getMemberLevelName(memberLevel) }}</strong>，可享受 <strong>{{ getDiscountByLevel(memberLevel) }}折</strong> 购书优惠</p>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 添加消费统计卡片 -->
    <el-row style="margin-top: 20px">
      <el-col :span="24">
        <el-card class="box-card consumption-stats">
          <template #header>
            <div class="clearfix">
              <span>消费统计</span>
            </div>
          </template>
          <el-row :gutter="20" class="stats-row">
            <el-col :span="8">
              <div class="stat-item">
                <div class="stat-value">{{ consumptionData.totalAmount }}元</div>
                <div class="stat-label">累计消费</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="stat-item">
                <div class="stat-value">{{ consumptionData.orderCount }}个</div>
                <div class="stat-label">订单总数</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="stat-item">
                <div class="stat-value">{{ consumptionData.bookCount }}本</div>
                <div class="stat-label">购买书籍</div>
              </div>
            </el-col>
          </el-row>
          
          <!-- 使用 Element Plus 组度条代替 echarts 图表 -->
          <div class="consumption-chart-container">
            <h4>最近六个月消费趋势</h4>
            <div class="month-stats" v-for="(stat, index) in consumptionData.monthlyStats" :key="index">
              <div class="month-label">{{ stat.month }}</div>
              <el-progress 
                :percentage="getProgressPercentage(stat.amount)" 
                :color="getProgressColor(getProgressPercentage(stat.amount))"
                :stroke-width="16"
                :format="() => `${stat.amount}元`"
                :show-text="true"
              />
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 添加历史订单卡片 -->
    <el-row style="margin-top: 20px">
      <el-col :span="24">
        <el-card class="box-card order-history">
          <template #header>
            <div class="clearfix">
              <span>历史订单</span>
              <el-button style="float: right; padding: 3px 0" type="primary" link @click="viewAllOrders">查看全部</el-button>
            </div>
          </template>
            <el-table :data="recentOrders" stripe style="width: 100%">
            <el-table-column prop="orderId" label="订单编号" width="200"></el-table-column>
            <el-table-column prop="createTime" label="下单时间" width="200">
              <template #default="scope">
                {{ formatDate(scope.row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="totalAmount" label="订单金额" width="200">
              <template #default="scope">
                ¥{{ scope.row.totalAmount.toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="订单状态">
              <template #default="scope">
                <el-tag :type="getOrderStatusType(scope.row.status)">
                  {{ getOrderStatusText(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
          
          <div class="empty-block" v-if="recentOrders.length === 0">
            <span class="empty-text">暂无订单记录</span>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 用户信息编辑表单 - 增强版 -->
    <el-dialog title="修改个人信息" v-model="editFormVisible" width="50%">
      <el-form ref="editForm" :model="editForm" :rules="rules" label-width="100px">
        <!-- 基本信息部分 -->
        <h3 class="form-section-title">基本信息</h3>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="editForm.name"></el-input>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editForm.email"></el-input>
        </el-form-item>
        <el-form-item label="手机号" prop="telephone">
          <el-input v-model="editForm.telephone"></el-input>
        </el-form-item>
        
        <!-- 扩展信息部分 -->
        <h3 class="form-section-title">扩展信息</h3>
        <el-form-item label="地址" prop="address">
          <el-input v-model="editForm.address" type="textarea" :rows="2"></el-input>
        </el-form-item>
        <el-form-item label="出生日期" prop="birthday">
          <el-date-picker v-model="editForm.birthday" type="date" placeholder="选择日期" style="width: 100%"></el-date-picker>
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="editForm.gender">
            <el-radio :label="1">男</el-radio>
            <el-radio :label="2">女</el-radio>
            <el-radio :label="0">保密</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="editFormVisible = false">取消</el-button>
          <el-button type="primary" @click="submitEditForm">确定</el-button>
        </div>
      </template>
    </el-dialog>
    
    <!-- 删除会员确认对话框 -->
    <el-dialog
      title="取消会员确认"
      v-model="deleteMemberDialogVisible"
      width="30%">
      <span>确定要取消您的会员资格吗？这将导致您失去所有会员特权。</span>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="deleteMemberDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="confirmDeleteMember">确定取消会员</el-button>
        </div>
      </template>
    </el-dialog>
      <!-- 添加头像URL设置对话框 -->
    <el-dialog title="更换头像" v-model="avatarUploadVisible" width="400px">
      <div class="avatar-upload-container">
        <el-form :model="avatarForm" label-position="top">
          <el-form-item label="头像URL">
            <el-input 
              v-model="avatarForm.url" 
              placeholder="请输入图片URL (例如: https://example.com/avatar.jpg)" 
              clearable>
            </el-input>
          </el-form-item>
        </el-form>
        
        <div class="avatar-preview-container" v-if="avatarForm.url">
          <p class="preview-title">头像预览</p>
          <img :src="avatarForm.url" class="avatar-preview" @error="handleImageError" />
        </div>
        
        <div v-else class="empty-preview">
          <i class="el-icon-picture-outline"></i>
          <p>输入URL后可预览头像</p>
        </div>
        
        <div class="avatar-tip">提示: 建议使用正方形图片，支持 JPG、PNG 等常见图片格式</div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="avatarUploadVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmAvatarUrl" :disabled="!isValidImageUrl">确认</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { axios } from '../../utils/request'
import { ElMessage, ElLoading } from 'element-plus'

// 日期格式化函数
const formatDate = (dateString, format = 'YYYY-MM-DD HH:mm:ss') => {
  if (!dateString) return '';
  
  const date = typeof dateString === 'string' ? new Date(dateString) : dateString;
  
  if (isNaN(date.getTime())) {
    return '无效日期';
  }
  
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  const seconds = String(date.getSeconds()).padStart(2, '0');
  
  return format
    .replace('YYYY', year)
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hours)
    .replace('mm', minutes)
    .replace('ss', seconds);
}

const router = useRouter()

// 用户信息
const userInfo = reactive({
  id: '',
  username: localStorage.getItem('username') || '',
  name: '',
  email: '',
  telephone: '',
  avatar: '',
  address: '', // 添加地址字段
  birthday: '', // 添加生日字段
  gender: 0,    // 添加性别字段 (0-保密, 1-男, 2-女)
  createTime: new Date()
})

// 默认头像
const defaultAvatar = '/src/assets/tomato@1x-1.0s-200px-200px.svg'

// 会员相关数据
const memberLevel = ref(0) // 0-普通用户，1-一级会员，2-二级会员，3-三级会员
const memberInfo = ref(null)

// 订单相关数据
const recentOrders = ref([])
const orderStatusMap = {
  'PENDING': { text: '待支付', type: 'warning' },
  'SUCCESS': { text: '支付成功', type: 'success' },
  'FAILED': { text: '支付失败', type: 'danger' },
  'TIMEOUT': { text: '支付超时', type: 'danger' },
  // 保留数字格式的映射以兼容旧数据
  0: { text: '待支付', type: 'warning' },
  1: { text: '已支付', type: 'success' },
  2: { text: '已发货', type: 'primary' },
  3: { text: '已完成', type: 'success' },
  4: { text: '已取消', type: 'info' },
  5: { text: '已退款', type: 'danger' }
}

// 消费统计数据 - 使用空值而非假数据
const consumptionData = reactive({
  totalAmount: 0,
  orderCount: 0,
  bookCount: 0,
  monthlyStats: (() => {
    const now = new Date()
    const stats = []
    for (let i = 5; i >= 0; i--) {
      const date = new Date(now.getFullYear(), now.getMonth() - i, 1)
      stats.push({
        month: `${date.getMonth() + 1}月`,
        amount: 0
      })
    }
    return stats
  })()
})

// 头像URL设置相关
const avatarUploadVisible = ref(false)
const avatarForm = reactive({
  url: ''
})
const isValidImageUrl = ref(true) // 默认允许提交，在图片加载失败时会设为false

// 会员相关对话框
const deleteMemberDialogVisible = ref(false)

// 编辑表单相关 - 扩展表单
const editFormVisible = ref(false)
const editForm = reactive({
  name: '',
  email: '',
  telephone: '',
  address: '',
  birthday: '',
  gender: 0
})

const rules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  telephone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  address: [
    { max: 100, message: '地址不能超过100个字符', trigger: 'blur' }
  ]
}

// 会员等级相关方法
const getMemberLevelName = (level) => {
  switch (level) {
    case 1:
      return '一级会员'
    case 2:
      return '二级会员'
    case 3:
      return '三级会员'
    default:
      return '普通用户'
  }
}

const getMemberLevelTagType = (level) => {
  switch (level) {
    case 1:
      return 'info' // 蓝色
    case 2:
      return 'success' // 绿色
    case 3:
      return 'danger' // 红色
    default:
      return 'info' // 默认蓝色（而不是空字符串）
  }
}

const getDiscountByLevel = (level) => {
  switch (level) {
    case 1:
      return 9
    case 2:
      return 8
    case 3:
      return 7
    default:
      return 10 // 非会员无折扣
  }
}

// 订单相关方法
const getOrderStatusText = (status) => {
  console.log('订单状态:', status, '类型:', typeof status)
  return orderStatusMap[status]?.text || `未知状态(${status})`
}

const getOrderStatusType = (status) => {
  return orderStatusMap[status]?.type || 'info'
}

// 替换 echarts 图表，使用 el-progress 来显示每月消费数据
const getProgressPercentage = (amount) => {
  // 假设最大消费额是200，计算进度比例
  const maxAmount = 200
  return Math.min(Math.round((amount / maxAmount) * 100), 100)
}

const getProgressColor = (percentage) => {
  if (percentage < 30) return '#909399'
  if (percentage < 70) return '#67c23a'
  return '#ff4400'
}

// 数据加载函数
const fetchUserInfo = async () => {
  try {
    const username = localStorage.getItem('username')
    const token = localStorage.getItem('token')
    
    if (!username || !token) {
      throw new Error('未找到用户登录信息')
    }
    
    console.log(`尝试获取用户 ${username} 的信息，token存在: ${!!token}`)
    
    // 对用户名进行URL编码，确保中文字符正确传输
    const encodedUsername = encodeURIComponent(username)
    console.log(`原始用户名: ${username}, 编码后: ${encodedUsername}`)
    
    // 直接使用原始axios请求，确保URL路径正确
    const response = await axios.get(`/api/accounts/${encodedUsername}`, {
      headers: { token }
    })
      console.log('API响应状态:', response.status)
    console.log('API响应数据:', response.data)
    
    if (response.data && (response.data.code === 200 || response.data.code === '200')) {
      const userData = response.data.data
        // 更新用户信息
      userInfo.id = userData.id
      userInfo.username = userData.username
      userInfo.name = userData.name || userData.username
      userInfo.email = userData.email
      userInfo.telephone = userData.telephone
      userInfo.address = userData.location // 使用location作为地址字段
      userInfo.createTime = userData.createTime ? new Date(userData.createTime) : new Date()
      
      // 优先使用localStorage中存储的本地头像，每个用户有独立的存储键
      const username = userData.username || localStorage.getItem('username')
      const userAvatarKey = `userAvatar_${username}` // 用户特定的头像键名
      const localAvatar = localStorage.getItem(userAvatarKey)
      
      if (localAvatar) {
        userInfo.avatar = localAvatar
        console.log(`使用用户 ${username} 的本地存储头像`)
      } else if (userData.avatar) {
        userInfo.avatar = userData.avatar
        console.log('使用后端返回的头像')
      }        // 会员等级 - 直接从账户信息中获取
      if (userData.memberLevel !== undefined && userData.memberLevel !== null) {
        memberLevel.value = parseInt(userData.memberLevel) || 0
        console.log('从账户信息中获取会员等级:', memberLevel.value)
        
        // 重要：将会员等级同步到localStorage，供购物车和结算页面使用
        localStorage.setItem('memberLevel', memberLevel.value.toString())
        console.log('会员等级已同步到localStorage:', memberLevel.value)
        
        // 根据会员等级创建会员信息对象
        if (memberLevel.value > 0) {
          memberInfo.value = {
            level: memberLevel.value,
            discountRate: getDiscountByLevel(memberLevel.value) / 10,
            createTime: new Date().toISOString()
          }
          console.log('创建会员信息对象:', memberInfo.value)
        } else {
          memberInfo.value = null
        }
      } else {
        memberLevel.value = 0
        memberInfo.value = null
        // 清除localStorage中的会员等级
        localStorage.removeItem('memberLevel')
        console.log('用户不是会员，已清除localStorage中的会员等级')
      }
      
      console.log('成功获取用户信息:', userData)
      ElMessage.success('个人信息加载成功')
    } else {
      throw new Error(response.data?.msg || '获取用户信息失败')
    }
  } catch (error) {
    console.error('获取用户信息出错:', error)
    ElMessage.error('获取用户信息失败: ' + (error.response?.data?.msg || error.message))
    throw error
  }
}

// 修改fetchMemberInfo函数 - 目前可能后端还没有实现会员API
const fetchMemberInfo = async () => {
  try {
    // 如果后端没有会员API，我们根据会员等级简单创建会员信息
    if (memberLevel.value > 0) {
      // 暂时不调用API，直接创建memberInfo对象
      memberInfo.value = {
        level: memberLevel.value,
        discountRate: getDiscountByLevel(memberLevel.value) / 10,
        createTime: new Date().toISOString()
      }
      console.log('创建会员信息:', memberInfo.value)
    } else {
      memberInfo.value = null
    }
  } catch (error) {
    console.error('获取会员信息出错:', error)
    memberInfo.value = null
  }
}

// 修改fetchRecentOrders函数 - 使用简化的API路径或暂时不调用API
const fetchRecentOrders = async () => {
  try {
    const username = localStorage.getItem('username')
    const token = localStorage.getItem('token')
    
    if (!username || !token) {
      recentOrders.value = []
      return
    }
    
    // 对用户名进行URL编码
    const encodedUsername = encodeURIComponent(username)
    
    // 尝试获取真实订单数据
    const response = await axios.get('/api/orders', {
      params: { 
        username: encodedUsername,
        limit: 5 
      },
      headers: { token }
    })
    
    if (response.data && response.data.code === 200) {
      recentOrders.value = response.data.data || []
      console.log('获取到的订单数据:', recentOrders.value)
      // 打印每个订单的状态信息
      recentOrders.value.forEach((order, index) => {
        console.log(`订单${index + 1} - ID: ${order.orderId}, 状态: ${order.status}, 状态类型: ${typeof order.status}`)
      })
        // 从最近订单数据中计算基础统计信息
      calculateConsumptionStatsFromOrders(recentOrders.value)
    } else {
      recentOrders.value = []
      console.error('获取订单记录失败:', response.data?.msg)
    }
  } catch (error) {
    console.error('获取订单记录出错:', error)
    recentOrders.value = []
  }
}

// 修改fetchConsumptionStats函数，从订单数据中计算统计信息
const fetchConsumptionStats = async () => {
  try {
    const username = localStorage.getItem('username')
    const token = localStorage.getItem('token')
    
    if (!username || !token) {
      resetConsumptionData()
      return
    }
    
    // 对用户名进行URL编码
    const encodedUsername = encodeURIComponent(username)
    
    // 获取所有订单数据用于统计（不限制数量）
    const response = await axios.get('/api/orders', {
      params: { 
        username: encodedUsername
        // 移除limit参数以获取所有订单
      },
      headers: { token }
    })
    
    if (response.data && response.data.code === 200) {
      const allOrders = response.data.data || []
      console.log('获取到所有订单数据用于统计:', allOrders.length, '个订单')
      
      // 从所有订单数据中计算消费统计
      calculateConsumptionStatsFromAllOrders(allOrders)
    } else {
      // 如果API返回错误，重置为空数据
      resetConsumptionData()
      console.error('获取订单数据失败:', response.data?.msg)
    }
  } catch (error) {
    console.error('获取订单数据失败:', error.response?.data?.msg || error.message)
    // 初始化空数据
    resetConsumptionData()
  }
}

// 新增：从订单数据计算消费统计的函数
const calculateConsumptionStatsFromOrders = (orders) => {
  // 这个函数用于从最近的订单数据计算基本统计，但不包括月度统计
  if (!orders || orders.length === 0) {
    return
  }
  
  console.log('从订单计算基本统计...', orders.length, '个订单')
  
  // 计算订单的统计
  let totalAmount = 0
  let orderCount = 0
  let bookCount = 0
  
  orders.forEach(order => {
    // 只统计成功支付的订单
    if (order.status === 'SUCCESS' || order.status === 1 || order.status === 3) {
      totalAmount += order.totalAmount || 0
      orderCount += 1
      // 如果订单中有书籍数量信息，可以累加
      bookCount += order.bookCount || 1 // 假设每个订单默认包含1本书
    }
  })
    // 只有从最近订单计算时，才更新基础统计（避免与完整统计冲突）
  // 注意：如果 fetchConsumptionStats 已经完成，则不覆盖完整统计数据
  if (orders === recentOrders.value && consumptionData.monthlyStats.every(stat => stat.amount === 0)) {
    // 只有月度统计为空时，才用最近订单的统计数据
    consumptionData.totalAmount = totalAmount
    consumptionData.orderCount = orderCount  
    consumptionData.bookCount = bookCount
    console.log('使用最近订单的统计数据')
  } else if (orders !== recentOrders.value) {
    // 如果不是最近订单，则只记录日志
    console.log('从其他订单数据计算的统计数据（不更新UI）')
  }
  
  console.log(`订单统计 - 金额: ${totalAmount}, 订单数: ${orderCount}, 书籍数: ${bookCount}`)
}

// 新增：从所有订单数据计算完整的消费统计
const calculateConsumptionStatsFromAllOrders = (allOrders) => {
  console.log('从所有订单计算完整统计...')
  
  // 重置统计数据
  consumptionData.totalAmount = 0
  consumptionData.orderCount = 0
  consumptionData.bookCount = 0
  
  // 初始化最近6个月的统计
  const now = new Date()
  const monthlyStats = []
  
  for (let i = 5; i >= 0; i--) {
    const date = new Date(now.getFullYear(), now.getMonth() - i, 1)
    monthlyStats.push({
      month: `${date.getMonth() + 1}月`,
      amount: 0,
      year: date.getFullYear(),
      monthNum: date.getMonth()
    })
  }
  
  // 遍历所有订单进行统计
  allOrders.forEach(order => {
    // 只统计成功支付的订单
    if (order.status === 'SUCCESS' || order.status === 1 || order.status === 3) {
      const orderAmount = order.totalAmount || 0
      
      // 累计总统计
      consumptionData.totalAmount += orderAmount
      consumptionData.orderCount += 1
      consumptionData.bookCount += order.bookCount || 1
      
      // 月度统计
      if (order.createTime) {
        const orderDate = new Date(order.createTime)
        const orderYear = orderDate.getFullYear()
        const orderMonth = orderDate.getMonth()
        
        // 查找对应的月份并累加金额
        const monthStat = monthlyStats.find(stat => 
          stat.year === orderYear && stat.monthNum === orderMonth
        )
        
        if (monthStat) {
          monthStat.amount += orderAmount
        }
      }
    }
  })
  
  // 更新月度统计数据（移除year和monthNum临时字段）
  consumptionData.monthlyStats = monthlyStats.map(stat => ({
    month: stat.month,
    amount: Math.round(stat.amount * 100) / 100 // 保留两位小数
  }))
  
  console.log('完整统计结果:', {
    totalAmount: consumptionData.totalAmount,
    orderCount: consumptionData.orderCount,
    bookCount: consumptionData.bookCount,
    monthlyStats: consumptionData.monthlyStats
  })
}

// 修改resetConsumptionData函数，允许只重置特定字段
const resetConsumptionData = (field = 'all') => {
  if (field === 'all' || field === 'totalAmount') {
    consumptionData.totalAmount = 0
  }
  if (field === 'all' || field === 'orderCount') {
    consumptionData.orderCount = 0
  }
  if (field === 'all' || field === 'bookCount') {
    consumptionData.bookCount = 0
  }  if (field === 'all' || field === 'monthlyStats') {
    const now = new Date()
    consumptionData.monthlyStats = []
    for (let i = 5; i >= 0; i--) {
      const date = new Date(now.getFullYear(), now.getMonth() - i, 1)
      consumptionData.monthlyStats.push({
        month: `${date.getMonth() + 1}月`,
        amount: 0
      })
    }
  }
}

// 修改submitEditForm函数，使用正确的API路径
const submitEditForm = async () => {
  try {
    // 获取token
    const token = localStorage.getItem('token')
    if (!token) {
      throw new Error('未找到用户登录信息')
    }
    
    // 创建符合后端要求的更新对象
    const updateInfo = {
      name: editForm.name,
      email: editForm.email,
      telephone: editForm.telephone,
      location: editForm.address, // 注意：字段名称是location而不是address
      // birthday和gender字段可能需要后端支持
    }
    
    // 使用user.ts中定义的API
    const response = await axios.put('/api/accounts', updateInfo, {
      headers: {
        'Content-Type': 'application/json',
        token
      }
    })
    
    if (response.data && response.data.code === 200) {
      // 更新本地用户信息
      userInfo.name = editForm.name
      userInfo.email = editForm.email
      userInfo.telephone = editForm.telephone
      userInfo.address = editForm.address // 在前端仍然使用address
      
      ElMessage.success('个人信息更新成功')
      editFormVisible.value = false
    } else {
      ElMessage.error(response.data?.msg || '更新个人信息失败')
    }
  } catch (error) {
    console.error('更新个人信息出错:', error)
    ElMessage.error('更新个人信息失败')
  }
}

// 图片URL验证错误处理
const handleImageError = () => {
  isValidImageUrl.value = false
  ElMessage.warning('图片URL无效或无法加载，请检查链接是否正确')
}

// 监听URL输入，重置验证状态
const watchAvatarUrl = (newUrl) => {
  if (newUrl) {
    isValidImageUrl.value = true // 每当URL改变时，重置为有效状态
  }
}

// 确认并保存头像URL
const confirmAvatarUrl = () => {
  if (!avatarForm.url || !isValidImageUrl.value) return
  
  // 显示加载提示，使用 ElLoading 服务
  const loadingInstance = ElLoading.service({
    fullscreen: true,
    text: '保存头像中...',
    background: 'rgba(0, 0, 0, 0.7)'
  })
  
  // 为了更好的用户体验，添加一个短暂的延迟
  setTimeout(() => {
    try {      // 1. 将头像URL保存在userInfo中
      userInfo.avatar = avatarForm.url
      
      // 2. 同时保存到用户专属的localStorage键中，避免不同用户间相互覆盖
      const username = userInfo.username || localStorage.getItem('username')
      const userAvatarKey = `userAvatar_${username}`
      localStorage.setItem(userAvatarKey, avatarForm.url)
      
      // 3. 记录保存时间（也使用用户专属键）
      localStorage.setItem(`userAvatarTime_${username}`, new Date().toISOString())
      
      console.log('头像URL已保存到本地存储:', avatarForm.url)
      ElMessage.success('头像保存成功')
      avatarUploadVisible.value = false
    } catch (error) {
      console.error('保存头像URL出错:', error)
      ElMessage.error('保存头像失败: ' + error.message)
    } finally {
      // 关闭加载提示
      loadingInstance.close()
    }
  }, 800) // 短暂延迟，增强用户体验
}

// UI交互方法
// 显示头像上传对话框
const showAvatarUpload = () => {
  // 重置头像URL表单
  avatarForm.url = userInfo.avatar || ''
  isValidImageUrl.value = true
  avatarUploadVisible.value = true
}

const showEditForm = () => {
  editForm.name = userInfo.name
  editForm.email = userInfo.email
  editForm.telephone = userInfo.telephone
  editForm.address = userInfo.address
  editForm.birthday = userInfo.birthday
  editForm.gender = userInfo.gender
  
  editFormVisible.value = true
}

const deleteMemberShip = () => {
  deleteMemberDialogVisible.value = true
}

const confirmDeleteMember = async () => {
  try {
    const response = await axios.post('/api/membership/cancel')
    
    if (response.data && response.data.code === 200) {
      memberLevel.value = 0
      memberInfo.value = null
      localStorage.removeItem('memberLevel')
      ElMessage.success('已成功取消会员资格')
      deleteMemberDialogVisible.value = false
    } else {
      ElMessage.error(response.data?.msg || '取消会员失败')
    }
  } catch (error) {
    console.error('取消会员出错:', error)
    ElMessage.error('取消会员失败')
  }
}

const goToCreateMember = () => {
  router.push('/membership')
}

const viewOrderDetail = (orderId) => {
  router.push(`/orders/${orderId}`)
}

const viewAllOrders = () => {
  router.push('/orders')
}

// 添加对头像URL的监听
watch(() => avatarForm.url, (newUrl) => {
  watchAvatarUrl(newUrl)
})

// 生命周期钩子
onMounted(async () => {
  try {
    // 先获取用户信息
    await fetchUserInfo()
    
    // 然后获取其他信息
    fetchMemberInfo()
    fetchRecentOrders()
    fetchConsumptionStats()
  } catch (error) {
    console.error('初始化数据出错:', error)
  }
})
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
}
.user-profile {
  display: flex;
  align-items: center;
}
.avatar-container {
  position: relative;
  margin-right: 20px;
}
.user-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
  cursor: pointer;
  transition: filter 0.3s;
}
.avatar-edit-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  opacity: 0;
  transition: opacity 0.3s;
  cursor: pointer;
}
.avatar-container:hover .avatar-edit-overlay {
  opacity: 1;
}
.avatar-container:hover .user-avatar {
  filter: brightness(0.8);
}
.user-info h3 {
  margin-top: 0;
  margin-bottom: 10px;
}
.user-info p {
  margin: 5px 0;
  color: #606266;
}
.clearfix::after {
  content: "";
  display: table;
  clear: both;
}
.member-identity {
  margin: 8px 0;
}
.identity-label {
  margin-right: 8px;
  color: #606266;
}
.discount-info {
  margin: 5px 0;
  color: #606266;
}
.discount-value {
  font-weight: bold;
  color: #ff4400;
}
.non-member-tip {
  text-align: center;
  padding: 30px 0;
}
.non-member-tip i {
  font-size: 50px;
  color: #E6A23C;
}
.non-member-tip h3 {
  margin: 15px 0;
  color: #606266;
}
.non-member-tip p {
  margin-bottom: 20px;
  color: #909399;
}
.consumption-stats {
  margin-bottom: 20px;
}
.stats-row {
  margin-bottom: 20px;
}
.stat-item {
  text-align: center;
  padding: 10px;
}
.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #ff4400;
  margin-bottom: 5px;
}
.stat-label {
  font-size: 14px;
  color: #606266;
}
.consumption-chart-container {
  margin-top: 20px;
}
.month-stats {
  margin: 15px 0;
  display: flex;
  align-items: center;
}
.month-label {
  width: 50px;
  font-size: 14px;
  color: #606266;
  margin-right: 10px;
}
.order-history {
  margin-bottom: 20px;
}
.empty-block {
  text-align: center;
  padding: 30px 0;
}
.empty-text {
  color: #909399;
  font-size: 14px;
}
.member-description {
  padding: 10px 0;
  text-align: center;
  font-size: 16px;
}
.avatar-uploader {
  text-align: center;
}
.avatar-uploader-placeholder {
  width: 150px;
  height: 150px;
  border: 1px dashed #d9d9d9;
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #8c939d;
  cursor: pointer;
}
.avatar-uploader-placeholder:hover {
  border-color: #409EFF;
}
.avatar-preview {
  width: 150px;
  height: 150px;
  border-radius: 50%;
  object-fit: cover;
}
.avatar-upload-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 0;
}
.avatar-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 10px;
}
.avatar-preview-container {
  margin: 20px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.preview-title {
  margin-bottom: 10px;
  font-size: 14px;
  color: #606266;
}
.empty-preview {
  width: 150px;
  height: 150px;
  margin: 15px auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border: 1px dashed #d9d9d9;
  border-radius: 50%;
  color: #c0c4cc;
}
.empty-preview i {
  font-size: 36px;
  margin-bottom: 10px;
}
.form-section-title {
  margin: 20px 0 10px;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 10px;
  color: #303133;
  font-size: 16px;
  font-weight: bold;
}

/* 订单状态样式 */
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