<template>
  <!-- 动态背景 -->
  <VitalityBackground />

  <div class="my-shop-container">
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
      <!-- 店铺头部 -->
      <div class="shop-header">
        <div class="shop-info">
          <h1>{{ shopOwnerInfo.nickName }}的商铺</h1>
          <div class="header-actions">
            <span class="back-home-link" @click="goToUploadGoods">上架商品</span>
            <span class="back-home-link" @click="goBackHome">返回首页</span>
          </div>
        </div>
        <div class="shop-stats">
          <div class="stat-item">
            <span class="stat-value">{{ shopStats?.totalProducts || 0 }}</span>
            <span class="stat-label">商品数量</span>
          </div>
          <div class="stat-item">
            <span class="stat-value">{{ shopStats?.totalSales || 0 }}</span>
            <span class="stat-label">总销量</span>
          </div>
          <div class="stat-item">
            <span class="stat-value">{{ shopStats?.totalOrders || 0 }}</span>
            <span class="stat-label">订单数量</span>
          </div>
        </div>
      </div>

      <!-- 筛选栏 -->
      <div class="filter-section">
        <div class="status-tabs">
          <span
            class="status-tab"
            :class="{ active: queryParams.status === null }"
            @click="handleStatusChange(null)"
          >全部</span>
          <span
            class="status-tab"
            :class="{ active: queryParams.status === 1 }"
            @click="handleStatusChange(1)"
          >售卖中</span>
          <span
            class="status-tab"
            :class="{ active: queryParams.status === 2 }"
            @click="handleStatusChange(2)"
          >已下架</span>
          <span
            class="status-tab"
            :class="{ active: queryParams.status === 3 }"
            @click="handleStatusChange(3)"
          >已售空</span>
        </div>
        <div class="filter-group">
          <input
            v-model="queryParams.bookName"
            type="text"
            class="filter-input"
            placeholder="书籍名称"
          />
          <input
            v-model="queryParams.author"
            type="text"
            class="filter-input"
            placeholder="作者"
          />
          <el-select
            v-model="queryParams.degree"
            placeholder="成色"
            class="filter-select"
            clearable
          >
            <el-option label="全新" :value="1" />
            <el-option label="九成新" :value="2" />
            <el-option label="八成新" :value="3" />
            <el-option label="七成新及以下" :value="4" />
          </el-select>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </div>
      </div>

      <!-- 商品列表 -->
      <div class="shop-list" ref="shopListRef" @scroll="handleScroll">
        <div
          v-for="item in goodsList"
          :key="item.id"
          class="shop-item"
        >
          <div class="item-image" @click="goToProductDetail(item.id, item.status)">
            <div class="image-wrapper">
              <img
                v-if="item.bookImg"
                :src="convertToExternalUrl(item.bookImg)"
                :alt="item.bookName"
                class="item-img"
                :class="{ 'sold-out-img': item.status === 2 || item.status === 3 || item.stock === 0 }"
              />
              <div v-else class="placeholder-img">商品图片</div>
              <div v-if="item.status === 2" class="sold-out-overlay">
                <span class="sold-out-text">已下架</span>
              </div>
              <div v-else-if="item.stock === 0" class="sold-out-overlay sold-out-sold">
                <span class="sold-out-text">已售空</span>
              </div>
            </div>
          </div>
          <div class="item-info">
            <div class="item-title" @click="goToProductDetail(item.id, item.status)">{{ item.bookName }}</div>
            <div class="item-meta">
              <span class="meta-item">{{ item.author }}</span>
              <span class="meta-item">{{ item.publisher }}</span>
            </div>
            <div class="item-price">
              <span class="price-label">售价</span>
              <span class="price-value">¥{{ item.price }}</span>
            </div>
            <div class="item-stats">
              <span class="stat">销量：{{ (item.totalNumber || 0) - (item.stock || 0) }}</span>
              <span class="stat">库存：{{ item.stock || 0 }}</span>
            </div>
            <div class="item-actions">
              <el-button
                v-if="item.status !== 3 && isMyShop"
                type="primary"
                size="small"
                @click="editProduct(item)"
              >
                编辑
              </el-button>
              <template v-if="item.status === 1 && isMyShop">
                <el-button
                  type="warning"
                  size="small"
                  @click="toggleStatus(item)"
                >
                  下架
                </el-button>
              </template>
              <template v-else-if="item.status === 2 && isMyShop">
                <el-button
                  type="success"
                  size="small"
                  @click="toggleStatus(item)"
                >
                  上架
                </el-button>
                <el-button
                  v-if="((item.totalNumber || 0) - (item.stock || 0)) <= 0 && isMyShop"
                  type="danger"
                  size="small"
                  @click="deleteProduct(item)"
                >
                  删除
                </el-button>
              </template>
              <template v-else>
                <!-- 已售空商品无操作按钮 -->
              </template>
            </div>
          </div>
        </div>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="loading-text">加载中...</div>
      <div v-if="!loading && !goodsHasMore && goodsList.length > 0" class="no-more-text">已经到底了</div>
      <div v-if="!loading && goodsList.length === 0" class="no-products">暂无商品</div>
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

    <!-- 删除确认弹窗 -->
    <Teleport to="body">
      <Transition name="dialog-fade">
        <div v-if="deleteDialog.visible" class="dialog-overlay" @click.self="cancelDelete">
          <div class="delete-dialog">
            <div class="dialog-icon">
              <svg width="48" height="48" viewBox="0 0 24 24" fill="none">
                <circle cx="12" cy="12" r="10" stroke="#ff6b6b" stroke-width="2"/>
                <path d="M12 7v6M12 16v1" stroke="#ff6b6b" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </div>
            <h3 class="dialog-title">确认删除</h3>
            <p class="dialog-message">确定要删除商品 <strong>{{ deleteDialog.bookName }}</strong> 吗？删除后不可恢复。</p>
            <div class="dialog-actions">
              <button class="btn-cancel" @click="cancelDelete">取消</button>
              <button class="btn-confirm" @click="confirmDelete">确认删除</button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup>
import UserMenu from './components/UserMenu.vue'
import GlitchText from './components/GlitchText.vue'
import VitalityBackground from './components/VitalityBackground.vue'
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from './request'
import { convertToExternalUrl } from './utils/imageUtils'

const route = useRoute()
const router = useRouter()

const userInfo = ref(null)
const nickName = ref('')
const avatarUrl = ref('')

const goodsList = ref([])
const goodsPage = ref(1)
const goodsTotal = ref(0)
const goodsHasMore = ref(true)
const goodsLoading = ref(false)
const loading = ref(false)
const shopListRef = ref(null)
const isMyShop = ref(true)

const queryParams = ref({
  bookName: '',
  author: '',
  sellerId: '',
  degree: null,
  status: null
})

const shopStats = ref({
  totalProducts: 0,
  totalSales: 0,
  totalOrders: 0
})

const shopOwnerInfo = ref({
  nickName: '我的商铺',
  id: null
})

const deleteDialog = ref({
  visible: false,
  bookName: '',
  item: null
})

// 获取用户信息
const getUserInfo = () => {
  const userInfoStr = localStorage.getItem('userInfo')
  if (userInfoStr) {
    const user = JSON.parse(userInfoStr)
    userInfo.value = user
    nickName.value = user.nickName || user.username || '用户'
    avatarUrl.value = user.avatar || ''
    return user
  }
  return null
}

// 获取商铺所有者信息
const loadShopOwnerInfo = async () => {
  const routeSellerId = route.query.sellerId
  const isViewSellerStore = route.query.isViewSellerStore === 'true'

  // 如果没有传递 sellerId，则使用本地用户的 ID
  const sellerId = routeSellerId || (localStorage.getItem('userInfo') ? JSON.parse(localStorage.getItem('userInfo')).id : null)

  if (sellerId) {
    try {
      const result = await request(`/user/info/${sellerId}`)
      if (result && result.code === 200 && result.data) {
        const userVO = result.data
        shopOwnerInfo.value.nickName = userVO.nickName || '用户'
        shopOwnerInfo.value.id = userVO.id
      }
    } catch (error) {
      console.error('获取商铺所有者信息失败:', error)
    }
  }
}

// 获取商铺商品列表
const loadShopProducts = async (page = 1) => {
  if (goodsLoading.value || (!goodsHasMore.value && goodsList.value.length > 0)) return

  goodsLoading.value = true

  try {
    const user = getUserInfo()
    if (!user) {
      ElMessage.warning('请先登录')
      router.push('/login')
      return
    }

    // 设置参数
    const routeSellerId = route.query.sellerId
    // 只要在商铺页面就传递 isViewSellerStore=true
    const isViewSellerStore = true
    // 判断是否是自己的商铺（没有传 sellerId 或者是自己的 id）
    isMyShop.value = !routeSellerId || routeSellerId == user.id

    // 如果没有传递 sellerId，则使用本地用户的 ID
    const sellerId = routeSellerId || user.id

    const params = {
      page: page,
      size: 10,
      sellerId: sellerId,
      bookName: queryParams.value.bookName,
      author: queryParams.value.author,
      degree: queryParams.value.degree,
      isViewSellerStore: true
    }

    // 只有 status 有值时才添加
    if (queryParams.value.status !== null && queryParams.value.status !== '') {
      params.status = queryParams.value.status
    }

    console.log('请求参数:', params)

    const result = await request('/goods/list', {
      method: 'GET',
      params
    })

    console.log('商铺商品数据:', result, 'status参数:', queryParams.value.status)

    if (result && result.code === 200 && result.data) {
      const list = result.data.list || []

      if (page === 1) {
        goodsList.value = list
      } else {
        goodsList.value = [...goodsList.value, ...list]
      }

      goodsPage.value = page
      goodsTotal.value = result.data.total
      goodsHasMore.value = goodsList.value.length < result.data.total
    } else {
      ElMessage.error(result.message || '加载商品失败')
    }
  } catch (error) {
    console.error('加载商铺商品失败:', error)
    ElMessage.error('加载商品失败')
  } finally {
    goodsLoading.value = false
  }
}

// 滚动加载更多
const handleScroll = (e) => {
  const { scrollTop, scrollHeight, clientHeight } = e.target
  // 距离底部还有 50px 时触发加载
  if (scrollTop + clientHeight >= scrollHeight - 50 && !goodsLoading.value && goodsHasMore.value) {
    loadMore()
  }
}

// 加载更多
const loadMore = () => {
  if (goodsHasMore.value) {
    loadShopProducts(goodsList.value.length === 0 ? 1 : goodsPage.value + 1)
  }
}

// 获取商铺统计信息
const loadShopStats = async () => {
  try {
    const user = getUserInfo()
    if (!user) {
      return
    }

    // 设置参数
    const routeSellerId = route.query.sellerId
    const sellerId = routeSellerId || user.id
    const isViewSellerStore = route.query.isViewSellerStore === 'true'

    const result = await request(`/goods/stats/seller/${sellerId}`, {
      method: 'GET',
      params: {
        isViewSellerStore: isViewSellerStore
      }
    })

    console.log('商铺统计:', result)

    if (result && result.code === 200 && result.data) {
      shopStats.value = result.data
    }
  } catch (error) {
    console.error('加载商铺统计失败:', error)
  }
}

// 搜索
const handleSearch = () => {
  goodsList.value = []
  goodsPage.value = 1
  goodsHasMore.value = true
  loadShopProducts(1)
}

// 切换状态标签
const handleStatusChange = (status) => {
  queryParams.value.status = status
  handleSearch()
}

// 重置
const handleReset = () => {
  queryParams.value = {
    bookName: '',
    author: '',
    degree: null
  }
  handleSearch()
}

// 编辑商品
const editProduct = (item) => {
  router.push({
    path: '/upload-goods',
    query: {
      goodsId: item.id
    }
  })
}

// 删除商品
const deleteProduct = (item) => {
  deleteDialog.value = {
    visible: true,
    bookName: item.bookName,
    item
  }
}

const cancelDelete = () => {
  deleteDialog.value.visible = false
}

const confirmDelete = async () => {
  const item = deleteDialog.value.item
  deleteDialog.value.visible = false
  try {
    const result = await request(`/goods/${item.id}`, {
      method: 'DELETE'
    })

    if (result && result.code === 200) {
      ElMessage.success('删除成功')
      goodsList.value = goodsList.value.filter(g => g.id !== item.id)
      shopStats.value.totalProducts = Math.max(0, shopStats.value.totalProducts - (item.stock || 0))
    } else {
      ElMessage.error(result.message || '删除失败')
    }
  } catch (error) {
    console.error('删除商品失败:', error)
    ElMessage.error('删除商品失败')
  }
}

// 切换商品上下架状态
const toggleStatus = async (item) => {
  const newStatus = item.status === 1 ? 2 : 1
  try {
    const result = await request('/goods/status', {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ id: item.id, status: newStatus })
    })

    if (result && result.code === 200) {
      ElMessage.success(newStatus === 1 ? '上架成功' : '下架成功')
      item.status = newStatus
      // 重新查询当前状态列表
      goodsList.value = []
      goodsPage.value = 1
      goodsHasMore.value = true
      await loadShopProducts(1)
    } else {
      ElMessage.error(result.message || '操作失败')
    }
  } catch (error) {
    console.error('切换状态失败:', error)
    ElMessage.error('操作失败')
  }
}

// 导航函数
const goBackHome = () => {
  router.push('/home')
}

const goToShoppingCart = () => {
  router.push('/shopping-cart')
}

const goToProfile = () => {
  router.push('/profile')
}

const goToOrderList = () => {
  router.push('/order-list')
}

const goToFavorites = () => {
  router.push('/favorites')
}

const goToHistory = () => {
  router.push('/history')
}

const goToAddress = () => {
  router.push('/address')
}

const goToUploadGoods = () => {
  router.push('/upload-goods')
}

const goToProductDetail = (goodsId, status) => {
  const routeSellerId = route.query.sellerId
  const isViewSellerStore = route.query.isViewSellerStore === 'true'

  // 如果是别人商铺且商品状态不是售卖中，不能点击跳转
  if (isViewSellerStore && status !== 1) {
    return
  }

  console.log('跳转到商品详情，goodsId:', goodsId, 'sellerId:', routeSellerId)

  router.push({
    path: '/product',
    query: {
      id: goodsId,
      fromShop: isViewSellerStore,
      sellerId: routeSellerId
    }
  })
}

const handleLogout = () => {
  localStorage.removeItem('userInfo')
  localStorage.removeItem('token')
  router.push('/login')
}

// 初始化
onMounted(() => {
  getUserInfo()
  loadShopOwnerInfo()
  loadShopStats()
  loadShopProducts(1)
})
</script>

<style scoped>
/* === 我的商铺页 === */
.my-shop-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: transparent; position: relative; z-index: 1;
}

/* === 导航栏 === */
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

/* === 主内容 === */
.main-content {
  width: 1200px;
  margin: 20px auto;
  padding: 0;
}

/* === 店铺头部 === */
.shop-header {
  background: linear-gradient(135deg, #fff9f5 0%, #ffffff 100%);
  padding: 30px;
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-lg);
  margin-bottom: 24px;
  border: 1px solid var(--color-neutral-100);
}

.shop-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 16px;
}

.shop-info h1 {
  font-size: 28px;
  font-weight: 700;
  color: var(--color-neutral-700);
  margin: 0;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-dark) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.back-home-link {
  font-size: 14px;
  color: var(--color-neutral-500);
  cursor: pointer;
  padding: 8px 16px;
  background: white;
  border: 2px solid var(--color-neutral-100);
  border-radius: var(--radius-pill);
  display: inline-flex;
  align-items: center;
  gap: 6px;
  transition: all var(--transition-base);
  font-weight: 500;
}

.back-home-link:hover {
  color: var(--color-primary);
  border-color: var(--color-primary);
  background: var(--color-primary-soft);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.shop-stats {
  display: flex;
  gap: 40px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  background: white;
  border-radius: var(--radius-md);
  border: 1px solid var(--color-neutral-100);
  flex: 1;
  transition: all var(--transition-base);
  text-align: center;
}

.stat-item:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: var(--color-primary);
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: var(--color-neutral-500);
  font-weight: 500;
}

/* === 筛选区 === */
.filter-section {
  background: white;
  padding: 24px;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
  margin-bottom: 24px;
  border: 1px solid var(--color-neutral-100);
}

.status-tabs {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--color-neutral-100);
}

.status-tab {
  padding: 6px 20px;
  border-radius: var(--radius-pill);
  font-size: 14px;
  color: var(--color-neutral-500);
  cursor: pointer;
  transition: all var(--transition-base);
  background: white;
  border: 1px solid var(--color-neutral-200);
  font-weight: 500;
}

.status-tab:hover {
  color: var(--color-primary);
  border-color: var(--color-primary-light);
}

.status-tab.active {
  color: white;
  background: var(--gradient-primary);
  border-color: var(--color-primary);
}

.filter-group {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  align-items: center;
}

.filter-input {
  flex: 1;
  min-width: 150px;
  padding: 10px 16px;
  border: 1px solid var(--color-neutral-200);
  border-radius: var(--radius-sm);
  font-size: 14px;
  transition: all var(--transition-base);
  background-color: white;
  color: var(--color-neutral-700);
}

.filter-input::placeholder {
  color: var(--color-neutral-300);
}

.filter-input:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 2px rgba(37,99,235, 0.1);
}

:deep(.el-select) {
  flex: 1;
  min-width: 150px;
}

:deep(.el-select .el-input__wrapper) {
  padding: 0;
  box-shadow: none;
  border: 1px solid var(--color-neutral-200);
  border-radius: var(--radius-sm);
  transition: all var(--transition-base);
  background-color: white;
}

:deep(.el-input__inner) {
  padding: 10px 16px;
  font-size: 14px;
  color: var(--color-neutral-700);
}

:deep(.el-select:hover .el-input__wrapper) {
  border-color: var(--color-primary-light);
}

:deep(.el-select.is-focused .el-input__wrapper) {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 2px rgba(37,99,235, 0.1);
}

:deep(.el-button) {
  padding: 10px 24px !important;
  border-radius: var(--radius-sm);
  font-weight: 500;
  transition: all var(--transition-base);
}

:deep(.el-button--primary) {
  background: var(--gradient-primary);
  border: none;
}

:deep(.el-button--primary:hover) {
  background: var(--gradient-primary);
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
}

:deep(.el-button) {
  border: 1px solid var(--color-neutral-200);
  color: var(--color-neutral-500);
  background-color: white;
}

:deep(.el-button:hover) {
  border-color: var(--color-primary);
  color: var(--color-primary);
  background-color: var(--color-primary-soft);
  transform: translateY(-2px);
}

/* === 商品列表 === */
.shop-list {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  max-height: calc(100vh - 300px);
  overflow-y: auto;
  padding: 10px;
  background: white;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--color-neutral-100);
}

.shop-list::-webkit-scrollbar {
  width: 6px;
}

.shop-list::-webkit-scrollbar-thumb {
  background-color: var(--color-neutral-200);
  border-radius: 3px;
}

.shop-list::-webkit-scrollbar-track {
  background-color: var(--color-bg-tertiary);
}

.shop-item {
  background-color: white;
  border-radius: var(--radius-md);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-base);
  cursor: pointer;
  border: 1px solid var(--color-neutral-100);
  position: relative;
}

.shop-item:hover {
  transform: translateY(-6px);
  box-shadow: var(--shadow-lg);
  border-color: var(--color-primary-light);
}

.item-image {
  width: 100%;
  height: 220px;
  overflow: hidden;
  background: linear-gradient(135deg, #fff9f5 0%, #ffffff 100%);
  position: relative;
  cursor: pointer;
  transition: transform var(--transition-base);
}

.item-image:hover {
  transform: scale(1.02);
}

.image-wrapper {
  width: 100%;
  height: 100%;
  position: relative;
}

.item-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform var(--transition-slow);
}

.sold-out-img {
  filter: grayscale(60%) brightness(0.7);
}

.shop-item:hover .item-img {
  transform: scale(1.08);
}

.sold-out-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.3);
  z-index: 1;
}

.sold-out-text {
  font-size: 28px;
  font-weight: 700;
  color: white;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.5);
  background: linear-gradient(135deg, rgba(37,99,235, 0.9) 0%, rgba(29,78,216, 0.9) 100%);
  padding: 8px 24px;
  border-radius: var(--radius-sm);
  transform: rotate(-15deg);
  border: 2px solid rgba(255, 255, 255, 0.3);
}

.placeholder-img {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-neutral-300);
  font-size: 14px;
  background: linear-gradient(135deg, #fff9f5 0%, #ffffff 100%);
}

.item-info {
  padding: 20px;
}

.item-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-neutral-700);
  margin-bottom: 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  transition: color var(--transition-base);
  cursor: pointer;
}

.shop-item:hover .item-title {
  color: var(--color-primary);
}

.item-meta {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 14px;
}

.meta-item {
  font-size: 13px;
  color: var(--color-neutral-500);
  font-weight: 500;
}

.item-price {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 14px;
  padding: 10px;
  background: linear-gradient(135deg, #fff9f5 0%, #ffffff 100%);
  border-radius: var(--radius-sm);
}

.price-label {
  font-size: 13px;
  color: var(--color-neutral-400);
  font-weight: 500;
}

.price-value {
  font-size: 20px;
  font-weight: 700;
  color: var(--color-primary);
}

.item-stats {
  display: flex;
  gap: 20px;
  margin-bottom: 16px;
  padding: 10px 0;
  border-top: 1px solid var(--color-neutral-100);
  border-bottom: 1px solid var(--color-neutral-100);
}

.stat {
  font-size: 13px;
  color: var(--color-neutral-500);
  font-weight: 500;
}

.item-actions {
  display: flex;
  gap: 10px;
  margin-top: 12px;
}

.item-actions :deep(.el-button) {
  flex: 1;
  padding: 8px 16px !important;
  font-size: 14px !important;
  border-radius: var(--radius-sm);
  font-weight: 500;
  transition: all var(--transition-base);
}

.item-actions :deep(.el-button--primary) {
  background: var(--gradient-primary);
  border: none;
}

.item-actions :deep(.el-button--primary:hover) {
  background: var(--gradient-primary);
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
}

.item-actions :deep(.el-button--danger) {
  background: linear-gradient(135deg, #ff4d4d 0%, #e63333 100%);
  border: none;
}

.item-actions :deep(.el-button--danger:hover) {
  background: linear-gradient(135deg, #e63333 0%, #ff4d4d 100%);
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
}

.loading-text,
.no-more-text,
.no-products {
  text-align: center;
  padding: 40px 20px;
  color: var(--color-neutral-400);
  font-size: 14px;
}

/* === 底部 === */
.footer {
  background-color: white;
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
  text-decoration: none;
  font-size: 14px;
  transition: color var(--transition-fast);
}

.footer-links a:hover {
  color: var(--color-primary);
}

/* === Responsive === */
@media (max-width: 1024px) {
  .shop-stats {
    gap: 20px;
  }

  .stat-item {
    padding: 16px;
  }

  .stat-value {
    font-size: 24px;
  }

  .shop-list {
    grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
    gap: 16px;
  }
}

@media (max-width: 768px) {
  .nav-content {
    padding: 0 12px;
  }

  .main-content {
    padding: 0 12px;
  }

  .shop-header {
    padding: 24px;
  }

  .shop-info {
    flex-direction: column;
    align-items: stretch;
  }

  .header-actions {
    justify-content: center;
  }

  .shop-stats {
    flex-direction: column;
    gap: 16px;
  }

  .filter-section {
    padding: 20px;
  }

  .status-tabs {
    justify-content: center;
  }

  .filter-group {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-input,
  :deep(.el-select) {
    min-width: auto;
  }

  .shop-list {
    grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
    gap: 12px;
    max-height: none;
    overflow-y: visible;
  }

  .item-image {
    height: 180px;
  }

  .item-info {
    padding: 16px;
  }
}

@media (max-width: 480px) {
  .shop-header {
    padding: 20px 16px;
  }

  .shop-info h1 {
    font-size: 22px;
  }

  .stat-item {
    padding: 12px;
  }

  .stat-value {
    font-size: 20px;
  }

  .status-tabs {
    gap: 8px;
    flex-wrap: wrap;
  }

  .status-tab {
    padding: 6px 16px;
    font-size: 13px;
  }

  .shop-list {
    grid-template-columns: 1fr;
    padding: 8px;
  }

  .item-image {
    height: 160px;
  }

  .item-title {
    font-size: 15px;
  }

  .price-value {
    font-size: 18px;
  }
}

/* 删除确认弹窗 */
.dialog-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.delete-dialog {
  background: white;
  border-radius: 16px;
  padding: 36px 32px 28px;
  width: 360px;
  text-align: center;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.18);
  animation: dialog-pop 0.28s cubic-bezier(0.34, 1.56, 0.64, 1);
}

@keyframes dialog-pop {
  from { opacity: 0; transform: scale(0.85); }
  to { opacity: 1; transform: scale(1); }
}

.dialog-icon {
  margin-bottom: 16px;
}

.dialog-title {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0 0 10px;
}

.dialog-message {
  font-size: 14px;
  color: #666;
  margin: 0 0 28px;
  line-height: 1.6;
}

.dialog-message strong {
  color: #ff6b6b;
  font-weight: 600;
}

.dialog-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.btn-cancel,
.btn-confirm {
  padding: 10px 28px;
  border-radius: 24px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  border: 2px solid transparent;
  transition: all 0.2s ease;
}

.btn-cancel {
  background: #f5f5f5;
  color: #666;
  border-color: #e0e0e0;
}

.btn-cancel:hover {
  background: #ececec;
  border-color: #d0d0d0;
}

.btn-confirm {
  background: linear-gradient(135deg, #ff6b6b, #ee5a5a);
  color: white;
  box-shadow: 0 4px 14px rgba(255, 107, 107, 0.35);
}

.btn-confirm:hover {
  background: linear-gradient(135deg, #ff5252, #ee4444);
  box-shadow: 0 6px 18px rgba(255, 107, 107, 0.45);
  transform: translateY(-1px);
}

/* 弹窗过渡动画 */
.dialog-fade-enter-active,
.dialog-fade-leave-active {
  transition: opacity 0.2s ease;
}

.dialog-fade-enter-from,
.dialog-fade-leave-to {
  opacity: 0;
}
</style>
