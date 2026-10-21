<template>
  <!-- 动态背景 -->
  <VitalityBackground />

  <div class="profile-container">
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
            @click="goBackHome"
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
    <div class="main-content">
      <div class="profile-header">
        <h1>我的信息</h1>
        <span class="back-home-link" @click="goBackHome">返回首页</span>
      </div>

      <div class="profile-card">
        <div class="avatar-section">
          <el-avatar :size="100" :src="userInfo.avatar">
            <el-icon v-if="!userInfo.avatar"><UserFilled /></el-icon>
          </el-avatar>
        </div>

        <div class="info-section">
          <div class="info-item">
            <span class="info-label">用户名：</span>
            <span class="info-value">{{ userInfo.nickName || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">手机号：</span>
            <span class="info-value">{{ userInfo.phone || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">邮箱：</span>
            <span class="info-value">{{ userInfo.email || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">性别：</span>
            <span class="info-value">{{ getGenderText(userInfo.sex) }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">所在城市：</span>
            <span class="info-value">{{ userInfo.city || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">注册时间：</span>
            <span class="info-value">{{ formatTime(userInfo.createTime) }}</span>
          </div>
        </div>

        <div class="action-buttons">
          <button class="action-btn edit-btn" @click="goToEditProfile">修改基本信息</button>
          <button class="action-btn password-btn" @click="goToChangePassword" v-if="canChangePassword">修改密码</button>
        </div>
      </div>
    </div>

    <!-- 底部 -->
    <div class="footer">
      <div class="footer-content">
        <div class="footer-links">
          <span><a href="#">关于我们</a></span>
          <span><a href="#">联系我们</a></span>
          <span><a href="#">商家入驻</a></span>
          <span><a href="#">友情链接</a></span>
          <span><a href="#">帮助中心</a></span>
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

const userInfo = ref({})
const nickName = ref('')
const avatarUrl = ref('')
const canChangePassword = ref(false) // 是否可以修改密码（手机号登录）

const handleLogout = () => {
  localStorage.removeItem('userInfo')
  localStorage.removeItem('token')
  ElMessage.success('退出登录成功')
  router.push('/')
}

const goToProfile = () => {
  router.push('/user/profile')
}

const goToEditProfile = () => {
  router.push('/user/edit-profile')
}

const goToChangePassword = () => {
  router.push('/user/change-password')
}

const goToShoppingCart = () => {
  router.push('/shopping-cart')
}

const goToAddress = () => {
  router.push('/address')
}

const goToFavorites = () => {
  router.push('/favorites')
}

const goToHistory = () => {
  router.push('/history')
}

const goToOrderList = () => {
  router.push('/order-list')
}

const getGenderText = (sex) => {
  if (sex === 1) return '男'
  if (sex === 2) return '女'
  return '未知'
}

const formatTime = (timestamp) => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  return date.toLocaleString('zh-CN')
}

const getUserInfo = async (id) => {
  try {
    const result = await request(`/user/info/${id}`)
    
    if (result && result.code === 200 && result.data) {
      return result.data
    }
    return null
  } catch (error) {
    console.error('获取用户信息失败:', error)
    ElMessage.error('获取用户信息失败')
    return null
  }
}

const goBackHome = () => {
  router.push('/')
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
    
    // 判断是否是手机号登录（有手机号则显示修改密码按钮）
    if (user && user.phone) {
      canChangePassword.value = true
    }
    
    // 从后端获取最新的用户信息（不阻塞页面渲染）
    if (user && user.id) {
      getUserInfo(user.id).then(latestInfo => {
        if (latestInfo) {
          userInfo.value = latestInfo
          if (latestInfo.avatar) {
            avatarUrl.value = convertToExternalUrl(latestInfo.avatar)
          }
          // 再次检查手机号
          if (latestInfo.phone) {
            canChangePassword.value = true
          }
        }
      })
    }
  } else {
    ElMessage.error('请先登录')
    router.push('/')
  }
})
</script>

<style scoped>
.profile-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: transparent; position: relative; z-index: 1;
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

a {
  text-decoration: none;
  color: var(--color-neutral-700);
}

a:hover {
  color: var(--color-primary);
}

.top-nav {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid var(--color-neutral-100);
}

.nav-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  height: 56px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.nav-right {
  display: flex;
  gap: 8px;
  align-items: center;
}

.nav-item a {
  padding: 6px 12px;
  border-radius: var(--radius-sm);
  color: var(--color-neutral-500);
  font-size: 13px;
  transition: all var(--transition-fast);
}

.nav-item a:hover {
  color: var(--color-primary);
  background-color: var(--color-primary-soft);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 10px;
  border-radius: var(--radius-pill);
  transition: background-color var(--transition-fast);
}

.user-info:hover {
  background-color: var(--color-bg-tertiary);
}

.username {
  font-size: 13px;
  color: var(--color-neutral-700);
  font-weight: 500;
}

:deep(.user-popover) {
  padding: 8px;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-lg);
}

.user-menu {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.menu-item {
  cursor: pointer;
  padding: 10px 16px;
  color: var(--color-neutral-500);
  font-size: 13px;
  border-radius: var(--radius-sm);
  transition: all var(--transition-fast);
}

.menu-item:hover {
  background-color: var(--color-bg-tertiary);
  color: var(--color-primary);
}

.main-content {
  width: 1200px;
  margin: 24px auto;
  padding: 24px 20px;
  background-color: white;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  min-height: 600px;
  flex: 1;
}

.profile-header {
  margin-bottom: 30px;
  padding-bottom: 15px;
  border-bottom: 2px solid var(--color-neutral-100);
  position: relative;
}

.profile-header h1 {
  font-size: 24px;
  color: var(--color-neutral-700);
  font-weight: 600;
}

.back-home-link {
  position: absolute;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
  font-size: 13px;
  color: var(--color-neutral-400);
  cursor: pointer;
  transition: color var(--transition-fast);
}

.back-home-link:hover {
  color: var(--color-primary);
}

.profile-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px;
  gap: 30px;
}

.avatar-section {
  margin-bottom: 20px;
}

.info-section {
  width: 100%;
  max-width: 600px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.info-item {
  display: flex;
  align-items: center;
  padding: 15px 20px;
  background: transparent; position: relative; z-index: 1;
  border-radius: var(--radius-sm);
  border: 1px solid var(--color-neutral-100);
}

.info-label {
  font-size: 15px;
  color: var(--color-neutral-500);
  font-weight: 500;
  width: 100px;
  flex-shrink: 0;
}

.info-value {
  font-size: 15px;
  color: var(--color-neutral-700);
  font-weight: 400;
}

.action-buttons {
  display: flex;
  gap: 20px;
  margin-top: 30px;
}

.action-btn {
  padding: 12px 40px;
  border: none;
  border-radius: var(--radius-pill);
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.edit-btn {
  background-color: var(--color-primary-soft);
  color: var(--color-primary);
}

.edit-btn:hover {
  background-color: var(--color-primary-light);
}

.password-btn {
  background-color: var(--color-primary-soft);
  color: var(--color-primary);
}

.password-btn:hover {
  background-color: var(--color-primary-light);
}

.footer {
  background: white;
  border-top: 1px solid var(--color-neutral-100);
  padding: 32px 0;
  margin-top: auto;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  text-align: center;
}

.footer-links {
  display: flex;
  justify-content: center;
  gap: 32px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.footer-links a {
  color: var(--color-neutral-400);
  font-size: 13px;
  transition: color var(--transition-fast);
}

.footer-links a:hover {
  color: var(--color-primary);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .nav-content {
    padding: 0 12px;
  }

  .main-content {
    padding: 16px 12px;
    margin: 16px auto;
  }

  .profile-header h1 {
    font-size: 20px;
  }

  .profile-card {
    padding: 24px 16px;
  }

  .info-item {
    padding: 12px 16px;
  }

  .info-label {
    width: 80px;
    font-size: 14px;
  }

  .info-value {
    font-size: 14px;
  }

  .action-buttons {
    flex-direction: column;
    align-items: center;
  }

  .action-btn {
    width: 100%;
    max-width: 200px;
    padding: 10px 20px;
  }
}
</style>
