<template>
  <div class="advertisement-list">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>广告列表</span>
          <el-button v-if="isAdmin" type="primary" @click="handleCreate">新建广告</el-button>
        </div>
      </template>
      
      <!-- 空状态显示 -->
      <div v-if="!loading && (!advertisements || advertisements.length === 0)" class="empty-state">
        <el-empty description="暂无广告" />
      </div>

      <!-- 广告列表 -->      <el-table v-else :data="advertisements" style="width: 100%">
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="content" label="内容" show-overflow-tooltip />
        <el-table-column label="图片">
          <template #default="{ row }">
            <el-image :src="row.imgUrl" style="width: 100px" fit="contain" />
          </template>
        </el-table-column>
        
        <!-- 操作列 - 只对管理员显示 -->
        <el-table-column v-if="isAdmin" label="操作" width="150">
          <template #default="{ row }">
            <el-button 
              type="danger" 
              size="small" 
              @click="handleDelete(row)"
              :loading="row.deleting">
              删除
            </el-button>
            <el-button
              type="primary"
              size="small"
              @click="handleEdit(row)">
              编辑
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { getAdvertisements, deleteAdvertisement } from '../../api/advertisement/index'
import type { Advertisement as BaseAdvertisement } from '../../api/advertisement/index'

// 扩展Advertisement接口，添加UI状态属性
interface Advertisement extends BaseAdvertisement {
  deleting?: boolean;
}

const router = useRouter()
const advertisements = ref<Advertisement[]>([])
const isAdmin = ref(localStorage.getItem('role') === 'admin')
const loading = ref(false)

const fetchAdvertisements = async () => {
  loading.value = true
  try {
    const response = await getAdvertisements()
    console.log('广告列表响应:', response)
    if (response.data && response.data.code === 200) {
      advertisements.value = response.data.data
    } else {
      console.error('获取广告列表失败，响应:', response.data)
      ElMessage.error(response.data?.msg || '获取广告列表失败')
    }
  } catch (error: any) {
    console.error('获取广告列表失败:', error)
    if (error.response?.status === 401) {
      ElMessage.error('登录已过期，请重新登录')
      router.push('/login')
    } else {
      ElMessage.error(error.response?.data?.msg || '获取广告列表失败')
    }
  } finally {
    loading.value = false
  }
}

// 注意：已移除未使用的handleViewProduct方法

const handleCreate = () => {
  router.push('/advertisement/create')
}

const handleEdit = (row: Advertisement) => {
  router.push(`/advertisement/edit/${row.id}`)
}

const handleDelete = async (row: Advertisement) => {
  try {
    // 确认删除操作
    await ElMessageBox.confirm('确定要删除这个广告吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 设置当前行的删除状态为加载中
    row.deleting = true
    
    // 调用删除API
    const response = await deleteAdvertisement(row.id!)
    if (response.data && response.data.code === 200) {
      ElMessage.success('广告删除成功')
      // 重新获取广告列表
      await fetchAdvertisements()
    } else {
      ElMessage.error(response.data?.msg || '删除广告失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除广告失败:', error)
      ElMessage.error(error?.message || '删除操作失败')
    }
  } finally {
    // 确保无论成功或失败，都重置删除状态
    row.deleting = false
  }
}

onMounted(() => {
  fetchAdvertisements()
})
</script>

<style scoped>
.advertisement-list {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.empty-state {
  padding: 40px 0;
  text-align: center;
}
</style>