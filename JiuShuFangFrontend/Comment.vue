<template>
  <!-- 动态背景 -->
  <VitalityBackground />

  <div class="comment-container">
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
     
      <!-- 右侧评价表单 -->
      <div class="comment-content">
        <!-- 评价标题 -->
        <div class="comment-header">
          <h1>发表评价</h1>
          <span class="back-link" @click="goBack">返回</span>
        </div>

        <!-- 选择商品 -->
        <div v-if="showProductSelect" class="product-select-section">
          <div class="section-title">选择商品</div>
          <div class="product-list">
            <div 
              v-for="(item, index) in selectedOrderItems" 
              :key="index"
              class="product-item"
              :class="{ selected: selectedProductIndex === index }"
              @click="selectProduct(index)"
            >
              <div class="product-image">
                <img 
                  v-if="item.goodsImage" 
                  :src="convertToExternalUrl(item.goodsImage)" 
                  :alt="item.goodsName"
                  class="product-img"
                />
                <div v-else class="product-placeholder">商品图片</div>
              </div>
              <div class="product-info">
                <div class="product-name">{{ item.goodsName }}</div>
                <div class="product-spec" v-if="item.goodsSpec">
                  <span>{{ item.goodsSpec }}</span>
                </div>
                <div class="product-quantity">x{{ item.num }}</div>
              </div>
              <div class="product-check" v-if="selectedProductIndex === index">
                <el-icon class="check-icon"><CircleCheck /></el-icon>
              </div>
            </div>
          </div>
        </div>

        <!-- 评价表单 -->
        <div class="comment-form">
          <!-- 商品信息 -->
          <div class="product-info-section">
            <div class="section-title">商品信息</div>
            <div class="product-info-content">
              <div class="product-image">
                <img 
                  v-if="selectedProduct?.goodsImage" 
                  :src="convertToExternalUrl(selectedProduct?.goodsImage)" 
                  :alt="selectedProduct?.goodsName"
                  class="product-img"
                />
                <div v-else class="product-placeholder">商品图片</div>
              </div>
              <div class="product-details">
                <div class="product-name">{{ selectedProduct?.goodsName }}</div>
                <div class="product-spec" v-if="selectedProduct?.goodsSpec">
                  <span>{{ selectedProduct?.goodsSpec }}</span>
                </div>
                <div class="product-quantity">购买数量: x{{ selectedProduct?.num }}</div>
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

          <!-- 已评价内容展示 -->
          <div v-if="selectedProduct?.existingComment" class="existing-comment-section">
            <div class="existing-comment-header">
              <div class="existing-comment-icon">
                <el-icon class="check-icon"><Select /></el-icon>
              </div>
              <div class="existing-comment-title">已评价</div>
            </div>
            <div class="existing-comment-content">
              <!-- 评分 -->
              <div class="existing-rating">
                <div class="rating-stars">
                  <div 
                    v-for="star in 5" 
                    :key="star"
                    class="star"
                    :class="{ filled: star <= selectedProduct?.existingComment?.score }"
                  >
                    <el-icon class="star-icon"><Star /></el-icon>
                  </div>
                </div>
                <div class="rating-text">
                  {{ getRatingText(selectedProduct?.existingComment?.score) }}
                </div>
              </div>
              
              <!-- 评论内容 -->
              <div class="existing-content">
                {{ selectedProduct?.existingComment?.content }}
              </div>
              
              <!-- 图片 -->
              <div v-if="hasValidImages(selectedProduct?.existingComment?.picture)" class="existing-images">
                <div 
                  v-for="(image, index) in getValidImages(selectedProduct?.existingComment?.picture)" 
                  :key="index"
                  class="existing-image"
                >
                  <img 
                    :src="convertToExternalUrl(image)" 
                    class="existing-image-preview"
                  />
                </div>
              </div>
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

          <!-- 提交按钮 -->
          <div class="submit-section">
            <button class="submit-btn" @click="handleSubmit" :disabled="isSubmitting">
              {{ isSubmitting ? '提交中...' : '提交评价' }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部 -->
    <div class="footer">
      <div class="footer-content">
        <div class="footer-links">
          <span> <a href="#">关于我们</a> </span>
          <span> <a href="#">联系我们</a> </span>
          <span> <a href="#">商家入驻</a> </span>
          <span> <a href="#">友情链接</a> </span>
          <span> <a href="#">帮助中心</a> </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import UserMenu from './components/UserMenu.vue'
import GlitchText from './components/GlitchText.vue'
import VitalityBackground from './components/VitalityBackground.vue'
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { UserFilled, Star, Close, Plus, CircleCheck, CircleCheckFilled, Select } from '@element-plus/icons-vue'
import request from './request'
import { convertToExternalUrl } from './utils/imageUtils'

const route = useRoute()
const router = useRouter()

const userInfo = ref(null)
const nickName = ref('')
const avatarUrl = ref('')

const showProductSelect = ref(false)
const selectedOrderItems = ref([])
const selectedProductIndex = ref(0)
const rating = ref(0)
const content = ref('')
const images = ref([])
const maxImages = 4
const fileInput = ref(null)
const isSubmitting = ref(false)

// 获取用户信息
const getUserInfo = () => {
  const userInfo = localStorage.getItem('userInfo')
  if (userInfo) {
    const user = JSON.parse(userInfo)
    nickName.value = user.nickName || user.username || '用户'
    avatarUrl.value = user.avatar || ''
  }
}

// 获取订单详情
const getOrderDetail = async (orderId) => {
  try {
    const result = await request(`/order/${orderId}`)
    console.log('订单详情完整返回数据:', JSON.stringify(result.data, null, 2))
    
    if (result && result.code === 200 && result.data) {
      // 兼容两种字段名：orderItemVOList 和 orderItemList
      const orderItems = result.data.orderItemVOList || result.data.orderItemList || []
      
      console.log('订单商品列表:', orderItems)
      console.log('订单商品数量:', orderItems.length)
      
      if (orderItems.length > 0) {
        // 打印第一个商品的完整数据结构
        console.log('第一个商品完整数据:', JSON.stringify(orderItems[0], null, 2))
        
        // 从路由参数中获取需要评价的商品 ID
        const goodsIdsParam = route.query.goodsIds
        let goodsIdsToComment = []
        if (goodsIdsParam) {
          goodsIdsToComment = goodsIdsParam.split(',')
        }
        
        console.log('goodsIdsParam:', goodsIdsParam)
        console.log('goodsIdsToComment:', goodsIdsToComment)
        
        // 先获取所有可能需要评价的商品项
        let candidateItems = []
        if (goodsIdsToComment.length > 0) {
          candidateItems = orderItems.filter(item => 
            goodsIdsToComment.includes(String(item.goodsId))
          )
        } else {
          candidateItems = orderItems
        }
        
        console.log('候选商品:', candidateItems)
        console.log('候选商品数量:', candidateItems.length)
        
        // 查询每个商品是否已经被评价，并过滤掉已评价的商品
        const uncommentedItems = []
        for (const item of candidateItems) {
          try {
            const commentResult = await request(`/comment/user/${item.goodsId}/${orderId}`)
            if (commentResult && commentResult.code === 200 && commentResult.data) {
              // 如果有评价内容，说明已经评价过了，不添加到列表中
              console.log(`商品 ${item.goodsName} 已评价，跳过`)
              continue
            }
          } catch (error) {
            console.log(`查询商品 ${item.goodsName} 评价状态失败，假设未评价`)
          }
          // 没有评价或者查询失败，添加到待评价列表
          uncommentedItems.push(item)
        }
        
        selectedOrderItems.value = uncommentedItems
        console.log('需要评价的商品:', selectedOrderItems.value)
        console.log('需要评价的商品数量:', selectedOrderItems.value.length)
        
        if (selectedOrderItems.value.length > 0) {
          console.log('第一个待评价商品完整数据:', JSON.stringify(selectedOrderItems.value[0], null, 2))
        }
        
        // 加载已存在的评价（虽然我们已经过滤了，但还是保留这个逻辑以防万一）
        await loadExistingComments()
        
        if (selectedOrderItems.value.length > 1) {
          showProductSelect.value = true
        } else if (selectedOrderItems.value.length === 1) {
          selectedProductIndex.value = 0
        } else {
          // 没有需要评价的商品
          ElMessage.info('该订单所有商品都已评价')
          setTimeout(() => {
            router.back()
          }, 1500)
        }
      } else {
        console.log('订单商品列表为空')
      }
    } else {
      ElMessage.error('获取订单详情失败')
    }
  } catch (error) {
    console.error('获取订单详情失败:', error)
    ElMessage.error('获取订单详情失败')
  }
}

// 加载已存在的评价
const loadExistingComments = async () => {
  try {
    const orderId = route.query.orderId
    for (const item of selectedOrderItems.value) {
      // 查询用户本人在该商品下的评论
      const result = await request(`/comment/user/${item.goodsId}/${orderId}`)
      
      if (result && result.code === 200 && result.data) {
        // 只有当后端返回了评价内容（包括 picture 非空）时才展示该评论
        if (result.data.picture && result.data.picture.length > 0) {
          // 如果有评价，回显评价内容
          item.existingComment = result.data
          
          // 如果是第一个商品或者是当前选中的商品，更新全局的 rating、content、images
          if (selectedProductIndex.value === selectedOrderItems.value.indexOf(item)) {
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
              images.value = result.data.picture.map(imgUrl => {
                // 如果图片是相对路径，转换为完整 URL
                if (imgUrl && !imgUrl.startsWith('http')) {
                  return convertToExternalUrl(imgUrl)
                }
                return imgUrl
              })
            }
          }
        }
      }
    }
  } catch (error) {
    console.error('加载已存在的评价失败:', error)
    // 即使加载失败也不提示错误，继续显示空的评价表单
  }
}

// 选择商品
const selectProduct = (index) => {
  selectedProductIndex.value = index
  
  // 切换商品时，更新 rating、content、images
  const item = selectedOrderItems.value[index]
  if (item) {
    // 如果有已存在的评价，回显评价内容
    if (item.existingComment && item.existingComment.picture && item.existingComment.picture.length > 0) {
      rating.value = item.existingComment.score || 0
      content.value = item.existingComment.content || ''
      images.value = item.existingComment.picture.map(imgUrl => {
        if (imgUrl && !imgUrl.startsWith('http')) {
          return convertToExternalUrl(imgUrl)
        }
        return imgUrl
      })
    } else {
      // 如果没有已存在的评价，清空评价表单
      rating.value = 0
      content.value = ''
      images.value = []
    }
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

// 获取选中的商品
const selectedProduct = computed(() => {
  const product = selectedOrderItems.value[selectedProductIndex.value]
  if (!product) return null
  
  // 如果有已存在的评价，返回带有已评价内容的商品对象
  if (product.existingComment) {
    return {
      ...product,
      rating: product.rating || product.existingComment.score,
      content: product.content || product.existingComment.content,
      images: product.images && product.images.length > 0 ? product.images : (product.existingComment.picture || []).map(imgUrl => {
        if (imgUrl && !imgUrl.startsWith('http')) {
          return convertToExternalUrl(imgUrl)
        }
        return imgUrl
      })
    }
  }
  
  // 如果没有已存在的评价，返回带有默认属性的商品对象
  return {
    ...product,
    rating: product.rating || 0,
    content: product.content || '',
    images: product.images || []
  }
})

// 获取评分文本
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
  if (files && files.length > 0) {
    const maxFiles = maxImages - (selectedProduct.value?.images?.length || 0)
    const uploadedCount = Math.min(files.length, maxFiles)
    
    for (let i = 0; i < uploadedCount; i++) {
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
            // 添加上传成功的图片 URL 到 images
            images.value = [...images.value, result.data]
            if (images.value.length >= maxFiles) {
              ElMessage.success(`成功上传 ${uploadedCount} 张图片`)
            }
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
    
    // 清空文件选择器，允许重复上传同一张图片
    event.target.value = ''
  }
}

// 删除图片
const removeImage = (index) => {
  images.value.splice(index, 1)
}

// 提交评价
const handleSubmit = async () => {
  // 防止重复提交
  if (isSubmitting.value) {
    return
  }
  
  // 验证
  if (!selectedProduct.value) {
    ElMessage.warning('请选择商品')
    return
  }
  
  if (rating.value === 0) {
    ElMessage.warning('请选择评分')
    return
  }
  
  if (!content.value || content.value.trim().length < 5) {
    ElMessage.warning('评价内容至少需要 5 个字符')
    return
  }
  
  // 构建 DTO
  const commentDto = {
    goodsId: selectedProduct.value.goodsId,
    orderId: route.query.orderId,
    content: content.value.trim(),
    picture: images.value,
    score: rating.value
  }
  
  console.log('提交评价 DTO:', commentDto)
  
  // 设置提交中状态
  isSubmitting.value = true
  
  try {
    const result = await request('/comment', {
      method: 'POST',
      body: JSON.stringify(commentDto)
    })
    
    if (result && result.code === 200) {
      ElMessage.success('评价成功')
      
      
      router.push('/order-list')
    } else {
      ElMessage.error(result.message || '评价失败')
    }
  } catch (error) {
    console.error('评价失败:', error)
    ElMessage.error('评价失败')
  } finally {
    // 重置提交状态
    isSubmitting.value = false
  }
}

// 返回
const goBack = () => {
  router.back()
}

// 返回首页
const goBackHome = () => {
  router.push('/home')
}

// 跳转到订单列表
const goToOrderList = () => {
  router.push('/order-list')
}

const goToShoppingCart = () => {
  router.push('/shopping-cart')
}

const goToProfile = () => {
  router.push('/profile')
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
  localStorage.removeItem('token')
  router.push('/login')
}

onMounted(() => {
  getUserInfo()
  const orderId = route.query.orderId
  if (orderId) {
    getOrderDetail(orderId)
  } else {
    ElMessage.error('订单 ID 不能为空')
  }
})
</script>

<style scoped>
.comment-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: transparent;
  position: relative;
  z-index: 1;
}

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
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.welcome-text {
  font-size: 14px;
  color: #333;
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.nav-item {
  font-size: 14px;
  color: #666;
  cursor: pointer;
}

.nav-item:hover {
  color: #2563eb;
}

.nav-item a {
  color: #666;
  text-decoration: none;
}

.nav-item a:hover {
  color: #2563eb;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.username {
  font-size: 14px;
  color: #333;
}

.user-menu {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.menu-item {
  padding: 8px 15px;
  cursor: pointer;
  font-size: 14px;
  color: #333;
  border-radius: 4px;
}

.menu-item:hover {
  background-color: #f5f5f5;
}

.main-content {
  width: 1200px;
  margin: 20px auto;
  display: flex;
  gap: 20px;
  padding: 0 20px;
}

.sidebar {
  width: 200px;
  flex-shrink: 0;
}

.sidebar-header {
  margin-bottom: 20px;
}

.sidebar-header h1 {
  font-size: 20px;
  color: #333;
  margin-bottom: 10px;
}

.back-home-link {
  font-size: 14px;
  color: #2563eb;
  cursor: pointer;
}

.back-home-link:hover {
  text-decoration: underline;
}

.sidebar-menu {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.sidebar-menu .menu-item {
  padding: 10px 15px;
  background-color: #fff;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  color: #333;
  transition: all 0.3s;
}

.sidebar-menu .menu-item:hover {
  background-color: #2563eb;
  color: #fff;
}

.sidebar-title {
  font-size: 16px;
  color: #666;
  font-weight: bold;
}

.comment-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
  min-width: 1000px;
  width: 100%;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.comment-header h1 {
  font-size: 20px;
  color: #333;
  margin: 0;
}

.back-link {
  font-size: 14px;
  color: #666;
  cursor: pointer;
}

.back-link:hover {
  color: #2563eb;
}

/* 选择商品区域 */
.product-select-section {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.section-title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #e0e0e0;
}

.product-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.product-item {
  display: flex;
  gap: 15px;
  padding: 15px;
  background-color: #fafafa;
  border-radius: 4px;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s;
  border: 2px solid transparent;
}

.product-item:hover {
  background-color: #f5f5f5;
  border-color: #e0e0e0;
}

.product-item.selected {
  background-color: #fff;
  border-color: #2563eb;
}

.product-image {
  width: 80px;
  height: 80px;
  flex-shrink: 0;
  border-radius: 4px;
  overflow: hidden;
  background-color: #f5f5f5;
}

.product-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  font-size: 14px;
}

.product-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 200px;
}

.product-name {
  font-size: 14px;
  color: #333;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.product-spec {
  font-size: 12px;
  color: #999;
}

.product-quantity {
  font-size: 14px;
  color: #999;
}

.product-check {
  min-width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.check-icon {
  color: #2563eb;
  font-size: 24px;
}

/* 评价表单 */
.comment-form {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

/* 商品信息 */
.product-info-section {
  margin-bottom: 20px;
}

.product-info-content {
  display: flex;
  gap: 15px;
  padding: 15px;
  background-color: #fafafa;
  border-radius: 4px;
}

.product-image {
  width: 100px;
  height: 100px;
  flex-shrink: 0;
  border-radius: 4px;
  overflow: hidden;
  background-color: #f5f5f5;
}

.product-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.product-name {
  font-size: 14px;
  color: #333;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.product-spec {
  font-size: 12px;
  color: #999;
}

.product-quantity {
  font-size: 14px;
  color: #999;
}

/* 评分 */
.rating-section {
  margin-bottom: 20px;
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

/* 之前的评价 */
.existing-comment-section {
  margin-bottom: 20px;
  background-color: #fafafa;
  border-radius: 4px;
  padding: 15px;
}

.existing-comment-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.existing-comment-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: #28a745;
  display: flex;
  align-items: center;
  justify-content: center;
}

.check-icon {
  color: white;
  font-size: 24px;
}

.existing-comment-title {
  font-size: 16px;
  font-weight: bold;
  color: #28a745;
}

.existing-comment-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.existing-rating {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.existing-content {
  font-size: 14px;
  color: #333;
  line-height: 1.6;
  padding: 10px;
  background-color: #fff;
  border-radius: 4px;
  border: 1px solid #e0e0e0;
}

.existing-images {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.existing-image {
  width: 80px;
  height: 80px;
  border-radius: 4px;
  overflow: hidden;
}

.existing-image-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 评论内容 */
.content-section {
  margin-bottom: 20px;
}

.textarea-placeholder {
  width: 100%;
  min-height: 150px;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  color: #999;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #fafafa;
}

.textarea {
  width: 100%;
  min-height: 150px;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  line-height: 1.5;
  resize: vertical;
  outline: none;
  transition: border-color 0.3s;
}

.textarea:focus {
  border-color: #2563eb;
}

/* 上传图片 */
.image-section {
  margin-bottom: 20px;
}

.image-upload {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 10px;
}

.uploaded-image {
  position: relative;
  width: 80px;
  height: 80px;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
}

.image-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.delete-icon {
  position: absolute;
  top: 0;
  right: 0;
  width: 20px;
  height: 20px;
  background-color: rgba(0, 0, 0, 0.5);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 0 4px 0 4px;
  cursor: pointer;
  font-size: 14px;
}

.upload-btn {
  width: 80px;
  height: 80px;
  border: 2px dashed #ddd;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
}

.upload-btn:hover {
  border-color: #2563eb;
  background-color: #fafafa;
}

.upload-icon {
  font-size: 24px;
  color: #999;
  margin-bottom: 5px;
}

.upload-text {
  font-size: 12px;
  color: #666;
}

.upload-count {
  position: absolute;
  bottom: 5px;
  right: 5px;
  font-size: 10px;
  color: #999;
}

.file-input {
  display: none;
}

.image-hint {
  font-size: 12px;
  color: #999;
}

/* 提交按钮 */
.submit-section {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #e0e0e0;
}

.submit-btn {
  width: 100%;
  padding: 12px 20px;
  background-color: #2563eb;
  color: #fff;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s;
}

.submit-btn:hover {
  background-color: #1d4ed8;
}

.submit-btn:disabled {
  background-color: #ccc !important;
  cursor: not-allowed;
}

.submit-btn:disabled:hover {
  background-color: #ccc !important;
}

/* 底部 */
.footer {
  background-color: #f5f5f5;
  padding: 30px 0;
  margin-top: auto;
  border-top: 1px solid #e0e0e0;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.footer-links {
  display: flex;
  justify-content: center;
  gap: 30px;
  flex-wrap: wrap;
}

.footer-links a {
  color: #666;
  text-decoration: none;
  font-size: 14px;
}

.footer-links a:hover {
  color: #2563eb;
}
</style>
