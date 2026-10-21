<template>
  <div class="ai-companion-container">
    <!-- 活力背景 -->
    <VitalityBackground />

    <!-- 顶部导航栏 -->
    <CommonHeader />

    <!-- 页面主体 -->
    <div class="main-content">
      <!-- 左侧边栏 - 对话历史 -->
      <div class="sidebar">
        <div class="sidebar-header">
          <h1 class="sidebar-title">
            <span class="ai-icon">✨</span>
            AI 陪伴助手
          </h1>
          <button class="new-chat-btn" @click="startNewChat" title="新建对话">
            <el-icon><Plus /></el-icon>
          </button>
        </div>

        <div class="chat-history-list">
          <div v-for="group in groupedChatHistory" :key="group.label" class="chat-history-group">
            <GroupHeader :label="group.label" />
            <div
              v-for="chat in group.chats"
              :key="chat.id"
              :class="['history-item', currentChatId === chat.id ? 'active' : '']"
              @click="switchChat(chat.id)"
            >
              <div class="history-info">
                <div class="history-title">{{ chat.title }}</div>
                <div class="history-time">{{ formatHistoryTime(chat.time) }}</div>
              </div>
              <div class="history-delete" @click.stop="handleDeleteChat(chat)">
                <el-icon><Delete /></el-icon>
              </div>
            </div>
          </div>
        </div>

        <EmptyState v-if="chatHistory.length === 0" icon="📝" text="暂无对话记录" hint="开始新的对话吧" />
      </div>

      <!-- 中间聊天区域 -->
      <div class="chat-area">
        <!-- 消息列表 -->
        <div class="message-list" ref="messageListRef">
          <div v-if="messages.length === 0" class="welcome-state">
            <div class="welcome-avatar">
              <div class="ai-avatar-large">
                <img src="/utils/aiAvatar.jpg" alt="AI 助手" class="ai-avatar-img" />
              </div>
            </div>
            <h2 class="welcome-title">
              <TextType
                :text="['你好，我是旧书坊 AI 助手~小坊']"
                :typingSpeed="100"
                :variableSpeed="{ min: 80, max: 150 }"
                :showCursor="true"
                :cursorCharacter="'|'"
                :cursorBlinkDuration="0.6"
                :loop="false"
                :textColors="['#2563eb', '#1d4ed8', '#3b82f6']"
              />
            </h2>
            <p class="welcome-subtitle">买卖二手书，找我就可以啦~ 闲置书籍轻松变现，好书低价带回家！</p>

            <!-- 快捷指令区域 -->
            <div class="quick-commands">
              <div class="quick-title">试试这些快捷指令</div>
              <div class="quick-buttons">
                <button
                  v-for="(cmd, index) in quickCommands"
                  :key="index"
                  class="quick-btn"
                  @click="sendQuickCommand(cmd)"
                >
                  <span class="quick-icon">{{ cmd.icon }}</span>
                  <span class="quick-text">{{ cmd.text }}</span>
                </button>
              </div>
            </div>
          </div>

          <div v-else>
            <div v-for="(msg, index) in messages" :key="index">
              <!-- 日期分隔线 -->
              <div v-if="shouldShowDateDivider(msg, index)" class="date-divider">
                <span class="date-text">{{ formatDateDivider(msg.time) }}</span>
              </div>

              <div :class="['message-item', msg.type]">
                <div class="message-avatar">
                  <div v-if="msg.type === 'user'" class="user-avatar">
                    <el-avatar :size="36" :src="avatarUrl ? convertToExternalUrl(avatarUrl) : ''" :key="avatarUrl">
                      <el-icon><UserFilled /></el-icon>
                    </el-avatar>
                  </div>
                  <div v-else class="ai-avatar">
                    <img src="/utils/aiAvatar.jpg" alt="AI 助手" class="ai-avatar-img-small" />
                  </div>
                </div>

                <div class="message-content">
                  <!-- 纯图片消息（无文本） -->
                  <img
                    v-if="msg.type === 'user' && msg.imageUrl && !msg.content"
                    :src="getImageSrc(msg.imageUrl)"
                    alt="用户图片"
                    class="message-image"
                  />
                  <!-- 有文本的消息气泡 -->
                  <div v-else :class="['message-bubble', msg.type]">
                    <div v-if="msg.type === 'ai' && !msg.content && msg.isStreaming" class="loading-text">
                      小坊正在思考中<span class="dots">{{'.'.repeat(loadingDots)}}</span>
                    </div>
                    <div v-else-if="msg.content" class="message-text" v-html="msg.content"></div>
                    <!-- 有文本时的图片 -->
                    <img
                      v-if="msg.type === 'user' && msg.imageUrl && msg.content"
                      :src="getImageSrc(msg.imageUrl)"
                      alt="用户图片"
                      class="message-image"
                    />
                  </div>
                  <div class="message-time">{{ formatMessageTime(msg.time) }}</div>
                </div>
              </div>
            </div>

          </div>
        </div>

        <!-- 输入区域 -->
        <div class="input-area">
          <div class="input-wrapper">
            <!-- 图片预览区域 -->
            <div v-if="imagePreviewUrl" class="image-preview-area">
              <div class="image-preview-item">
                <img :src="getImageSrc(imagePreviewUrl)" alt="预览图片" class="preview-image" />
                <el-icon class="preview-remove" @click="removeSelectedImage"><Close /></el-icon>
              </div>
            </div>
            <textarea
              v-model="inputMessage"
              class="message-input"
              placeholder="输入消息，和小坊聊聊天吧..."
              @keydown.enter.exact.prevent="sendMessage"
              rows="2"
              maxlength="2000"
              :disabled="isAiThinking"
            />
            <div class="input-footer">
              <span class="char-count">{{ inputMessage.length }}/2000</span>
              <div class="input-actions">
                <div class="upload-menu-wrapper" @mouseenter="showUploadMenu = true" @mouseleave="handleMenuMouseLeave">
                  <button class="action-btn" title="上传图片">
                    <el-icon><Plus /></el-icon>
                  </button>
                  <div v-if="showUploadMenu" class="upload-menu" @click.stop>
                    <div class="upload-menu-item" @click="triggerImageUpload">
                      <el-icon><Picture /></el-icon>
                      <span>上传图片</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <!-- 隐藏的文件输入 -->
            <input
              ref="imageInputRef"
              type="file"
              accept="image/*"
              style="display: none"
              @change="handleImageSelect"
            />
          </div>
          <button
            v-if="isAiThinking"
            class="stop-button"
            @click="stopAiResponse"
          >
            <el-icon><VideoPause /></el-icon>
            停止
          </button>
          <button
            v-else
            class="send-button"
            @click="sendMessage"
            :disabled="!inputMessage.trim() && !imagePreviewUrl"
          >
            <span>
              <el-icon><Promotion /></el-icon>
              发送
            </span>
          </button>
        </div>
      </div>

      <!-- 右侧快捷指令面板 -->
      <div class="quick-panel">
        <div class="quick-panel-header">
          <h3>💡 快捷指令</h3>
        </div>
        <div class="quick-panel-content">
          <div
            v-for="(cmd, index) in quickCommands"
            :key="index"
            :class="['quick-command-item', { 'disabled': isAiThinking }]"
            @click="!isAiThinking && sendQuickCommand(cmd)"
          >
            <div class="quick-command-icon">{{ cmd.icon }}</div>
            <div class="quick-command-info">
              <div class="quick-command-title">{{ cmd.title }}</div>
              <div class="quick-command-desc">{{ cmd.desc }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- 地址选择弹窗 -->
  <el-dialog v-model="addressDialogVisible" title="" width="420px" class="address-custom-dialog" :show-close="false">
    <template #header>
      <div class="address-dialog-header">
        <span class="address-dialog-title">选择收货地址</span>
        <span class="address-dialog-subtitle">点击选择或修改</span>
      </div>
    </template>
    <div class="address-dialog-list">
      <transition-group name="address-item">
        <div
          v-for="(addr, index) in addressList"
          :key="addr.id"
          :class="['address-dialog-item', { selected: selectedAddressId === addr.id }]"
          :style="{ animationDelay: (index * 0.05) + 's' }"
          @click="selectAddress(addr.id)"
        >
          <div class="address-dialog-info">
            <div class="address-dialog-top">
              <span class="receiver-name">{{ addr.receiver }}</span>
              <span class="receiver-phone">{{ addr.phone }}</span>
              <span class="default-tag" v-if="addr.isDefault">默认</span>
            </div>
            <div class="address-dialog-detail">
              {{ addr.province }}{{ addr.city }}{{ addr.district }}{{ addr.detail }}
            </div>
          </div>
          <div class="address-dialog-check" v-if="selectedAddressId === addr.id">
            <el-icon class="check-icon"><Check /></el-icon>
          </div>
        </div>
      </transition-group>
      <div v-if="addressList.length === 0" class="empty-address-text">
        <span>暂无地址</span>
        <router-link to="/address" class="add-address-link">去添加 →</router-link>
      </div>
    </div>
    <template #footer>
      <div class="address-dialog-footer">
        <el-button @click="addressDialogVisible = false" class="cancel-btn">取消</el-button>
        <el-button type="primary" @click="confirmAddressChange" class="confirm-btn" :disabled="selectedAddressId === originalAddressId">确认选择</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import CommonHeader from './components/CommonHeader.vue'
import GroupHeader from './components/GroupHeader.vue'
import EmptyState from './components/EmptyState.vue'
import VitalityBackground from './components/VitalityBackground.vue'
import TextType from './components/TextType.vue'
import AiTypingText from './components/AiTypingText.vue'
import GlitchText from './components/GlitchText.vue'
import { ref, onMounted, onUnmounted, nextTick, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UserFilled, Plus, Delete, Promotion, VideoPause, Check, Picture, Close } from '@element-plus/icons-vue'
import { convertToExternalUrl } from './utils/imageUtils'
import request from './request'

const router = useRouter()
const messageListRef = ref(null)

// 用户信息
const userInfo = ref(null)
const nickName = ref('')
const avatarUrl = ref('')
const currentUserId = ref(null)

// 聊天数据
const messages = ref([])
const inputMessage = ref('')
const isSending = ref(false)
const isAiThinking = ref(false)
const currentChatId = ref(null)
const chatHistory = ref([])
const loadingDots = ref(0)

// 按天数分组的聊天历史
const groupedChatHistory = computed(() => {
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

  chatHistory.value.forEach(chat => {
    if (!chat.time) {
      groups.older.chats.push(chat)
      return
    }

    const chatDate = new Date(chat.time)
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
let loadingDotsTimer = null

// 图片上传相关
const imageInputRef = ref(null)
const showUploadMenu = ref(false)
const imagePreviewUrl = ref('')
const selectedImageFile = ref(null)

// 快捷指令 (根据后端系统提示词配置)
const quickCommands = [
  {
    icon: '📚',
    text: '买书',
    title: '我要买书',
    desc: '帮我推荐好书',
    prompt: '你好，我想买一本好书，请根据我的喜好推荐一些二手书给我~'
  },
  {
    icon: '💰',
    text: '卖书',
    title: '我要卖书',
    desc: '上架闲置书籍',
    prompt: '你好，我有一些闲置的书想卖掉，请帮我上架这些二手书~'
  },
  {
    icon: '🏷️',
    text: '改价',
    title: '修改价格',
    desc: '修改商品售价',
    prompt: '你好，我想修改我上架的书籍价格，请帮我修改一下~'
  },
  {
    icon: '❌',
    text: '下架',
    title: '下架商品',
    desc: '下架在售书籍',
    prompt: '你好，我想下架一些在售的书籍，请帮我操作一下~'
  },
  {
    icon: '📖',
    title: '图书分类',
    text: '分类推荐',
    desc: '按分类推荐书籍',
    prompt: '你好，我想了解一下有哪些图书分类，请给我介绍一下各类书籍~'
  },
  {
    icon: '❓',
    text: '如何使用',
    title: '使用帮助',
    desc: '平台使用指南',
    prompt: '你好，我是新用户，请告诉我如何在这个二手书交易平台买书和卖书~'
  }
]

// 生成唯一 ID
const generateId = () => {
  return 'chat_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9)
}

// 获取用户信息
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

// 加载聊天历史（从后端API）
const loadChatHistory = async () => {
  try {
    const result = await request('/chat-memory/list')
    console.log('>>> loadChatHistory result:', result)
    if (result && result.code === 200 && result.data) {
      chatHistory.value = result.data.map(item => ({
        id: item.conversationId,
        title: item.conversationName || '新对话',
        time: item.createTime,
        messages: []
      }))
      console.log('>>> chatHistory loaded:', chatHistory.value.length, 'items')
    }
  } catch (e) {
    console.error('加载聊天历史失败:', e)
  }
}

// 开始新对话
const startNewChat = () => {
  // 如果当前没有任何消息，不创建新对话项，只清空输入框
  if (messages.value.length === 0) {
    inputMessage.value = ''
    return
  }

  isAiThinking.value = false
  const newId = generateId()
  currentChatId.value = newId
  messages.value = []
  // 立即在左侧添加新对话，标题为"新对话"
  chatHistory.value.unshift({
    id: newId,
    title: '新对话',
    time: new Date().toISOString(),
    messages: []
  })
  nextTick(() => {
    scrollToBottom()
  })
}

// 更新对话标题（仅第一句用户消息时调用）
const updateChatTitle = (firstMessage) => {
  const chat = chatHistory.value.find(c => c.id === currentChatId.value)
  if (chat && chat.title === '新对话') {
    chat.title = firstMessage.length > 20 ? firstMessage.substring(0, 20) + '...' : firstMessage
  }
}

// 删除聊天会话
const handleDeleteChat = async (chat) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这段对话吗？删除后将无法恢复。',
      '确认删除',
      {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning',
        customClass: 'common-confirm-dialog'
      }
    )

    await request(`/chat-memory/delete?conversationId=${chat.id}`, {
      method: 'DELETE'
    })

    ElMessage.success('删除成功')

    // 从列表中移除
    const index = chatHistory.value.findIndex(c => c.id === chat.id)
    if (index !== -1) {
      chatHistory.value.splice(index, 1)
    }

    // 如果删除的是当前选中的对话，切换到另一个或创建新对话
    if (currentChatId.value === chat.id) {
      if (chatHistory.value.length > 0) {
        switchChat(chatHistory.value[0].id)
      } else {
        startNewChat()
      }
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除聊天失败:', error)
      ElMessage.error('删除失败，请重试')
    }
  }
}

// 切换对话
const switchChat = async (chatId) => {
  isAiThinking.value = false
  currentChatId.value = chatId
  try {
    const result = await request(`/chat-memory/${chatId}`)
    if (result && result.code === 200 && result.data) {
      messages.value = result.data.map(item => {
        let content = item.type === 'USER' ? item.content : formatMessageText(item.content)
        let imageUrl = ''

        // 如果是用户消息，尝试从内容中解析 imageUrl
        if (item.type === 'USER' && content) {
          const imageUrlMatch = content.match(/,imageUrl\s*:\s*(.+)$/i)
          if (imageUrlMatch) {
            imageUrl = imageUrlMatch[1].trim()
            content = content.replace(/,imageUrl\s*:\s*.+$/i, '').trim()
          }
        }

        return {
          type: item.type === 'USER' ? 'user' : 'ai',
          content: content,
          imageUrl: imageUrl,
          time: item.timestamp,
          isStreaming: false
        }
      })
    }
  } catch (e) {
    console.error('加载聊天消息失败:', e)
    messages.value = []
  }
  nextTick(() => {
    scrollToBottom()
  })
}

// 清空对话
const clearMessages = () => {
  if (messages.value.length === 0) {
    ElMessage.info('当前没有对话内容')
    return
  }

  // 询问是否确认清空
  if (!confirm('确定要清空当前对话吗？此操作不可恢复。')) {
    return
  }

  messages.value = []
  isAiThinking.value = false

  ElMessage.success('已清空对话')
}

// 切换上传菜单显示
const toggleUploadMenu = () => {
  showUploadMenu.value = !showUploadMenu.value
}

// 鼠标离开菜单
let menuLeaveTimer = null
const handleMenuMouseLeave = () => {
  menuLeaveTimer = setTimeout(() => {
    showUploadMenu.value = false
  }, 150)
}

// 点击空白处关闭菜单
const handleDocumentClick = (e) => {
  if (menuLeaveTimer) {
    clearTimeout(menuLeaveTimer)
    menuLeaveTimer = null
  }
  showUploadMenu.value = false
}

// 触发图片上传
const triggerImageUpload = () => {
  showUploadMenu.value = false
  imageInputRef.value?.click()
}

// 处理图片选择
const handleImageSelect = async (event) => {
  const file = event.target.files[0]
  if (!file) return

  // 验证文件类型
  if (!file.type.startsWith('image/')) {
    ElMessage.error('请选择图片文件')
    return
  }

  // 验证文件大小 (2MB)
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过 5MB')
    return
  }

  selectedImageFile.value = file

  // 先创建本地预览
  imagePreviewUrl.value = URL.createObjectURL(file)

  // 上传到后端
  try {
    const formData = new FormData()
    formData.append('file', file)

    const token = localStorage.getItem('userInfo') ? JSON.parse(localStorage.getItem('userInfo')).token || '' : ''

    const response = await fetch('/api/image/picture/upload', {
      method: 'POST',
      headers: {
        'token': token
      },
      body: formData,
      credentials: 'include'
    })

    const result = await response.json()
    console.log('>>> 图片上传响应:', result)
    if (result && result.code === 200 && result.data) {
      // 上传成功，用后端返回的 URL 替换本地预览 URL
      URL.revokeObjectURL(imagePreviewUrl.value)
      imagePreviewUrl.value = result.data
      console.log('>>> 图片上传成功, image:', imagePreviewUrl.value)
      console.log('>>> 图片上传成功, URL:', result.data)
      ElMessage.success('图片上传成功')
    } else {
      ElMessage.error(result.message || '图片上传失败')
      removeSelectedImage()
    }
  } catch (e) {
    console.error('图片上传失败:', e)
    ElMessage.error('图片上传失败')
    removeSelectedImage()
  }

  // 清空 input 以允许再次选择同一文件
  event.target.value = ''
}

// 获取图片显示源（处理 blob 和外网 URL）
const getImageSrc = (url) => {
  if (!url) return ''
  if (url.startsWith('blob:') || url.startsWith('data:') ) {
    return url
  }
  const url1=convertToExternalUrl(url)
  console.log('>>> 转换后的 URL:', url1)
  return url1
}

// 移除已选图片
const removeSelectedImage = () => {
  if (imagePreviewUrl.value && imagePreviewUrl.value.startsWith('blob:')) {
    URL.revokeObjectURL(imagePreviewUrl.value)
  }
  imagePreviewUrl.value = ''
  selectedImageFile.value = null
}

// 发送消息
const sendMessage = async () => {
  if ((!inputMessage.value.trim() && !imagePreviewUrl.value) || isSending.value) {
    return
  }

  if (!currentChatId.value || currentChatId.value === 'null') {
    currentChatId.value = generateId()
  }

  let uploadedImageUrl = ''
  if (imagePreviewUrl.value) {
    uploadedImageUrl = imagePreviewUrl.value
  }

  const userMsg = {
    type: 'user',
    content: inputMessage.value.trim(),
    imageUrl: uploadedImageUrl,
    time: new Date().toISOString()
  }

  const isFirstMessage = messages.value.length === 0
  messages.value.push(userMsg)

  if (isFirstMessage) {
    updateChatTitle(userMsg.content || '图片消息')
  }
  inputMessage.value = ''
  removeSelectedImage()
  isSending.value = true
  isAiThinking.value = true

  nextTick(() => {
    scrollToBottom()
  })

  // 发送请求
  sendRequest(userMsg.content, uploadedImageUrl)
}

// 发送 HTTP 请求
const sendRequest = async (prompt, imageUrl = '') => {
  console.log('>>> sendRequest called')
  const conversationId = currentChatId.value
  console.log("会话id:"+conversationId)

  // 添加空的 AI 消息占位
  const aiMsg = {
    type: 'ai',
    content: '',
    time: new Date().toISOString(),
    isStreaming: true
  }
  const aiMsgIndex = messages.value.push(aiMsg) - 1

  // 启动加载动画
  loadingDots.value = 0
  loadingDotsTimer = setInterval(() => {
    loadingDots.value = (loadingDots.value + 1) % 4
  }, 200)

  try {
    let url = `/api/chat/chat?prompt=${encodeURIComponent(prompt)}&conversationId=${conversationId}`
    if (imageUrl) {
      url += `&imageUrl=${encodeURIComponent(imageUrl)}`
    }
    const result = await request(url)
    console.log('>>> request result:', result)
    clearInterval(loadingDotsTimer)
    loadingDotsTimer = null
    if (result && result.code === 200 && result.data) {
      const fullContent = result.data
      simulateStreaming(aiMsgIndex, fullContent)
    } else {
      messages.value[aiMsgIndex].content = '获取回复失败，请稍后重试。😔'
      messages.value[aiMsgIndex].isStreaming = false
      isAiThinking.value = false
    }
  } catch (e) {
    console.error('>>> request error:', e)
    clearInterval(loadingDotsTimer)
    loadingDotsTimer = null
    messages.value[aiMsgIndex].content = '网络错误，请稍后重试。😔'
    messages.value[aiMsgIndex].isStreaming = false
    isAiThinking.value = false
  }

  isSending.value = false
  nextTick(() => {
    scrollToBottom()
  })
}

// 模拟流式输出
const simulateStreaming = (aiMsgIndex, fullText) => {
  const formatted = formatMessageText(fullText)
  console.log('>>> simulateStreaming, formatted length:', formatted.length)
  const CHUNK_SIZE = 80
  let currentLength = 0

  const typeInterval = setInterval(() => {
    if (!messages.value[aiMsgIndex]) {
      clearInterval(typeInterval)
      return
    }

    currentLength += CHUNK_SIZE
    const partial = formatted.substring(0, currentLength)

    // DOMParser 安全截断 HTML
    const parser = new DOMParser()
    const doc = parser.parseFromString(partial, 'text/html')
    const safeContent = doc.body.innerHTML

    messages.value[aiMsgIndex].content = safeContent

    nextTick(() => {
      scrollToBottom()
    })

    if (currentLength >= formatted.length) {
      clearInterval(typeInterval)
      if (messages.value[aiMsgIndex]) {
        messages.value[aiMsgIndex].isStreaming = false
      }
      isAiThinking.value = false
    }
  }, 50)
}

// 停止 AI 回复
const stopAiResponse = () => {
  isAiThinking.value = false
  isSending.value = false
  ElMessage.info('已停止 AI 回复')
}

// 发送快捷指令
const sendQuickCommand = (cmd) => {
  inputMessage.value = cmd.prompt
  nextTick(() => {
    sendMessage()
  })
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (messageListRef.value) {
      messageListRef.value.scrollTop = messageListRef.value.scrollHeight
    }
  })
}

// 格式化时间
const formatMessageTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')
  return `${hours}:${minutes}`
}

const formatHistoryTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date

  const oneDay = 24 * 60 * 60 * 1000

  if (diff < oneDay) {
    const hours = date.getHours().toString().padStart(2, '0')
    const minutes = date.getMinutes().toString().padStart(2, '0')
    return `${hours}:${minutes}`
  } else if (diff < 7 * oneDay) {
    const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
    return weekdays[date.getDay()]
  } else {
    const month = (date.getMonth() + 1).toString().padStart(2, '0')
    const day = date.getDate().toString().padStart(2, '0')
    return `${month}/${day}`
  }
}

const shouldShowDateDivider = (msg, index) => {
  if (index === 0) return true
  const prevMsg = messages.value[index - 1]
  if (!prevMsg || !prevMsg.time || !msg.time) return false
  const prevDate = new Date(prevMsg.time).toDateString()
  const currDate = new Date(msg.time).toDateString()
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

// 格式化消息文本 (支持简单的 markdown)
const formatMessageText = (text) => {
  if (!text) return ''

  // 尝试解析订单 JSON
  const orderJsonResult = extractJsonWithContext(text, 'order')
  if (orderJsonResult) {
    try {
      const json = parseJsonWithBigInt(orderJsonResult.json)
      if (json.id !== undefined && json.totalPrice !== undefined) {
        return orderJsonResult.before + renderOrderCard(json) + orderJsonResult.after
      }
    } catch (e) {
      // 不是有效 JSON，继续
    }
  }

  // 尝试解析书籍 JSON
  const bookJsonResult = extractJsonWithContext(text, 'books')
  if (bookJsonResult) {
    try {
      const json = parseJsonWithBigInt(bookJsonResult.json)
      if (json.books && Array.isArray(json.books)) {
        return bookJsonResult.before + renderBookCards(json.books) + bookJsonResult.after
      }
    } catch (e) {
      // 不是有效 JSON，继续
    }
  }

  // 没有 JSON，直接格式化文本
  return formatTextSimple(text)
}

// 解析 JSON，保留大数字为字符串（避免精度丢失）
const parseJsonWithBigInt = (jsonStr) => {
  // 在 JSON 字符串层面把 15 位以上的数字用引号包起来，避免 JavaScript 精度丢失
  // JavaScript safe integer max is 2^53-1 ≈ 9*10^15 (16 digits but some 16-digit nums exceed it)
  const processed = jsonStr
    .replace(/:\s*(\d{15,})\s*([,\]\}])/g, ':"$1"$2')
  return JSON.parse(processed)
}

// 提取 JSON 块内容并保留上下文
const extractJsonWithContext = (text, type) => {
  // 匹配 ```json ... ``` 块（支持多个）
  const tripleMatches = text.matchAll(/```json\s*([\s\S]*?)\s*```/gi)
  const triples = [...tripleMatches]

  if (triples.length > 0) {
    let before = ''
    let lastEnd = 0
    const jsons = []

    for (const match of triples) {
      const fullMatch = match[0]
      const json = match[1].trim()
      const startIdx = match.index

      // 累积 before 部分（从上次结束到这次开始之间的文本）
      before += formatTextSimple(text.substring(lastEnd, startIdx))
      jsons.push(json)
      lastEnd = startIdx + fullMatch.length
    }

    const after = formatTextSimple(text.substring(lastEnd))
    const combinedJson = jsons.join('\n')
    return { json: combinedJson, before, after }
  }

  // 匹配 `json ... ` 块
  const backtickMatches = text.matchAll(/`json\s*([\s\S]*?)\s*`/gi)
  const backticks = [...backtickMatches]

  if (backticks.length > 0) {
    let before = ''
    let lastEnd = 0
    const jsons = []

    for (const match of backticks) {
      const fullMatch = match[0]
      const json = match[1].trim()
      const startIdx = match.index

      before += formatTextSimple(text.substring(lastEnd, startIdx))
      jsons.push(json)
      lastEnd = startIdx + fullMatch.length
    }

    const after = formatTextSimple(text.substring(lastEnd))
    const combinedJson = jsons.join('\n')
    return { json: combinedJson, before, after }
  }

  // 匹配 "json" 标签后面跟 {...} 的格式
  const jsonLabelIdx = text.toLowerCase().indexOf('json')
  if (jsonLabelIdx !== -1) {
    const startIdx = text.indexOf('{', jsonLabelIdx)
    if (startIdx !== -1) {
      // 找最后一个 } 来处理嵌套
      const endIdx = text.lastIndexOf('}')
      if (endIdx !== -1 && endIdx > startIdx) {
        const json = text.substring(startIdx, endIdx + 1)
        const before = text.substring(0, startIdx)
        const after = text.substring(endIdx + 1)
        return { json, before: formatTextSimple(before), after: formatTextSimple(after) }
      }
    }
  }
  return null
}

// 格式化纯文本（不含 JSON）
const formatTextSimple = (text) => {
  // 转义 HTML
  let formatted = text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')

  // 三级标题 ### 标题
  formatted = formatted.replace(/^### (.+)$/gm, '<h4>$1</h4>')
  // 二级标题 ## 标题
  formatted = formatted.replace(/^## (.+)$/gm, '<h3>$1</h3>')
  // 一级标题 # 标题
  formatted = formatted.replace(/^# (.+)$/gm, '<h2>$1</h2>')
  // 加粗
  formatted = formatted.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
  // 斜体
  formatted = formatted.replace(/\*(.+?)\*/g, '<em>$1</em>')
  // 无序列表项 - 或 • 开头
  formatted = formatted.replace(/^[\-\•] (.+)$/gm, '<li>$1</li>')
  // 有序列表项
  formatted = formatted.replace(/^\d+\. (.+)$/gm, '<li>$1</li>')
  // 代码块
  formatted = formatted.replace(/```([\s\S]*?)```/g, '<pre class="code-block"><code>$1</code></pre>')
  // 行内代码
  formatted = formatted.replace(/`(.+?)`/g, '<code class="inline-code">$1</code>')
  // 链接
  formatted = formatted.replace(/\[(.+?)\]\((.+?)\)/g, '<a href="$2" target="_blank" rel="noopener noreferrer">$1</a>')
  // 换行
  formatted = formatted.replace(/\n/g, '<br>')

  return formatted
}

// 渲染书籍卡片
const renderBookCards = (books) => {
  const degreeMap = { 1: '全新', 2: '九成新', 3: '八成新', 4: '七成新及以下' }
  const convertDegree = (d) => degreeMap[d] || d
  return books.map(book => `
    <div class="book-card" onclick="window.goToProduct('${String(book.bookId)}')" style="cursor:pointer;">
      <div class="book-card-image">
        <img src="${convertToExternalUrl(book.bookImg)}" alt="${book.title}" onerror="this.style.display='none'" />
      </div>
      <div class="book-card-content">
        <h4 class="book-card-title">${book.title}</h4>
        <p class="book-card-author">${book.author || '未知作者'}</p>
        <div class="book-card-footer">
          <span class="book-card-price">¥${book.price || '--'}</span>
          ${book.originalPrice ? `<span class="book-card-original-price">¥${book.originalPrice}</span>` : ''}
        </div>
        ${book.degree ? `<p class="book-card-degree">${convertDegree(book.degree)}</p>` : ''}
        ${book.description ? `<p class="book-card-desc">${book.description}</p>` : ''}
      </div>
    </div>
  `).join('')
}

// 渲染订单卡片
const renderOrderCard = (order) => {
  const statusMap = { 1: '待付款', 2: '待发货', 3: '待收货', 4: '已完成', 5: '已取消' }
  const statusText = statusMap[order.status] || '未知'
  const items = order.orderItemList || []
  const itemHtml = items.map(item => `
    <div class="ai-order-product-item" onclick="window.goToProduct('${String(item.goodsId)}')" style="cursor:pointer;">
      <div class="ai-order-product-image">
        <img src="${convertToExternalUrl(item.goodsImage)}" alt="${item.goodsName}" onerror="this.style.display='none'" />
      </div>
      <div class="ai-order-product-info">
        <div class="ai-order-product-name">${item.goodsName || '商品'}</div>
        <div class="ai-order-product-price">¥${item.price || '--'} × ${item.num || 1}</div>
      </div>
    </div>
  `).join('')
  const payBtn = order.status == 1 ? `<button class="ai-pay-btn" onclick="window.goToPay('${String(order.id)}')">去支付</button>` : ''
  const viewBtn = (order.status == 2 || order.status == 3 || order.status == 4) ? `<button class="ai-view-btn" onclick="window.goToOrderDetail('${String(order.id)}')">查看订单</button>` : ''
  // 处理 createTime，可能是数组或字符串
  let timeStr = ''
  if (order.createTime) {
    if (Array.isArray(order.createTime)) {
      const [y, m, d, h, min] = order.createTime
      timeStr = `${y}-${String(m).padStart(2, '0')}-${String(d).padStart(2, '0')} ${String(h).padStart(2, '0')}:${String(min).padStart(2, '0')}`
    } else {
      timeStr = String(order.createTime).substring(0, 16).replace('T', ' ')
    }
  }
  return `
    <div class="ai-order-card">
      <div class="ai-order-header">
        <span class="ai-order-id">订单号：${order.id}</span>
        <span class="ai-order-status">${statusText}</span>
      </div>
      <div class="ai-order-body">
        <div class="ai-order-product-list">
          ${itemHtml}
        </div>
        <div class="ai-order-receiver">
          <div class="ai-order-receiver-name">${order.receiverName || ''}</div>
          <div class="ai-order-receiver-addr">${order.receiverAddress || '未填写地址'}</div>
          ${order.status == 1 ? `<div class="ai-order-edit-addr" onclick="window.showAddressDialog('${String(order.id)}', '${order.receiverName}', '${order.receiverPhone}', '${order.receiverAddress}')">修改地址 ∨</div>` : ''}
        </div>
        <div class="ai-order-amount">
          <div class="ai-order-amount-label">应付</div>
          <div class="ai-order-amount-value">¥${order.totalPrice || '--'}</div>
        </div>
      </div>
      <div class="ai-order-footer">
        <span class="ai-order-time">${timeStr}</span>
        <div class="ai-order-actions">
          ${payBtn}
          ${viewBtn}
        </div>
      </div>
    </div>
  `
}

// 订单相关状态
const addressDialogVisible = ref(false)
const addressList = ref([])
const currentOrderId = ref(null)
const selectedAddressId = ref(null)
const originalAddressId = ref(null)

// 显示地址选择弹窗
window.showAddressDialog = async (orderId, orderReceiverName, orderReceiverPhone, orderReceiverAddress) => {
  currentOrderId.value = orderId
  await loadAddressList()
  // 尝试匹配当前地址
  const matchedAddr = addressList.value.find(addr =>
    addr.receiver === orderReceiverName &&
    addr.phone === orderReceiverPhone &&
    (addr.province + addr.city + addr.district + addr.detail) === orderReceiverAddress
  )
  originalAddressId.value = matchedAddr ? matchedAddr.id : null
  selectedAddressId.value = originalAddressId.value
  addressDialogVisible.value = true
}

// 加载地址列表
const loadAddressList = async () => {
  try {
    const result = await request('/address/list')
    if (result && result.code === 200) {
      addressList.value = result.data || []
      // 设置默认选中
      const defaultAddr = addressList.value.find(a => a.isDefault)
      if (defaultAddr) {
        selectedAddressId.value = defaultAddr.id
      } else if (addressList.value.length > 0) {
        selectedAddressId.value = addressList.value[0].id
      }
    }
  } catch (e) {
    console.error('加载地址列表失败:', e)
  }
}

// 选择地址
const selectAddress = (id) => {
  selectedAddressId.value = id
}

// 确认修改地址
const confirmAddressChange = async () => {
  if (!selectedAddressId.value) {
    ElMessage.warning('请选择收货地址')
    return
  }
  // 如果选择的是原地址，不提交
  if (selectedAddressId.value === originalAddressId.value) {
    addressDialogVisible.value = false
    return
  }
  try {
    const result = await request('/order/ai', {
      method: 'PUT',
      body: JSON.stringify({
        orderId: currentOrderId.value,
        addressId: selectedAddressId.value,
        conversationId: currentChatId.value
      })
    })
    if (result && result.code === 200) {
      ElMessage.success('地址修改成功')
      addressDialogVisible.value = false
      // 刷新当前对话
      const chatId = currentChatId.value
      if (chatId) {
        switchChat(chatId)
      }
    } else {
      ElMessage.error(result?.message || '修改失败')
    }
  } catch (e) {
    ElMessage.error('修改地址失败')
  }
}

// 去支付
window.goToPay = (orderId) => {
  router.push({ path: '/payment', query: { orderId } })
}

window.goToOrderDetail = (orderId) => {
  router.push({ path: '/order-detail', query: { orderId } })
}

// 导航方法
const goHome = () => {
  router.push('/home')
}

const goToShoppingCart = () => {
  router.push('/shopping-cart')
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
  // 先获取用户信息，确保头像能正确显示
  getUserInfo()
  await loadChatHistory()

  // 如果没有对话历史，创建一个新对话
  if (chatHistory.value.length === 0) {
    startNewChat()
  } else {
    // 切换到最近的对话
    switchChat(chatHistory.value[0].id)
  }

  // 全局跳转商品详情函数
  window.goToProduct = (bookId) => {
    router.push({ path: '/product', query: { id: bookId } })
  }

  // 点击空白处关闭上传菜单
  document.addEventListener('click', handleDocumentClick)
})

onUnmounted(() => {
  document.removeEventListener('click', handleDocumentClick)
})
</script>

<style scoped>
/* === 容器布局 === */
.ai-companion-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  position: relative;
  overflow-x: hidden;
}

/* === 顶部导航栏 === */
.top-nav {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid var(--color-neutral-100);
  transition: all var(--transition-base);
}

.nav-content {
  max-width: 1600px;
  margin: 0 auto;
  padding: 0 20px;
  height: 56px;
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
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-error) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  color: transparent;
  font-size: 28px;
  font-weight: 800;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.welcome-text:hover {
  transform: scale(1.05) rotate(-2deg);
  text-shadow: 0 0 20px rgba(37,99,235, 0.3);
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

/* === 主体内容 === */
.main-content {
  flex: 1;
  display: grid;
  grid-template-columns: 280px 1fr 260px;
  gap: 20px;
  max-width: 1600px;
  width: 100%;
  margin: 0 auto;
  padding: 20px;
  box-sizing: border-box;
}

/* === 左侧边栏 === */
.sidebar {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border-radius: var(--radius-lg);
  padding: 20px;
  box-shadow: var(--shadow-md);
  display: flex;
  flex-direction: column;
  height: calc(100vh - 120px);
  overflow: hidden;
}

.sidebar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--color-neutral-100);
  margin-bottom: 16px;
}

.sidebar-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--color-neutral-700);
  display: flex;
  align-items: center;
  gap: 8px;
}

.ai-icon {
  font-size: 22px;
}

.new-chat-btn {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-md);
  border: none;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  position: relative;
  overflow: hidden;
}

.new-chat-btn::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: linear-gradient(
    45deg,
    transparent 30%,
    rgba(255, 255, 255, 0.3) 50%,
    transparent 70%
  );
  transform: translateX(-100%) rotate(45deg);
  transition: transform 0.6s ease;
}

.new-chat-btn:hover {
  transform: scale(1.15) rotate(90deg);
  box-shadow: 0 8px 25px rgba(37,99,235, 0.5);
}

.new-chat-btn:hover::before {
  transform: translateX(100%) rotate(45deg);
}

.chat-history-list {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.chat-history-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.history-group-header {
  font-size: 12px;
  color: #999;
  padding: 8px 14px 4px;
  font-weight: 500;
}

.history-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  background: transparent;
  border: 2px solid transparent;
  position: relative;
  overflow: hidden;
  animation: slideInRight 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}

@keyframes slideInRight {
  from {
    opacity: 0;
    transform: translateX(-20px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateX(0) scale(1);
  }
}

.history-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(37,99,235, 0.08),
    transparent
  );
  transition: left 0.5s ease;
}

.history-item:hover {
  background: var(--color-primary-soft);
  transform: translateX(6px) scale(1.02);
  box-shadow: 0 4px 15px rgba(37,99,235, 0.15);
}

.history-item:hover::before {
  left: 100%;
}

.history-item.active {
  background: linear-gradient(135deg, rgba(37,99,235, 0.15) 0%, rgba(37,99,235, 0.25) 100%);
  border: 2px solid var(--color-primary);
  box-shadow: 0 4px 20px rgba(37,99,235, 0.25),
              inset 0 0 20px rgba(37,99,235, 0.1);
  animation: activeGlow 2s ease-in-out infinite;
}

@keyframes activeGlow {
  0%, 100% {
    box-shadow: 0 4px 20px rgba(37,99,235, 0.25),
                inset 0 0 20px rgba(37,99,235, 0.1);
  }
  50% {
    box-shadow: 0 4px 25px rgba(37,99,235, 0.4),
                inset 0 0 25px rgba(37,99,235, 0.15);
  }
}

.history-delete {
  color: #999;
  padding: 6px;
  transition: all 0.3s;
  flex-shrink: 0;
  opacity: 0;
}

.history-item:hover .history-delete {
  opacity: 1;
}

.history-delete:hover {
  color: #ff4d4f;
  transform: scale(1.1);
}

.history-icon {
  font-size: 20px;
  flex-shrink: 0;
}

.history-info {
  flex: 1;
  min-width: 0;
}

.history-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-neutral-700);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 4px;
}

.history-time {
  font-size: 11px;
  color: var(--color-neutral-400);
}

.empty-history {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: var(--color-neutral-400);
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.empty-text {
  font-size: 14px;
  margin-bottom: 4px;
}

.empty-hint {
  font-size: 12px;
}

/* === 聊天区域 === */
.chat-area {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
  display: flex;
  flex-direction: column;
  height: calc(100vh - 120px);
  overflow: hidden;
}

/* 消息列表 */
.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 欢迎状态 */
.welcome-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  animation: fadeInUp 0.6s ease;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.welcome-avatar {
  margin-bottom: 24px;
}

.ai-avatar-large {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  box-shadow: 0 8px 24px rgba(96,165,250, 0.3);
  animation: avatarFloat 3s ease-in-out infinite;
}

.ai-avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

@keyframes avatarFloat {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.ai-emoji {
  font-size: 50px;
}

.welcome-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--color-neutral-700);
  margin-bottom: 12px;
  background: linear-gradient(135deg, var(--color-primary) 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.welcome-subtitle {
  font-size: 15px;
  color: var(--color-neutral-500);
  margin-bottom: 32px;
}

/* 快捷指令 */
.quick-commands {
  width: 100%;
  max-width: 600px;
}

.quick-title {
  font-size: 14px;
  color: var(--color-neutral-400);
  margin-bottom: 16px;
  text-align: center;
}

.quick-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  justify-content: center;
}

.quick-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  background: white;
  border: 2px solid var(--color-primary-soft);
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all var(--transition-base);
  font-size: 14px;
  color: var(--color-neutral-700);
}

.quick-btn:hover {
  background: var(--color-primary-soft);
  border-color: var(--color-primary);
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(37,99,235, 0.2);
}

.quick-icon {
  font-size: 18px;
}

/* 消息项 */
.message-item {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  animation: messageSlideIn 0.3s ease;
}

@keyframes messageSlideIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.message-item.user {
  flex-direction: row-reverse;
}

.message-avatar {
  flex-shrink: 0;
}

.user-avatar {
  width: 36px;
  height: 36px;
}

.ai-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(96,165,250, 0.2);
}

.ai-avatar-img-small {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.message-content {
  display: flex;
  flex-direction: column;
  gap: 6px;
  max-width: 70%;
}

.message-item.user .message-content {
  align-items: flex-end;
}

.message-bubble {
  padding: 14px 18px;
  border-radius: var(--radius-lg);
  line-height: 1.6;
  font-size: 14px;
  position: relative;
  word-break: break-word;
}

.message-bubble.user {
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
  color: white;
  border-bottom-right-radius: 4px;
  box-shadow: 0 4px 12px rgba(37,99,235, 0.25);
}

.message-bubble.ai-message {
  background: var(--color-neutral-100);
  color: var(--color-neutral-700);
  border-bottom-left-radius: 4px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
}

.message-text {
  white-space: pre-wrap;
}

.ai-typing-text {
  white-space: pre-wrap;
word-break: break-word;
  line-height: 1.6;
}

/* 书籍卡片 */
.message-text :deep(.book-card) {
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  gap: 12px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 12px;
  margin: 8px 0;
  border: 1px solid rgba(37,99,235, 0.1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  max-width: 340px;
  width: 100%;
  box-sizing: border-box;
  overflow: hidden;
}

.message-text :deep(.book-card-image) {
  width: 75px;
  height: 100px;
  flex-shrink: 0;
  border-radius: 8px;
  overflow: hidden;
  background: #f5f5f5;
  display: flex;
  align-items: flex-start;
}

.message-text :deep(.book-card-image img) {
  width: 75px;
  height: 100px;
  object-fit: cover;
  display: block;
  vertical-align: top;
}

.message-text :deep(.book-card-content) {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
  max-width: calc(340px - 75px - 24px);
  line-height: 1.4;
}

.message-text :deep(.book-card-title) {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.message-text :deep(.book-card-author) {
  font-size: 12px;
  color: #888;
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.message-text :deep(.book-card-footer) {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: auto;
}

.message-text :deep(.book-card-price) {
  font-size: 16px;
  font-weight: 700;
  color: #2563eb;
}

.message-text :deep(.book-card-original-price) {
  font-size: 12px;
  color: #aaa;
  text-decoration: line-through;
}

.message-text :deep(.book-card-degree) {
  font-size: 11px;
  color: #3b82f6;
  background: rgba(59,130,246, 0.1);
  padding: 2px 6px;
  border-radius: 4px;
  margin: 0;
  width: fit-content;
}

.message-text :deep(.book-card-desc) {
  font-size: 11px;
  color: #999;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

/* 订单卡片 - 匹配OrderList样式 */
.message-text :deep(.ai-order-card) {
  border: 1px solid #e5e5e5;
  border-radius: 8px;
  overflow: hidden;
  max-width: 100%;
  display: flex;
  flex-direction: column;
}

.message-text :deep(.ai-order-header) {
  display: flex;
  justify-content: space-between;
  padding: 15px 20px;
  background-color: #f9f9f9;
  border-bottom: 1px solid #e5e5e5;
  font-size: 14px;
  color: #999;
}

.message-text :deep(.ai-order-id) {
  color: #999;
}

.message-text :deep(.ai-order-status) {
  font-size: 14px;
  font-weight: bold;
}

.message-text :deep(.ai-order-body) {
  display: flex;
  align-items: center;
  padding: 20px;
  gap: 20px;
}

.message-text :deep(.ai-order-product-list) {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.message-text :deep(.ai-order-product-item) {
  display: flex;
  gap: 15px;
}

.message-text :deep(.ai-order-product-image) {
  width: 80px;
  height: 80px;
  border: 1px solid #e5e5e5;
  border-radius: 4px;
  overflow: hidden;
  flex-shrink: 0;
  display: flex;
  align-items: flex-start;
}

.message-text :deep(.ai-order-product-image img) {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.message-text :deep(.ai-order-product-info) {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.message-text :deep(.ai-order-product-name) {
  font-size: 14px;
  color: #333;
  line-height: 1.5;
}

.message-text :deep(.ai-order-product-price) {
  font-size: 14px;
  color: #999;
}

.message-text :deep(.ai-order-receiver) {
  width: 150px;
  text-align: center;
}

.message-text :deep(.ai-order-receiver-name) {
  color: #333;
  font-size: 14px;
  margin-bottom: 4px;
}

.message-text :deep(.ai-order-receiver-addr) {
  font-size: 12px;
  color: #999;
  word-break: break-all;
  line-height: 1.4;
}

.message-text :deep(.ai-order-amount) {
  width: 100px;
  text-align: center;
}

.message-text :deep(.ai-order-amount-label) {
  font-size: 12px;
  color: #999;
  margin-bottom: 5px;
}

.message-text :deep(.ai-order-amount-value) {
  font-size: 18px;
  font-weight: bold;
  color: #2563eb;
}

.message-text :deep(.ai-order-actions) {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.message-text :deep(.ai-pay-btn) {
  padding: 8px 20px;
  background-color: #2563eb;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 14px;
}

.message-text :deep(.ai-pay-btn:hover) {
  background-color: #3b82f6;
}

.message-text :deep(.ai-view-btn) {
  padding: 8px 20px;
  background-color: #6c757d;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 14px;
}

.message-text :deep(.ai-view-btn:hover) {
  background-color: #5a6268;
}

.message-text :deep(.ai-order-footer) {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  border-top: 1px solid #e5e5e5;
  background-color: #f9f9f9;
}

.message-text :deep(.ai-order-time) {
  font-size: 12px;
  color: #999;
}

.message-text :deep(.ai-order-edit-addr) {
  color: #2563eb;
  font-size: 12px;
  cursor: pointer;
  margin-top: 4px;
  display: inline-block;
}

.message-text :deep(.ai-order-edit-addr:hover) {
  text-decoration: underline;
}

/* 地址弹窗 */
.address-custom-dialog {
  border-radius: 16px;
  overflow: hidden;
}

.address-custom-dialog :deep(.el-dialog) {
  border-radius: 16px;
  overflow: hidden;
}

.address-custom-dialog :deep(.el-dialog__header) {
  padding: 20px 24px 10px;
  margin: 0;
}

.address-custom-dialog :deep(.el-dialog__body) {
  padding: 0 24px 20px;
}

.address-custom-dialog :deep(.el-dialog__footer) {
  padding: 10px 24px 20px;
}

.address-dialog-header {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.address-dialog-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.address-dialog-subtitle {
  font-size: 12px;
  color: #999;
}

.address-dialog-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 350px;
  overflow-y: auto;
  padding: 4px;
}

.address-dialog-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border: 2px solid #f0f0f0;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: #fff;
  animation: slideInUp 0.4s ease-out forwards;
  opacity: 0;
  transform: translateY(10px);
}

@keyframes slideInUp {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.address-dialog-item:hover {
  border-color: #2563eb;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(37,99,235, 0.15);
}

.address-dialog-item.selected {
  border-color: #2563eb;
  background: linear-gradient(135deg, rgba(37,99,235, 0.05) 0%, rgba(37,99,235, 0.1) 100%);
  box-shadow: 0 2px 8px rgba(37,99,235, 0.2);
}

.address-dialog-info {
  flex: 1;
}

.address-dialog-top {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-bottom: 6px;
}

.address-dialog-top .receiver-name {
  font-weight: 600;
  color: #333;
  font-size: 15px;
}

.address-dialog-top .receiver-phone {
  color: #666;
  font-size: 13px;
}

.address-dialog-top .default-tag {
  background: linear-gradient(135deg, #2563eb 0%, #3b82f6 100%);
  color: #fff;
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 10px;
}

.address-dialog-detail {
  font-size: 13px;
  color: #888;
  line-height: 1.4;
}

.address-dialog-check {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.address-dialog-check .check-icon {
  animation: bounceIn 0.3s ease-out;
}

@keyframes bounceIn {
  0% {
    transform: scale(0);
  }
  50% {
    transform: scale(1.2);
  }
  100% {
    transform: scale(1);
  }
}

.empty-address-text {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  color: #999;
  padding: 30px;
}

.add-address-link {
  color: #2563eb;
  font-size: 14px;
  text-decoration: none;
}

.address-dialog-footer {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.address-dialog-footer .cancel-btn {
  border-radius: 20px;
  padding: 8px 20px;
}

.address-dialog-footer .confirm-btn {
  border-radius: 20px;
  padding: 8px 24px;
  background: linear-gradient(135deg, #2563eb 0%, #3b82f6 100%);
  border: none;
}

.address-dialog-footer .confirm-btn:hover {
  opacity: 0.9;
}

/* 地址项动画 */
.address-item-enter-active {
  animation: slideInUp 0.4s ease-out forwards;
}

.address-item-leave-active {
  animation: slideOutDown 0.3s ease-in forwards;
}

@keyframes slideOutDown {
  to {
    opacity: 0;
    transform: translateY(-10px);
  }
}

.message-text :deep(.code-block) {
  background: #1a1a2e;
  color: #eaeaea;
  padding: 12px 16px;
  border-radius: var(--radius-md);
  margin: 8px 0;
  overflow-x: auto;
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 13px;
}

.message-text :deep(.inline-code) {
  background: rgba(0, 0, 0, 0.1);
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 13px;
}

.message-text :deep(a) {
  color: var(--color-primary);
  text-decoration: underline;
}

.message-time {
  font-size: 11px;
  color: var(--color-neutral-400);
}

.message-item.user .message-time {
  text-align: right;
}

/* 日期分隔线 */
.date-divider {
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 8px 0;
}

.date-text {
  font-size: 12px;
  color: var(--color-neutral-400);
  padding: 4px 12px;
  background: var(--color-neutral-100);
  border-radius: var(--radius-pill);
}

/* AI 思考状态 */
.loading-text {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 0;
  color: var(--color-neutral-500);
  font-size: 14px;
}

.loading-text .dots {
  min-width: 24px;
}

/* 输入区域 */
.input-area {
  padding: 20px 24px;
  border-top: 1px solid var(--color-neutral-100);
  display: flex;
  gap: 16px;
  background: rgba(255, 255, 255, 0.8);
}

.input-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.message-input {
  width: 100%;
  padding: 14px 18px;
  border: 2px solid var(--color-neutral-200);
  border-radius: var(--radius-lg);
  font-size: 14px;
  resize: none;
  outline: none;
  transition: all var(--transition-base);
  font-family: inherit;
  background: white;
  min-height: 60px;
  max-height: 150px;
}

.message-input:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 4px var(--color-primary-soft);
}

.input-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.char-count {
  font-size: 12px;
  color: var(--color-neutral-400);
}

.input-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-sm);
  border: none;
  background: var(--color-neutral-100);
  color: var(--color-neutral-500);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--transition-base);
}

.action-btn:hover {
  background: #2563eb;
  color: white;
}

.send-button {
  padding: 0 28px;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
  color: white;
  border: none;
  border-radius: var(--radius-lg);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all var(--transition-base);
  min-width: 100px;
}

.send-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(37,99,235, 0.35);
}

.send-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.stop-button {
  padding: 0 28px;
  background: linear-gradient(135deg, #f5222d 0%, #d9363e 100%);
  color: white;
  border: none;
  border-radius: var(--radius-lg);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all var(--transition-base);
  min-width: 100px;
}

.stop-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(245, 34, 45, 0.35);
}

.message-input:disabled {
  background: var(--color-neutral-100);
  color: var(--color-neutral-400);
  cursor: not-allowed;
}

.sending-text {
  animation: pulse 1s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

/* === 右侧快捷面板 === */
.quick-panel {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border-radius: var(--radius-lg);
  padding: 20px;
  box-shadow: var(--shadow-md);
  height: calc(100vh - 120px);
  overflow-y: auto;
}

.quick-panel-header {
  padding-bottom: 16px;
  border-bottom: 1px solid var(--color-neutral-100);
  margin-bottom: 16px;
}

.quick-panel-header h3 {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-neutral-700);
  display: flex;
  align-items: center;
  gap: 8px;
}

.quick-panel-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.quick-command-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px;
  background: white;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-base);
  border: 2px solid transparent;
}

.quick-command-item:hover {
  border-color: var(--color-primary);
  background: var(--color-primary-soft);
  transform: translateX(-4px);
  box-shadow: 0 4px 12px rgba(37,99,235, 0.15);
}

.quick-command-item.disabled {
  opacity: 0.5;
  cursor: not-allowed;
  pointer-events: none;
}

.quick-command-icon {
  font-size: 24px;
  flex-shrink: 0;
}

.quick-command-info {
  flex: 1;
  min-width: 0;
}

.quick-command-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-neutral-700);
  margin-bottom: 4px;
}

.quick-command-desc {
  font-size: 12px;
  color: var(--color-neutral-500);
}

/* === 响应式设计 === */
@media (max-width: 1200px) {
  .main-content {
    grid-template-columns: 240px 1fr;
  }

  .quick-panel {
    display: none;
  }
}

@media (max-width: 768px) {
  .main-content {
    grid-template-columns: 1fr;
    padding: 10px;
    gap: 10px;
  }

  .sidebar {
    display: none;
  }

  .chat-area {
    height: calc(100vh - 80px);
  }

  .nav-content {
    padding: 0 12px;
  }

  .welcome-text {
    font-size: 22px;
  }

  .message-content {
    max-width: 85%;
  }

  .input-area {
    padding: 12px;
  }

  .message-list {
    padding: 16px;
  }
}

/* 上传菜单样式 */
.upload-menu-wrapper {
  position: relative;
}

.upload-menu {
  position: absolute;
  bottom: 100%;
  left: 50%;
  transform: translateX(-50%);
  background: white;
  border-radius: 8px;
  box-shadow: 0 8px 30px rgba(37,99,235, 0.18), 0 2px 8px rgba(0, 0, 0, 0.08);
  padding: 8px 0;
  min-width: 140px;
  z-index: 100;
  margin-bottom: 10px;
  animation: menuFadeIn 0.2s ease-out;
}

@keyframes menuFadeIn {
  from {
    opacity: 0;
    transform: translateX(-50%) translateY(8px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateX(-50%) translateY(0) scale(1);
  }
}

.upload-menu::after {
  content: '';
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translateX(-50%);
  border: 8px solid transparent;
  border-top-color: white;
}

.upload-menu-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 18px;
  cursor: pointer;
  color: #444;
  font-size: 14px;
  transition: all 0.2s ease;
  border-left: 3px solid transparent;
}

.upload-menu-item:hover {
  background: linear-gradient(135deg, #f0f6ff 0%, #fff 100%);
  color: #2563eb;
  border-left-color: #2563eb;
  padding-left: 20px;
}

.upload-menu-item .el-icon {
  font-size: 18px;
  transition: transform 0.2s ease;
}

.upload-menu-item:hover .el-icon {
  transform: scale(1.15);
}

/* 图片预览区域 */
.image-preview-area {
  padding: 12px 16px;
  border-top: 1px solid #f0f0f0;
  background: #fafafa;
}

.image-preview-item {
  position: relative;
  display: inline-block;
}

.preview-image {
  max-width: 120px;
  max-height: 120px;
  border-radius: 8px;
  object-fit: cover;
  border: 1px solid #eee;
}

.preview-remove {
  position: absolute;
  top: -8px;
  right: -8px;
  width: 20px;
  height: 20px;
  background: rgba(0, 0, 0, 0.6);
  color: white;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  transition: background-color 0.2s;
}

.preview-remove:hover {
  background: rgba(0, 0, 0, 0.8);
}

/* 消息图片样式 */
.message-image {
  max-width: 280px;
  max-height: 200px;
  border-radius: 12px;
  object-fit: cover;
  margin-top: 8px;
  display: block;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.message-image:hover {
  transform: scale(1.02);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.15);
}

/* 纯图片消息样式（无文本） */
.message-image-only {
  max-width: 320px;
  max-height: 300px;
  border-radius: 16px;
  object-fit: cover;
  display: block;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.12);
}

.message-image-only:hover {
  transform: scale(1.03);
  box-shadow: 0 8px 25px rgba(37,99,235, 0.25);
}

</style>
