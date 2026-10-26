<template>
  <div class="cute-card" :style="cardStyle">
    <!-- 卡片头部 -->
    <div class="card-header">
      <div class="logo-section">
        <span class="brand-name">潮流购物</span>
        <BlinkEmoji :size="50" :interval="2000" expression="happy" />
      </div>
      <p class="welcome-text">{{ greetingText }}</p>
    </div>
    
    <!-- 登录表单 -->
    <div class="form-container">
      <!-- Tab 切换 -->
      <div class="tab-container">
        <div class="tab-slider" :style="sliderStyle"></div>
        <button
          v-for="tab in tabs"
          :key="tab.key"
          class="tab-button"
          :class="{ active: activeTab === tab.key }"
          @click="activeTab = tab.key"
        >
          {{ tab.label }}
        </button>
      </div>
      
      <!-- 密码登录 -->
      <div v-show="activeTab === 'password'" class="form-content">
        <div class="input-group">
          <input type="text" placeholder="请输入用户名/手机号/邮箱" class="vitality-input" />
        </div>
        <div class="input-group">
          <input type="password" placeholder="请输入密码" class="vitality-input" />
        </div>
        <button class="vitality-button" @click="handleLogin">登录</button>
      </div>
      
      <!-- 手机登录 -->
      <div v-show="activeTab === 'phone'" class="form-content">
        <div class="input-group">
          <input type="tel" placeholder="请输入手机号" class="vitality-input" />
        </div>
        <div class="input-group phone-code">
          <input type="text" placeholder="验证码" class="vitality-input" />
          <button class="code-button">获取验证码</button>
        </div>
        <button class="vitality-button" @click="handleLogin">登录</button>
      </div>
      
      <!-- 扫码登录 -->
      <div v-show="activeTab === 'qrcode'" class="form-content qrcode-content">
        <div class="qrcode-placeholder">
          <div class="qrcode-icon">📱</div>
          <p>请使用手机扫码登录</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import BlinkEmoji from './BlinkEmoji.vue'

const activeTab = ref('password')

const tabs = [
  { key: 'password', label: '密码登录' },
  { key: 'phone', label: '手机登录' },
  { key: 'qrcode', label: '扫码登录' }
]

const sliderStyle = computed(() => {
  const tabIndex = tabs.findIndex(tab => tab.key === activeTab.value)
  return {
    transform: `translateX(${tabIndex * 100}%)`
  }
})

const greetingText = computed(() => {
  const hour = new Date().getHours()
  if (hour < 12) return '早上好，欢迎回来！☀️'
  if (hour < 18) return '下午好，欢迎回来！🌤️'
  return '晚上好，欢迎回来！🌙'
})

const cardStyle = computed(() => ({
  transform: 'translate(15%, -5%)'
}))

const handleLogin = () => {
  // 登录逻辑
  alert('登录功能待实现')
}
</script>

<style scoped>
.cute-card {
  position: relative;
  z-index: 10;
  background: #FFFFFF;
  border-radius: 30px;
  box-shadow: 0 20px 60px rgba(255, 225, 53, 0.3);
  border: 3px solid #FFE135;
  padding: 40px;
  max-width: 450px;
  width: 90%;
  animation: slide-in-left 0.8s cubic-bezier(0.68, -0.55, 0.265, 1.55);
}

.card-header {
  margin-bottom: 30px;
}

.logo-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.brand-name {
  font-size: 28px;
  font-weight: bold;
  color: #333;
}

.welcome-text {
  font-size: 14px;
  color: #666;
  margin: 0;
}

.tab-container {
  position: relative;
  display: flex;
  background: #F5F5F5;
  border-radius: 20px;
  margin-bottom: 20px;
  overflow: hidden;
}

.tab-slider {
  position: absolute;
  width: 33.33%;
  height: 100%;
  background: linear-gradient(135deg, #FFE135, #FFC107);
  border-radius: 20px;
  transition: transform 0.3s cubic-bezier(0.68, -0.55, 0.265, 1.55);
}

.tab-button {
  flex: 1;
  padding: 12px;
  background: transparent;
  border: none;
  cursor: pointer;
  font-weight: 500;
  color: #666;
  position: relative;
  z-index: 1;
  transition: color 0.3s ease;
}

.tab-button.active {
  color: #333;
}

.input-group {
  margin-bottom: 15px;
}

.phone-code {
  display: flex;
  gap: 10px;
}

.code-button {
  background: #FFE135;
  border: none;
  border-radius: 15px;
  padding: 12px 16px;
  cursor: pointer;
  font-weight: 500;
  white-space: nowrap;
}

.qrcode-content {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
}

.qrcode-placeholder {
  text-align: center;
}

.qrcode-icon {
  font-size: 64px;
  margin-bottom: 10px;
}

.vitality-button {
  width: 100%;
  background: linear-gradient(135deg, #FFE135, #FFC107);
  color: #FFFFFF;
  border: none;
  border-radius: 25px;
  padding: 14px 32px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 8px 24px rgba(255, 225, 53, 0.4);
}

.vitality-button:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 32px rgba(255, 225, 53, 0.6);
}

.vitality-button:active {
  transform: scale(0.98);
}

.vitality-input {
  width: 100%;
  background: #FFFFFF;
  border: 2px solid #E0E0E0;
  border-radius: 15px;
  padding: 14px 16px;
  font-size: 14px;
  transition: all 0.3s ease;
}

.vitality-input:focus {
  outline: none;
  border-color: #FFE135;
  transform: scale(1.02);
  box-shadow: 0 0 0 3px rgba(255, 225, 53, 0.2);
}
</style>
