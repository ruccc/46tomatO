<template>
  <div class="create-advertisement">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>新建广告</span>
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
          <el-button type="primary" @click="handleSubmit">创建</el-button>
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
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { createAdvertisement } from '../../api/advertisement/index'
import type { Advertisement } from '../../api/advertisement/index'

const router = useRouter()
const formRef = ref<FormInstance>()
const previewVisible = ref(false)

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
    { required: true, message: '请输入图片链接', trigger: 'blur' },
    { type: 'url', message: '请输入有效的URL地址', trigger: 'blur' }
  ],
  productId: [
    { required: true, message: '请输入商品ID', trigger: 'blur' }
  ]
})

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate()
  try {
    await createAdvertisement(form)
    ElMessage.success('创建成功')
    router.push('/advertisement/list')
  } catch (error) {
    ElMessage.error('创建失败')
  }
}

const handleCancel = () => {
  router.back()
}

const handlePreview = () => {
  previewVisible.value = true
}
</script>

<style scoped>
.create-advertisement {
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