<template>
  <!-- 动态背景 -->
  <VitalityBackground />

  <div class="chat-list-container">
    <!-- 顶部导航栏 -->
    <CommonHeader />

    <!-- 页面主体 -->
    <div class="main-content">
      <!-- 右侧聊天列表 -->
      <div class="chat-content">
        <div class="chat-header">
          <h2>聊天</h2>
          <span class="back-home-link" @click="goBackHome">← 返回首页</span>
        </div>

        <!-- 聊天列表 -->
        <div class="chat-list">
          <div v-for="group in groupedChatList" :key="group.label" class="chat-group">
            <GroupHeader :label="group.label" />
            <div v-for="chat in group.chats" :key="chat.id" class="chat-item" @click="goToChatDetail(chat)">
              <div class="chat-avatar">
                <el-avatar :size="50" :src="chat.avator ? convertToExternalUrl(chat.avator) : ''">
                  <el-icon><UserFilled /></el-icon>
                </el-avatar>
              </div>
              <div class="chat-info">
                <div class="chat-name-row">
                  <span class="chat-name">{{ chat.name }}</span>
                  <span class="chat-time">{{ formatTime(chat.lastTime) }}</span>
                </div>
                <div class="chat-preview-row">
                  <div class="chat-preview">{{ chat.lastMsg }}</div>
                  <div v-if="chat.notReadNum > 0" class="unread-badge">{{ chat.notReadNum > 99 ? '99+' : chat.notReadNum }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <LoadingSpinner v-if="chatLoading" />
        <EmptyState v-else-if="chatList.length === 0" icon="💬" text="暂无聊天记录" />
      </div>
    </div>

    <!-- 底部 -->
    <CommonFooter />
  </div>
</template>

<script setup>
import CommonHeader from './components/CommonHeader.vue'
import GroupHeader from './components/GroupHeader.vue'
import EmptyState from './components/EmptyState.vue'
import LoadingSpinner from './components/LoadingSpinner.vue'
import CommonFooter from './components/CommonFooter.vue'
import VitalityBackground from './components/VitalityBackground.vue'
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElNotification, ElMessage } from 'element-plus'
import { UserFilled } from '@element-plus/icons-vue'
import request from './request'
import { convertToExternalUrl } from './utils/imageUtils'

const router = useRouter()

const userInfo = ref(null)
const nickName = ref('')
const avatarUrl = ref('')
const currentUserId = ref(null)

const chatList = ref([])
const chatLoading = ref(false)

// 按天数分组的聊天列表
const groupedChatList = computed(() => {
  const now = new Date()
  const todayStart = new Date(now.getFullYear(), now.getMonth(), now.getDate()).getTime()

  const groups = {
    today: { label: '今天', chats: [] },
    yesterday: { label: '昨天', chats: [] },
    twoDaysAgo: { label: '2天前', chats: [] },
    threeDaysAgo: { label: '3天前', chats: [] },
    week: { label: '7天内', chats: [] },
    month: { label: '30天内', chats: [] },
    older: { label: '30天前', chats: [] }
  }

  chatList.value.forEach(chat => {
    if (!chat.lastTime) {
      groups.older.chats.push(chat)
      return
    }

    const chatDate = new Date(chat.lastTime)
    const chatDateStart = new Date(chatDate.getFullYear(), chatDate.getMonth(), chatDate.getDate()).getTime()
    const diffDays = Math.floor((todayStart - chatDateStart) / (24 * 60 * 60 * 1000))

    if (diffDays === 0) {
      groups.today.chats.push(chat)
    } else if (diffDays === 1) {
      groups.yesterday.chats.push(chat)
    } else if (diffDays === 2) {
      groups.twoDaysAgo.chats.push(chat)
    } else if (diffDays === 3) {
      groups.threeDaysAgo.chats.push(chat)
    } else if (diffDays <= 7) {
      groups.week.chats.push(chat)
    } else if (diffDays <= 30) {
      groups.month.chats.push(chat)
    } else {
      groups.older.chats.push(chat)
    }
  })

  return Object.values(groups).filter(g => g.chats.length > 0)
})

// WebSocket连接
let chatListWs = null

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

const connectChatListWebSocket = () => {
  if (!currentUserId.value) {
    console.error('用户ID为空，无法连接聊天列表WebSocket')
    return
  }
  
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  // 类型3：聊天列表连接，toUId=0, goodsId=0, type=3
  const wsUrl = `${protocol}//${window.location.host}/chat/${currentUserId.value}?toUId=0&goodsId=0&type=3`
  
  console.log('连接聊天列表WebSocket:', wsUrl)
  
  try {
    chatListWs = new WebSocket(wsUrl)
    
    chatListWs.onopen = () => {
      console.log('聊天列表WebSocket连接成功')
    }
    
    chatListWs.onmessage = (event) => {
      console.log('收到新消息通知:', event.data)
      try {
        const message = JSON.parse(event.data)
        handleNewMessage(message)
      } catch (e) {
        console.error('解析消息失败:', e)
      }
    }
    
    chatListWs.onclose = () => {
      console.log('聊天列表WebSocket连接关闭')
    }
    
    chatListWs.onerror = (error) => {
      console.error('聊天列表WebSocket错误:', error)
    }
  } catch (error) {
    console.error('创建聊天列表WebSocket连接失败:', error)
  }
}

const handleNewMessage = (message) => {
  console.log('处理新消息:', message)
  
  // 显示通知
  const senderName = message.fromNickName || message.fromUserName || '好友'
  const previewContent = message.content ? message.content.substring(0, 30) + (message.content.length > 30 ? '...' : '') : '新消息'
  
  ElNotification({
    title: `${senderName} 发来新消息`,
    message: previewContent,
    type: 'info',
    duration: 3000,
    position: 'top-right',
    onClick: () => {
      // 点击通知跳转到聊天详情
      if (message.sessionId) {
        // 优先从 chatList 中查找，如果找不到则使用 message 中的数据
        const chat = chatList.value.find(c => c.id == message.sessionId)
        if (chat) {
          router.push({
            path: '/chat-detail',
            query: {
              id: chat.id,
              name: chat.name,
              avator: chat.avator,
              userId: chat.userId,
              productId: chat.productId,
              sessionId: chat.id,
              productName: chat.productName || '',
              productImage: chat.productImage || '',
              productPrice: chat.price || ''
            }
          })
        } else {
          // 使用 message 中的数据构造路由参数
          const productId = message.goodsId || message.productId
          if (!productId) {
            // 如果没有商品 ID，跳转到聊天列表
            console.warn('消息中没有商品 ID，跳转到聊天列表')
            router.push('/chat-list')
            return
          }
          router.push({
            path: '/chat-detail',
            query: {
              id: message.sessionId,
              name: senderName,
              avator: message.fromAvator || '',
              userId: message.fromId,
              productId: productId,
              sessionId: message.sessionId,
              productName: message.productName || '',
              productImage: message.productImage || '',
              productPrice: message.productPrice || ''
            }
          })
        }
      }
    }
  })
  
  // 查找对应的聊天会话
  const chatIndex = chatList.value.findIndex(chat => chat.id == message.sessionId)
  
  if (chatIndex !== -1) {
    // 更新现有会话
    const chat = chatList.value[chatIndex]
    chat.lastMsg = message.content
    chat.lastTime = message.createTime
    chat.notReadNum = (chat.notReadNum || 0) + 1
    
    // 将该会话移到列表顶部
    chatList.value.splice(chatIndex, 1)
    chatList.value.unshift(chat)
  } else {
    // 新会话，重新加载列表
    console.log('新会话，重新加载聊天列表')
    getAllChatSessions()
  }
}

const disconnectChatListWebSocket = () => {
  if (chatListWs) {
    chatListWs.close()
    chatListWs = null
  }
}

const getAllChatSessions = async () => {
  try {
    const params = new URLSearchParams({
      pageNo: '1',
      pageSize: '1000',
      isAsc: 'false',
      sortBy: 'last_time'
    })
    const result = await request(`/chat/session/page?${params.toString()}`)
    
    if (result && result.code === 200 && result.data && result.data.list) {
      chatList.value = result.data.list.map(chat => ({
        ...chat,
        productName: chat.productName || '',
        productImage: chat.productImage || '',
        price: chat.price || ''
      }))
    }
  } catch (error) {
    console.error('获取聊天列表失败:', error)
  } finally {
    chatLoading.value = false
  }
}

const formatTime = (time) => {
  if (!time) return ''
  
  const now = new Date()
  const date = new Date(time)
  const diff = now - date
  
  const oneDay = 24 * 60 * 60 * 1000
  const oneWeek = 7 * oneDay
  
  if (diff < oneDay) {
    const hours = date.getHours().toString().padStart(2, '0')
    const minutes = date.getMinutes().toString().padStart(2, '0')
    return `${hours}:${minutes}`
  } else if (diff < oneWeek) {
    const weekdays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
    return weekdays[date.getDay()]
  } else {
    const month = (date.getMonth() + 1).toString().padStart(2, '0')
    const day = date.getDate().toString().padStart(2, '0')
    return `${month}/${day}`
  }
}

const goToHome = () => {
  router.push('/home')
}

const goBackHome = () => {
  router.push('/home')
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

const goToChatDetail = (chat) => {
  router.push({
    path: '/chat-detail',
    query: {
      id: chat.id,
      name: chat.name,
      avator: chat.avator,
      userId: chat.userId,
      productId: chat.productId,
      sessionId: chat.id,
      productName: chat.productName || '',
      productImage: chat.productImage || '',
      productPrice: chat.price || ''
    }
  })
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

onMounted(() => {
  getUserInfo()
  chatLoading.value = true
  getAllChatSessions()
  connectChatListWebSocket()
})

onUnmounted(() => {
  disconnectChatListWebSocket()
})
</script>

<style scoped>
.chat-list-container {
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
  color: var(--color-neutral-500);
  font-size: 14px;
  transition: background-color 0.3s;
}

.menu-item:hover {
  background-color: #f5f5f5;
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
  padding: 20px;
}

.chat-header {
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chat-header h2 {
  font-size: 20px;
  color: #333;
  font-weight: bold;
}

.back-home-link {
  font-size: 14px;
  color: var(--color-primary);
  cursor: pointer;
}

.back-home-link:hover {
  color: var(--color-primary-light);
}

.chat-list {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.chat-group {
  margin-bottom: 10px;
}

.chat-group-header {
  font-size: 13px;
  color: #999;
  padding: 10px 15px;
  background-color: #fafafa;
  font-weight: 500;
}

.chat-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  cursor: pointer;
  border-bottom: 1px solid #f5f5f5;
  transition: background-color 0.3s;
}

.chat-item:hover {
  background-color: #f9f9f9;
}

.chat-avatar {
  position: relative;
  flex-shrink: 0;
}

.chat-info {
  flex: 1;
  min-width: 0;
}

.chat-name-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.chat-name {
  font-size: 16px;
  color: #333;
  font-weight: 500;
}

.chat-time {
  font-size: 12px;
  color: #999;
  margin-left: auto;
}

.chat-preview {
  font-size: 14px;
  color: var(--color-neutral-500);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chat-preview-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.chat-preview-row .chat-preview {
  flex: 1;
  margin-right: 10px;
}

.unread-badge {
  background-color: #ff4d4f;
  color: #fff;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 10px;
  min-width: 20px;
  text-align: center;
  flex-shrink: 0;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  gap: 15px;
}

.empty-icon {
  font-size: 64px;
}

.empty-text {
  font-size: 16px;
  color: #999;
}

.loading-text {
  text-align: center;
  padding: 20px;
  color: #999;
  font-size: 14px;
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
  gap: 20px;
  margin-bottom: 20px;
}

.footer-links span {
  color: var(--color-neutral-500);
  font-size: 12px;
}

.footer-links span a {
  color: var(--color-neutral-500);
  text-decoration: none;
}

.footer-links span a:hover {
  color: var(--color-error);
}

/* 删除确认对话框样式 */
:deep(.delete-chat-box.el-message-box) {
  border-radius: 20px !important;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15), 0 0 0 1px rgba(0, 0, 0, 0.05);
  max-width: 380px;
  background: linear-gradient(180deg, #ffffff 0%, #fafafa 100%);
}

:deep(.delete-chat-box .el-message-box__header) {
  padding: 28px 32px 0 !important;
  background: transparent !important;
  text-align: center;
}

:deep(.delete-chat-box .el-message-box__title) {
  font-size: 20px !important;
  font-weight: 600 !important;
  color: #1a1a1a !important;
  letter-spacing: 0.5px;
}

:deep(.delete-chat-box .el-message-box__content) {
  padding: 20px 32px 28px !important;
}

:deep(.delete-chat-box .el-message-box__message) {
  font-size: 14px !important;
  color: #666666 !important;
  line-height: 1.7;
  text-align: center;
}

:deep(.delete-chat-box .el-message-box__status) {
  display: none !important;
}

:deep(.delete-chat-box .el-message-box__footer) {
  padding: 0 32px 28px !important;
  display: flex;
  justify-content: center;
  gap: 12px;
}

:deep(.delete-chat-box .el-button) {
  min-width: 100px;
  height: 40px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

:deep(.delete-chat-box .el-button--primary) {
  background: linear-gradient(135deg, #2563eb 0%, #3b82f6 100%) !important;
  border: none !important;
  box-shadow: 0 4px 15px rgba(37,99,235, 0.35);
}

:deep(.delete-chat-box .el-button--primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(37,99,235, 0.45);
  background: linear-gradient(135deg, #3b82f6 0%, #93c5fd 100%) !important;
}

:deep(.delete-chat-box .el-button--default) {
  background: #ffffff !important;
  border: 1.5px solid #e5e5e5 !important;
  color: #666666;
}

:deep(.delete-chat-box .el-button--default:hover) {
  border-color: #2563eb !important;
  color: #2563eb !important;
  background: #f0f6ff !important;
}
</style>
