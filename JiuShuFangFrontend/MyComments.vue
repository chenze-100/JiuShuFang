<template>
  <!-- 动态背景 -->
  <VitalityBackground />

  <div class="my-comments-container">
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
      <!-- 左侧导航 -->
      <div class="sidebar">
        <div class="sidebar-header">
          <h1>我的评价</h1>
          <span class="back-home-link" @click="goBackHome">返回首页</span>
        </div>
        <div class="sidebar-menu">
          <div class="menu-item" @click="goToOrderList('bought')">我买到的</div>
          <div class="menu-item" @click="goToOrderList('sold')">我卖出的</div>
          <div class="menu-item active">我的评价</div>
          <div class="menu-item">代下单</div>
        </div>
        
        <div class="sidebar-title" style="margin-top: 20px;">关注中心</div>
        <div class="sidebar-menu">
          <div class="menu-item">关注的店铺</div>
          <div class="menu-item">关注的活动</div>
        </div>
      </div>

      <!-- 右侧评价列表 -->
      <div class="comments-content">
        <!-- 评价列表 -->
        <div class="comment-list">
          <div 
            v-for="item in commentList" 
            :key="item.id"
            class="comment-item"
            @click="goToProductDetail(item.goodsId)"
          >
            <!-- 商品信息 -->
            <div class="comment-product">
              <div class="product-image">
                <img 
                  v-if="item.bookImg" 
                  :src="convertToExternalUrl(item.bookImg)" 
                  :alt="item.bookName"
                  class="product-img"
                />
                <div v-else class="product-placeholder">商品图片</div>
              </div>
              <div class="product-info">
                <div class="product-name">{{ item.bookName }}</div>
                <div class="product-author">作者：{{ item.author }}</div>
              </div>
            </div>
            
            <!-- 评价内容 -->
            <div class="comment-detail">
              <!-- 评分 -->
              <div class="comment-rating">
                <div class="rating-stars">
                  <span 
                    v-for="star in 5" 
                    :key="star"
                    class="star"
                    :class="{ filled: star <= item.score }"
                  >
                    ★
                  </span>
                </div>
              </div>
              
              <!-- 评论内容 -->
              <div class="comment-content">
                {{ item.content }}
              </div>
              
              <!-- 图片 -->
              <div v-if="hasValidImages(item.picture)" class="comment-images">
                <div 
                  v-for="(image, index) in getValidImages(item.picture)" 
                  :key="index"
                  class="comment-image"
                >
                  <img 
                    :src="convertToExternalUrl(image)" 
                    class="image-preview"
                  />
                </div>
              </div>
              
              <!-- 时间 -->
              <div class="comment-time">
                {{ formatTime(item.createTime) }}
              </div>
              
              <!-- 操作按钮 -->
              <div class="comment-actions">
                <button class="edit-btn" @click.stop="editComment(item)">修改评价</button>
                <button class="delete-btn" @click.stop="deleteComment(item)">删除评价</button>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 加载状态 -->
        <div v-if="loading" class="loading-text">加载中...</div>
        <div v-if="!loading && !hasMore && commentList.length > 0" class="no-more-text">已经到底了</div>
        <div v-if="!loading && commentList.length === 0" class="no-comments">暂无评价</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import UserMenu from './components/UserMenu.vue'
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

const commentList = ref([])
const loading = ref(false)
const hasMore = ref(true)
const currentPage = ref(1)
const pageSize = ref(10)

// 获取用户信息
const getUserInfo = () => {
  const stored = localStorage.getItem('userInfo')
  if (stored) {
    userInfo.value = JSON.parse(stored)
    const user = JSON.parse(stored)
    nickName.value = user.nickName || user.username || '用户'
    avatarUrl.value = user.avatar || ''
  }
}

// 加载评价列表
const loadComments = async () => {
  if (loading.value || (!hasMore.value && currentPage.value > 1)) {
    return
  }
  
  loading.value = true
  
  try {
    const result = await request(`/comment/user/page?pageNo=${currentPage.value}&pageSize=${pageSize.value}`)
    
    if (result && result.code === 200 && result.data) {
      const newComments = result.data.list || []
      
      if (newComments.length < pageSize.value) {
        hasMore.value = false
      }
      
      commentList.value = [...commentList.value, ...newComments]
      currentPage.value++
    } else {
      ElMessage.error('加载评价失败')
    }
  } catch (error) {
    console.error('加载评价失败:', error)
    ElMessage.error('加载评价失败')
  } finally {
    loading.value = false
  }
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  
  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour
  const month = 30 * day
  const year = 12 * month
  
  if (diff < minute) {
    return '刚刚'
  } else if (diff < hour) {
    return Math.floor(diff / minute) + '分钟前'
  } else if (diff < day) {
    return Math.floor(diff / hour) + '小时前'
  } else if (diff < month) {
    return Math.floor(diff / day) + '天前'
  } else if (diff < year) {
    return Math.floor(diff / month) + '个月前'
  } else {
    return Math.floor(diff / year) + '年前'
  }
}

// 判断是否有有效图片
const hasValidImages = (pictures) => {
  if (!pictures || !Array.isArray(pictures)) return false
  return pictures.some(pic => pic && pic.trim() !== '')
}

// 获取有效图片列表
const getValidImages = (pictures) => {
  if (!pictures || !Array.isArray(pictures)) return []
  return pictures.filter(pic => pic && pic.trim() !== '')
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

const goToOrderList = (tab) => {
  router.push({ path: '/order-list', query: { tab } })
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

const goToProductDetail = (goodsId) => {
  router.push(`/product?id=${goodsId}`)
}

const editComment = (item) => {
  router.push(`/edit-comment?id=${item.id}&goodsId=${item.goodsId}&orderId=${item.orderId || ''}&bookName=${encodeURIComponent(item.bookName)}&author=${encodeURIComponent(item.author)}&bookImg=${item.bookImg}`)
}

const deleteComment = async (item) => {
  try {
    // 使用 Element Plus 的 MessageBox 显示确认对话框
    const { ElMessageBox } = await import('element-plus')
    
    await ElMessageBox.confirm(
      '确定要删除该评价吗？删除后不可恢复，且无法重新评价。',
      '删除评价',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'custom-confirm-btn',
        cancelButtonClass: 'custom-cancel-btn',
        customClass: 'custom-delete-confirm'
      }
    )
    
    if (!item.id) {
      ElMessage.error('评论 ID 不能为空')
      return
    }
    
    const result = await request(`/comment/delete/${item.id}`, {
      method: 'DELETE'
    })
    
    if (result && result.code === 200) {
      ElMessage.success('删除成功')
      // 从列表中移除该评价
      commentList.value = commentList.value.filter(c => c.id !== item.id)
    } else {
      ElMessage.error(result.message || '删除失败')
    }
  } catch (error) {
    if (error === 'cancel') {
      // 用户取消操作，不做任何处理
      return
    }
    console.error('删除评价失败:', error)
    ElMessage.error('删除评价失败')
  }
}

const handleLogout = () => {
  localStorage.removeItem('userInfo')
  router.push('/login')
}

// 滚动加载
const handleScroll = () => {
  const scrollTop = window.pageYOffset || document.documentElement.scrollTop || document.body.scrollTop
  const windowHeight = window.innerHeight
  const documentHeight = document.documentElement.scrollHeight
  
  // 距离底部还有 100px 时加载
  if (scrollTop + windowHeight >= documentHeight - 100) {
    if (hasMore.value && !loading.value) {
      loadComments()
    }
  }
}

onMounted(() => {
  getUserInfo()
  loadComments()
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
.my-comments-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: transparent;
  position: relative;
  z-index: 1;
}

/* 顶部导航栏 */
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

/* 主体内容 */
.main-content {
  max-width: 1200px;
  margin: 30px auto;
  display: flex;
  gap: 20px;
  padding: 0 20px;
  justify-content: center;
}

/* 左侧边栏 */
.sidebar {
  width: 200px;
  background-color: #fff;
  border-radius: 8px;
  padding: 20px 0;
  height: fit-content;
}

.sidebar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px 15px;
  border-bottom: 1px solid #e5e5e5;
}

.sidebar-header h1 {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  margin: 0;
}

.back-home-link {
  font-size: 14px;
  color: #2563eb;
  cursor: pointer;
  transition: color 0.3s;
}

.back-home-link:hover {
  color: #3b82f6;
}

.sidebar-title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  padding: 15px 20px 10px;
  border-bottom: 1px solid #e5e5e5;
}

.sidebar-menu {
  padding: 10px 0;
}

.menu-item {
  padding: 12px 20px;
  color: #666;
  cursor: pointer;
  transition: all 0.3s;
}

.menu-item:hover {
  background-color: #f0f6ff;
  color: #2563eb;
}

.menu-item.active {
  background-color: #f0f6ff;
  color: #2563eb;
  font-weight: bold;
}

/* 评价内容 */
.comments-content {
  width: 940px;
  min-height: 605px;
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  box-sizing: border-box;
}

/* 评价列表 */
.comment-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.comment-item {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s;
  cursor: pointer;
}

.comment-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

.comment-product {
  display: flex;
  gap: 15px;
  padding: 15px;
  background-color: #fafafa;
  border-bottom: 1px solid #e0e0e0;
}

.product-image {
  width: 80px;
  height: 80px;
  flex-shrink: 0;
}

.product-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 4px;
}

.product-placeholder {
  width: 100%;
  height: 100%;
  background-color: #e0e0e0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  font-size: 12px;
  border-radius: 4px;
}

.product-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 8px;
}

.product-name {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.product-author {
  font-size: 12px;
  color: #999;
}

.comment-detail {
  padding: 15px;
}

.comment-rating {
  margin-bottom: 10px;
}

.rating-stars {
  display: flex;
  gap: 5px;
}

.star {
  font-size: 18px;
  color: #e0e0e0;
  transition: all 0.3s;
}

.star.filled {
  color: #3b82f6;
}

.comment-content {
  font-size: 14px;
  color: #333;
  line-height: 1.6;
  margin-bottom: 15px;
}

.comment-images {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
  flex-wrap: wrap;
}

.comment-image {
  width: 100px;
  height: 100px;
  border-radius: 4px;
  overflow: hidden;
}

.image-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
  cursor: pointer;
  transition: transform 0.3s;
}

.image-preview:hover {
  transform: scale(1.05);
}

.comment-time {
  font-size: 12px;
  color: #999;
}

.comment-actions {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #e0e0e0;
  display: flex;
  gap: 10px;
}

.edit-btn {
  padding: 8px 20px;
  background-color: #2563eb;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.edit-btn:hover {
  background-color: #3b82f6;
}

.delete-btn {
  padding: 8px 20px;
  background-color: #f23030;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.delete-btn:hover {
  background-color: #d92626;
}

/* 加载状态 */
.loading-text {
  text-align: center;
  padding: 20px;
  color: #999;
  font-size: 14px;
}

.no-more-text {
  text-align: center;
  padding: 20px;
  color: #999;
  font-size: 14px;
}

.no-comments {
  text-align: center;
  padding: 50px 20px;
  color: #999;
  font-size: 14px;
}
</style>

<!-- 全局样式，用于自定义 MessageBox 按钮 -->
<style>
/* 删除确认对话框整体样式 */
.custom-delete-confirm {
  padding: 0 !important;
  border-radius: 12px !important;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15) !important;
  overflow: hidden;
}

.custom-delete-confirm .el-message-box__header {
  padding: 20px 24px 15px !important;
  border-bottom: 1px solid #f0f0f0;
  background: linear-gradient(135deg, #f0f6ff 0%, #ffffff 100%);
}

.custom-delete-confirm .el-message-box__title {
  font-size: 18px !important;
  font-weight: 600 !important;
  color: #333 !important;
}

.custom-delete-confirm .el-message-box__content {
  padding: 24px !important;
}

.custom-delete-confirm .el-message-box__message {
  font-size: 15px !important;
  color: #666 !important;
  line-height: 1.6 !important;
}

.custom-delete-confirm .el-message-box__status {
  font-size: 24px !important;
  color: #2563eb !important;
}

.custom-delete-confirm .el-message-box__btns {
  padding: 0 24px 20px !important;
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

/* 确认按钮样式 */
.custom-confirm-btn {
  background: linear-gradient(135deg, #2563eb 0%, #3b82f6 100%) !important;
  border: none !important;
  color: #fff !important;
  padding: 10px 28px !important;
  font-size: 15px !important;
  font-weight: 500 !important;
  border-radius: 6px !important;
  box-shadow: 0 3px 8px rgba(37,99,235, 0.3) !important;
  transition: all 0.3s ease !important;
}

.custom-confirm-btn:hover {
  background: linear-gradient(135deg, #3b82f6 0%, #93c5fd 100%) !important;
  box-shadow: 0 4px 12px rgba(37,99,235, 0.4) !important;
  transform: translateY(-1px) !important;
}

.custom-confirm-btn:active {
  transform: translateY(0) !important;
  box-shadow: 0 2px 6px rgba(37,99,235, 0.3) !important;
}

/* 取消按钮样式 */
.custom-cancel-btn {
  background-color: #fff !important;
  border: 1px solid #ddd !important;
  color: #666 !important;
  padding: 10px 28px !important;
  font-size: 15px !important;
  font-weight: 500 !important;
  border-radius: 6px !important;
  transition: all 0.3s ease !important;
}

.custom-cancel-btn:hover {
  border-color: #999 !important;
  color: #333 !important;
  background-color: #f9f9f9 !important;
  transform: translateY(-1px) !important;
}

.custom-cancel-btn:active {
  transform: translateY(0) !important;
}

/* 关闭按钮样式 */
.custom-delete-confirm .el-message-box__headerbtn {
  top: 18px !important;
  right: 18px !important;
}

.custom-delete-confirm .el-message-box__headerbtn .el-message-box__close {
  color: #999 !important;
  font-size: 20px !important;
  transition: all 0.3s ease !important;
}

.custom-delete-confirm .el-message-box__headerbtn .el-message-box__close:hover {
  color: #333 !important;
}
</style>
