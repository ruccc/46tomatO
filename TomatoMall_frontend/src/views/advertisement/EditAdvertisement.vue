<template>
  <div class="edit-advertisement">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>编辑广告</span>
        </div>
      </template>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px" class="ad-form">
        <el-form-item label="广告标题" prop="title" required>
          <el-input v-model="form.title" placeholder="请输入广告标题" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="广告内容" prop="content" required>
          <el-input v-model="form.content" type="textarea" :rows="4" placeholder="请输入广告内容" maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item label="图片链接" prop="imgUrl" required>
          <el-input v-model="form.imgUrl" placeholder="请输入图片链接">
            <template #append>
              <el-button @click="handlePreview">预览</el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="商品ID" prop="productId" required>
          <el-input v-model="form.productId" placeholder="请输入关联的商品ID" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">保存</el-button>
          <el-button @click="handleCancel">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-dialog v-model="previewVisible" title="图片预览" width="50%">
      <div class="preview-container">
        <el-image v-if="form.imgUrl" :src="form.imgUrl" fit="contain" />
        <div v-else class="no-image">暂无图片</div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getAdvertisements, updateAdvertisement } from '../../api/advertisement/index'
import type { Advertisement } from '../../api/advertisement/index'

const route = useRoute()
const router = useRouter()
const formRef = ref<FormInstance>()
const previewVisible = ref(false)
const loading = ref(false)
const submitting = ref(false)

const form = reactive<Advertisement>({
  title: '',
  content: '',
  imgUrl: '',
  productId: ''
})

const rules = reactive<FormRules>({
  title: [
    { required: true, message: '请输入广告标题', trigger: 'blur' },
    { max: 50, message: '标题长度不能超过50个字符', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入广告内容', trigger: 'blur' },
    { max: 500, message: '内容长度不能超过500个字符', trigger: 'blur' }
  ],
  imgUrl: [
    { required: true, message: '请输入图片链接', trigger: 'blur' }
  ],
  productId: [
    { required: true, message: '请输入商品ID', trigger: 'blur' }
  ]
})

const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    submitting.value = true
    console.log('提交的广告数据:', { ...form, id: route.params.id })
    const response = await updateAdvertisement({ ...form, id: route.params.id as string })
    console.log('更新广告响应:', response)
    if (response.data && response.data.code === 200) {
      ElMessage.success('更新成功')
      router.push('/advertisement/list')
    } else {
      ElMessage.error(response.data?.msg || '更新失败')
    }
  } catch (error: any) {
    console.error('更新广告失败:', error)
    if (error.response?.status === 404) {
      ElMessage.error('广告不存在')
    } else if (error.response?.status === 400) {
      ElMessage.error(error.response.data?.msg || '请求参数错误')
    } else {
      ElMessage.error('更新失败，请稍后重试')
    }
  } finally {
    submitting.value = false
  }
}

const handleCancel = () => {
  router.back()
}

const handlePreview = () => {
  previewVisible.value = true
}

onMounted(async () => {
  loading.value = true
  try {
    const response = await getAdvertisements()
    console.log('获取广告列表响应:', response)
    if (response.data && response.data.code === 200) {
      const currentAd = response.data.data.find((ad: Advertisement) => ad.id === route.params.id)
      if (currentAd) {
        Object.assign(form, currentAd)
      } else {
        ElMessage.error('广告不存在')
        router.back()
      }
    } else {
      ElMessage.error(response.data?.msg || '获取广告信息失败')
      router.back()
    }
  } catch (error: any) {
    console.error('获取广告信息失败:', error)
    if (error.response?.status === 401) {
      ElMessage.error('登录已过期，请重新登录')
      router.push('/login')
    } else {
      ElMessage.error('获取广告信息失败')
      router.back()
    }
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.edit-advertisement {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}
.card-header {
  font-size: 18px;
  font-weight: bold;
}
.ad-form {
  margin-top: 20px;
}
.preview-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
}
.no-image {
  color: #909399;
  font-size: 14px;
}
:deep(.el-form-item__content) {
  flex-wrap: nowrap;
}
</style>