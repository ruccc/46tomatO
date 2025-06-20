<template>
  <el-badge :value="count" :max="99" :hidden="count === 0" class="message-badge-wrapper">
    <slot />
  </el-badge>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { getUnreadCount } from '../../api/messages'

const props = defineProps({
  userId: {
    type: [Number, String],
    default: null
  },
  autoRefresh: {
    type: Boolean,
    default: true
  },
  refreshInterval: {
    type: Number,
    default: 30000 // 30秒
  }
})

const count = ref(0)
let refreshTimer = null

// 获取当前用户ID
const getCurrentUserId = () => {
  if (props.userId) return props.userId
  
  const userInfo = localStorage.getItem('userInfo')
  if (userInfo) {
    return JSON.parse(userInfo).id
  }
  return localStorage.getItem('userId') || 1
}

// 加载未读消息数
const loadUnreadCount = async () => {
  try {
    const userId = getCurrentUserId()
    const response = await getUnreadCount(userId)
    
    if (response.data && response.data.code === 200) {
      count.value = response.data.data
    }
  } catch (error) {
    console.error('获取未读消息数失败:', error)
  }
}

// 手动刷新
const refresh = () => {
  loadUnreadCount()
}

// 暴露给父组件的方法
defineExpose({
  refresh,
  count
})

onMounted(async () => {
  await loadUnreadCount()
  
  if (props.autoRefresh && props.refreshInterval > 0) {
    refreshTimer = setInterval(loadUnreadCount, props.refreshInterval)
  }
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})
</script>

<style scoped>
.message-badge-wrapper {
  display: inline-block;
}
</style>
