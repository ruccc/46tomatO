<template>
  <div class="messages-container">
    <el-header class="header">
      <div class="logo">番茄书城 - 消息中心</div>
      <div class="nav-buttons">
        <el-badge :value="unreadCount" :max="99" :hidden="unreadCount === 0">
          <el-button type="primary" @click="refreshMessages">
            刷新消息
          </el-button>
        </el-badge>
        <el-button type="success" @click="$router.push('/main')">
          返回主页
        </el-button>
      </div>
    </el-header>

    <el-container class="main-container">
      <!-- 左侧联系人列表 -->
      <el-aside class="contacts-panel" width="300px">
        <el-card class="contacts-card">
          <template #header>
            <div class="clearfix">
              <span>联系人</span>
              <el-button type="primary" size="small" @click="showNewMessageDialog">
                新建会话
              </el-button>
            </div>
          </template>
          
          <div class="contacts-list" v-loading="contactsLoading">
            <div 
              v-for="contact in contactList" 
              :key="contact.id"
              :class="['contact-item', { active: selectedContactId === contact.id }]"
              @click="selectContact(contact)"
            >
              <el-badge :value="contact.unreadCount" :max="99" :hidden="contact.unreadCount === 0">
                <el-avatar :src="contact.avatar" :size="40">
                  {{ contact.name.charAt(0) }}
                </el-avatar>
              </el-badge>
              <div class="contact-info">
                <div class="contact-name">{{ contact.name }}</div>
                <div class="contact-last-message">{{ contact.lastMessage || '暂无消息' }}</div>
              </div>
              <div class="contact-time">
                {{ formatTime(contact.lastMessageTime) }}
              </div>
            </div>
            
            <div v-if="!contactsLoading && contactList.length === 0" class="empty-contacts">
              <el-empty description="暂无联系人">
                <el-button type="primary" @click="showNewMessageDialog">
                  开始聊天
                </el-button>
              </el-empty>
            </div>
          </div>
        </el-card>
      </el-aside>

      <!-- 右侧聊天区域 -->
      <el-main class="chat-panel">
        <el-card class="chat-card" v-if="selectedContactId">
          <template #header>
            <div class="chat-header">
              <div class="contact-info">
                <el-avatar :src="selectedContact?.avatar" :size="32">
                  {{ selectedContact?.name?.charAt(0) }}
                </el-avatar>
                <span class="contact-name">{{ selectedContact?.name }}</span>
              </div>
              <el-button type="primary" size="small" @click="markCurrentAsRead">
                标记已读
              </el-button>
            </div>
          </template>

          <!-- 消息列表 -->
          <div class="messages-area" ref="messagesRef" v-loading="messagesLoading">
            <div 
              v-for="message in messages" 
              :key="message.id"
              :class="['message-item', { 'is-sender': message.isSender }]"
            >
              <div class="message-avatar">
                <el-avatar :src="message.senderAvatar" :size="32">
                  {{ message.senderName.charAt(0) }}
                </el-avatar>
              </div>
              <div class="message-content">
                <div class="message-header">
                  <span class="sender-name">{{ message.senderName }}</span>
                  <span class="message-time">{{ formatTime(message.createTime) }}</span>
                </div>
                <div class="message-body" v-if="message.contentType === 'text'">
                  {{ message.content }}
                </div>
                <div class="message-body" v-else-if="message.contentType === 'image'">
                  <el-image :src="message.content" fit="contain" style="max-width: 200px;" />
                </div>
                <div class="message-body" v-else-if="message.contentType === 'link'">
                  <el-link :href="message.content" target="_blank">{{ message.content }}</el-link>
                </div>
                <div class="message-status" v-if="message.isSender">
                  <el-tag :type="message.status === 2 ? 'success' : 'info'" size="small">
                    {{ message.status === 2 ? '已读' : '已发送' }}
                  </el-tag>
                </div>
              </div>
            </div>
            
            <div v-if="!messagesLoading && messages.length === 0" class="empty-messages">
              <el-empty description="还没有消息，快开始聊天吧！" />
            </div>
          </div>

          <!-- 消息输入区 -->
          <div class="message-input-area">
            <el-divider />
            <div class="input-controls">
              <el-select v-model="messageType" style="width: 120px;">
                <el-option label="文本" value="text" />
                <el-option label="图片链接" value="image" />
                <el-option label="网页链接" value="link" />
              </el-select>
              <el-input
                v-model="newMessage"
                :placeholder="getInputPlaceholder()"
                :type="messageType === 'text' ? 'textarea' : 'text'"
                :rows="3"
                @keyup.enter="handleEnterKey"
                style="flex: 1; margin: 0 10px;"
              />
              <el-button 
                type="primary" 
                @click="sendNewMessage"
                :disabled="!newMessage.trim()"
                :loading="sending"
              >
                发送
              </el-button>
            </div>
          </div>
        </el-card>
        
        <!-- 未选择联系人时的提示 -->
        <div v-else class="no-contact-selected">
          <el-empty description="请选择一个联系人开始聊天">
            <el-button type="primary" @click="showNewMessageDialog">
              开始新对话
            </el-button>
          </el-empty>
        </div>
      </el-main>
    </el-container>

    <!-- 新建会话对话框 -->
    <el-dialog title="新建会话" v-model="newMessageDialogVisible" width="400px">
      <el-form :model="newConversationForm" label-width="80px">        <el-form-item label="联系人">
          <el-input 
            v-model="newConversationForm.receiverName" 
            placeholder="请输入用户ID（数字）"
            @blur="validateReceiver"
          />
          <div v-if="receiverError" class="error-text">{{ receiverError }}</div>
        </el-form-item>
        <el-form-item label="消息内容">
          <el-input
            v-model="newConversationForm.content"
            type="textarea"
            :rows="3"
            placeholder="请输入消息内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="newMessageDialogVisible = false">取消</el-button>
          <el-button 
            type="primary" 
            @click="createNewConversation"
            :disabled="!newConversationForm.receiverName || !newConversationForm.content"
          >
            发送
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  sendMessage, 
  getConversation, 
  getContacts, 
  getUnreadCount,
  getUnreadCountFromUser,
  markAsRead 
} from '../../api/messages'

const router = useRouter()

// 响应式数据
const unreadCount = ref(0)
const contactsLoading = ref(false)
const messagesLoading = ref(false)
const sending = ref(false)
const contactList = ref([])
const messages = ref([])
const selectedContactId = ref(null)
const newMessage = ref('')
const messageType = ref('text')
const messagesRef = ref(null)

// 新建会话相关
const newMessageDialogVisible = ref(false)
const newConversationForm = reactive({
  receiverName: '',
  content: ''
})
const receiverError = ref('')

// 计算属性
const selectedContact = computed(() => {
  return contactList.value.find(contact => contact.id === selectedContactId.value)
})

// 获取当前用户信息
const getCurrentUserId = () => {
  // 先尝试从userInfo获取
  const userInfo = localStorage.getItem('userInfo')
  if (userInfo) {
    try {
      const parsed = JSON.parse(userInfo)
      if (parsed && parsed.id) {
        return parsed.id
      }
    } catch (e) {
      console.error('解析userInfo失败:', e)
    }
  }
  
  // 再尝试从userId获取
  const userId = localStorage.getItem('userId')
  if (userId && userId !== 'null' && userId !== 'undefined') {
    return parseInt(userId)
  }
  
  // 如果都没有，说明用户未登录，应该跳转到登录页
  console.warn('用户未登录，无法获取用户ID')
  ElMessage.error('请先登录')
  // 这里可以添加跳转到登录页的逻辑
  // router.push('/login')
  return null
}

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const messageDate = new Date(date.getFullYear(), date.getMonth(), date.getDate())
  
  if (messageDate.getTime() === today.getTime()) {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }
  
  return date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' })
}

// 获取输入框占位符
const getInputPlaceholder = () => {
  switch (messageType.value) {
    case 'text': return '输入消息内容...'
    case 'image': return '输入图片链接...'
    case 'link': return '输入网页链接...'
    default: return '输入消息内容...'
  }
}

// 加载联系人列表
const loadContacts = async () => {
  contactsLoading.value = true
  try {
    const userId = getCurrentUserId()
    if (!userId) {
      console.error('无法获取用户ID，无法加载联系人列表')
      return
    }
    const response = await getContacts(userId)
    
    if (response.data && response.data.code === 200) {
      const contactIds = response.data.data
      
      // 为每个联系人获取详细信息和未读消息数
      const contactDetails = await Promise.all(
        contactIds.map(async (contactId) => {
          try {
            // 这里应该调用获取用户信息的API，暂时使用模拟数据
            const unreadResponse = await getUnreadCountFromUser(userId, contactId)
            const unreadCount = unreadResponse.data?.data || 0
            
            // 获取最新消息用于预览
            // const latestResponse = await getLatestMessages(userId, contactId, 1)
            // const latestMessage = latestResponse.data?.data?.[0]
            
            return {
              id: contactId,
              name: `用户${contactId}`, // 临时名称，实际应从用户信息API获取
              avatar: '', // 实际应从用户信息API获取
              unreadCount,
              lastMessage: '', // latestMessage?.content || '',
              lastMessageTime: '' // latestMessage?.createTime || ''
            }
          } catch (error) {
            console.error(`获取联系人${contactId}信息失败:`, error)
            return {
              id: contactId,
              name: `用户${contactId}`,
              avatar: '',
              unreadCount: 0,
              lastMessage: '',
              lastMessageTime: ''
            }
          }
        })
      )
      
      contactList.value = contactDetails.sort((a, b) => 
        new Date(b.lastMessageTime || 0) - new Date(a.lastMessageTime || 0)
      )
    }
  } catch (error) {
    console.error('获取联系人列表失败:', error)
    ElMessage.error('获取联系人列表失败')
  } finally {
    contactsLoading.value = false
  }
}

// 加载未读消息总数
const loadUnreadCount = async () => {
  try {
    const userId = getCurrentUserId()
    if (!userId) {
      console.error('无法获取用户ID，无法加载未读消息数')
      return
    }
    const response = await getUnreadCount(userId)
    
    if (response.data && response.data.code === 200) {
      unreadCount.value = response.data.data
    }
  } catch (error) {
    console.error('获取未读消息数失败:', error)
  }
}

// 选择联系人
const selectContact = async (contact) => {
  selectedContactId.value = contact.id
  await loadConversation()
}

// 加载对话消息
const loadConversation = async (page = 0) => {
  if (!selectedContactId.value) return
  
  messagesLoading.value = true
  try {
    const userId = getCurrentUserId()
    const response = await getConversation(userId, selectedContactId.value, page)
    
    if (response.data && response.data.code === 200) {
      messages.value = response.data.data.content.reverse() // 按时间顺序排列
      
      // 滚动到底部
      await nextTick()
      scrollToBottom()
    }
  } catch (error) {
    console.error('获取对话消息失败:', error)
    ElMessage.error('获取对话消息失败')
  } finally {
    messagesLoading.value = false
  }
}

// 发送消息
const sendNewMessage = async () => {
  if (!newMessage.value.trim() || !selectedContactId.value) return
  
  sending.value = true
  try {
    const userId = getCurrentUserId()
    const messageData = {
      senderId: userId,
      receiverId: selectedContactId.value,
      content: newMessage.value.trim(),
      contentType: messageType.value
    }
    
    const response = await sendMessage(messageData)
    
    if (response.data && response.data.code === 200) {
      // 添加新消息到列表
      messages.value.push(response.data.data)
      newMessage.value = ''
      
      // 滚动到底部
      await nextTick()
      scrollToBottom()
      
      // 刷新联系人列表
      await loadContacts()
    } else {
      ElMessage.error(response.data?.message || '发送消息失败')
    }
  } catch (error) {
    console.error('发送消息失败:', error)
    ElMessage.error('发送消息失败')
  } finally {
    sending.value = false
  }
}

// 处理回车键
const handleEnterKey = (event) => {
  if (messageType.value === 'text' && !event.shiftKey) {
    event.preventDefault()
    sendNewMessage()
  }
}

// 滚动到底部
const scrollToBottom = () => {
  if (messagesRef.value) {
    messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  }
}

// 标记当前对话为已读
const markCurrentAsRead = async () => {
  if (!selectedContactId.value) return
  
  try {
    const userId = getCurrentUserId()
    await markAsRead(userId, selectedContactId.value)
    
    // 更新未读数
    await loadUnreadCount()
    await loadContacts()
    
    ElMessage.success('已标记为已读')
  } catch (error) {
    console.error('标记已读失败:', error)
    ElMessage.error('标记已读失败')
  }
}

// 刷新消息
const refreshMessages = async () => {
  await Promise.all([
    loadContacts(),
    loadUnreadCount(),
    selectedContactId.value ? loadConversation() : Promise.resolve()
  ])
  ElMessage.success('刷新成功')
}

// 显示新建会话对话框
const showNewMessageDialog = () => {
  newConversationForm.receiverName = ''
  newConversationForm.content = ''
  receiverError.value = ''
  newMessageDialogVisible.value = true
}

// 验证接收者
const validateReceiver = () => {
  if (!newConversationForm.receiverName) {
    receiverError.value = '请输入用户ID'
    return false
  }
  
  // 验证是否为有效的数字ID
  const receiverId = parseInt(newConversationForm.receiverName)
  if (isNaN(receiverId) || receiverId <= 0) {
    receiverError.value = '请输入有效的用户ID（正整数）'
    return false
  }
  
  receiverError.value = ''
  return true
}

// 创建新会话
const createNewConversation = async () => {
  if (!validateReceiver() || !newConversationForm.content) return
  
  try {
    const userId = getCurrentUserId()
    if (!userId) {
      ElMessage.error('请先登录')
      return
    }
    
    const receiverId = parseInt(newConversationForm.receiverName)
    if (isNaN(receiverId)) {
      ElMessage.error('请输入有效的用户ID')
      return
    }
      if (receiverId === userId) {
      ElMessage.error('不能给自己发送消息')
      return
    }
    
    const messageData = {
      senderId: userId,
      receiverId,
      content: newConversationForm.content,
      contentType: 'text'
    }
    
    const response = await sendMessage(messageData)
    
    if (response.data && response.data.code === 200) {
      newMessageDialogVisible.value = false
      
      // 刷新联系人列表
      await loadContacts()
      
      // 选择新的联系人
      selectedContactId.value = receiverId
      await loadConversation()
      
      ElMessage.success('消息发送成功')
    } else {
      ElMessage.error(response.data?.message || '发送消息失败')
    }
  } catch (error) {
    console.error('创建新会话失败:', error)
    ElMessage.error('创建新会话失败')
  }
}

// 生命周期
onMounted(async () => {
  // 调试信息：检查用户登录状态
  console.log('=== 消息中心调试信息 ===')
  console.log('localStorage.userInfo:', localStorage.getItem('userInfo'))
  console.log('localStorage.userId:', localStorage.getItem('userId'))
  console.log('getCurrentUserId():', getCurrentUserId())
  console.log('========================')
  
  await Promise.all([
    loadContacts(),
    loadUnreadCount()
  ])
})
</script>

<style scoped>
.messages-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  background: #409eff;
  color: white;
}

.logo {
  font-size: 18px;
  font-weight: bold;
}

.nav-buttons {
  display: flex;
  gap: 10px;
}

.main-container {
  flex: 1;
  height: calc(100vh - 60px);
}

.contacts-panel {
  border-right: 1px solid #ebeef5;
}

.contacts-card {
  height: 100%;
}

.contacts-list {
  height: calc(100vh - 140px);
  overflow-y: auto;
}

.contact-item {
  display: flex;
  align-items: center;
  padding: 12px;
  cursor: pointer;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.3s;
}

.contact-item:hover {
  background-color: #f5f7fa;
}

.contact-item.active {
  background-color: #e6f7ff;
}

.contact-info {
  flex: 1;
  margin-left: 12px;
  min-width: 0;
}

.contact-name {
  font-weight: 500;
  color: #303133;
}

.contact-last-message {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.contact-time {
  font-size: 12px;
  color: #c0c4cc;
  margin-left: 8px;
}

.chat-panel {
  flex: 1;
}

.chat-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chat-header .contact-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.messages-area {
  height: calc(100vh - 280px);
  overflow-y: auto;
  padding: 16px;
  background-color: #f8f9fa;
}

.message-item {
  display: flex;
  margin-bottom: 16px;
}

.message-item.is-sender {
  flex-direction: row-reverse;
}

.message-item.is-sender .message-content {
  align-items: flex-end;
}

.message-item.is-sender .message-body {
  background-color: #409eff;
  color: white;
}

.message-avatar {
  margin: 0 8px;
}

.message-content {
  max-width: 60%;
  display: flex;
  flex-direction: column;
}

.message-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
  font-size: 12px;
  color: #909399;
}

.message-body {
  padding: 8px 12px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  word-wrap: break-word;
}

.message-status {
  margin-top: 4px;
  text-align: right;
}

.message-input-area {
  padding: 16px;
}

.input-controls {
  display: flex;
  align-items: flex-end;
  gap: 8px;
}

.no-contact-selected {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.empty-contacts, .empty-messages {
  padding: 40px 20px;
  text-align: center;
}

.clearfix {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.error-text {
  color: #f56c6c;
  font-size: 12px;
  margin-top: 4px;
}
</style>
