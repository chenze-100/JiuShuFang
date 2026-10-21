<template>
  <!-- 动态背景 -->
  <VitalityBackground />

  <div class="search-results-container">
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

    <!-- 搜索框 -->
    <div class="search-section">
      <div class="search-container">
        <div class="search-input-wrapper">
          <input
            v-model="searchKeyword"
            type="text"
            class="search-input"
            placeholder="请输入书名搜索"
            @keyup.enter="handleSearch"
          />
          <div class="search-btn-group">
            <button class="search-btn" @click="handleSearch">搜索</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 回顶部按钮 -->
    <transition name="back-top-fade">
      <div v-if="showBackToTop" class="back-to-top" @click="scrollToTop">
        <el-icon><Top /></el-icon>
      </div>
    </transition>

    <!-- 页面主体 -->
    <div class="main-content">
      <div class="center-content-full">
        <div class="recommend-section">
          <div class="section-header">
            <h3>{{ categoryId ? '商品分类' : '搜索结果' }}</h3>
            <span class="view-all" @click="goToHome">返回首页</span>
          </div>
          <ChromaGrid
            :items="goodsList"
            :radius="300"
            :damping="0.45"
            :fadeOut="0.6"
            @item-click="viewProductDetail"
          />
          <div v-if="goodsLoading" class="loading-text">加载中...</div>
          <div v-if="!goodsHasMore && goodsList.length > 0" class="no-more-text">已经到底了</div>
          <div v-if="goodsList.length === 0 && !goodsLoading" class="no-results">暂无相关商品</div>
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
import ChromaGrid from './components/ChromaGrid.vue'
import GlitchText from './components/GlitchText.vue'
import VitalityBackground from './components/VitalityBackground.vue'
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { UserFilled, Top } from '@element-plus/icons-vue'
import request from './request'
import { useRoute, useRouter } from 'vue-router'
import { convertToExternalUrl } from './utils/imageUtils'

const route = useRoute()
const router = useRouter()

const userInfo = ref(null)
const nickName = ref('')
const avatarUrl = ref('')

const goodsList = ref([])
const goodsPage = ref(1)
const goodsHasMore = ref(false)
const goodsLoading = ref(false)
const goodsTotal = ref(0)

const searchKeyword = ref('')
const categoryId = ref('')
const showBackToTop = ref(false)
const collectLock = ref({})

const GOODS_PAGE_SIZE = 20

const goToHome = () => {
  router.push('/home')
}

const handleLogout = async () => {
  try {
    await request('/user/logout', {
      method: 'POST'
    })
  } catch (error) {
    console.error('退出登录请求失败:', error)
  } finally {
    ElMessage.success('已退出登录')
    localStorage.removeItem('userInfo')
    localStorage.removeItem('token')
    window.location.href = '/'
  }
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

const goToFavorites = () => {
  router.push('/favorites')
}

const goToHistory = () => {
  router.push('/history')
}

const goToOrderList = () => {
  router.push('/order-list')
}

const goToMyShop = () => {
  router.push('/my-shop')
}

const getGoodsList = async (pageNo, pageSize) => {
  try {
    const params = new URLSearchParams({
      pageNo: pageNo.toString(),
      pageSize: pageSize.toString(),
      isAsc: 'false',
      sortBy: 'create_time',
      status: '1'
    })
    if (searchKeyword.value) {
      params.append('bookName', searchKeyword.value)
    }
    if (categoryId.value) {
      params.append('categoryId', categoryId.value)
    }
    const result = await request(`/goods/list?${params.toString()}`)

    if (result && result.code === 200 && result.data) {
      return result.data
    }
    return null
  } catch (error) {
    console.error('获取商品列表失败:', error)
    return null
  }
}

const handleSearch = () => {
  const query = {
    keyword: searchKeyword.value || undefined
  }
  router.push({ path: '/search', query })
}

const goToAIChat = () => {
  router.push('/ai-chat')
}


const viewProductDetail = (item) => {
  const productId = item.id || item.bookId
  router.push({ path: '/product', query: { id: productId } })
}

const loadGoods = async () => {
  if (goodsLoading.value || (!goodsHasMore.value && goodsList.value.length > 0)) return

  goodsLoading.value = true
  const currentPage = goodsList.value.length === 0 ? 1 : goodsPage.value + 1
  const pageData = await getGoodsList(currentPage, GOODS_PAGE_SIZE)

  if (pageData && pageData.list && pageData.list.length > 0) {
    const processedList = pageData.list.map(item => ({
      ...item,
      isLike: item.isLike !== undefined ? item.isLike : false
    }))
    goodsList.value = [...goodsList.value, ...processedList]
    goodsPage.value = currentPage
    goodsTotal.value = pageData.total
    goodsHasMore.value = goodsList.value.length < pageData.total
  } else {
    goodsHasMore.value = false
  }
  goodsLoading.value = false
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

const setUserInfo = (data) => {
  if (data && data.nickName) {
    nickName.value = data.nickName.substring(0, 5)
  }
  if (data && data.avatar) {
    avatarUrl.value = convertToExternalUrl(data.avatar)
  }
}

const handleScroll = () => {
  const scrollTop = window.pageYOffset || document.documentElement.scrollTop
  const windowHeight = window.innerHeight
  const documentHeight = document.documentElement.scrollHeight

  showBackToTop.value = scrollTop > 400

  if (scrollTop + windowHeight >= documentHeight - 100 && !goodsLoading.value && goodsHasMore.value) {
    loadGoods()
  }
}

const scrollToTop = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

onMounted(async () => {
  const stored = localStorage.getItem('userInfo')
  if (stored) {
    userInfo.value = JSON.parse(stored)
    setUserInfo(userInfo.value)
  }

  searchKeyword.value = route.query.keyword || ''
  categoryId.value = route.query.categoryId || ''

  // 不阻塞页面渲染，后台加载数据
  loadGoods()

  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})

// 监听路由变化
watch(() => route.query, () => {
  searchKeyword.value = route.query.keyword || ''
  categoryId.value = route.query.categoryId || ''
  goodsList.value = []
  goodsPage.value = 0
  goodsHasMore.value = true
  loadGoods()
}, { deep: true })
</script>

<style scoped>
/* === 搜索页 - Neo-Editorial Youth 美学 === */
.search-results-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: transparent;
  position: relative;
  z-index: 1;
  overflow-x: hidden;
}

/* 页面加载动画 */
.top-nav, .search-section, .main-content, .footer {
  animation: fadeInUp 0.7s cubic-bezier(0.16, 1, 0.3, 1) forwards;
  opacity: 0;
  transform: translateY(20px);
}

.top-nav { animation-delay: 0.05s; z-index: 300; }
.search-section { animation-delay: 0.15s; }
.main-content { animation-delay: 0.25s; }
.footer { animation-delay: 0.35s; position: relative; z-index: 10; }

@keyframes fadeInUp {
  to { opacity: 1; transform: translateY(0); }
}

/* === 顶部导航栏 === */
.top-nav {
  position: sticky;
  top: 0;
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
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

.welcome-text {
  background: linear-gradient(135deg, #2563eb 0%, #3b82f6 50%, #1d4ed8 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  font-size: 24px;
  font-weight: 900;
  letter-spacing: -0.5px;
  cursor: pointer;
  transition: all 0.3s ease;
  filter: drop-shadow(0 2px 6px rgba(37,99,235, 0.3));
}

.welcome-text:hover {
  transform: scale(1.06) rotate(-1.5deg);
  filter: drop-shadow(0 4px 12px rgba(37,99,235, 0.45));
}

.nav-right {
  display: flex;
  gap: 4px;
  align-items: center;
}

.nav-item a {
  padding: 8px 14px;
  border-radius: 10px;
  color: #6b7280;
  font-size: 13px;
  font-weight: 500;
  transition: all 0.25s ease;
  text-decoration: none;
}

.nav-item a:hover {
  color: #2563eb;
  background: rgba(37,99,235, 0.08);
}

/* === 搜索区域 === */
.search-section {
  background: linear-gradient(160deg, #f5f8ff 0%, #eaf1ff 50%, #dce8ff 100%);
  padding: 36px 0 32px;
  position: sticky;
  top: 0;
  z-index: 200;
  border-bottom: 1px solid rgba(37,99,235, 0.08);
}

.search-container {
  max-width: 860px;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  justify-content: center;
}

.search-input-wrapper {
  display: flex;
  width: 100%;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(12px);
  border-radius: 20px;
  overflow: hidden;
  box-shadow:
    0 4px 24px rgba(37,99,235, 0.1),
    0 1px 4px rgba(0, 0, 0, 0.06),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
  transition: all 0.35s cubic-bezier(0.16, 1, 0.3, 1);
  border: 1.5px solid rgba(37,99,235, 0.12);
  position: relative;
}

.search-input-wrapper::before {
  content: '';
  position: absolute;
  top: -2px;
  left: -2px;
  right: -2px;
  height: 3px;
  background: linear-gradient(90deg, transparent, #2563eb, #1d4ed8, transparent);
  border-radius: 20px 20px 0 0;
  opacity: 0;
  transition: opacity 0.35s ease;
}

.search-input-wrapper:focus-within {
  box-shadow: 0 8px 40px rgba(37,99,235, 0.18), 0 2px 8px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
  border-color: rgba(37,99,235, 0.3);
}

.search-input-wrapper:focus-within::before {
  opacity: 1;
}

.search-input {
  flex: 1;
  height: 56px;
  padding: 0 24px;
  border: none;
  font-size: 15px;
  outline: none;
  font-family: inherit;
  background: transparent;
  color: #1f2937;
}

.search-input::placeholder {
  color: #9ca3af;
}

.search-btn-group {
  display: flex;
}

.search-btn {
  height: 56px;
  padding: 0 28px;
  background: linear-gradient(135deg, #2563eb 0%, #3b82f6 100%);
  color: white;
  border: none;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  letter-spacing: 0.5px;
}

.search-btn:hover {
  transform: scale(1.02);
  box-shadow: 0 6px 24px rgba(37,99,235, 0.4);
}

/* === 主体内容 === */
.main-content {
  max-width: 1280px;
  margin: 0 auto;
  padding: 28px 32px 48px;
  width: 100%;
  box-sizing: border-box;
  position: relative;
  z-index: 1;
  flex: 1;
}

.center-content-full {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.recommend-section {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(16px);
  border-radius: 20px;
  padding: 28px;
  box-shadow: 0 4px 20px rgba(37,99,235, 0.07), 0 1px 4px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(37,99,235, 0.07);
  transition: all 0.35s ease;
}

.recommend-section:hover {
  box-shadow: 0 8px 36px rgba(37,99,235, 0.1), 0 2px 8px rgba(0, 0, 0, 0.05);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.section-header h3 {
  font-size: 17px;
  color: #1f2937;
  font-weight: 700;
  letter-spacing: -0.2px;
  position: relative;
  padding-left: 14px;
}

.section-header h3::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 18px;
  background: linear-gradient(180deg, #2563eb, #1d4ed8);
  border-radius: 4px;
}

.view-all {
  font-size: 13px;
  color: #9ca3af;
  cursor: pointer;
  font-weight: 600;
  padding: 6px 14px;
  border-radius: 20px;
  transition: all 0.25s ease;
  border: 1px solid transparent;
}

.view-all:hover {
  color: #2563eb;
  background: rgba(37,99,235, 0.06);
  border-color: rgba(37,99,235, 0.15);
}

.loading-text,
.no-more-text,
.no-results {
  text-align: center;
  color: #9ca3af;
  font-size: 13px;
  padding: 32px 0;
  font-weight: 500;
}

/* === 底部 === */
.footer {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(12px);
  border-top: 1px solid rgba(0, 0, 0, 0.06);
  padding: 36px 0;
  margin-top: auto;
}

.footer-content {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 32px;
  text-align: center;
}

.footer-links {
  display: flex;
  justify-content: center;
  gap: 36px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.footer-links a {
  color: #9ca3af;
  font-size: 13px;
  font-weight: 500;
  text-decoration: none;
  transition: color 0.25s ease;
  position: relative;
}

.footer-links a::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  width: 0;
  height: 1.5px;
  background: linear-gradient(90deg, #2563eb, #1d4ed8);
  transition: all 0.3s ease;
  transform: translateX(-50%);
}

.footer-links a:hover {
  color: #2563eb;
}

.footer-links a:hover::after {
  width: 100%;
}

/* === Responsive === */
@media (max-width: 768px) {
  .nav-content { padding: 0 16px; }
  .search-section { padding: 24px 0; }
  .search-container { padding: 0 16px; }
  .main-content { padding: 16px 16px 40px; }
  .recommend-section { padding: 20px; }
  .footer-links { gap: 20px; }
}

@media (max-width: 480px) {
  .search-btn-group { flex-direction: column; }
  .search-btn { height: 44px; font-size: 14px; }
  .search-input { height: 44px; }
}
</style>
