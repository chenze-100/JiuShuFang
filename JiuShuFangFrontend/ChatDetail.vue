<template>
  <!-- 动态背景 -->
  <VitalityBackground />

  <div class="chat-detail-container">
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
            <div class="chat-content">
        <!-- 聊天头部 -->
        <div class="chat-header">
          <span class="back-chat-link" @click="goBack">← 返回聊天列表</span>
          <div class="chat-header-info">
            <el-avatar :size="40" :src="chatPartner.avator ? convertToExternalUrl(chatPartner.avator) : ''">
              <el-icon><UserFilled /></el-icon>
            </el-avatar>
            <span class="chat-partner-name">{{ chatPartner.name }}</span>
          </div>
        </div>

        <!-- 商品信息 -->
        <div v-if="chatPartner.productName" class="product-info-bar">
          <div class="product-info-content">
            <div class="product-image-wrapper">
              <img 
                v-if="chatPartner.productImage" 
                :src="convertToExternalUrl(chatPartner.productImage)" 
                :alt="chatPartner.productName"
                class="product-thumb"
              />
              <div v-else class="product-thumb-placeholder">商品图片</div>
            </div>
            <div class="product-details">
              <div class="product-name">{{ chatPartner.productName }}</div>
              <div class="product-price" v-if="chatPartner.productPrice">
                <span class="price-symbol">¥</span>
                <span class="price-value">{{ chatPartner.productPrice }}</span>
              </div>
            </div>
            <button class="go-to-buy-btn" @click="goToProductDetail">查看商品</button>
          </div>
        </div>

        <!-- 消息列表 -->
        <div class="message-list" ref="messageListRef" @scroll="handleScroll">
          <div v-if="hasMore && messageList.length > 0" class="loading-more">加载更多...</div>
          <div v-if="messageList.length === 0 && !isLoading" class="empty-message">
            <div class="empty-message-icon">💬</div>
            <div class="empty-message-text">暂无聊天记录</div>
            <div class="empty-message-hint">发送一条消息开始聊天吧~</div>
          </div>
          <div v-for="(msg, index) in messageList" :key="msg.id || index">
            <!-- 日期分隔线 -->
            <div v-if="shouldShowDateDivider(msg, index)" class="date-divider">
              <span class="date-text">{{ formatDateDivider(msg.createTime) }}</span>
            </div>
            <div :class="['message-item', isMyMessage(msg) ? 'my-message' : 'other-message']">
              <template v-if="isMyMessage(msg)">
                <div class="message-bubble">
                  <div class="message-content">{{ msg.content }}</div>
                  <div class="message-time">{{ formatMessageTime(msg.createTime) }}</div>
                  <div v-if="isLastMyMessage(index)" class="message-status">
                    {{ msg.isRead === 1 ? '已读' : '未读' }}
                  </div>
                </div>
                <div class="message-avatar">
                  <el-avatar :size="36" :src="avatarUrl ? convertToExternalUrl(avatarUrl) : ''">
                    <el-icon><UserFilled /></el-icon>
                  </el-avatar>
                </div>
              </template>
              <template v-else>
                <div class="message-avatar">
                  <el-avatar :size="36" :src="chatPartner.avator ? convertToExternalUrl(chatPartner.avator) : ''">
                    <el-icon><UserFilled /></el-icon>
                  </el-avatar>
                </div>
                <div class="message-bubble">
                  <div class="message-content">{{ msg.content }}</div>
                  <div class="message-time">{{ formatMessageTime(msg.createTime) }}</div>
                </div>
              </template>
            </div>
          </div>
        </div>

        <!-- 输入区域 -->
        <div class="input-area">
          <div class="input-wrapper">
            <textarea
              v-model="inputMessage"
              class="message-input"
              placeholder="请输入消息..."
              @keydown.enter.prevent="sendMessage"
              rows="3"
            />
          </div>
          <div class="send-button-wrapper">
            <button class="send-button" @click="sendMessage" :disabled="!inputMessage.trim() || isSending">
              {{ isSending ? '发送中...' : '发送' }}
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
import { ref, onMounted, onUnmounted, nextTick, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { UserFilled } from '@element-plus/icons-vue'
import request from './request'
import { convertToExternalUrl } from './utils/imageUtils'

const router = useRouter()
const route = useRoute()
const messageListRef = ref(null)

const userInfo = ref(null)
const nickName = ref('')
const avatarUrl = ref('')
const currentUserId = ref(null)

const chatPartner = ref({
  id: null,
  name: '',
  avator: '',
  userId: null,
  productId: null,
  productName: '',
  productImage: '',
  productPrice: ''
})

const messageList = ref([])
const inputMessage = ref('')
const isSending = ref(false)
const ws = ref(null)

const currentPage = ref(1)
const pageSize = 20
const hasMore = ref(true)
const isLoading = ref(false)
const sessionId = ref(null)

const isMyMessage = (msg) => {
  return Number(msg.fromId) === Number(currentUserId.value)
}

const isLastMyMessage = (index) => {
  // 检查当前消息是否是最后一条自己发送的消息
  for (let i = messageList.value.length - 1; i >= 0; i--) {
    if (isMyMessage(messageList.value[i])) {
      return i === index
    }
  }
  return false
}

const formatMessageTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')
  return `${hours}:${minutes}`
}

const shouldShowDateDivider = (msg, index) => {
  if (index === 0) return true
  const prevMsg = messageList.value[index - 1]
  if (!prevMsg || !prevMsg.createTime || !msg.createTime) return false
  const prevDate = new Date(prevMsg.createTime).toDateString()
  const currDate = new Date(msg.createTime).toDateString()
  return prevDate !== currDate
}

const formatDateDivider = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const yesterday = new Date(today.getTime() - 24 * 60 * 60 * 1000)
  const msgDate = new Date(date.getFullYear(), date.getMonth(), date.getDate())
  
  if (msgDate.getTime() === today.getTime()) {
    return '今天'
  } else if (msgDate.getTime() === yesterday.getTime()) {
    return '昨天'
  } else {
    const month = date.getMonth() + 1
    const day = date.getDate()
    return `${month}-${day}`
  }
}

const getUserInfo = () => {
  const userInfoStr = localStorage.getItem('userInfo')
  if (userInfoStr) {
    const user = JSON.parse(userInfoStr)
    userInfo.value = user
    nickName.value = user.nickName || user.username || '用户'
    avatarUrl.value = user.avatar || ''
    currentUserId.value = user.id || user.userId
    return user
  }
  return null
}

// 根据 userId 查询对方用户信息
const getPartnerUserInfo = async (userId) => {
  try {
    const result = await request(`/user/info/${userId}`)
    if (result && result.code === 200 && result.data) {
      chatPartner.value.name = result.data.nickName || result.data.username || '用户'
      chatPartner.value.avator = result.data.avatar || ''
      console.log('获取到对方用户信息:', result.data)
    }
  } catch (error) {
    console.error('查询对方用户信息失败:', error)
  }
}

// 根据 productId 查询商品信息
const getProductInfo = async (productId) => {
  try {
    const result = await request(`/goods/${productId}`)
    if (result && result.code === 200 && result.data) {
      chatPartner.value.productName = result.data.goodsName || result.data.bookName || ''
      chatPartner.value.productImage = result.data.goodsImage || result.data.bookImg || ''
      chatPartner.value.productPrice = result.data.price || ''
      chatPartner.value.productSellerId = result.data.userId || ''
      console.log('获取到商品信息:', result.data)
    }
  } catch (error) {
    console.error('查询商品信息失败:', error)
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messageListRef.value) {
      messageListRef.value.scrollTop = messageListRef.value.scrollHeight
    }
  })
}

const getMessageHistory = async (pageNo = 1) => {
  if (isLoading.value || !sessionId.value) return
  
  isLoading.value = true
  try {
    const params = new URLSearchParams({
      pageNo: pageNo.toString(),
      pageSize: pageSize.toString(),
      sessionId: sessionId.value,
      isAsc: 'false',
      sortBy: 'create_time'
    })
    const result = await request(`/chat/message/page?${params.toString()}`)
    
    if (result && result.code === 200 && result.data) {
      const records = result.data.list || []
      
      // 后端返回的是倒序（最新的在前），需要反转成正序（最新的在后）
      records.reverse()
      
      if (pageNo === 1) {
        messageList.value = records
      } else {
        // 加载更多历史消息时，插入到列表前面
        messageList.value = [...records, ...messageList.value]
      }
      
      hasMore.value = records.length >= pageSize
      currentPage.value = pageNo
      
      if (pageNo === 1) {
        scrollToBottom()
      }
    }
  } catch (error) {
    console.error('获取消息历史失败:', error)
  } finally {
    isLoading.value = false
  }
}

const handleScroll = () => {
  if (!messageListRef.value) return
  
  const { scrollTop } = messageListRef.value
  if (scrollTop < 50 && hasMore.value && !isLoading.value) {
    getMessageHistory(currentPage.value + 1)
  }
}

const connectWebSocket = () => {
  console.log('开始连接 WebSocket...')
  console.log('currentUserId:', currentUserId.value)
  console.log('chatPartner:', chatPartner.value)
  
  if (!currentUserId.value) {
    console.error('currentUserId 为空，无法连接 WebSocket')
    ElMessage.error('用户信息缺失，请重新登录')
    return
  }
  
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const wsUrl = `${protocol}//${window.location.host}/chat/${currentUserId.value}?toUId=${chatPartner.value.userId}&goodsId=${chatPartner.value.productId}`
  
  console.log('WebSocket 连接地址:', wsUrl)
  
  try {
    ws.value = new WebSocket(wsUrl)
    
    ws.value.onopen = () => {
      console.log('WebSocket 连接成功')
      console.log('WebSocket readyState:', ws.value.readyState)
      
      // 如果没有 sessionId，等待一小段时间后尝试加载消息历史
      if (!sessionId.value) {
        setTimeout(() => {
          if (sessionId.value) {
            console.log('WebSocket 创建了新会话，sessionId:', sessionId.value)
            getMessageHistory(1)
          }
        }, 500)
      }
    }
    
    ws.value.onmessage = (event) => {
      console.log('收到消息:', event.data)
      const data = event.data.trim()
      
      // 判断是否是纯数字（sessionId），表示对方已读
      if (/^\d+$/.test(data)) {
        console.log('收到 sessionId（已读回执）:', data)
        // 如果是第一次收到 sessionId，说明对方在页面，加载历史消息
        if (!sessionId.value) {
          sessionId.value = data
          console.log('首次收到 sessionId，加载历史消息')
          getMessageHistory(1)
        } else {
          // 更新自己发送的最后一条消息为已读
          for (let i = messageList.value.length - 1; i >= 0; i--) {
            const msg = messageList.value[i]
            if (msg.fromId === currentUserId.value) {
              msg.isRead = 1
              console.log('更新消息已读状态:', msg.id)
              break
            }
          }
        }
        return
      }
      
      // 收到的是 JSON 消息（对方发送的消息）
      const message = JSON.parse(data)
      
      // 检查是否是自己发送的消息（通过内容匹配，因为没有id）
      const existingIndex = messageList.value.findIndex(
        msg => msg.content === message.content && 
               msg.fromId === message.fromId && 
               msg.toId === message.toId &&
               !msg.id
      )
      
      if (existingIndex !== -1) {
        // 更新已有消息的真实ID和已读状态
        messageList.value[existingIndex] = {
          ...messageList.value[existingIndex],
          id: message.id,
          isRead: message.isRead,
          createTime: message.createTime
        }
        console.log('更新消息ID和已读状态:', message.id)
      } else {
        // 对方发送的新消息，添加到列表
        messageList.value.push(message)
      }
      
      if (message.sessionId && !sessionId.value) {
        sessionId.value = message.sessionId
        console.log('从消息对象中获取 sessionId，加载历史消息')
        getMessageHistory(1)
      }
      
      scrollToBottom()
    }
    
    ws.value.onclose = (event) => {
      console.log('WebSocket 连接关闭')
      console.log('关闭代码:', event.code)
      console.log('关闭原因:', event.reason)
    }
    
    ws.value.onerror = (error) => {
      console.error('WebSocket 错误:', error)
    }
  } catch (error) {
    console.error('创建 WebSocket 连接失败:', error)
    ElMessage.error('连接失败，请刷新页面重试')
  }
}

const sendMessage = async () => {
  console.log('准备发送消息...')
  console.log('inputMessage:', inputMessage.value)
  console.log('ws.value:', ws.value)
  console.log('ws.readyState:', ws.value ? ws.value.readyState : '无连接')
  
  if (!inputMessage.value.trim() || isSending.value) {
    return
  }
  
  if (!ws.value) {
    ElMessage.error('未建立连接，请刷新页面重试')
    return
  }
  
  if (ws.value.readyState === WebSocket.CONNECTING) {
    ElMessage.warning('连接正在建立中，请稍候...')
    let waitCount = 0
    while (ws.value.readyState === WebSocket.CONNECTING && waitCount < 50) {
      await new Promise(resolve => setTimeout(resolve, 100))
      waitCount++
    }
  }
  
  if (ws.value.readyState !== WebSocket.OPEN) {
    console.error('WebSocket 未连接，readyState:', ws.value.readyState)
    ElMessage.error('连接已断开，请刷新页面重试')
    return
  }
  
  isSending.value = true
  
  const message = {
    fromId: currentUserId.value,
    toId: chatPartner.value.userId,
    content: inputMessage.value.trim(),
    msgType: 1,
    isRead: 0
  }
  
  console.log('准备发送的消息:', message)
  
  try {
    ws.value.send(JSON.stringify(message))
    console.log('消息发送成功')
    
    // 立即将消息添加到列表中显示（临时状态）
    const tempMessage = {
      fromId: currentUserId.value,
      toId: chatPartner.value.userId,
      content: inputMessage.value.trim(),
      msgType: 1,
      isRead: 0,
      createTime: new Date().toISOString()
      // 不设置id，等待后端返回真实id
    }
    messageList.value.push(tempMessage)
    scrollToBottom()
    
    inputMessage.value = ''
  } catch (error) {
    console.error('发送消息失败:', error)
    ElMessage.error('发送失败，请重试')
  } finally {
    isSending.value = false
  }
}

const goToHome = () => {
  router.push('/home')
}

const goBack = () => {
  router.push('/chat-list')
}

const goToProductDetail = () => {
  if (chatPartner.value.productId) {
    router.push({ path: '/product', query: { id: chatPartner.value.productId } })
  } else {
    ElMessage.warning('商品信息不存在')
  }
}

const goToProfile = () => {
  router.push('/profile')
}

const goToOrderList = () => {
  router.push('/order-list')
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

const handleLogout = async () => {
  try {
    await request('/user/logout', {
      method: 'POST'
    })
  } catch (error) {
    console.error('退出登录请求失败:', error)
  } finally {
    if (ws.value) {
      ws.value.close()
    }
    ElMessage.success('已退出登录')
    localStorage.removeItem('userInfo')
    localStorage.removeItem('token')
    window.location.href = '/'
  }
}

onMounted(() => {
  getUserInfo()
  
  const chatData = route.query
  chatPartner.value = {
    id: chatData.id,
    name: chatData.name || '',
    avator: chatData.avator || '',
    userId: chatData.userId,
    productId: chatData.productId,
    productName: chatData.productName || '',
    productImage: chatData.productImage || '',
    productPrice: chatData.productPrice || ''
  }
  
  console.log('聊天页面初始化，chatData:', chatData)
  console.log('chatPartner:', chatPartner.value)
  
  if (!chatPartner.value.userId || !chatPartner.value.productId) {
    ElMessage.error('参数错误，返回聊天列表')
    router.push('/chat-list')
    return
  }
  
  // 如果没有头像和昵称，根据 userId 查询对方用户信息
  if (!chatPartner.value.name || !chatPartner.value.avator) {
    getPartnerUserInfo(chatPartner.value.userId)
  }
  
  // 如果没有商品信息，根据 productId 查询商品信息
  if (!chatPartner.value.productName || !chatPartner.value.productImage) {
    getProductInfo(chatPartner.value.productId)
  }
  
  // 如果有 sessionId，直接加载消息历史
  if (chatData.sessionId) {
    sessionId.value = chatData.sessionId
    getMessageHistory(1)
  }
  // 如果没有 sessionId，WebSocket 连接成功后会自动创建会话
  
  connectWebSocket()
})

onUnmounted(() => {
  if (ws.value) {
    ws.value.close()
  }
})
</script>

<style scoped>
.chat-detail-container {
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
  width: 1200px;
  margin: 0 auto;
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
  background-color: var(--color-bg-tertiary);
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
  background-color: var(--color-bg-tertiary);
  color: var(--color-error);
}

.main-content {
  width: 1200px;
  margin: 20px auto;
  display: flex;
  flex: 1;
}

.chat-content {
  flex: 1;
  background-color: #fff;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  height: calc(100vh - 200px);
}

.chat-header {
  padding: 15px 20px;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.back-chat-link {
  font-size: 14px;
  color: var(--color-primary);
  cursor: pointer;
  transition: color 0.3s;
}

.back-chat-link:hover {
  color: var(--color-primary-light);
}

.chat-header-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.chat-partner-name {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

/* 商品信息栏 */
.product-info-bar {
  padding: 12px 20px;
  background-color: #f8f8f8;
  border-bottom: 1px solid #eee;
}

.product-info-content {
  display: flex;
  align-items: center;
  gap: 12px;
}

.product-image-wrapper {
  width: 50px;
  height: 50px;
  flex-shrink: 0;
  border-radius: 4px;
  overflow: hidden;
  background-color: #fff;
}

.product-thumb {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-thumb-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  color: #999;
  background-color: #f0f0f0;
}

.product-details {
  flex: 1;
  min-width: 0;
}

.product-name {
  font-size: 14px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 4px;
}

.product-price {
  color: var(--color-primary);
  font-size: 16px;
  font-weight: 500;
}

.price-symbol {
  font-size: 12px;
}

.price-value {
  font-size: 16px;
}

.go-to-buy-btn {
  padding: 8px 20px;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-dark) 100%);
  color: white;
  border: none;
  border-radius: var(--radius-md);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition-base);
  white-space: nowrap;
  flex-shrink: 0;
}

.go-to-buy-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(37,99,235, 0.3);
}

.go-to-buy-btn:active {
  transform: translateY(0);
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message-item {
  display: flex;
  gap: 10px;
  align-items: flex-start;
}

.my-message {
  justify-content: flex-end;
}

.other-message {
  justify-content: flex-start;
}

.message-avatar {
  flex-shrink: 0;
}

.message-bubble {
  max-width: 60%;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.my-message .message-bubble {
  align-items: flex-end;
}

.message-content {
  padding: 10px 14px;
  border-radius: 8px;
  word-break: break-word;
  line-height: 1.5;
}

.other-message .message-content {
  background-color: var(--color-bg-tertiary);
  color: #333;
}

.my-message .message-content {
  background-color: var(--color-primary);
  color: #fff;
}

.message-time {
  font-size: 12px;
  color: #999;
}

.message-status {
  font-size: 11px;
  color: #999;
  margin-top: 2px;
}

.loading-more {
  text-align: center;
  padding: 10px;
  color: #999;
  font-size: 14px;
}

/* 日期分隔线 */
.date-divider {
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 16px 0;
}

.date-text {
  font-size: 12px;
  color: #999;
  padding: 4px 12px;
  background-color: #f0f0f0;
  border-radius: 12px;
}

/* 空消息状态 */
.empty-message {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #999;
}

.empty-message-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.empty-message-text {
  font-size: 16px;
  margin-bottom: 8px;
  color: #666;
}

.empty-message-hint {
  font-size: 14px;
  color: #999;
}

.input-area {
  padding: 15px 20px;
  border-top: 1px solid #eee;
  display: flex;
  gap: 15px;
}

.input-wrapper {
  flex: 1;
}

.message-input {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 14px;
  resize: none;
  outline: none;
  transition: border-color 0.3s;
}

.message-input:focus {
  border-color: var(--color-primary);
}

.send-button-wrapper {
  display: flex;
  align-items: flex-end;
}

.send-button {
  padding: 10px 30px;
  background-color: var(--color-primary);
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.send-button:hover:not(:disabled) {
  background-color: #1e40af;
}

.send-button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.footer {
  background-color: var(--color-bg-tertiary);
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
  gap: 20px;
  margin-bottom: 20px;
}

.footer-links span {
  color: #666;
  font-size: 12px;
}

.footer-links span a {
  color: #666;
  text-decoration: none;
}

.footer-links span a:hover {
  color: var(--color-error);
}
</style>
