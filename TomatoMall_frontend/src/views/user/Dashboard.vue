<template>
  <div class="dashboard-container">
    <el-row :gutter="20">
      <!-- 用户基本信息卡片 -->
      <el-col :span="8">
        <el-card class="box-card">
          <div slot="header" class="clearfix">
            <span>个人信息</span>
            <el-button style="float: right; padding: 3px 0" type="text" @click="showEditForm">修改信息</el-button>
          </div>
          <div class="user-profile">
            <div class="avatar-container">
              <img :src="avatarUrl || defaultAvatar" alt="用户头像" class="user-avatar">
            </div>
            <div class="user-info">
              <h3>{{ userInfo.name || '未设置姓名' }}</h3>
              <p>账号: {{ userInfo.account || userInfo.email || '未设置' }}</p>
              <p>邮箱: {{ userInfo.email || '未设置' }}</p>
              <p>手机: {{ userInfo.phone || '未设置' }}</p>
              <p>注册时间: {{ formatDate(userInfo.createTime) }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <!-- 会员信息卡片 -->
      <el-col :span="16">
        <el-card class="box-card" v-if="memberInfo">
          <div slot="header" class="clearfix">
            <span>会员信息</span>
            <el-button style="float: right; padding: 3px 0; margin-left: 10px;" type="text" 
                      @click="deleteMemberShip" v-if="memberInfo">取消会员</el-button>
          </div>
          
          <el-row class="member-info-row">
            <el-col :span="8">
              <div class="info-item">
                <div class="label">会员编号</div>
                <div class="value">{{ memberInfo.memberNo }}</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="info-item">
                <div class="label">会员等级</div>
                <div class="value">
                  <el-tag :type="getLevelTag(memberInfo.level)">
                    {{ memberInfo.levelInfo ? memberInfo.levelInfo.levelName : `等级${memberInfo.level}` }}
                  </el-tag>
                </div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="info-item">
                <div class="label">会员状态</div>
                <div class="value">
                  <el-tag :type="memberInfo.status === 1 ? 'success' : 'danger'">
                    {{ memberInfo.status === 1 ? '正常' : '禁用' }}
                  </el-tag>
                </div>
              </div>
            </el-col>
          </el-row>
          
          <el-row class="member-info-row">
            <el-col :span="8">
              <div class="info-item">
                <div class="label">当前积分</div>
                <div class="value points">{{ memberInfo.points }}</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="info-item">
                <div class="label">成长值</div>
                <div class="value growth">{{ memberInfo.growthValue }}</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="info-item">
                <div class="label">升级还需</div>
                <div class="value" v-if="nextLevelInfo">
                  {{ nextLevelInfo.growthPoint - memberInfo.growthValue }} 成长值
                </div>
                <div class="value" v-else>
                  已是最高等级
                </div>
              </div>
            </el-col>
          </el-row>
          
          <el-progress 
            :percentage="levelProgress" 
            :stroke-width="18"
            :color="customColorMethod">
          </el-progress>
          
          <el-divider content-position="center">会员特权</el-divider>
          
          <el-row class="member-benefits-row">
            <el-col :span="8" v-for="(benefit, index) in memberBenefits" :key="index">
              <div class="benefit-item">
                <i :class="benefit.icon"></i>
                <div class="benefit-text">{{ benefit.text }}</div>
              </div>
            </el-col>
          </el-row>
        </el-card>
        
        <!-- 非会员提示卡片 -->
        <el-card class="box-card" v-else>
          <div slot="header" class="clearfix">
            <span>会员特权</span>
            <el-button style="float: right; padding: 3px 0" type="text" @click="goToCreateMember">
              立即加入会员
            </el-button>
          </div>
          <div class="non-member-tip">
            <i class="el-icon-star-off"></i>
            <h3>尚未成为会员</h3>
            <p>成为会员即可享受购书折扣、积分累积、等级特权等多重福利</p>
            <el-button type="primary" @click="goToCreateMember">立即成为会员</el-button>
          </div>
        </el-card>
        
        <!-- 最近订单或其他内容... -->
      </el-col>
    </el-row>
    
    <!-- 用户信息编辑表单 -->
    <el-dialog title="修改个人信息" :visible.sync="editFormVisible" width="50%">
      <el-form ref="editForm" :model="editForm" :rules="rules" label-width="100px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="editForm.name"></el-input>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editForm.email"></el-input>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="editForm.phone"></el-input>
        </el-form-item>
        <el-form-item label="地址" prop="address" v-if="memberInfo">
          <el-input v-model="editForm.address"></el-input>
        </el-form-item>
        <el-form-item label="生日" prop="birthday" v-if="memberInfo">
          <el-date-picker v-model="editForm.birthday" type="date" 
                          placeholder="选择生日" format="yyyy-MM-dd" value-format="yyyy-MM-dd">
          </el-date-picker>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="editFormVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEditForm">确定</el-button>
      </div>
    </el-dialog>
    
    <!-- 删除会员确认对话框 -->
    <el-dialog
      title="取消会员确认"
      :visible.sync="deleteMemberDialogVisible"
      width="30%">
      <span>确定要取消您的会员资格吗？这将导致您失去所有会员特权和积累的积分。</span>
      <div slot="footer" class="dialog-footer">
        <el-button @click="deleteMemberDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmDeleteMember">确定取消会员</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import { formatDate } from '@/utils/date'
import { getMemberById, updateMember, deleteMember } from '@/api/members/member'

export default {
  name: 'Dashboard',
  data() {
    return {
      userInfo: {},
      memberInfo: null,
      nextLevelInfo: null,
      editFormVisible: false,
      deleteMemberDialogVisible: false,
      defaultAvatar: require('@/assets/default-avatar.png'),
      editForm: {
        name: '',
        email: '',
        phone: '',
        address: '',
        birthday: ''
      },
      rules: {
        name: [
          { required: true, message: '请输入姓名', trigger: 'blur' },
          { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
        ],
        email: [
          { required: true, message: '请输入邮箱地址', trigger: 'blur' },
          { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
        ],
        phone: [
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
        ]
      },
      memberLevels: [
        { id: 1, levelName: '普通会员', growthPoint: 0, discount: 0.95, description: '注册即可成为' },
        { id: 2, levelName: '白银会员', growthPoint: 1000, discount: 0.9, description: '消费满1000成长值' },
        { id: 3, levelName: '黄金会员', growthPoint: 5000, discount: 0.85, description: '消费满5000成长值' },
        { id: 4, levelName: '钻石会员', growthPoint: 10000, discount: 0.8, description: '消费满10000成长值' }
      ],
      memberBenefits: [
        { icon: 'el-icon-shopping-cart-full', text: '购书专属折扣' },
        { icon: 'el-icon-star-on', text: '积分累积升级' },
        { icon: 'el-icon-present', text: '会员专享活动' },
        { icon: 'el-icon-time', text: '优先预约新书' },
        { icon: 'el-icon-goods', text: '生日专属礼品' },
        { icon: 'el-icon-medal', text: '专属客服服务' }
      ],
      levelTags: {
        1: '',
        2: 'success',
        3: 'warning',
        4: 'danger'
      }
    }
  },
  computed: {
    ...mapGetters([
      'userId',
      'userName',
      'avatarUrl'
    ]),
    levelProgress() {
      if (!this.memberInfo || !this.nextLevelInfo) {
        return 100
      }
      
      const currentGrowth = this.memberInfo.growthValue
      const currentLevelGrowth = this.getCurrentLevelGrowthPoint()
      const nextLevelGrowth = this.nextLevelInfo.growthPoint
      
      const progress = ((currentGrowth - currentLevelGrowth) / (nextLevelGrowth - currentLevelGrowth)) * 100
      return Math.min(Math.max(progress, 0), 100)
    }
  },
  created() {
    this.fetchUserInfo()
    this.fetchMemberInfo()
  },
  methods: {
    formatDate,
    getLevelTag(level) {
      return this.levelTags[level] || ''
    },
    fetchUserInfo() {
      // 获取用户基本信息
      this.userInfo = {
        id: this.userId,
        name: this.userName,
        email: '',
        phone: '',
        createTime: new Date()
      }
      // TODO: 通过API获取用户详细信息
    },
    fetchMemberInfo() {
      // 获取会员信息
      if (!this.userId) return
      
      getMemberById(this.userId).then(response => {
        if (response.data.code === 200) {
          this.memberInfo = response.data.data
          this.findNextLevel()
        }
      }).catch(error => {
        console.error('获取会员信息出错:', error)
        // 不需要提示错误，因为可能用户不是会员
      })
    },
    findNextLevel() {
      if (!this.memberInfo) return
      
      const currentLevel = this.memberInfo.level
      const nextLevel = this.memberLevels.find(level => level.id > currentLevel)
      this.nextLevelInfo = nextLevel
    },
    getCurrentLevelGrowthPoint() {
      const currentLevel = this.memberInfo.level
      const level = this.memberLevels.find(lvl => lvl.id === currentLevel)
      return level ? level.growthPoint : 0
    },
    customColorMethod(percentage) {
      if (percentage < 30) {
        return '#909399'
      } else if (percentage < 70) {
        return '#67c23a'
      } else {
        return '#f56c6c'
      }
    },
    showEditForm() {
      this.editForm = {
        name: this.userInfo.name || '',
        email: this.userInfo.email || '',
        phone: this.userInfo.phone || ''
      }
      
      // 如果是会员，添加会员特有字段
      if (this.memberInfo) {
        this.editForm.id = this.memberInfo.id
        this.editForm.address = this.memberInfo.address || ''
        this.editForm.birthday = this.memberInfo.birthday || ''
      }
      
      this.editFormVisible = true
    },
    submitEditForm() {
      this.$refs.editForm.validate((valid) => {
        if (valid) {
          // 更新基本用户信息
          // TODO: 调用用户信息更新API
          
          // 如果是会员，同时更新会员信息
          if (this.memberInfo) {
            // 添加必要字段
            const memberUpdateData = {
              ...this.editForm,
              id: this.memberInfo.id,
              status: this.memberInfo.status
            }
            
            updateMember(memberUpdateData).then(response => {
              if (response.data.code === 200) {
                this.$message.success('会员信息更新成功')
                // 刷新会员信息
                this.fetchMemberInfo()
              } else {
                this.$message.error(response.data.message || '会员信息更新失败')
              }
            }).catch(error => {
              console.error('更新会员信息出错:', error)
              this.$message.error('会员信息更新失败')
            })
          }
          
          this.editFormVisible = false
          this.$message.success('个人信息更新成功')
        } else {
          this.$message.warning('请完善表单信息')
          return false
        }
      })
    },
    goToCreateMember() {
      this.$router.push('/member/create')
    },
    deleteMemberShip() {
      this.deleteMemberDialogVisible = true
    },
    confirmDeleteMember() {
      if (!this.memberInfo) return
      
      deleteMember(this.memberInfo.id).then(response => {
        if (response.data.code === 200) {
          this.$message.success('已成功取消会员资格')
          this.memberInfo = null
        } else {
          this.$message.error(response.data.message || '取消会员失败')
        }
      }).catch(error => {
        console.error('取消会员出错:', error)
        this.$message.error('取消会员失败')
      }).finally(() => {
        this.deleteMemberDialogVisible = false
      })
    }
  }
}
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
  margin-right: 20px;
}
.user-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
}
.user-info h3 {
  margin-top: 0;
  margin-bottom: 10px;
}
.user-info p {
  margin: 5px 0;
  color: #606266;
}
.member-info-row {
  margin-bottom: 20px;
}
.info-item {
  text-align: center;
  padding: 10px;
}
.info-item .label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 5px;
}
.info-item .value {
  font-size: 18px;
  font-weight: bold;
}
.points {
  color: #f56c6c;
}
.growth {
  color: #67c23a;
}
.member-benefits-row {
  margin-top: 20px;
}
.benefit-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 20px;
}
.benefit-item i {
  font-size: 30px;
  color: #409EFF;
  margin-bottom: 10px;
}
.benefit-text {
  font-size: 14px;
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
</style>
