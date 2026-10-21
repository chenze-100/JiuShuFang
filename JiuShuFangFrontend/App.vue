<template>
  <div class="bg-animation">
    <div class="bg-particle particle-1"></div>
    <div class="bg-particle particle-2"></div>
    <div class="bg-particle particle-3"></div>
    <div class="bg-particle particle-4"></div>
    <div class="bg-particle particle-5"></div>
    <div class="bg-particle particle-6"></div>
    <div class="bg-particle particle-7"></div>
    <div class="bg-particle particle-8"></div>
    <div class="bg-particle particle-9"></div>
    <div class="bg-particle particle-10"></div>
    <div class="bg-particle particle-11"></div>
    <div class="bg-particle particle-12"></div>
  </div>
  <router-view />
</template>

<script setup>
import { onMounted, onUnmounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { connectGlobalWebSocket, disconnectGlobalWebSocket } from './utils/websocket.js'

const route = useRoute()

const setupGlobalWebSocket = () => {
  // 在聊天详情页和聊天列表页不连接全局WebSocket，避免冲突
  if (route.path === '/chat-detail' || route.path === '/chat-list') {
    console.log('在聊天页面，断开全局WebSocket')
    disconnectGlobalWebSocket()
    return
  }
  
  // 从 localStorage 获取用户信息并建立全局 WebSocket 连接
  const userInfoStr = localStorage.getItem('userInfo')
  if (userInfoStr) {
    try {
      const userInfo = JSON.parse(userInfoStr)
      if (userInfo.id) {
        connectGlobalWebSocket(userInfo.id)
      }
    } catch (e) {
      console.error('解析用户信息失败:', e)
    }
  }
}

onMounted(() => {
  setupGlobalWebSocket()
})

onUnmounted(() => {
  disconnectGlobalWebSocket()
})

// 监听路由变化，在聊天页面断开全局WebSocket
watch(() => route.path, (newPath) => {
  setupGlobalWebSocket()
})
</script>

<style>
/* === CSS 变量系统 - 旧书坊靛蓝美学 === */
:root {
  /* 主色调 - 靛蓝 accent */
  --color-primary: #2563eb;
  --color-primary-light: #3b82f6;
  --color-primary-dark: #1d4ed8;
  --color-primary-soft: #eff6ff;

  /* 中性色 */
  --color-neutral-900: #1a1a1a;
  --color-neutral-700: #333333;
  --color-neutral-500: #666666;
  --color-neutral-300: #b3b3b3;
  --color-neutral-200: #e0e0e0;
  --color-neutral-100: #f0f0f0;
  --color-neutral-50: #fafafa;

  /* 背景色 */
  --color-bg-primary: #ffffff;
  --color-bg-secondary: #fafafa;
  --color-bg-tertiary: #f5f5f5;

  /* 功能色 */
  --color-success: #52c41a;
  --color-warning: #faad14;
  --color-error: #f5222d;
  --color-info: #1890ff;

  /* 渐变 */
  --gradient-primary: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
  --gradient-soft: linear-gradient(135deg, #eff6ff 0%, #ffffff 100%);
  --gradient-card: linear-gradient(145deg, #ffffff 0%, #f4f8fe 100%);
  --gradient-bg: linear-gradient(135deg, #f0f6ff 0%, #f7faff 50%, #eaf1fb 100%);
  --gradient-mesh: radial-gradient(at 40% 20%, hsla(221,90%,62%,0.15) 0px, transparent 50%),
                   radial-gradient(at 80% 0%, hsla(205,85%,55%,0.1) 0px, transparent 50%),
                   radial-gradient(at 0% 50%, hsla(225,85%,68%,0.1) 0px, transparent 50%),
                   radial-gradient(at 80% 50%, hsla(225,85%,68%,0.1) 0px, transparent 50%),
                   radial-gradient(at 0% 100%, hsla(221,90%,62%,0.1) 0px, transparent 50%);

  /* 阴影 */
  --shadow-sm: 0 2px 8px rgba(0, 0, 0, 0.04);
  --shadow-md: 0 4px 16px rgba(0, 0, 0, 0.06);
  --shadow-lg: 0 8px 32px rgba(0, 0, 0, 0.08);
  --shadow-xl: 0 12px 48px rgba(0, 0, 0, 0.1);
  --shadow-focus: 0 0 0 3px rgba(37,99,235, 0.12);

  /* 圆角 */
  --radius-xs: 4px;
  --radius-sm: 8px;
  --radius-md: 12px;
  --radius-lg: 16px;
  --radius-xl: 24px;
  --radius-pill: 9999px;

  /* 过渡 */
  --transition-fast: 0.15s ease;
  --transition-base: 0.25s ease;
  --transition-slow: 0.35s ease;
}

/* === 全局样式重置 === */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body {
  height: 100%;
  font-family: 'Noto Sans SC', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
  font-size: 14px;
  line-height: 1.6;
  color: var(--color-neutral-700);
  background: var(--gradient-bg);
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

#app {
  min-height: 100vh;
  position: relative;
}

/* 背景装饰图案 */
#app::before {
  content: '';
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: var(--gradient-mesh);
  pointer-events: none;
  z-index: 0;
  animation: meshShift 15s ease-in-out infinite;
}

/* 浮动装饰圆圈 */
#app::after {
  content: '';
  position: fixed;
  top: -50%;
  right: -10%;
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, rgba(37,99,235,0.08) 0%, transparent 70%);
  border-radius: 50%;
  pointer-events: none;
  z-index: 0;
  animation: float 20s ease-in-out infinite;
}

/* 左下角装饰光晕 */
body::before {
  content: '';
  position: fixed;
  bottom: -30%;
  left: -20%;
  width: 800px;
  height: 800px;
  background: radial-gradient(circle, rgba(96,165,250,0.06) 0%, transparent 70%);
  border-radius: 50%;
  pointer-events: none;
  z-index: 0;
  animation: float 25s ease-in-out infinite reverse;
}

/* 顶部装饰光晕 */
body::after {
  content: '';
  position: fixed;
  top: -40%;
  left: 30%;
  width: 700px;
  height: 700px;
  background: radial-gradient(circle, rgba(191,219,254,0.05) 0%, transparent 70%);
  border-radius: 50%;
  pointer-events: none;
  z-index: 0;
  animation: pulse 18s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  25% { transform: translate(-20px, 30px) scale(1.05); }
  50% { transform: translate(10px, -20px) scale(0.95); }
  75% { transform: translate(-10px, 10px) scale(1.02); }
}

@keyframes meshShift {
  0%, 100% { opacity: 0.6; transform: scale(1); }
  50% { opacity: 1; transform: scale(1.02); }
}

@keyframes pulse {
  0%, 100% { transform: scale(1) translate(0, 0); opacity: 0.5; }
  50% { transform: scale(1.1) translate(-20px, 10px); opacity: 0.8; }
}

@keyframes glow {
  0%, 100% { box-shadow: 0 0 20px rgba(37,99,235,0.1); }
  50% { box-shadow: 0 0 40px rgba(37,99,235,0.2), 0 0 60px rgba(59,130,246,0.15); }
}

/* === 背景粒子动画 - 增强版 === */
.bg-animation {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 0;
  overflow: hidden;
}

.bg-particle {
  position: absolute;
  border-radius: 50%;
  animation: particleFloat linear infinite;
}

.particle-1 {
  width: 120px;
  height: 120px;
  left: 8%;
  top: 100%;
  background: radial-gradient(circle at 40% 40%, rgba(147,197,253,0.35) 0%, rgba(37,99,235,0.15) 40%, transparent 70%);
  animation-duration: 20s;
  animation-delay: 0s;
}

.particle-2 {
  width: 80px;
  height: 80px;
  left: 28%;
  top: 100%;
  background: radial-gradient(circle at 40% 40%, rgba(37,99,235,0.3) 0%, rgba(147,197,253,0.12) 40%, transparent 70%);
  animation-duration: 26s;
  animation-delay: 4s;
}

.particle-3 {
  width: 140px;
  height: 140px;
  left: 52%;
  top: 100%;
  background: radial-gradient(circle at 40% 40%, rgba(125,175,250,0.28) 0%, rgba(29,78,216,0.1) 40%, transparent 70%);
  animation-duration: 30s;
  animation-delay: 8s;
}

.particle-4 {
  width: 70px;
  height: 70px;
  left: 72%;
  top: 100%;
  background: radial-gradient(circle at 40% 40%, rgba(147,197,253,0.25) 0%, rgba(100,150,240,0.08) 40%, transparent 70%);
  animation-duration: 23s;
  animation-delay: 2s;
}

.particle-5 {
  width: 100px;
  height: 100px;
  left: 88%;
  top: 100%;
  background: radial-gradient(circle at 40% 40%, rgba(125,170,245,0.22) 0%, rgba(29,78,216,0.08) 40%, transparent 70%);
  animation-duration: 28s;
  animation-delay: 6s;
}

.particle-6 {
  width: 90px;
  height: 90px;
  left: 5%;
  top: 100%;
  background: radial-gradient(circle at 40% 40%, rgba(125,175,250,0.3) 0%, rgba(37,99,235,0.1) 40%, transparent 70%);
  animation-duration: 22s;
  animation-delay: 1s;
}

.particle-7 {
  width: 130px;
  height: 130px;
  left: 40%;
  top: 100%;
  background: radial-gradient(circle at 40% 40%, rgba(147,197,253,0.22) 0%, rgba(37,99,235,0.08) 40%, transparent 70%);
  animation-duration: 34s;
  animation-delay: 12s;
}

.particle-8 {
  width: 60px;
  height: 60px;
  left: 60%;
  top: 100%;
  background: radial-gradient(circle at 40% 40%, rgba(147,197,253,0.28) 0%, rgba(125,175,250,0.1) 40%, transparent 70%);
  animation-duration: 19s;
  animation-delay: 5s;
}

.particle-9 {
  width: 110px;
  height: 110px;
  left: 18%;
  top: 100%;
  background: radial-gradient(circle at 40% 40%, rgba(37,99,235,0.25) 0%, rgba(29,78,216,0.08) 40%, transparent 70%);
  animation-duration: 27s;
  animation-delay: 15s;
}

.particle-10 {
  width: 75px;
  height: 75px;
  left: 78%;
  top: 100%;
  background: radial-gradient(circle at 40% 40%, rgba(37,99,235,0.26) 0%, rgba(147,197,253,0.1) 40%, transparent 70%);
  animation-duration: 24s;
  animation-delay: 9s;
}

.particle-11 {
  width: 95px;
  height: 95px;
  left: 35%;
  top: 100%;
  background: radial-gradient(circle at 40% 40%, rgba(29,78,216,0.15) 0%, rgba(37,99,235,0.06) 40%, transparent 70%);
  animation-duration: 31s;
  animation-delay: 18s;
}

.particle-12 {
  width: 85px;
  height: 85px;
  left: 92%;
  top: 100%;
  background: radial-gradient(circle at 40% 40%, rgba(100,150,240,0.3) 0%, rgba(37,99,235,0.12) 40%, transparent 70%);
  animation-duration: 21s;
  animation-delay: 3s;
}

@keyframes particleFloat {
  0% {
    transform: translateY(0) rotate(0deg) scale(1);
    opacity: 0;
  }
  8% {
    opacity: 0.7;
  }
  85% {
    opacity: 0.5;
  }
  100% {
    transform: translateY(-130vh) rotate(400deg) scale(0.6);
    opacity: 0;
  }
}

a {
  text-decoration: none;
  color: inherit;
  transition: color var(--transition-fast);
}

/* === 滚动条美化 === */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background: transparent;
}

::-webkit-scrollbar-thumb {
  background: var(--color-neutral-200);
  border-radius: var(--radius-pill);
  transition: background var(--transition-base);
}

::-webkit-scrollbar-thumb:hover {
  background: var(--color-neutral-300);
}

/* === 通用容器 === */
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

@media (max-width: 768px) {
  .container {
    padding: 0 16px;
  }
}

/* === 动画 === */
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.fade-in {
  animation: fadeIn 0.4s ease forwards;
}

.slide-up {
  animation: slideUp 0.5s ease forwards;
}
</style>
