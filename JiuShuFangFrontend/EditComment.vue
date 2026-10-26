<template>
  <!-- 动态背景 -->
  <VitalityBackground />

  <div class="edit-comment-container">
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
            @click="goBack"
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
     
      <!-- 右侧评价表单 -->
      <div class="edit-content">
        <!-- 评价表单 -->
        <div class="comment-form">
          <!-- 商品信息 -->
          <div class="product-info-section">
            <div class="section-title">商品信息</div>
            <div class="product-info-content">
              <div class="product-image">
                <img 
                  v-if="productInfo.bookImg" 
                  :src="convertToExternalUrl(productInfo.bookImg)" 
                  :alt="productInfo.bookName"
                  class="product-img"
                />
                <div v-else class="product-placeholder">商品图片</div>
              </div>
              <div class="product-detail">
                <div class="product-name">{{ productInfo.bookName }}</div>
                <div class="product-author">作者：{{ productInfo.author }}</div>
              </div>
            </div>
          </div>

          <!-- 评分 -->
          <div class="rating-section">
            <div class="section-title">评分</div>
            <div class="rating-stars">
              <div 
                v-for="star in 5" 
                :key="star"
                class="star"
                :class="{ filled: star <= rating }"
                @click="rating = star"
              >
                <el-icon class="star-icon"><Star /></el-icon>
              </div>
            </div>
            <div class="rating-text" v-if="rating > 0">
              {{ getRatingText(rating) }}
            </div>
          </div>

          <!-- 评论内容 -->
          <div class="content-section">
            <div class="section-title">评价内容</div>
            <div class="content-input">
              <textarea 
                v-model="content" 
                placeholder="请输入您的评价内容（不少于 5 个字符）" 
                class="textarea"
                maxlength="500"
                show-word-limit
              ></textarea>
            </div>
          </div>

          <!-- 上传图片 -->
          <div class="image-section">
            <div class="section-title">上传图片</div>
            <div class="image-upload">
              <div 
                v-for="(image, index) in images" 
                :key="index"
                class="uploaded-image"
              >
                <img :src="image" class="image-preview" />
                <el-icon 
                  class="delete-icon" 
                  @click="removeImage(index)"
                >
                  <Close />
                </el-icon>
              </div>
              <div 
                v-if="images.length < 4"
                class="upload-btn"
                @click="handleImageUpload"
              >
                <el-icon class="upload-icon"><Plus /></el-icon>
                <span class="upload-text">添加图片</span>
                <span class="upload-count">{{ images.length }}/{{ maxImages }}</span>
              </div>
            </div>
            <div class="image-hint">
              *最多上传 4 张图片，支持 jpg、jpeg、png 格式
            </div>
            <input 
              ref="fileInput" 
              type="file" 
              class="file-input" 
              accept="image/*" 
              multiple
              @change="handleFileChange"
            />
          </div>

          <!-- 操作按钮 -->
          <div class="comment-actions">
            <button class="submit-btn" @click="handleSubmit" :disabled="isSubmitting">
              {{ isSubmitting ? '保存中...' : '保存修改' }}
            </button>
            <button class="cancel-btn" @click="goBack">取消</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import UserMenu from './components/UserMenu.vue'
import GlitchText from './components/GlitchText.vue'
import VitalityBackground from './components/VitalityBackground.vue'
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { UserFilled, Star, Close, Plus } from '@element-plus/icons-vue'
import request from './request'
import { convertToExternalUrl } from './utils/imageUtils'

const route = useRoute()
const router = useRouter()

const userInfo = ref(null)
const nickName = ref('')
const avatarUrl = ref('')

const productInfo = ref({
  bookName: '',
  author: '',
  bookImg: ''
})

const rating = ref(0)
const content = ref('')
const images = ref([])
const maxImages = 4
const fileInput = ref(null)
const commentId = ref('')
const isSubmitting = ref(false)

// 获取用户信息
const getUserInfo = () => {
  const stored = localStorage.getItem('userInfo')
  if (stored) {
    const user = JSON.parse(stored)
    userInfo.value = user
    nickName.value = user.nickName || user.username || '用户'
    avatarUrl.value = user.avatar || ''
  }
}

// 从路由参数获取商品信息
const loadProductInfo = () => {
  const bookName = route.query.bookName
  const author = route.query.author
  const bookImg = route.query.bookImg
  
  if (bookName) {
    productInfo.value.bookName = decodeURIComponent(bookName)
  }
  if (author) {
    productInfo.value.author = decodeURIComponent(author)
  }
  if (bookImg) {
    productInfo.value.bookImg = bookImg
  }
}

// 加载已存在的评价数据
const loadExistingComment = async () => {
  try {
    const goodsId = route.query.goodsId
    const orderId = route.query.orderId
    commentId.value = route.query.id
    if (!goodsId) {
      ElMessage.error('商品 ID 不能为空')
      return
    }
    
    // 使用之前的接口查询用户的评价
    const url = orderId ? `/comment/user/${goodsId}/${orderId}` : `/comment/user/${goodsId}`
    const result = await request(url)
    
    if (result && result.code === 200 && result.data) {
      // 回显评分
      if (result.data.score) {
        rating.value = result.data.score
      }
      
      // 回显评论内容
      if (result.data.content) {
        content.value = result.data.content
      }
      
      // 回显图片
      if (result.data.picture && result.data.picture.length > 0) {
        images.value = result.data.picture
          .filter(imgUrl => imgUrl && imgUrl.trim() !== '')
          .map(imgUrl => {
            if (imgUrl && !imgUrl.startsWith('http')) {
              return convertToExternalUrl(imgUrl)
            }
            return imgUrl
          })
      }
    } else {
      ElMessage.error('加载评价数据失败')
    }
  } catch (error) {
    console.error('加载评价数据失败:', error)
    ElMessage.error('加载评价数据失败')
  }
}

// 获取评分文字
const getRatingText = (rating) => {
  const textMap = {
    1: '非常差',
    2: '差',
    3: '一般',
    4: '好',
    5: '非常好'
  }
  return textMap[rating] || ''
}

// 处理图片上传
const handleImageUpload = () => {
  fileInput.value.click()
}

// 处理文件选择
const handleFileChange = async (event) => {
  const files = event.target.files
  if (files.length === 0) return
  
  const maxFiles = Math.min(files.length, maxImages - images.value.length)
  if (maxFiles === 0) {
    ElMessage.warning(`最多只能上传${maxImages}张图片`)
    return
  }
  
  let uploadedCount = 0
  
  for (let i = 0; i < maxFiles; i++) {
    const file = files[i]
    if (file.type.startsWith('image/')) {
      try {
        // 上传图片到后端
        const formData = new FormData()
        formData.append('file', file)
        
        const userInfo = localStorage.getItem('userInfo')
        let token = ''
        if (userInfo) {
          const user = JSON.parse(userInfo)
          token = user.token || ''
        }
        
        const response = await fetch('/api/image/picture/upload', {
          method: 'POST',
          headers: {
            'token': token
          },
          body: formData
        })
        
        if (!response.ok) {
          throw new Error('图片上传失败')
        }
        
        const result = await response.json()
        
        if (result && result.code === 200 && result.data) {
          images.value = [...images.value, result.data]
          uploadedCount++
        } else {
          ElMessage.error(result.message || '图片上传失败')
        }
      } catch (error) {
        console.error('图片上传失败:', error)
        ElMessage.error('图片上传失败')
      }
    }
  }
  
  if (uploadedCount === 0) {
    ElMessage.warning('只能上传图片文件')
  }
  
  event.target.value = ''
}

// 删除图片
const removeImage = (index) => {
  images.value.splice(index, 1)
}

// 提交修改
const handleSubmit = async () => {
  if (rating.value === 0) {
    ElMessage.warning('请选择评分')
    return
  }
  
  if (!content.value || content.value.trim().length < 5) {
    ElMessage.warning('评价内容至少需要 5 个字符')
    return
  }
  
  const goodsId = route.query.goodsId
  if (!goodsId) {
    ElMessage.error('商品 ID 不能为空')
    return
  }
  
  // 构建 DTO
  const commentDto = {
    goodsId: goodsId,
    content: content.value.trim(),
    picture: images.value,
    score: rating.value
  }
  
  console.log('修改评价 DTO:', commentDto)
  
  // 设置提交中状态
  isSubmitting.value = true
  
  try {
    const result = await request('/comment/set', {
      method: 'PUT',
      body: JSON.stringify(commentDto)
    })
    
    console.log('修改评价响应:', result)
    
    if (result && result.code === 200) {
      ElMessage.success('修改成功')
      router.push('/my-comments')
    } else {
      ElMessage.error(result.message || '修改失败')
    }
  } catch (error) {
    console.error('修改评价失败:', error)
    ElMessage.error('修改评价失败')
  } finally {
    // 重置提交状态
    isSubmitting.value = false
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

const goToMyComments = () => {
  router.push('/my-comments')
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

const handleLogout = () => {
  localStorage.removeItem('userInfo')
  router.push('/login')
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  getUserInfo()
  loadProductInfo()
  loadExistingComment()
})
</script>

<style scoped>
.edit-comment-container {
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
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  box-shadow: 0 1px 0 rgba(37,99,235, 0.06);
}

.nav-content {
  height: 60px;
  width: 100%;
  max-width: 1280px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 32px;
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
  margin: 20px auto;
  display: flex;
  gap: 20px;
  padding: 0 20px;
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
.edit-content {
  flex: 1;
  width: 1200px;
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  min-height: 500px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

/* 评价表单 */
.comment-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 商品信息 */
.product-info-section {
  padding: 15px;
  background-color: #fafafa;
  border-radius: 4px;
}

.section-title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin-bottom: 15px;
}

.product-info-content {
  display: flex;
  gap: 15px;
}

.product-image {
  width: 100px;
  height: 100px;
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

.product-detail {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 10px;
}

.product-name {
  font-size: 16px;
  color: #333;
  font-weight: 500;
}

.product-author {
  font-size: 14px;
  color: #999;
}

/* 评分 */
.rating-section {
  padding: 15px 0;
}

.rating-stars {
  display: flex;
  gap: 10px;
  margin-bottom: 10px;
}

.star {
  cursor: pointer;
  transition: all 0.3s;
}

.star-icon {
  font-size: 32px;
  color: #e0e0e0;
  transition: all 0.3s;
}

.star:hover .star-icon {
  transform: scale(1.1);
}

.star.filled .star-icon {
  color: #3b82f6;
}

.rating-text {
  font-size: 14px;
  color: #666;
}

/* 评论内容 */
.content-section {
  padding: 15px 0;
}

.textarea {
  width: 100%;
  min-height: 120px;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  resize: vertical;
  font-family: inherit;
}

.textarea:focus {
  outline: none;
  border-color: #2563eb;
}

/* 上传图片 */
.image-section {
  padding: 15px 0;
}

.image-upload {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 10px;
}

.uploaded-image {
  position: relative;
  width: 100px;
  height: 100px;
  border-radius: 4px;
  overflow: hidden;
}

.image-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.delete-icon {
  position: absolute;
  top: 2px;
  right: 2px;
  width: 20px;
  height: 20px;
  background-color: rgba(0, 0, 0, 0.5);
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
}

.delete-icon:hover {
  background-color: rgba(0, 0, 0, 0.8);
}

.upload-btn {
  width: 100px;
  height: 100px;
  border: 1px dashed #ddd;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 5px;
  cursor: pointer;
  transition: all 0.3s;
  background-color: #fafafa;
}

.upload-btn:hover {
  border-color: #2563eb;
  background-color: #f0f6ff;
}

.upload-icon {
  font-size: 24px;
  color: #999;
}

.upload-btn:hover .upload-icon {
  color: #2563eb;
}

.upload-text {
  font-size: 12px;
  color: #999;
}

.upload-count {
  font-size: 10px;
  color: #ccc;
}

.image-hint {
  font-size: 12px;
  color: #999;
}

.file-input {
  display: none;
}

/* 提交按钮 */
.comment-actions {
  display: flex;
  gap: 15px;
  padding: 20px 0;
  justify-content: center;
}

.submit-btn {
  padding: 12px 40px;
  background-color: #2563eb;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  transition: all 0.3s;
}

.submit-btn:hover {
  background-color: #3b82f6;
}

.cancel-btn {
  padding: 12px 40px;
  background-color: #fff;
  color: #666;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  transition: all 0.3s;
}

.cancel-btn:hover {
  border-color: #999;
  color: #333;
}

.submit-btn:disabled {
  background-color: #ccc !important;
  cursor: not-allowed;
}

.submit-btn:disabled:hover {
  background-color: #ccc !important;
}
</style>
