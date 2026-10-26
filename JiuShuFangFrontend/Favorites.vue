<template>
  <!-- 动态背景 -->
  <VitalityBackground />

  <div class="favorites-container">
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
      <div class="favorites-header">
        <h1>我的关注</h1>
        <span class="count-text">共 {{ total }} 个商品</span>
        <span class="back-home-link" @click="goBackHome">返回首页</span>
      </div>

      <ChromaGrid
        :items="favoritesList"
        :radius="300"
        :damping="0.45"
        :fadeOut="0.6"
        @item-click="viewProductDetail"
      />

      <div v-if="loading" class="loading-text">加载中...</div>
      <div v-if="favoritesList.length === 0 && !loading" class="no-favorites">暂无收藏商品</div>
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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { UserFilled } from '@element-plus/icons-vue'
import request from './request'
import { convertToExternalUrl } from './utils/imageUtils'

const router = useRouter()

const userInfo = ref(null)
const nickName = ref('')
const avatarUrl = ref('')

const favoritesList = ref([])
const loading = ref(false)
const total = ref(0)
const collectLock = ref({})

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

const goToHistory = () => {
  router.push('/history')
}

const goToOrderList = () => {
  router.push('/order-list')
}

const getFavoritesList = async () => {
  try {
    const result = await request('/collect')
    
    if (result && result.code === 200) {
      return result.data
    }
    return null
  } catch (error) {
    console.error('获取收藏列表失败:', error)
    return null
  }
}

const loadFavorites = async () => {
  if (loading.value) return
  
  loading.value = true
  
  try {
    const result = await getFavoritesList()
    
    console.log('查询结果:', result)
    console.log('result.data:', result?.data)
    
    if (result && Array.isArray(result)) {
      // 直接返回数组，倒序展示
      const processedList = result.map(item => ({
        ...item,
        isLike: item.isLike !== undefined ? item.isLike : true
      })).reverse()
      favoritesList.value = processedList
      total.value = result.length
    } else if (result && result.data && Array.isArray(result.data)) {
      // 返回的是 { data: [...] }，倒序展示
      const processedList = result.data.map(item => ({
        ...item,
        isLike: item.isLike !== undefined ? item.isLike : true
      })).reverse()
      favoritesList.value = processedList
      total.value = result.data.length
    }
    
    console.log('加载完成，列表长度:', favoritesList.value.length)
  } catch (error) {
    console.error('加载收藏失败:', error)
  } finally {
    loading.value = false
    console.log('loading 已设置为 false')
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
      
      // 如果取消收藏，从列表中移除
      if (!newIsLike) {
        favoritesList.value = favoritesList.value.filter(fav => fav.id !== item.id)
        total.value = total.value - 1
      }
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
  
  loadFavorites()
})
</script>

<style scoped>
.favorites-container {
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

.favorites-header {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 30px;
  padding-bottom: 15px;
  border-bottom: 2px solid #f5f5f5;
}

.favorites-header h1 {
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

.loading-text, .no-more-text, .no-favorites {
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
