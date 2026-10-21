<template>
  <!-- 动态背景 -->
  <VitalityBackground />

  <div class="change-password-container">
    <!-- 顶部导航栏 -->
    <div class="top-nav">
      <div class="nav-content">
        <div class="nav-left">
          <GlitchText
            text="旧书坊"
            :speed="0.8"
            :enableShadows="true"
            :enableOnHover="true"
            :className="'welcome-text'"
            @click="goBack"
          />
        </div>
        <div class="nav-right">
          <span class="nav-item"><a href="#" @click.prevent="goToShoppingCart">我的购物车</a></span>
          <span class="nav-item">我的优惠券</span>
          <UserMenu :userInfo="userInfo" @logout="handleLogout" />
        </div>
      </div>
    </div>

    <!-- 页面主体 -->
    <div class="main-wrapper">
      <div class="main-content">
        <div class="profile-header">
          <h1>修改密码</h1>
          <span class="back-home-link" @click="goBackHome">返回首页</span>
        </div>

        <div class="password-card">
          <el-form :model="form" :rules="rules" ref="formRef" label-width="120px" class="password-form">
            <div class="form-section">
              <div class="section-title">第一步：验证手机号</div>
              
              <el-form-item label="手机号" prop="phone">
                <el-input 
                  v-model="form.phone" 
                  placeholder="请输入手机号" 
                  maxlength="11"
                  style="width: 300px;"
                >
                  <template #append>
                    <el-button 
                      @click="sendCode" 
                      :disabled="countdown > 0"
                      :loading="sendingCode"
                    >
                      {{ countdown > 0 ? `${countdown}秒后重发` : '发送验证码' }}
                    </el-button>
                  </template>
                </el-input>
              </el-form-item>

              <el-form-item label="验证码" prop="code">
                <el-input 
                  v-model="form.code" 
                  placeholder="请输入验证码" 
                  maxlength="6"
                  style="width: 300px;"
                ></el-input>
              </el-form-item>
            </div>

            <div class="form-section">
              <div class="section-title">第二步：设置新密码</div>
              
              <el-form-item label="新密码" prop="newPassword">
                <el-input 
                  v-model="form.newPassword" 
                  type="password" 
                  placeholder="请输入新密码" 
                  show-password
                  style="width: 300px;"
                ></el-input>
              </el-form-item>

              <el-form-item label="确认密码" prop="confirmPassword">
                <el-input 
                  v-model="form.confirmPassword" 
                  type="password" 
                  placeholder="请再次输入新密码" 
                  show-password
                  style="width: 300px;"
                ></el-input>
              </el-form-item>
            </div>

            <div class="action-buttons">
              <button class="action-btn edit-btn" @click="handleSubmit" :disabled="submitting">
                {{ submitting ? '修改中...' : '确认修改' }}
              </button>
              <button class="action-btn cancel-btn" @click="goBack">取消</button>
            </div>
          </el-form>
        </div>
      </div>
    </div>

    <!-- 底部 -->
    <div class="footer">
      <div class="footer-content">
        <div class="footer-links">
          <span> <a href="#">关于我们</a> </span>
          <span> <a href="#">联系我们</a> </span>
          <span> <a href="#">商家入驻</a> </span>
          <span> <a href="#">友情链接</a> </span>
          <span> <a href="#">帮助中心</a> </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import UserMenu from './components/UserMenu.vue'
import GlitchText from './components/GlitchText.vue'
import VitalityBackground from './components/VitalityBackground.vue'
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { UserFilled } from '@element-plus/icons-vue'
import request from './request'
import { convertToExternalUrl } from './utils/imageUtils'

const router = useRouter()
const formRef = ref(null)
const submitting = ref(false)
const sendingCode = ref(false)
const countdown = ref(0)

const userInfo = ref({})
const nickName = ref('')
const avatarUrl = ref('')

const form = ref({
  phone: '',
  code: '',
  newPassword: '',
  confirmPassword: ''
})

const rules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码为 6 位数字', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { 
      validator: (rule, value, callback) => {
        if (value !== form.value.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const sendCode = async () => {
  await formRef.value.validateField('phone').catch(() => {})
  
  if (!form.value.phone || !/^1[3-9]\d{9}$/.test(form.value.phone)) {
    ElMessage.error('请输入正确的手机号')
    return
  }
  
  sendingCode.value = true
  
  try {
    const result = await request('/code/send', {
      method: 'POST',
      body: JSON.stringify({
        phone: form.value.phone,
        type: 3
      })
    })
    
    if (result && result.code === 200) {
      ElMessage.success('验证码发送成功')
      countdown.value = 60
      const timer = setInterval(() => {
        countdown.value--
        if (countdown.value <= 0) {
          clearInterval(timer)
        }
      }, 1000)
    } else {
      ElMessage.error(result.message || '发送失败')
    }
  } catch (error) {
    console.error('发送验证码失败:', error)
    ElMessage.error('发送失败，请重试')
  } finally {
    sendingCode.value = false
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    
    try {
      const result = await request('/user/pwd/update', {
        method: 'PUT',
        body: JSON.stringify({
          phone: form.value.phone,
          code: form.value.code,
          password: form.value.newPassword
        })
      })
      
      if (result && result.code === 200) {
        ElMessage.success('密码修改成功')
        localStorage.removeItem('userInfo')
        localStorage.removeItem('token')
        setTimeout(() => {
          router.push('/login')
        }, 1000)
      } else {
        ElMessage.error(result.message || '修改失败')
      }
    } catch (error) {
      console.error('修改失败:', error)
      ElMessage.error('修改失败')
    } finally {
      submitting.value = false
    }
  })
}

const handleLogout = () => {
  localStorage.removeItem('userInfo')
  localStorage.removeItem('token')
  ElMessage.success('退出登录成功')
  router.push('/')
}

const goToProfile = () => {
  router.push('/profile')
}

const goBack = () => {
  router.push('/profile')
}

const goBackHome = () => {
  router.push('/')
}

const goToOrderList = () => {
  router.push('/order-list')
}

onMounted(async () => {
  const stored = localStorage.getItem('userInfo')
  if (stored) {
    const user = JSON.parse(stored)
    userInfo.value = user
    
    if (user && user.nickName) {
      nickName.value = user.nickName.substring(0, 5)
    }
    if (user && user.avatar) {
      avatarUrl.value = convertToExternalUrl(user.avatar)
    }
    
    if (user && user.phone) {
      form.value.phone = user.phone
    }
  } else {
    ElMessage.error('请先登录')
    router.push('/login')
  }
})
</script>

<style scoped>
.change-password-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: transparent;
  position: relative;
  z-index: 1;
}

.top-nav {
  position: sticky;
  top: 0;
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  box-shadow: 0 1px 0 rgba(37,99,235, 0.06);
}

.nav-content {
  width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.nav-left {
  display: flex;
  gap: 15px;
  align-items: center;
}

.welcome-text {
  color: #999;
  font-size: 14px;
}

.nav-right {
  display: flex;
  gap: 20px;
  align-items: center;
}

.nav-item {
  color: #999;
  font-size: 14px;
}

.nav-item a {
  color: #999;
  text-decoration: none;
}

.nav-item a:hover {
  color: #f23030;
}

.user-profile {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 5px 10px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: #f5f5f5;
}

.username {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

:deep(.user-popover) {
  padding: 10px;
}

.user-menu {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.menu-item {
  cursor: pointer;
  padding: 8px 12px;
  color: #666;
  font-size: 14px;
  transition: background-color 0.3s;
}

.menu-item:hover {
  background-color: #f5f5f5;
  color: #f23030;
}

.main-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.main-content {
  width: 1200px;
  margin: 30px auto;
  padding: 30px;
  background-color: #f5f5f5;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.profile-header {
  margin-bottom: 30px;
  padding-bottom: 15px;
  border-bottom: 2px solid #f5f5f5;
  position: relative;
}

.profile-header h1 {
  font-size: 24px;
  color: #333;
  font-weight: 600;
}

.back-home-link {
  position: absolute;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
  font-size: 13px;
  color: #999;
  cursor: pointer;
  transition: color 0.3s;
}

.back-home-link:hover {
  color: #2563eb;
}

.password-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px;
}

.password-form {
  width: 100%;
  max-width: 600px;
}

.form-section {
  margin-bottom: 30px;
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 8px;
  border: 1px solid #e5e5e5;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #e5e5e5;
}

.action-buttons {
  display: flex;
  gap: 15px;
  margin-top: 30px;
  justify-content: center;
}

.action-btn {
  padding: 12px 40px;
  border: none;
  border-radius: 25px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.edit-btn {
  background-color: #dbeafe;
  color: #f23030;
}

.edit-btn:hover {
  background-color: #c7d9f5;
}

.edit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.cancel-btn {
  background-color: #f5f5f5;
  color: #666;
}

.cancel-btn:hover {
  background-color: #e5e5e5;
}

.footer {
  background-color: #f5f5f5;
  border-top: 1px solid #e5e5e5;
  padding: 30px 0;
  margin-top: auto;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  text-align: center;
}

.footer-links {
  display: flex;
  justify-content: center;
  gap: 30px;
  margin-bottom: 15px;
}

.footer-links a {
  color: #666;
  text-decoration: none;
  font-size: 14px;
}

.footer-links a:hover {
  color: #f23030;
}
</style>
