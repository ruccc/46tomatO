<template>
  <div class="dashboard-container">
    <el-row :gutter="20">
      <!-- 用户基本信息卡片 -->
      <el-col :span="8">
        <el-card class="box-card">
          <template #header>
            <div class="clearfix">
              <span>个人信息</span>
              <el-button style="float: right; padding: 3px 0" type="text" @click="showEditForm">修改信息</el-button>
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
              <p>账号: {{ userInfo.username || userInfo.email || '未设置' }}</p>
              <p>邮箱: {{ userInfo.email || '未设置' }}</p>
              <p>手机: {{ userInfo.telephone || '未设置' }}</p>
              <!-- 添加地址信息 -->
              <p>地址: {{ userInfo.address || '未设置' }}</p>
              <p>注册时间: {{ formatDate(userInfo.createTime || new Date()) }}</p>
              
              <!-- 添加会员身份显示 -->
              <div class="member-identity">
                <span class="identity-label">会员等级:</span>
                <el-tag :type="getMemberLevelTagType(memberLevel)" size="medium">
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
              <span>会员信息</span>
              <el-button style="float: right; padding: 3px 0; margin-left: 10px;" type="text" 
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
              <el-button style="float: right; padding: 3px 0" type="text" @click="viewAllOrders">查看全部</el-button>
            </div>
          </template>
          
          <el-table :data="recentOrders" stripe style="width: 100%">
            <el-table-column prop="orderId" label="订单编号" width="180"></el-table-column>
            <el-table-column prop="createTime" label="下单时间" width="160">
              <template #default="scope">
                {{ formatDate(scope.row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="totalAmount" label="订单金额" width="120">
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
            <el-table-column label="操作" width="150">
              <template #default="scope">
                <el-button size="mini" type="primary" @click="viewOrderDetail(scope.row.orderId)">详情</el-button>
                <el-button size="mini" type="danger" v-if="canCancelOrder(scope.row)" @click="cancelOrder(scope.row.orderId)">取消</el-button>
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
    
    <!-- 添加头像上传对话框 -->
    <el-dialog title="更换头像" v-model="avatarUploadVisible" width="400px">
      <div class="avatar-upload-container">
        <el-upload
          class="avatar-uploader"
          action="#"
          :http-request="handleAvatarUpload"
          :show-file-list="false"
          :before-upload="beforeAvatarUpload">
          <img v-if="avatarPreview" :src="avatarPreview" class="avatar-preview" />
          <div v-else class="avatar-uploader-placeholder">
            <i class="el-icon-plus avatar-uploader-icon"></i>
            <div>点击上传</div>
          </div>
        </el-upload>
        <div class="avatar-tip">支持 JPG、PNG 格式，文件大小不超过 2MB</div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="avatarUploadVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmAvatarUpload" :disabled="!avatarFile">确认</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'

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
  0: { text: '待支付', type: 'warning' },
  1: { text: '已支付', type: 'success' },
  2: { text: '已发货', type: 'primary' },
  3: { text: '已完成', type: 'success' },
  4: { text: '已取消', type: 'info' },
  5: { text: '已退款', type: 'danger' }
}

// 消费统计数据 - 简化结构
const consumptionData = reactive({
  totalAmount: 0,
  orderCount: 0,
  bookCount: 0,
  monthlyStats: [
    { month: '1月', amount: 0 },
    { month: '2月', amount: 0 },
    { month: '3月', amount: 50 },
    { month: '4月', amount: 100 },
    { month: '5月', amount: 150 },
    { month: '6月', amount: 0 }
  ]
})

// 头像上传相关
const avatarUploadVisible = ref(false)
const avatarPreview = ref('')
const avatarFile = ref(null)

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
      return '' // 默认灰色
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
  return orderStatusMap[status]?.text || '未知状态'
}

const getOrderStatusType = (status) => {
  return orderStatusMap[status]?.type || 'info'
}

const canCancelOrder = (order) => {
  // 只有待支付和已支付（未发货）的订单可以取消
  return [0, 1].includes(order.status)
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
const fetchUserInfo = () => {
  // 模拟从API获取用户数据
  // 实际项目中应替换为真实API调用
  setTimeout(() => {
    userInfo.name = '测试用户'
    userInfo.email = 'test@example.com'
    userInfo.telephone = '13800138000'
    userInfo.address = '北京市海淀区XX街道'
    userInfo.birthday = '1990-01-01'
    userInfo.gender = 1
  }, 500)
}

const fetchMemberInfo = () => {
  // 模拟从API获取会员数据
  // 实际项目中应替换为真实API调用
  const storedMemberLevel = localStorage.getItem('memberLevel')
  memberLevel.value = storedMemberLevel ? parseInt(storedMemberLevel) : 0
  
  if (memberLevel.value > 0) {
    memberInfo.value = {
      id: '1',
      level: memberLevel.value
    }
  }
}

const fetchRecentOrders = () => {
  // 模拟获取最近订单数据
  recentOrders.value = [
    { orderId: 'ORD20230001', createTime: '2023-05-01T08:30:00', totalAmount: 99.90, status: 3 },
    { orderId: 'ORD20230002', createTime: '2023-05-15T14:20:00', totalAmount: 149.60, status: 2 },
    { orderId: 'ORD20230003', createTime: '2023-05-20T11:45:00', totalAmount: 50.00, status: 1 }
  ]
}

const fetchConsumptionStats = () => {
  // 模拟获取消费统计数据
  consumptionData.totalAmount = 299.50
  consumptionData.orderCount = 3
  consumptionData.bookCount = 5
  
  // 模拟月度数据 - 保留不变
  consumptionData.monthlyStats = [
    { month: '1月', amount: 0 },
    { month: '2月', amount: 0 },
    { month: '3月', amount: 50 },
    { month: '4月', amount: 100 },
    { month: '5月', amount: 150 },
    { month: '6月', amount: 0 }
  ]
}

// 头像上传相关方法
const showAvatarUpload = () => {
  avatarUploadVisible.value = true
  avatarPreview.value = userInfo.avatar || defaultAvatar
  avatarFile.value = null
}

const beforeAvatarUpload = (file) => {
  const isJPG = file.type === 'image/jpeg'
  const isPNG = file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG && !isPNG) {
    ElMessage.error('头像只能是JPG或PNG格式!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('头像大小不能超过 2MB!')
    return false
  }
  
  return true
}

const handleAvatarUpload = (options) => {
  const file = options.file
  avatarFile.value = file
  
  // 本地预览
  const reader = new FileReader()
  reader.onload = (e) => {
    avatarPreview.value = e.target.result
  }
  reader.readAsDataURL(file)
}

const confirmAvatarUpload = () => {
  if (!avatarFile.value) return
  
  // 实际项目中这里应该调用API上传文件到服务器
  // 这里仅模拟更新头像
  userInfo.avatar = avatarPreview.value
  ElMessage.success('头像上传成功')
  avatarUploadVisible.value = false
}

// UI交互方法
const showEditForm = () => {
  editForm.name = userInfo.name
  editForm.email = userInfo.email
  editForm.telephone = userInfo.telephone
  editForm.address = userInfo.address
  editForm.birthday = userInfo.birthday
  editForm.gender = userInfo.gender
  
  editFormVisible.value = true
}

const submitEditForm = () => {
  // 模拟表单提交
  userInfo.name = editForm.name
  userInfo.email = editForm.email
  userInfo.telephone = editForm.telephone
  userInfo.address = editForm.address
  userInfo.birthday = editForm.birthday
  userInfo.gender = editForm.gender
  
  ElMessage.success('个人信息更新成功')
  editFormVisible.value = false
}

const deleteMemberShip = () => {
  deleteMemberDialogVisible.value = true
}

const confirmDeleteMember = () => {
  // 模拟取消会员资格
  localStorage.removeItem('memberLevel')
  memberLevel.value = 0
  memberInfo.value = null
  deleteMemberDialogVisible.value = false
}

const goToCreateMember = () => {
  // 模拟跳转到会员购买页面
  router.push('/products') // 假设会员卡在商品列表中
}

const viewOrderDetail = (orderId) => {
  router.push(`/orders/${orderId}`)
}

const viewAllOrders = () => {
  router.push('/orders')
}

const cancelOrder = (orderId) => {
  // 模拟取消订单
  recentOrders.value = recentOrders.value.map(order => {
    if (order.orderId === orderId) {
      return { ...order, status: 4 } // 设置为已取消
    }
    return order
  })
}

// 生命周期钩子
onMounted(() => {
  fetchUserInfo()
  fetchMemberInfo()
  fetchRecentOrders()
  fetchConsumptionStats()
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
.form-section-title {
  margin: 20px 0 10px;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 10px;
  color: #303133;
  font-size: 16px;
  font-weight: bold;
}
</style>