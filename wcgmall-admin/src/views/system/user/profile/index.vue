<template>
  <div class="app-container">
    <el-row :gutter="20">
      <!-- 个人信息卡片 -->
      <el-col :span="8">
        <el-card class="profile-card" :body-style="{ padding: '0px' }">
          <div class="profile-header">
            <div class="header-backdrop"></div>
            <div class="header-content">
              <el-avatar 
                :size="100" 
                :src="userInfo.sysUser.avatar" 
                class="profile-avatar"
              />
              <h2 class="profile-name">{{ userInfo.sysUser.nickname }}</h2>
            </div>
          </div>
          <div class="profile-info">
            <ul class="profile-list">
              <li>
                <div class="info-label">
                  <el-icon><User /></el-icon>
                  <span>用户名称</span>
                </div>
                <div class="info-content">{{ userInfo.sysUser.username }}</div>
              </li>
              <li>
                <div class="info-label">
                  <el-icon><Iphone /></el-icon>
                  <span>手机号码</span>
                </div>
                <div class="info-content">{{ userInfo.sysUser.mobile || '未设置' }}</div>
              </li>
              <li>
                <div class="info-label">
                  <el-icon><Message /></el-icon>
                  <span>用户邮箱</span>
                </div>
                <div class="info-content">{{ userInfo.sysUser.email || '未设置' }}</div>
              </li>
              <li>
                <div class="info-label">
                  <el-icon><UserFilled /></el-icon>
                  <span>所属角色</span>
                </div>
                <div class="info-content">{{ userInfo.roles.join(',') || '未设置' }}</div>
              </li>
              <li>
                <div class="info-label">
                  <el-icon><Calendar /></el-icon>
                  <span>创建日期</span>
                </div>
                <div class="info-content">{{ userInfo.sysUser.createTime }}</div>
              </li>
            </ul>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧标签页 -->
      <el-col :span="16">
        <el-card class="tab-card">
          <el-tabs v-model="activeTab" class="profile-tabs">
            <!-- 基本资料 -->
            <el-tab-pane label="基本资料" name="basic">
              <el-form
                ref="userFormRef"
                :model="userForm"
                :rules="userRules"
                label-width="100px"
                class="profile-form"
              >
                <el-form-item label="用户昵称" prop="nickname">
                  <el-input 
                    v-model="userForm.nickname" 
                    maxlength="30"
                    placeholder="请输入用户昵称"
                  />
                </el-form-item>
                <el-form-item label="手机号码" prop="mobile">
                  <el-input 
                    v-model="userForm.mobile" 
                    maxlength="11"
                    placeholder="请输入手机号码"
                  />
                </el-form-item>
                <el-form-item label="邮箱" prop="email">
                  <el-input 
                    v-model="userForm.email" 
                    maxlength="50"
                    placeholder="请输入邮箱地址"
                  />
                </el-form-item>
                <el-form-item label="性别">
                  <el-radio-group v-model="userForm.sex">
                    <el-radio :value="1">男</el-radio>
                    <el-radio :value="2">女</el-radio>
                  </el-radio-group>
                </el-form-item>
                <el-form-item>
                  <el-button 
                    v-permission="['sys:user:update']"
                    type="primary" 
                    @click="submitUserForm"
                    :loading="submitLoading"
                  >
                    <el-icon><Check /></el-icon>
                    保存更改
                  </el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>

            <!-- 修改密码 -->
            <el-tab-pane label="修改密码" name="password">
              <el-form
                ref="pwdFormRef"
                :model="pwdForm"
                :rules="pwdRules"
                label-width="100px"
                class="profile-form"
              >
                <el-form-item label="旧密码" prop="oldPassword">
                  <el-input
                    v-model="pwdForm.oldPassword"
                    type="password"
                    placeholder="请输入旧密码"
                    show-password
                  />
                </el-form-item>
                <el-form-item label="新密码" prop="newPassword">
                  <el-input
                    v-model="pwdForm.newPassword"
                    type="password"
                    placeholder="请输入新密码"
                    show-password
                  />
                </el-form-item>
                <el-form-item label="确认密码" prop="confirmPassword">
                  <el-input
                    v-model="pwdForm.confirmPassword"
                    type="password"
                    placeholder="请确认新密码"
                    show-password
                  />
                </el-form-item>
                <el-form-item>
                  <el-button 
                    v-permission="['sys:user:update']"
                    type="primary" 
                    @click="submitPwdForm"
                    :loading="pwdLoading"
                  >
                    <el-icon><Key /></el-icon>
                    修改密码
                  </el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script lang="ts" setup>
import { ElMessage } from 'element-plus'
import { getUserProfileApi, updateUserProfileApi,updateUserPwdApi } from '@/api/system/user'

const activeTab = ref('basic')
const userFormRef = ref()
const pwdFormRef = ref()

// 用户信息
const userInfo = ref<any>({
  sysUser: {},
  roles: []
})

// 表单数据
const userForm = reactive({
  nickname: '',
  mobile: '',
  email: '',
  sex: 1
})

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 表单校验规则
const userRules = reactive<any>({
  nickname: [{ required: true, message: '请输入用户昵称', trigger: 'blur' }],
  email: [
    { required: false, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  mobile: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ]
})

const pwdRules = reactive<any>({
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule: any, value: string, callback: Function) => {
        if (value !== pwdForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
})

// 添加loading状态
const submitLoading = ref(false)
const pwdLoading = ref(false)

// 获取用户信息
const getUser = async () => {
  try {
    const { data } = await getUserProfileApi()
    Object.assign(userInfo.value, data)
    Object.assign(userForm, {
      id: data.sysUser.id,
      nickname: data.sysUser.nickname,
      mobile: data.sysUser.mobile,
      email: data.sysUser.email,
      sex: data.sysUser.sex
    })
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
}

// 提交用户表单
const submitUserForm = async () => {
  try {
    submitLoading.value = true
    await userFormRef.value.validate()
    await updateUserProfileApi(userForm)
    ElMessage.success('修改成功')
    getUser()
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    submitLoading.value = false
  }
}

// 提交密码表单
const submitPwdForm = async () => {
  try {
    pwdLoading.value = true
    await pwdFormRef.value.validate()
    await updateUserPwdApi(pwdForm.oldPassword, pwdForm.newPassword)
    ElMessage.success('修改成功')
    Object.assign(pwdForm, {
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    })
  } catch (error) {
    console.error('修改密码失败:', error)
  } finally {
    pwdLoading.value = false
  }
}

onMounted(() => {
  getUser()
})
</script>

<style lang="scss" scoped>


.profile-card {
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }
}

.profile-header {
  position: relative;
  height: 200px;
  
  .header-backdrop {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(135deg, #a2a3a3 0%, #85a2ad 100%);
  }
  
  .header-content {
    position: relative;
    height: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: white;
    padding: 20px;
    
    .profile-avatar {
      border: 4px solid rgba(255, 255, 255, 0.8);
      box-shadow: 0 2px 10px rgba(0, 0, 0, 0.15);
    }
    
    .profile-name {
      margin: 15px 0 10px;
      font-size: 24px;
      font-weight: 600;
    }
  }
}

.profile-info {
  padding: 20px;
  
  .profile-list {
    padding: 0;
    margin: 0;
    list-style: none;
    
    li {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 15px 0;
      border-bottom: 1px solid #f0f0f0;
      
      &:last-child {
        border-bottom: none;
      }
      
      .info-label {
        display: flex;
        align-items: center;
        color: #666;
        
        .el-icon {
          margin-right: 8px;
          font-size: 16px;
        }
      }
      
      .info-content {
        color: #333;
        font-weight: 500;
      }
    }
  }
}

.tab-card {
  border-radius: 8px;
  
  :deep(.el-tabs__nav-wrap) {
    padding: 0 20px;
  }
}

.profile-form {
  max-width: 500px;
  margin: 20px auto;
  padding: 20px;
  
  .el-form-item:last-child {
    margin-bottom: 0;
    text-align: center;
    
    .el-button {
      width: 120px;
    }
  }
}

.profile-tabs {
  :deep(.el-tabs__item) {
    font-size: 15px;
    
    &.is-active {
      font-weight: 600;
    }
  }
}
</style>