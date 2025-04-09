<template>
  <div class="create-container">
    <el-header class="header">
      <div class="logo">番茄书城</div>
      <div class="nav-buttons">
        <el-button type="default" @click="$router.push('/products')">返回列表</el-button>
      </div>
    </el-header>

    <el-main class="main-content">
      <el-card class="create-form">
        <template #header>
          <h2>添加新书籍</h2>
        </template>

        <el-form :model="bookForm" label-width="120px" :rules="rules" ref="bookFormRef">
          <el-form-item label="书名" prop="title">
            <el-input v-model="bookForm.title" placeholder="请输入书名"></el-input>
          </el-form-item>
          
          <el-form-item label="价格" prop="price">
            <el-input-number v-model="bookForm.price" :precision="2" :min="0"></el-input-number>
          </el-form-item>
          
          <el-form-item label="评分" prop="rate">
            <el-input-number v-model="bookForm.rate" :precision="1" :min="0" :max="10"></el-input-number>
          </el-form-item>
          
          <el-form-item label="描述" prop="description">
            <el-input v-model="bookForm.description" type="textarea" placeholder="请输入书籍简短描述"></el-input>
          </el-form-item>
          
          <el-form-item label="封面URL" prop="cover">
            <el-input v-model="bookForm.cover" placeholder="请输入封面图片URL"></el-input>
          </el-form-item>
          
          <el-form-item label="详细说明" prop="detail">
            <el-input v-model="bookForm.detail" type="textarea" placeholder="请输入详细说明"></el-input>
          </el-form-item>
          
          <el-divider>规格信息</el-divider>
          
          <div v-for="(spec, index) in specs" :key="index" class="spec-item">
            <el-row :gutter="10">
              <el-col :span="10">
                <el-form-item :label="'规格名称 ' + (index + 1)" :prop="'specifications.' + index + '.item'" :rules="[{ required: true, message: '规格名称不能为空', trigger: 'blur' }]">
                  <el-input v-model="spec.item" placeholder="如：作者、副标题、ISBN等"></el-input>
                </el-form-item>
              </el-col>
              <el-col :span="10">
                <el-form-item :label="'规格内容 ' + (index + 1)" :prop="'specifications.' + index + '.value'" :rules="[{ required: true, message: '规格内容不能为空', trigger: 'blur' }]">
                  <el-input v-model="spec.value" placeholder="请输入对应的内容"></el-input>
                </el-form-item>
              </el-col>
              <el-col :span="4" class="spec-actions">
                <el-button type="danger" @click="removeSpec(index)" :disabled="specs.length <= 1">删除</el-button>
              </el-col>
            </el-row>
          </div>
          
          <el-form-item>
            <el-button type="primary" @click="addSpec">添加规格</el-button>
          </el-form-item>
          
          <el-divider>库存信息</el-divider>
          
          <el-form-item label="初始库存" prop="initialStock">
            <el-input-number v-model="initialStock" :min="0" placeholder="请输入初始库存数量"></el-input-number>
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="submitForm">创建书籍</el-button>
            <el-button @click="resetForm">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </el-main>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { addInfo, type Specification } from '../../api/Book/products'
import { adjustStockpile } from '../../api/Book/stockpiles'

const router = useRouter()
const bookFormRef = ref<FormInstance>()
const initialStock = ref(0)

// 默认规格
const defaultSpec = (): Specification => {
  return {
    id: '', // 新建时不需要ID，后端会生成
    item: '',
    value: '',
    productId: '' // 新建时不需要productId，后端会关联
  }
}

// 表单数据
const bookForm = reactive({
  title: '',
  price: 0,
  rate: 0,
  description: '',
  cover: '',
  detail: ''
})

// 规格列表
const specs = ref<Specification[]>([defaultSpec()])

// 表单验证规则
const rules = {
  title: [{ required: true, message: '请输入书名', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  rate: [{ required: true, message: '请输入评分', trigger: 'blur' }]
}

// 添加规格
const addSpec = () => {
  specs.value.push(defaultSpec())
}

// 删除规格
const removeSpec = (index: number) => {
  specs.value.splice(index, 1)
}

// 提交表单
const submitForm = async () => {
  if (!bookFormRef.value) return
  
  await bookFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // 准备提交数据
        const formData = {
          ...bookForm,
          specifications: new Set(specs.value.filter(spec => spec.item && spec.value))
        }
        
        const res = await addInfo(formData)
        if (res.data && res.data.code === 200) {
          const newProductId = res.data.data.id
          
          // 如果设置了初始库存，创建库存记录
          if (initialStock.value > 0) {
            try {
              await adjustStockpile(newProductId, initialStock.value)
            } catch (error) {
              ElMessage.warning('商品创建成功，但设置初始库存失败')
            }
          }
          
          ElMessage.success('添加成功')
          router.push(`/products/${newProductId}`)
        } else {
          ElMessage.error(res.data.msg || '添加失败')
        }
      } catch (error) {
        ElMessage.error('添加失败')
      }
    } else {
      ElMessage.error('请填写完整信息')
    }
  })
}

// 重置表单
const resetForm = () => {
  if (bookFormRef.value) {
    bookFormRef.value.resetFields()
  }
  specs.value = [defaultSpec()]
  initialStock.value = 0
}
</script>

<style scoped>
.create-container {
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
  background-color: #faf3e0;
}

.create-form {
  max-width: 900px;
  margin: 0 auto;
}

.spec-item {
  margin-bottom: 10px;
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 4px;
}

.spec-actions {
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
