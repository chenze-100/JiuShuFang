<template>
  <!-- 动态背景 -->
  <VitalityBackground />

  <div class="history-container">
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
      <div class="history-header">
        <h1>浏览历史</h1>
        <span class="count-text">共 {{ total }} 个商品</span>
        <span class="back-home-link" @click="goBackHome">返回首页</span>
      </div>

      <ChromaGrid
        :items="historyList"
        :radius="300"
        :damping="0.45"
        :fadeOut="0.6"
        @item-click="viewProductDetail"
      />

      <div v-if="loading" class="loading-text">加载中...</div>
      <div v-if="!hasMore && historyList.length > 0" class="no-more-text">已经到底了</div>
      <div v-if="historyList.length === 0 && !loading" class="no-history">暂无浏览历史</div>
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
import ChromaGrid from './components/ChromaGrid.vue'
import GlitchText from './components/GlitchText.vue'
import VitalityBackground from './components/VitalityBackground.vue'
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { UserFilled } from '@element-plus/icons-vue'
import request from './request'
import { convertToExternalUrl } from './utils/imageUtils'

const router = useRouter()

const userInfo = ref(null)
const nickName = ref('')
const avatarUrl = ref('')

const historyList = ref([])
const loading = ref(false)
const hasMore = ref(true)
const pageNo = ref(1)
const total = ref(0)
const collectLock = ref({})
const PAGE_SIZE = 10

const handleLogout = () => {
  localStorage.removeItem('userInfo')
  localStorage.removeItem('token')
  router.push('/')
}

const goToProfile = () => {
  router.push('/profile')
}

const goToShoppingCart = () => {
  router.push('/shopping-cart')
}

const goToAddress = () => {
  router.push('/address')
}

const goToOrderList = () => {
  router.push('/order-list')
}

const getHistoryPage = async (pageNum, pageSize) => {
  try {
    const params = new URLSearchParams({
      pageNo: pageNum.toString(),
      pageSize: pageSize.toString()
    })
    const result = await request(`/history/page?${params.toString()}`)
    
    if (result && result.code === 200) {
      return result.data
    }
    return null
  } catch (error) {
    console.error('获取浏览历史失败:', error)
    return null
  }
}

const loadHistory = async () => {
  if (loading.value || !hasMore.value) return
  
  loading.value = true
  
  try {
    const pageData = await getHistoryPage(pageNo.value, PAGE_SIZE)
    
    if (pageData && pageData.list && pageData.list.length > 0) {
      const processedList = pageData.list.map(item => ({
        ...item,
        isLike: item.isLike !== undefined ? item.isLike : false
      }))
      historyList.value = [...historyList.value, ...processedList]
      pageNo.value = pageNo.value + 1
      total.value = pageData.total
      hasMore.value = historyList.value.length < pageData.total
    } else {
      hasMore.value = false
    }
  } catch (error) {
    console.error('加载浏览历史失败:', error)
  } finally {
    loading.value = false
  }
}

const toggleCollect = async (item) => {
  if (collectLock.value[item.id]) return
  
  collectLock.value[item.id] = true
  
  try {
    const newIsLike = !item.isLike
    const result = await request('/collect', {
      method: 'POST',
      body: JSON.stringify({
        goodsId: item.id,
        isCollect: newIsLike
      })
    })
    
    if (result && result.code === 200) {
      item.isLike = newIsLike
      ElMessage.success(newIsLike ? '收藏成功' : '取消收藏')
    } else {
      ElMessage.error('操作失败')
    }
  } catch (error) {
    console.error('收藏操作失败:', error)
    ElMessage.error('操作失败')
  } finally {
    collectLock.value[item.id] = false
  }
}

const viewProductDetail = (item) => {
  const productId = item.id || item.bookId
  router.push(`/product?id=${productId}`)
}

const goBackHome = () => {
  router.push('/home')
}

const formatTime = (timestamp) => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const handleScroll = () => {
  const scrollTop = window.pageYOffset || document.documentElement.scrollTop
  const windowHeight = window.innerHeight
  const documentHeight = document.documentElement.scrollHeight
  
  if (scrollTop + windowHeight >= documentHeight - 100 && !loading.value && hasMore.value) {
    loadHistory()
  }
}

onMounted(() => {
  const stored = localStorage.getItem('userInfo')
  if (stored) {
    userInfo.value = JSON.parse(stored)
    if (userInfo.value && userInfo.value.nickName) {
      nickName.value = userInfo.value.nickName.substring(0, 5)
    }
    if (userInfo.value && userInfo.value.avatar) {
      avatarUrl.value = convertToExternalUrl(userInfo.value.avatar)
    }
  }
  
  loadHistory()
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
.history-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: transparent;
  position: relative;
  z-index: 1;
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

a {
  text-decoration: none;
  color: #333;
}

a:hover {
  color: #f23030;
}

.top-nav {
  position: sticky;
  top: 0;
  z-index: 300;
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  box-shadow: 0 1px 0 rgba(37,99,235, 0.06);
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

.main-content {
  width: 1200px;
  margin: 30px auto;
  padding: 30px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  min-height: 600px;
}

.history-header {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 30px;
  padding-bottom: 15px;
  border-bottom: 2px solid #f5f5f5;
}

.history-header h1 {
  font-size: 24px;
  color: #333;
  font-weight: 600;
}

.count-text {
  font-size: 14px;
  color: #999;
}

.back-home-link {
  margin-left: auto;
  font-size: 14px;
  color: #2563eb;
  cursor: pointer;
  transition: color 0.3s;
}

.back-home-link:hover {
  color: #3b82f6;
}

.loading-text, .no-more-text, .no-history {
  text-align: center;
  color: #999;
  font-size: 14px;
  padding: 40px 0;
}

.footer {
  background-color: #f5f5f5;
  border-top: 1px solid #e5e5e5;
  padding: 30px 0;
  margin-top: auto;
}

.footer-content {
  width: 1200px;
  margin: 0 auto;
  text-align: center;
}

.footer-links {
  display: flex;
  justify-content: center;
  gap: 30px;
  margin-bottom: 15px;
}

.footer-links span {
  font-size: 14px;
  color: #666;
}
</style>
