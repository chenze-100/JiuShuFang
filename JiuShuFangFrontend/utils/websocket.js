import { ref } from 'vue'
import { ElNotification } from 'element-plus'

// 全局 WebSocket 实例
let globalWs = null
let isConnected = false
let reconnectTimer = null
let currentUserId = null
const messageHandlers = []

// 添加消息监听列表，用于通知组件注册消息处理
export function addMessageHandler(handler) {
  messageHandlers.push(handler)
  return () => {
    const index = messageHandlers.indexOf(handler)
    if (index > -1) {
      messageHandlers.splice(index, 1)
    }
  }
}

// 连接 WebSocket
export function connectGlobalWebSocket(userId) {
  if (globalWs && globalWs.readyState === WebSocket.OPEN) {
    console.log('全局 WebSocket 已连接')
    return
  }
  
  if (!userId) {
    console.error('userId 为空，无法连接全局 WebSocket')
    return
  }
  
  currentUserId = userId
  console.log('开始连接全局 WebSocket，userId:', userId)
  
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  // 全局连接传假的参数，只用于接收消息
  const wsUrl = `${protocol}//${window.location.host}/chat/${userId}?toUId=0&goodsId=0`
  
  try {
    globalWs = new WebSocket(wsUrl)
    
    globalWs.onopen = () => {
      console.log('全局 WebSocket 连接成功')
      isConnected = true
      if (reconnectTimer) {
        clearTimeout(reconnectTimer)
        reconnectTimer = null
      }
    }
    
    globalWs.onmessage = (event) => {
      console.log('全局 WebSocket 收到消息:', event.data)
      try {
        const message = JSON.parse(event.data)
        
        // 调用所有注册的消息处理器
        messageHandlers.forEach(handler => {
          try {
            handler(message)
          } catch (e) {
            console.error('消息处理器错误:', e)
          }
        })
        
        // 如果消息是发给自己的，显示通知
        if (message.toId == userId && message.fromId != userId) {
          showNotification(message)
        }
      } catch (e) {
        console.error('解析消息失败:', e)
      }
    }
    
    globalWs.onclose = (event) => {
      console.log('全局 WebSocket 连接关闭', event.code, event.reason)
      isConnected = false
      // 尝试重连
      scheduleReconnect()
    }
    
    globalWs.onerror = (error) => {
      console.error('全局 WebSocket 错误:', error)
      isConnected = false
    }
  } catch (error) {
    console.error('创建全局 WebSocket 连接失败:', error)
  }
}

// 显示通知
function showNotification(message) {
  // 检查当前是否在聊天详情页，避免重复提示
  const isInChatDetail = window.location.hash.includes('/chat-detail')
  
  if (isInChatDetail) {
    return
  }
  
  const senderName = message.fromNickName || message.fromUserName || '好友'
  const previewContent = message.content ? message.content.substring(0, 50) + (message.content.length > 50 ? '...' : '') : '新消息'
  
  ElNotification({
    title: `💬 ${senderName} 发来新消息`,
    message: previewContent,
    type: 'info',
    duration: 4000,
    position: 'top-right',
    onClick: () => {
      // 点击通知跳转到聊天列表，用户再从列表点击进入聊天详情
      window.location.hash = '#/chat-list'
    }
  })
}

// 重连调度
function scheduleReconnect() {
  if (reconnectTimer) {
    return
  }
  reconnectTimer = setTimeout(() => {
    console.log('尝试重新连接全局 WebSocket...')
    if (currentUserId) {
      connectGlobalWebSocket(currentUserId)
    }
    reconnectTimer = null
  }, 3000)
}

// 断开连接
export function disconnectGlobalWebSocket() {
  if (globalWs) {
    globalWs.close()
    globalWs = null
  }
  if (reconnectTimer) {
    clearTimeout(reconnectTimer)
    reconnectTimer = null
  }
  isConnected = false
}

// 发送消息
export function sendGlobalMessage(message) {
  if (!globalWs || globalWs.readyState !== WebSocket.OPEN) {
    console.error('全局 WebSocket 未连接')
    return false
  }
  try {
    globalWs.send(JSON.stringify(message))
    return true
  } catch (error) {
    console.error('发送消息失败:', error)
    return false
  }
}

// 获取连接状态
export function getWebSocketStatus() {
  return isConnected
}