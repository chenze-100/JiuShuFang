<template>
  <!-- 动态背景 -->
  <VitalityBackground />

  <div class="home-container">
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
            @click="goHome"
          />
        </div>
        <div class="nav-right">
          <span class="nav-item"><a href="#" @click.prevent="goToShoppingCart">我的购物车</a></span>
          <span class="nav-item"><a href="#" @click.prevent="goToAddShop">开设商铺</a></span>
          <span class="nav-item">我的优惠券</span>
          <UserMenu :userInfo="userInfo" @logout="handleLogout" />
        </div>
      </div>
    </div>

    <!-- 搜索框 -->
    <div class="search-section">
      <div class="search-container">
        <div class="search-input-wrapper">
          <input v-model="searchKeyword" type="text" class="search-input" placeholder="请输入书名搜索" @keyup.enter="handleSearch" />
          <div class="search-btn-group">
            <button class="search-btn" @click="handleSearch">
              <span>搜索</span>
            </button>
            <button class="ai-btn" @click="goToAIChat">
              <span>小坊助手</span>
            </button>
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

    <!-- 快捷入口 -->
    <div class="quick-links">
      <div class="links-content">
        <div class="link-item" @click="goToShoppingCart">
          <div class="link-icon">🛒</div>
          <div class="link-text">购物车</div>
        </div>
        <div class="link-item" @click="goToFavorites">
          <div class="link-icon">❤️</div>
          <div class="link-text">我的关注</div>
        </div>
        <div class="link-item" @click="goToHistory">
          <div class="link-icon">📜</div>
          <div class="link-text">我的足迹</div>
        </div>
        <div class="link-item" @click="goToOrderList">
          <div class="link-icon">📦</div>
          <div class="link-text">我的订单</div>
        </div>
        <div class="link-item" @click="goToMyShop">
          <div class="link-icon">🏪</div>
          <div class="link-text">我的商铺</div>
        </div>
        <div class="link-item" @click="goToChatList">
          <div class="link-icon">💬</div>
          <div class="link-text">我的消息</div>
        </div>
      </div>
    </div>

    <!-- 页面主体 -->
    <div class="main-wrapper">
      <!-- 左侧商品分类 - 独立于 grid 外层 -->
      <div class="category-sidebar">
        <div class="category-title">
          <span>商品分类</span>
        </div>
        <div class="category-list" v-if="categories.length > 0">
          <div v-for="item in displayCategories" :key="item.id" class="category-item" @click="selectCategory(item)">
            <div class="category-name">{{ item.name }}</div>
          </div>
        </div>
        <div v-if="showAll && categoryHasMore" class="load-more" @click="loadMoreCategories">
          <span>加载更多</span>
        </div>
      </div>

      <!-- 中间内容区 -->
      <div class="center-content">
        <!-- 为你推荐 -->
        <div class="recommend-section">
          <div class="section-header">
            <h3>为你推荐</h3>
            <span class="view-all" @click="viewAllCategories">查看全部</span>
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
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { Top } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from './request'
import { convertToExternalUrl } from './utils/imageUtils'
import UserMenu from './components/UserMenu.vue'
import ChromaGrid from './components/ChromaGrid.vue'
import GlitchText from './components/GlitchText.vue'
import VitalityBackground from './components/VitalityBackground.vue'

const router = useRouter()

const userInfo = ref(null)
const nickName = ref('')
const avatarUrl = ref('')

const categories = ref([])
const showAll = ref(false)
const categoryPage = ref(1)
const categoryHasMore = ref(false)
const categoryLoading = ref(false)
const isCategoryLoaded = ref(false)

const goodsList = ref([])
const goodsPage = ref(1)
const goodsHasMore = ref(false)
const goodsLoading = ref(false)
const goodsTotal = ref(0)

const showBackToTop = ref(false)

const searchKeyword = ref('')
const collectLock = ref({})

const INITIAL_PAGE_SIZE = 10
const LOAD_PAGE_SIZE = 20
const GOODS_PAGE_SIZE = 20

const displayCategories = computed(() => {
  return categories.value
})

const goHome = () => {
  router.push('/home')
}

const handleUnauthorized = () => {
  ElMessage.error('登录已过期，请重新登录')
  localStorage.removeItem('userInfo')
  localStorage.removeItem('token')
  window.location.href = '/'
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
    const result = await request(`/api/goods/list?${params.toString()}`)

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
  if (searchKeyword.value) {
    router.push(`/search?keyword=${encodeURIComponent(searchKeyword.value)}`)
  }
}

const goToAIChat = () => {
  router.push('/ai-chat')
}


const goToFavorites = () => {
  router.push('/favorites')
}

const goToShoppingCart = () => {
  router.push('/shopping-cart')
}

const goToAddShop = () => {
  router.push('/add-shop')
}

const goToAddress = () => {
  router.push('/address')
}

const goToHistory = () => {
  router.push('/history')
}

const goToProfile = () => {
  router.push('/profile')
}

const goToOrderList = () => {
  router.push('/order-list')
}

const goToMyShop = () => {
  router.push('/my-shop')
}

const goToChatList = () => {
  router.push('/chat-list')
}

const loadGoods = async () => {
  // 正在加载中直接返回（防止触底重复触发）
  if (goodsLoading.value || !goodsHasMore.value) return

  goodsLoading.value = true
  const nextPage = goodsList.value.length === 0 ? 1 : goodsPage.value + 1
  const pageData = await getGoodsList(nextPage, GOODS_PAGE_SIZE)

  if (pageData && pageData.list && pageData.list.length > 0) {
    const processedList = pageData.list.map(item => ({
      ...item,
      isLike: item.isLike !== undefined ? item.isLike : false
    }))
    goodsList.value = [...goodsList.value, ...processedList]
    goodsPage.value = nextPage
    goodsTotal.value = pageData.total
    goodsHasMore.value = goodsList.value.length < pageData.total
  } else {
    // 返回空页兜底：强制收敛，防止无限触发
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
    }
  } catch (error) {
    console.error('收藏操作失败:', error)
  } finally {
    collectLock.value[item.id] = false
  }
}

const getCategoryList = async (pageNo, pageSize) => {
  try {
    const params = new URLSearchParams({
      pageNo: pageNo.toString(),
      pageSize: pageSize.toString(),
      isAsc: 'true',
      sortBy: 'sort'
    })
    const result = await request(`/category/list?${params.toString()}`)

    if (result && result.code === 200 && result.data) {
      return result.data
    }
    return null
  } catch (error) {
    console.error('获取分类列表失败:', error)
    return null
  }
}

const loadInitialCategories = async () => {
  if (isCategoryLoaded.value) return

  categoryLoading.value = true
  const pageData = await getCategoryList(1, LOAD_PAGE_SIZE)

  if (pageData && pageData.list && pageData.list.length > 0) {
    categories.value = pageData.list
    categoryPage.value = 1
    categoryHasMore.value = pageData.total > LOAD_PAGE_SIZE
    isCategoryLoaded.value = true
  }
  categoryLoading.value = false
}

const loadMoreCategories = async () => {
  if (categoryLoading.value || !categoryHasMore.value) return

  categoryLoading.value = true
  const nextPage = categoryPage.value + 1
  const pageData = await getCategoryList(nextPage, LOAD_PAGE_SIZE)

  if (pageData && pageData.list && pageData.list.length > 0) {
    categories.value = [...categories.value, ...pageData.list]
    categoryPage.value = nextPage
    categoryHasMore.value = pageData.total > categories.value.length
  } else {
    categoryHasMore.value = false
  }
  categoryLoading.value = false
}

const viewAllCategories = () => {
  router.push('/search')
}

const selectCategory = (category) => {
  router.push(`/search?categoryId=${category.id}`)
}

const viewProductDetail = (item) => {
  const productId = item.id || item.bookId
  router.push({ path: '/product', query: { id: productId } })
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

onMounted(async () => {
  const storedToken = localStorage.getItem('token')

  const urlParams = new URLSearchParams(window.location.search)
  const code = urlParams.get('code')
  const state = urlParams.get('state')

  if (code) {
    try {
      const result = await request(`/api/user/login/check/QRCode?code=${code}&state=${state}`)

      if (!result || result.code !== 200) {
        ElMessage.error('二维码已失效，请重新扫码')
        return
      }

      if (result && result.code === 200 && result.data) {
        userInfo.value = result.data
        if (result.data.token) {
          localStorage.setItem('token', result.data.token)
        }
        localStorage.setItem('userInfo', JSON.stringify(result.data))
        ElMessage.success('扫码登录成功')

        window.history.replaceState({}, document.title, '/home')

        setUserInfo(result.data)
      } else {
        ElMessage.error('登录失败')
      }
    } catch (error) {
      console.error('登录失败:', error)
      ElMessage.error('登录失败，请重试')
    }
  } else {
    const stored = localStorage.getItem('userInfo')
    if (stored) {
      userInfo.value = JSON.parse(stored)
      setUserInfo(userInfo.value)
    }
  }

  // 不阻塞页面渲染，后台加载数据
  loadInitialCategories()

  goodsList.value = []
  goodsPage.value = 1
  goodsHasMore.value = true
  goodsLoading.value = false

  loadGoods()

  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})

const handleScroll = () => {
  const scrollTop = window.pageYOffset || document.documentElement.scrollTop
  const windowHeight = window.innerHeight
  const documentHeight = document.documentElement.scrollHeight

  showBackToTop.value = scrollTop > 400

  // 距离底部 100px 时自动加载下一页
  if (scrollTop + windowHeight >= documentHeight - 100) {
    loadGoods()
  }
}

const scrollToTop = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const setUserInfo = (data) => {
  if (data && data.nickName) {
    nickName.value = data.nickName.substring(0, 5)
  }
  if (data && data.avatar) {
    avatarUrl.value = convertToExternalUrl(data.avatar)
  }
}
</script>

<style scoped>
/* === 首页 - Neo-Editorial Youth 美学 === */
.home-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: transparent;
  position: relative;
  z-index: 1;
  overflow-x: hidden;
}

/* 页面加载动画 */
.top-nav,
.search-section,
.quick-links,
.category-sidebar,
.center-content,
.footer {
  animation: fadeInUp 0.7s cubic-bezier(0.16, 1, 0.3, 1) forwards;
  opacity: 0;
  transform: translateY(24px);
}

.top-nav { animation-delay: 0.05s; z-index: 300; }
.search-section { animation-delay: 0.15s; }
.quick-links { animation-delay: 0.25s; }
.category-sidebar { animation-delay: 0.3s; }
.center-content { animation-delay: 0.35s; }
.footer { animation-delay: 0.45s; z-index: 10; position: relative; }

@keyframes fadeInUp {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* === 顶部导航栏 === */
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

.welcome-text {
  background: linear-gradient(135deg, #2563eb 0%, #3b82f6 50%, #1d4ed8 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  font-size: 28px;
  font-weight: 900;
  letter-spacing: -0.5px;
  display: inline-block;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  filter: drop-shadow(0 2px 8px rgba(37,99,235, 0.3));
}

.welcome-text:hover {
  transform: scale(1.06) rotate(-1.5deg);
  filter: drop-shadow(0 4px 16px rgba(37,99,235, 0.45));
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

/* 流光动效 */
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
  box-shadow:
    0 8px 40px rgba(37,99,235, 0.18),
    0 2px 8px rgba(0, 0, 0, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.9);
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
  letter-spacing: 0.2px;
}

.search-input::placeholder {
  color: #9ca3af;
}

.search-btn-group {
  display: flex;
  align-items: stretch;
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

.search-btn::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.search-btn:hover {
  transform: scale(1.02);
  box-shadow: 0 6px 24px rgba(37,99,235, 0.4);
}

.search-btn:hover::after {
  opacity: 1;
}

.search-btn span { position: relative; z-index: 1; }

/* === AI 按钮 === */
.ai-btn {
  height: 56px;
  padding: 0 24px;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  color: #ffffff;
  border: none;
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  letter-spacing: 0.5px;
  border-left: 1px solid rgba(255,255,255,0.3);
  animation: aiBtnShake 3s ease-in-out infinite;
}

.ai-btn::before {
  content: '';
  position: absolute;
  top: -4px;
  left: -4px;
  right: -4px;
  bottom: -4px;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  z-index: -1;
  border-radius: 2px;
}

.ai-btn::after {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: linear-gradient(
    45deg,
    transparent 30%,
    rgba(255, 255, 255, 0.4) 50%,
    transparent 70%
  );
  transform: translateX(-100%) rotate(45deg);
  animation: aiShine 2.5s ease-in-out infinite;
}

@keyframes aiShine {
  0% { transform: translateX(-100%) rotate(45deg); }
  50%, 100% { transform: translateX(200%) rotate(45deg); }
}

.ai-btn:hover {
  transform: scale(1.03);
  box-shadow: 0 8px 28px rgba(59,130,246, 0.5);
  animation: none;
}

@keyframes aiBtnShake {
  0%, 100% { transform: rotate(0deg); }
  2% { transform: rotate(5deg); }
  4% { transform: rotate(-5deg); }
  6% { transform: rotate(5deg); }
  8% { transform: rotate(-5deg); }
  10% { transform: rotate(0deg); }
  92% { transform: rotate(0deg); }
  94% { transform: rotate(5deg); }
  96% { transform: rotate(-5deg); }
  98% { transform: rotate(5deg); }
}

.ai-btn span { position: relative; z-index: 1; }

/* === 回顶部按钮 === */
.back-to-top {
  position: fixed;
  right: 32px;
  bottom: 32px;
  z-index: 999;
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #2563eb 0%, #3b82f6 100%);
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 20px rgba(37,99,235, 0.4);
  transition: all 0.3s ease;
  color: white;
  font-size: 20px;
}

.back-to-top:hover {
  transform: translateY(-4px) scale(1.1);
  box-shadow: 0 8px 32px rgba(37,99,235, 0.5);
}

.back-to-top:active {
  transform: translateY(-2px) scale(1.05);
}

.back-top-fade-enter-active,
.back-top-fade-leave-active {
  transition: all 0.35s ease;
}

.back-top-fade-enter-from,
.back-top-fade-leave-to {
  opacity: 0;
  transform: translateY(16px) scale(0.8);
}

/* === 快捷入口 === */
.quick-links {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 32px;
  width: 100%;
  box-sizing: border-box;
  position: relative;
  z-index: 1;
}

.links-content {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 14px;
  width: 100%;
}

.link-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 22px 16px 20px;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(12px);
  border-radius: 18px;
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
  box-shadow:
    0 2px 12px rgba(37,99,235, 0.07),
    0 1px 3px rgba(0, 0, 0, 0.05),
    inset 0 1px 0 rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(37,99,235, 0.08);
  position: relative;
  overflow: hidden;
  transform-style: preserve-3d;
  perspective: 800px;
}

.link-item::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(145deg, rgba(37,99,235,0.06) 0%, rgba(29,78,216,0.05) 100%);
  opacity: 0;
  transition: opacity 0.35s ease;
  border-radius: 18px;
}

.link-item:hover::before {
  opacity: 1;
}

.link-item:hover {
  transform: translateY(-10px) rotateX(4deg);
  box-shadow:
    0 20px 48px rgba(37,99,235, 0.18),
    0 4px 12px rgba(0, 0, 0, 0.08);
  border-color: rgba(37,99,235, 0.2);
}

.link-icon {
  font-size: 34px;
  display: inline-block;
  transition: transform 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
  filter: drop-shadow(0 2px 4px rgba(0,0,0,0.1));
  position: relative;
  z-index: 1;
}

.link-item:hover .link-icon {
  transform: scale(1.3) rotate(-8deg);
  filter: drop-shadow(0 4px 8px rgba(37,99,235, 0.3));
}

.link-text {
  font-size: 12.5px;
  color: #6b7280;
  font-weight: 600;
  letter-spacing: 0.3px;
  position: relative;
  z-index: 1;
  transition: color 0.25s ease;
}

.link-item:hover .link-text {
  color: #2563eb;
}

/* === 主体内容区 === */
.main-wrapper {
  max-width: 1280px;
  margin: 0 auto;
  padding: 28px 32px 48px;
  width: 100%;
  box-sizing: border-box;
  position: relative;
  z-index: 1;
  display: flex;
  gap: 28px;
  align-items: flex-start;
}

/* === 侧边分类栏 === */
.category-sidebar {
  position: sticky;
  top: 140px;
  height: fit-content;
  width: 240px;
  flex-shrink: 0;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(16px);
  border-radius: 20px;
  padding: 20px 16px;
  box-shadow:
    0 4px 20px rgba(37,99,235, 0.08),
    0 1px 4px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(37,99,235, 0.08);
  transition: all 0.35s ease;
}

.category-sidebar:hover {
  box-shadow:
    0 8px 36px rgba(37,99,235, 0.12),
    0 2px 8px rgba(0, 0, 0, 0.06);
}

.category-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 10px 14px;
  font-size: 13px;
  font-weight: 700;
  color: #1f2937;
  border-bottom: 1.5px solid rgba(37,99,235, 0.12);
  margin-bottom: 6px;
  letter-spacing: 0.5px;
  text-transform: uppercase;
}

.category-list {
  padding: 8px 0;
  max-height: calc(100vh - 220px);
  overflow-y: auto;
}

.category-list::-webkit-scrollbar {
  width: 3px;
}
.category-list::-webkit-scrollbar-track {
  background: transparent;
}
.category-list::-webkit-scrollbar-thumb {
  background: rgba(37,99,235, 0.2);
  border-radius: 4px;
}

.category-item {
  padding: 10px 14px;
  cursor: pointer;
  border-radius: 12px;
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
  font-size: 13px;
  color: #6b7280;
  margin: 3px 0;
  position: relative;
  overflow: hidden;
  font-weight: 500;
  border: 1px solid transparent;
}

.category-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%) scaleY(0);
  height: 60%;
  width: 3px;
  background: linear-gradient(180deg, #2563eb, #1d4ed8);
  border-radius: 0 3px 3px 0;
  transition: transform 0.25s ease;
}

.category-item:hover::before {
  transform: translateY(-50%) scaleY(1);
}

.category-item:hover {
  background: linear-gradient(90deg, rgba(37,99,235,0.08) 0%, rgba(29,78,216,0.04) 100%);
  color: #2563eb;
  font-weight: 600;
  padding-left: 20px;
  border-color: rgba(37,99,235, 0.1);
}

.load-more {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 6px;
  padding: 12px;
  cursor: pointer;
  color: #9ca3af;
  font-size: 12px;
  font-weight: 600;
  transition: all 0.25s ease;
  margin-top: 8px;
  border-radius: 10px;
  border: 1px dashed rgba(37,99,235, 0.15);
  letter-spacing: 0.3px;
}

.load-more:hover {
  color: #2563eb;
  background: rgba(37,99,235, 0.05);
  border-color: rgba(37,99,235, 0.3);
}

/* === 中间商品列表 === */
.center-content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.recommend-section {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(16px);
  border-radius: 20px;
  padding: 28px;
  box-shadow:
    0 4px 20px rgba(37,99,235, 0.07),
    0 1px 4px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(37,99,235, 0.07);
  transition: all 0.35s ease;
}

.recommend-section:hover {
  box-shadow:
    0 8px 36px rgba(37,99,235, 0.1),
    0 2px 8px rgba(0, 0, 0, 0.05);
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
.no-more-text {
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
  padding: 4px 0;
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

/* === 响应式设计 === */
@media (max-width: 1100px) {
  .links-content {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 900px) {
  .main-wrapper {
    padding: 20px 24px 40px;
    gap: 20px;
  }
}

@media (max-width: 768px) {
  .nav-content {
    padding: 0 16px;
  }

  .search-section {
    padding: 24px 0;
  }

  .links-content {
    grid-template-columns: repeat(3, 1fr);
    gap: 10px;
    padding: 0 16px;
  }

  .link-item {
    padding: 16px 10px;
  }

  .link-icon {
    font-size: 28px;
  }

  .main-wrapper {
    display: flex;
    flex-direction: column;
    padding: 16px 16px 40px;
    gap: 16px;
  }

  .category-sidebar {
    display: none;
  }

  .recommend-section {
    padding: 20px;
  }

  .footer-links {
    gap: 20px;
  }
}

@media (max-width: 480px) {
  .links-content {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
