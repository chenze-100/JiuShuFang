<template>
  <div class="top-nav" :class="{ 'transparent': transparent }">
    <div class="nav-content">
      <div class="nav-left">
        <GlitchText
          text="知阅旧货"
          :speed="0.8"
          :enableShadows="true"
          :enableOnHover="true"
          :className="'welcome-text'"
          @click="$router.push('/home')"
        />
      </div>
      <div class="nav-right">
        <span class="nav-item" @click="$router.push('/shopping-cart')">我的购物车</span>
        <span class="nav-item">我的优惠券</span>
        <UserMenu :userInfo="userInfo" @logout="handleLogout" />
      </div>
    </div>
  </div>
</template>

<script setup>
import GlitchText from './GlitchText.vue'
import UserMenu from './UserMenu.vue'
import { ElMessage } from 'element-plus'

defineProps({
  transparent: {
    type: Boolean,
    default: false
  }
})

const userInfo = JSON.parse(localStorage.getItem('userInfo') || 'null')

const handleLogout = () => {
  ElMessage.success('已退出登录')
  localStorage.removeItem('userInfo')
  localStorage.removeItem('token')
  window.location.href = '/'
}
</script>

<style scoped>
.top-nav {
  position: sticky;
  top: 0;
  z-index: 300;
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  box-shadow: 0 1px 0 rgba(255, 107, 53, 0.06);
}

.top-nav.transparent {
  background: transparent;
  backdrop-filter: none;
  border-bottom: none;
  box-shadow: none;
}

.nav-content {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 32px;
  height: 60px;
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
  cursor: pointer;
}

.nav-item:hover {
  color: #ff6b35;
}
</style>
