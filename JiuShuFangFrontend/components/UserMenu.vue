<template>
  <div class="user-profile" @mouseenter="handleMouseEnter" @mouseleave="handleMouseLeave" ref="userProfileRef">
    <div class="user-info" ref="userAvatarRef">
      <el-avatar :size="32" :src="avatarUrl" class="user-avatar">
        <el-icon v-if="!avatarUrl"><UserFilled /></el-icon>
      </el-avatar>
      <span class="username">{{ nickName }}</span>
      <el-icon class="dropdown-icon" :class="{ 'rotated': showMenu }"><ArrowDown /></el-icon>
    </div>
    <!-- 悬停下拉菜单 -->
    <div v-show="showMenu" class="staggered-menu" ref="staggeredMenuRef" @mouseenter="handleMenuEnter" @mouseleave="handleMenuLeave">
      <div class="menu-header">
        <el-avatar :size="48" :src="avatarUrl" class="menu-avatar">
          <el-icon v-if="!avatarUrl"><UserFilled /></el-icon>
        </el-avatar>
        <div class="menu-user-info">
          <span class="menu-username">{{ nickName }}</span>
          <span class="menu-user-email">普通会员</span>
        </div>
      </div>
      <div class="menu-divider"></div>
      <div class="menu-body">
        <div class="menu-item menu-item-0" @click="handleMenuClick('profile')" :data-index="0">
          <el-icon class="menu-icon"><User /></el-icon>
          <span>我的信息</span>
        </div>
        <div class="menu-item menu-item-1" @click="handleMenuClick('orders')" :data-index="1">
          <el-icon class="menu-icon"><ShoppingBag /></el-icon>
          <span>我的订单</span>
        </div>
        <div class="menu-item menu-item-2" @click="handleMenuClick('cart')" :data-index="2">
          <el-icon class="menu-icon"><ShoppingCart /></el-icon>
          <span>我的购物车</span>
        </div>
        <div class="menu-item menu-item-3" @click="handleMenuClick('favorites')" :data-index="3">
          <el-icon class="menu-icon"><Star /></el-icon>
          <span>我的关注</span>
        </div>
        <div class="menu-item menu-item-4" @click="handleMenuClick('history')" :data-index="4">
          <el-icon class="menu-icon"><Clock /></el-icon>
          <span>浏览历史</span>
        </div>
        <div class="menu-item menu-item-5" @click="handleMenuClick('address')" :data-index="5">
          <el-icon class="menu-icon"><Location /></el-icon>
          <span>我的地址</span>
        </div>
        <div class="menu-item menu-item-6" :data-index="6">
          <el-icon class="menu-icon"><Ticket /></el-icon>
          <span>我的优惠券</span>
        </div>
        <div class="menu-item menu-item-7 menu-item-danger" @click="handleMenuClick('logout')" :data-index="7">
          <el-icon class="menu-icon"><SwitchButton /></el-icon>
          <span>退出登录</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, nextTick, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  UserFilled, User, ShoppingBag, ShoppingCart, Star, Clock, Location, Ticket, SwitchButton, ArrowDown
} from '@element-plus/icons-vue'
import { gsap } from 'gsap'
import { convertToExternalUrl } from '../utils/imageUtils'

const router = useRouter()

const props = defineProps({
  userInfo: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['logout'])

const nickName = ref('')
const avatarUrl = ref('')
const showMenu = ref(false)
const userProfileRef = ref(null)
const userAvatarRef = ref(null)
const staggeredMenuRef = ref(null)
const closeTimer = ref(null)

const initUserInfo = () => {
  if (props.userInfo) {
    if (props.userInfo.nickName) {
      nickName.value = props.userInfo.nickName.substring(0, 5)
    }
    if (props.userInfo.avatar) {
      avatarUrl.value = convertToExternalUrl(props.userInfo.avatar)
    }
  }
}

const handleMouseEnter = () => {
  if (closeTimer.value) {
    clearTimeout(closeTimer.value)
    closeTimer.value = null
  }
  showMenu.value = true
}

const handleMouseLeave = () => {
  closeTimer.value = setTimeout(() => {
    showMenu.value = false
  }, 150)
}

const handleMenuEnter = () => {
  if (closeTimer.value) {
    clearTimeout(closeTimer.value)
    closeTimer.value = null
  }
}

const handleMenuLeave = () => {
  closeTimer.value = setTimeout(() => {
    showMenu.value = false
  }, 150)
}

const handleMenuClick = (action) => {
  showMenu.value = false
  const routes = {
    profile: '/profile',
    orders: '/order-list',
    cart: '/shopping-cart',
    favorites: '/favorites',
    history: '/history',
    address: '/address'
  }

  if (action === 'logout') {
    emit('logout')
    return
  }

  if (routes[action]) {
    router.push(routes[action])
  }
}

watch(() => props.userInfo, (newVal) => {
  if (newVal) {
    initUserInfo()
  }
}, { immediate: true })

// 监听菜单显示状态，执行 GSAP 交错动画
watch(showMenu, async (newVal) => {
  if (newVal && staggeredMenuRef.value) {
    await nextTick()

    const menuItems = staggeredMenuRef.value.querySelectorAll('.menu-item')

    // 菜单整体淡入
    gsap.fromTo('.staggered-menu',
      { opacity: 0, y: 10, scale: 0.95 },
      { opacity: 1, y: 0, scale: 1, duration: 0.25, ease: 'power2.out' }
    )

    // 菜单头部 avatar 缩放效果
    gsap.fromTo('.menu-avatar',
      { scale: 0, opacity: 0 },
      { scale: 1, opacity: 1, duration: 0.5, ease: 'back.out(1.7)', delay: 0.05 }
    )

    // 颜色数组
    const bgColors = [
      'rgba(255,107,107,0.12)',
      'rgba(191,219,254,0.15)',
      'rgba(255,230,180,0.15)',
      'rgba(255,255,180,0.18)',
      'rgba(180,255,180,0.15)',
      'rgba(150,220,255,0.15)',
      'rgba(180,180,255,0.15)',
      'rgba(255,150,200,0.15)'
    ]

    // 书页穿插效果 - 从上往下翻入
    menuItems.forEach((item, index) => {
      // 初始状态：收缩 + 旋转 + 隐藏
      gsap.set(item, {
        scaleY: 0,
        transformOrigin: 'top center',
        opacity: 0,
        rotateX: -90,
        backgroundColor: 'transparent'
      })

      // 书页翻入动画
      gsap.to(item, {
        scaleY: 1,
        opacity: 1,
        rotateX: 0,
        backgroundColor: bgColors[index] || 'rgba(37,99,235,0.1)',
        duration: 0.5,
        ease: 'elastic.out(1, 0.7)',
        delay: index * 0.06
      })

      // 图标弹跳效果
      const icon = item.querySelector('.menu-icon')
      if (icon) {
        gsap.fromTo(icon,
          { scale: 0, rotation: -180 },
          {
            scale: 1,
            rotation: 0,
            duration: 0.4,
            ease: 'back.out(1.7)',
            delay: index * 0.06 + 0.15
          }
        )
      }

      // 文字滑入
      const text = item.querySelector('span')
      if (text) {
        gsap.fromTo(text,
          { x: -10, opacity: 0 },
          {
            x: 0,
            opacity: 1,
            duration: 0.3,
            ease: 'power2.out',
            delay: index * 0.06 + 0.1
          }
        )
      }
    })
  }
})

onUnmounted(() => {
  if (closeTimer.value) {
    clearTimeout(closeTimer.value)
  }
})
</script>

<style scoped>
.user-profile {
  position: relative;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: var(--radius-pill);
  transition: all var(--transition-fast);
}

.user-info:hover {
  background-color: var(--color-bg-tertiary);
}

.user-avatar {
  transition: transform var(--transition-base);
}

.user-info:hover .user-avatar {
  transform: scale(1.1);
}

.dropdown-icon {
  font-size: 14px;
  transition: transform var(--transition-base);
  color: var(--color-neutral-500);
}

.dropdown-icon.rotated {
  transform: rotate(180deg);
}

.username {
  font-size: 13px;
  color: var(--color-neutral-700);
  font-weight: 500;
}

/* === 交错下拉菜单 === */
.staggered-menu {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  width: 280px;
  background: white;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-xl);
  border: 1px solid var(--color-neutral-100);
  z-index: 1000;
  overflow: hidden;
  transform-origin: top right;
}

.menu-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px;
  background: linear-gradient(135deg, var(--color-primary-soft) 0%, transparent 100%);
}

.menu-avatar {
  flex-shrink: 0;
  border: 2px solid white;
  box-shadow: var(--shadow-sm);
}

.menu-user-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  overflow: hidden;
}

.menu-username {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-neutral-700);
  white-space: nowrap;
}

.menu-user-email {
  font-size: 12px;
  color: var(--color-neutral-400);
  white-space: nowrap;
}

.menu-divider {
  height: 1px;
  background: var(--color-neutral-100);
  margin: 0 16px;
}

.menu-body {
  padding: 8px;
  max-height: 400px;
  overflow-y: auto;
  perspective: 800px;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: all 0.2s ease;
  color: var(--color-neutral-600);
  font-size: 13px;
  font-weight: 500;
  position: relative;
  overflow: hidden;
  transform-origin: top center;
  backface-visibility: hidden;
}

.menu-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  width: 4px;
  height: 100%;
  border-radius: 2px 0 0 2px;
  transition: width 0.3s ease;
  z-index: 1;
}

/* 彩虹色系 */
.menu-item-0::before { background: linear-gradient(180deg, #ff6b6b, #ee5a5a); }
.menu-item-1::before { background: linear-gradient(180deg, #ff9999, #ff7777); }
.menu-item-2::before { background: linear-gradient(180deg, #ffb366, #ff9944); }
.menu-item-3::before { background: linear-gradient(180deg, #ffd966, #ffcc44); }
.menu-item-4::before { background: linear-gradient(180deg, #66cc66, #55bb55); }
.menu-item-5::before { background: linear-gradient(180deg, #66b3e6, #55a3d5); }
.menu-item-6::before { background: linear-gradient(180deg, #9999ff, #8888ee); }
.menu-item-7::before { background: linear-gradient(180deg, #ff6699, #ee5588); }

.menu-item:hover {
  transform: translateX(5px) scale(1.02);
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
}

.menu-item:hover::before {
  width: 100%;
  opacity: 0.12;
}

.menu-icon {
  font-size: 18px;
  transition: all 0.2s ease;
  flex-shrink: 0;
  position: relative;
  z-index: 2;
}

.menu-item:hover .menu-icon {
  transform: scale(1.2) rotate(10deg);
}

.menu-item-danger {
  margin-top: 8px;
  border-top: 1px dashed var(--color-neutral-100);
  padding-top: 12px;
}

.menu-item-danger:hover {
  color: var(--color-error);
}

.menu-item-danger:hover .menu-icon {
  color: var(--color-error);
}
</style>
