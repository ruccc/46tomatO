<template>
  <div class="detail-container">
    <el-header class="header">
      <div class="logo">番茄书城</div>
      <div class="nav-buttons">
        <el-button type="default" @click="$router.push('/products')">返回列表</el-button>
      </div>
    </el-header>

    <el-main class="main-content">
      <el-card class="book-detail">
        <div class="book-layout">
          <!-- 左侧封面 -->
          <div class="book-cover">
            <img :src="bookInfo.cover || defaultCover" alt="book cover">
          </div>

          <!-- 右侧信息 -->
          <div class="book-info">
            <h2>{{ bookInfo.title }}</h2>
            <el-descriptions :column="1" border>
              <el-descriptions-item label="价格">¥{{ bookInfo.price }}</el-descriptions-item>
              <!-- 添加平均评分显示 -->
              <el-descriptions-item label="评分">
                <div class="rating-display">
                  <el-rate
                    v-model="commentStats.averageRating"
                    disabled
                    show-score
                    text-color="#ff9900"
                    score-template="{value}"
                  />
                  <span class="comment-count">({{ commentStats.commentCount }}条评价)</span>
                </div>
              </el-descriptions-item>
              <el-descriptions-item label="库存">
                <span v-if="stockInfo">{{ stockInfo.amount }}</span>
                <span v-else>暂无库存信息</span>
              </el-descriptions-item>
              <el-descriptions-item label="描述">{{ bookInfo.description }}</el-descriptions-item>
              <el-descriptions-item label="详细信息">{{ bookInfo.detail }}</el-descriptions-item>
              
              <!-- 规格信息显示 -->
              <el-descriptions-item label="规格信息">
                <div v-if="bookInfo.specifications && bookInfo.specifications.length > 0">
                  <el-tag 
                    v-for="(spec, index) in bookInfo.specifications" 
                    :key="index"
                    class="spec-tag"
                    type="info"
                  >
                    {{ spec.item }}: {{ spec.value }}
                  </el-tag>
                </div>
                <span v-else>暂无规格信息</span>
              </el-descriptions-item>
            </el-descriptions>

            <!-- 所有用户可见的操作按钮 -->
            <div class="user-actions">
              <!-- 添加数量选择器 -->
              <el-input-number 
                v-model="quantity" 
                :min="1" 
                :max="stockInfo ? stockInfo.amount : 1"
                size="small"
                :disabled="!stockInfo || stockInfo.amount <= 0"
              />
              <el-button type="primary" @click="handleAddToCart" :disabled="!stockInfo || stockInfo.amount <= 0">
                加入购物车
              </el-button>
            </div>

            <!-- 管理员操作按钮 -->
            <div v-if="role === 'admin'" class="admin-actions">
              <el-button type="primary" @click="$router.push(`/products/${id}/stockpile`)">
                调整库存
              </el-button>
              <el-button type="warning" @click="$router.push(`/products/${id}/update`)">
                更新信息
              </el-button>
              <el-button type="danger" @click="handleDelete">
                删除商品
              </el-button>
            </div>
          </div>
        </div>
        
        <!-- 评论区 -->
        <div class="comments-section">
          <div class="section-title">
            <h3>用户评价 ({{ commentStats.commentCount }})</h3>
            <div class="comment-stats">
              <span class="average-rating">平均评分: {{ commentStats.averageRating.toFixed(1) }}</span>
              <el-rate
                v-model="commentStats.averageRating"
                disabled
                show-score
                text-color="#ff9900"
                score-template="{value}"
              />
            </div>
          </div>
          
          <!-- 评论表单 -->
          <div v-if="isLoggedIn && !hasCommented" class="comment-form">
            <h4>发表您的评价</h4>
            <el-form :model="commentForm" :rules="commentRules" ref="commentFormRef">
              <el-form-item prop="rating" label="评分">
                <el-rate 
                  v-model="commentForm.rating" 
                  show-score
                  :colors="['#99A9BF', '#F7BA2A', '#FF9900']"
                  text-color="#ff9900"
                  score-template="{value}"
                />
              </el-form-item>
              <el-form-item prop="content" label="评价内容">
                <el-input
                  type="textarea"
                  v-model="commentForm.content"
                  :rows="4"
                  placeholder="请分享您对这本书的评价..."
                  maxlength="500"
                  show-word-limit
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="submitComment" :loading="commentSubmitting">
                  发表评价
                </el-button>
              </el-form-item>
            </el-form>
          </div>
          
          <!-- 已评价提示 -->
          <div v-else-if="isLoggedIn && hasCommented" class="already-commented">
            <el-alert
              title="您已经对这本书发表过评价"
              type="info"
              show-icon
              :closable="false"
            />
          </div>
          
          <!-- 需要登录提示 -->
          <div v-else-if="!isLoggedIn" class="login-to-comment">
            <el-alert
              title="请先登录后再发表评价"
              type="info"
              show-icon
              :closable="false"
            >
              <template #default>
                <el-button type="primary" size="small" @click="$router.push('/login')">
                  去登录
                </el-button>
              </template>
            </el-alert>
          </div>
          
          <!-- 评论列表 -->
          <div class="comment-list">
            <div v-if="comments.length === 0" class="no-comments">
              <el-empty description="暂无评价" />
            </div>
            <el-card v-for="comment in comments" :key="comment.id" class="comment-item">
              <div class="comment-header">
                <div class="user-info">
                  <el-avatar :src="comment.userAvatar || defaultAvatar" :size="40" />
                  <div class="user-details">
                    <div class="username">{{ comment.userName }}</div>
                    <div class="comment-date">{{ formatDate(comment.createTime) }}</div>
                  </div>
                </div>
                <div class="comment-rating">
                  <el-rate
                    v-model="comment.rating"
                    disabled
                    text-color="#ff9900"
                  />
                </div>
              </div>
              <div class="comment-content">
                {{ comment.content }}
              </div>
            </el-card>
            
            <!-- 分页器 -->
            <div class="pagination-container" v-if="totalPages > 1">
              <el-pagination
                background
                layout="prev, pager, next"
                :total="totalComments"
                :page-size="pageSize"
                :current-page="currentPage"
                @current-change="handlePageChange"
              />
            </div>
          </div>
        </div>
      </el-card>
    </el-main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { getInfo, deleteInfo } from '../../api/Book/products'
import { getStockpileInfo } from '../../api/Book/stockpiles'
import { addToCart as cartApi } from '../../api/Book/cart'
import { 
  addComment, 
  getBookComments, 
  getBookCommentStats, 
  checkUserComment,
  type BookComment,
  type CommentStats
} from '../../api/Book/comments'
import defaultCover from '../../assets/tomato@1x-1.0s-200px-200px.svg'
import defaultAvatar from '../../assets/tomato@1x-1.0s-200px-200px.svg'
// 导入axios - 这是之前缺少的
import { axios } from '../../utils/request'
import { getUserId } from '../../utils/authDebug'

const route = useRoute()
const router = useRouter()
const id = route.params.id as string
const role = ref(localStorage.getItem('role') || '')
const bookInfo = ref({
  id: '',
  title: '',
  price: 0,
  rate: 0,
  description: '',
  cover: '',
  detail: '',
  stockpile: 0,
  specifications: [] as { item: string, value: string }[]
})
const stockInfo = ref<{ id: string; amount: number; frozen: number; productId: string; } | null>(null)
const quantity = ref(1) // 购买数量，默认为1

// 确保有以下变量定义
const comments = ref<BookComment[]>([]);
const commentStats = ref<CommentStats>({
  averageRating: 0,
  commentCount: 0
});
const hasCommented = ref(false);
const commentSubmitting = ref(false);
const currentPage = ref(1);
const pageSize = ref(10);
const totalPages = ref(1);
const totalComments = ref(0);

// 评论表单
const commentFormRef = ref<FormInstance>();
const commentForm = ref({
  rating: 5,
  content: ''
});
const commentRules = {
  rating: [{ required: true, message: '请选择评分', trigger: 'change' }],
  content: [
    { required: true, message: '请输入评价内容', trigger: 'blur' },
    { min: 5, max: 500, message: '评价内容长度在5到500个字符之间', trigger: 'blur' }
  ]
};

// 确保有登录状态检查
const isLoggedIn = computed(() => {
  return !!localStorage.getItem('token');
});

// 获取书籍详情
const fetchBookInfo = async () => {
  try {
    const res = await getInfo(id)
    if (res.data && res.data.code === 200) {
      bookInfo.value = res.data.data
    } else {
      ElMessage.error('获取书籍信息失败')
    }
  } catch (error) {
    ElMessage.error('获取书籍信息失败')
  }
}

// 获取库存信息
const fetchStockInfo = async () => {
  try {
    const res = await getStockpileInfo(id)
    if (res.data && res.data.code === 200) {
      stockInfo.value = res.data.data
    } else {
      stockInfo.value = null
    }
  } catch (error) {
    stockInfo.value = null
  }
}

// 删除书籍
const handleDelete = () => {
  ElMessageBox.confirm('确定要删除这本书吗？此操作不可恢复', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteInfo(id)
      if (res.data && res.data.code === 200) {
        ElMessage.success('删除成功')
        router.push('/products')
      } else {
        ElMessage.error(res.data.msg || '删除失败')
      }
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {
    ElMessage.info('已取消删除')
  })
}

// 添加到购物车（重命名以避免冲突）
const handleAddToCart = async () => {
  try {
    if (!stockInfo.value || stockInfo.value.amount <= 0) {
      ElMessage.warning('商品库存不足，无法加入购物车')
      return
    }

    if (quantity.value > stockInfo.value.amount) {
      ElMessage.warning(`当前可用库存仅${stockInfo.value.amount}，请减少购买数量`)
      return
    }

    const res = await cartApi(id, quantity.value)
    if (res.data && res.data.code === 200) {
      // 更新成功消息显示，并添加确认对话框
      const isExisting = res.data.data.isExistingItem
      if (isExisting) {
        // 如果是已存在的商品，显示合并信息
        ElMessageBox.confirm(
          `成功将《${bookInfo.value.title}》添加到购物车！商品数量已合并更新。`,
          '添加成功',
          {
            confirmButtonText: '去购物车',
            cancelButtonText: '继续购物',
            type: 'success',
          }
        ).then(() => {
          // 用户点击"去购物车"
          router.push('/cart')
        }).catch(() => {
          // 用户点击"继续购物"，不做操作
        })
      } else {
        // 新增商品
        ElMessageBox.confirm(
          `成功将《${bookInfo.value.title}》添加到购物车！`,
          '添加成功',
          {
            confirmButtonText: '去购物车',
            cancelButtonText: '继续购物',
            type: 'success',
          }
        ).then(() => {
          // 用户点击"去购物车"
          router.push('/cart')
        }).catch(() => {
          // 用户点击"继续购物"，不做操作
        })
      }
    } else {
      ElMessage.error(res.data?.msg || '添加到购物车失败')
    }
  } catch (error) {
    console.error('添加到购物车时发生错误:', error)
    ElMessage.error('添加到购物车失败')
  }
}

// 修改评论提交函数，使其能正确与后端交互并保存评论
const submitComment = async () => {
  if (!commentFormRef.value) return;
  
  await commentFormRef.value.validate(async (valid) => {
    if (!valid) {
      ElMessage.warning('请正确填写评价内容');
      return;
    }
    
    if (!isLoggedIn.value) {
      ElMessage.warning('请先登录后再发表评价');
      return;
    }
    
    commentSubmitting.value = true;    try {
      console.log(`提交评论，书籍ID: ${id}`);
      
      // 使用URL查询参数方式传递，后端会自动从会话中获取当前用户ID
      const response = await axios({
        method: 'post',
        url: '/api/books/comments',
        params: {
          bookId: id,
          content: commentForm.value.content,
          rating: commentForm.value.rating
        }
      });
      
      console.log('评论提交响应:', response);
      
      if (response.data && response.data.code === 200) {
        ElMessage.success('评价发表成功');
        
        // 重置表单
        commentForm.value.content = '';
        commentForm.value.rating = 5;
        
        // 刷新评论列表和统计
        await fetchComments(1);
        await fetchCommentStats();
      } else {
        throw new Error(response.data?.msg || '评论提交失败');
      }
    } catch (error: any) {
      console.error('评论提交失败:', error);
      
      // 在前端本地显示评论，提供良好的用户体验
      const username = localStorage.getItem('username') || '匿名用户';
      const localComment: BookComment = {
        id: 'local-' + Date.now(),
        bookId: id,
        bookTitle: bookInfo.value.title,
        bookCover: bookInfo.value.cover || defaultCover,
        userId: -1,
        userName: username,
        userAvatar: defaultAvatar,
        content: commentForm.value.content,
        rating: commentForm.value.rating,
        createTime: new Date().toISOString(),
        updateTime: null
      };
      
      // 显示错误消息
      ElMessage.warning('评论已在本地显示，但未能保存到数据库');
      
      // 在列表顶部显示本地评论
      comments.value.unshift(localComment);
      
      // 更新前端统计
      const newTotal = commentStats.value.commentCount + 1;
      const newAvg = 
        (commentStats.value.averageRating * commentStats.value.commentCount + commentForm.value.rating) 
        / newTotal;
      
      commentStats.value = {
        commentCount: newTotal,
        averageRating: newAvg
      };
      
      // 重置表单
      commentForm.value.content = '';
      commentForm.value.rating = 5;
    } finally {
      commentSubmitting.value = false;
    }
  });
};

// 获取评论列表
const fetchComments = async (page = 1) => {
  try {
    console.log(`获取商品 ${id} 的评论，页码: ${page-1}`);
    
    const response = await axios.get(`/api/books/comments/${id}`, {
      params: {
        page: page - 1,
        size: pageSize.value
      }
    });
    
    if (response.data && response.data.code === 200) {
      const data = response.data.data;
      comments.value = data.content || [];
      totalPages.value = data.totalPages || 1;
      totalComments.value = data.totalElements || 0;
      currentPage.value = page;
    } else {
      console.error('获取评论列表失败:', response.data);
      comments.value = [];
    }
  } catch (error) {
    console.error('获取评论列表异常:', error);
    comments.value = [];
  }
};

// 获取评论统计
const fetchCommentStats = async () => {
  try {
    const response = await axios.get(`/api/books/comments/${id}/stats`);
    
    if (response.data && response.data.code === 200) {
      commentStats.value = response.data.data;
    } else {
      commentStats.value = {
        averageRating: 0,
        commentCount: 0
      };
    }
  } catch (error) {
    console.error('获取评论统计失败:', error);
    commentStats.value = {
      averageRating: 0,
      commentCount: 0
    };
  }
};

// 简化onMounted钩子，确保页面正常加载
onMounted(() => {
  console.log('商品详情组件挂载开始...');
  
  fetchBookInfo();
  fetchStockInfo();
  fetchComments();
  fetchCommentStats();
  
  // 确保评论表单可见
  hasCommented.value = false;
});

// 格式化日期的辅助函数
const formatDate = (dateString: string): string => {
  if (!dateString) return '未知时间';
  
  try {
    const date = new Date(dateString);
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
  } catch {
    return dateString;
  }
};

// 分页处理函数
const handlePageChange = (page: number) => {
  fetchComments(page);
};
</script>

<style scoped>
.detail-container {
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

.nav-buttons {
  display: flex;
  gap: 10px;
}

.main-content {
  flex: 1;
  padding: 20px;
  background-color: #faf3e0;
}

.book-detail {
  max-width: 1200px;
  margin: 0 auto;
}

.book-layout {
  display: flex;
  gap: 40px;
  padding: 20px;
}

.book-cover {
  flex-shrink: 0;
  width: 300px;
  height: 400px;
}

.book-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 8px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.book-info {
  flex: 1;
}

.book-info h2 {
  margin-bottom: 20px;
  color: #333;
}

.admin-actions {
  margin-top: 20px;
  display: flex;
  gap: 10px;
}

.user-actions {
  margin-top: 20px;
  display: flex;
  gap: 15px;
  align-items: center;
}

.spec-tag {
  margin-right: 5px;
}

/* 评论区样式 */
.comments-section {
  margin-top: 40px;
  border-top: 1px solid #ebeef5;
  padding-top: 20px;
}

.section-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-title h3 {
  margin: 0;
  font-size: 18px;
}

.comment-stats {
  display: flex;
  align-items: center;
}

.average-rating {
  margin-right: 10px;
  font-size: 14px;
  color: #606266;
}

.comment-form {
  margin-bottom: 30px;
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 8px;
}

.comment-form h4 {
  margin-top: 0;
  margin-bottom: 15px;
  color: #303133;
}

.already-commented, .login-to-comment {
  margin-bottom: 30px;
}

.comment-item {
  margin-bottom: 15px;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 10px;
}

.user-info {
  display: flex;
  align-items: center;
}

.user-details {
  margin-left: 10px;
}

.username {
  font-weight: bold;
  color: #303133;
}

.comment-date {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.comment-content {
  color: #606266;
  line-height: 1.6;
  margin-top: 10px;
  white-space: pre-line;
}

.no-comments {
  padding: 20px 0;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.rating-display {
  display: flex;
  align-items: center;
}

.comment-count {
  margin-left: 8px;
  color: #909399;
  font-size: 14px;
}
</style>