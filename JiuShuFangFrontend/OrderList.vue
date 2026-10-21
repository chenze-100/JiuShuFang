<template>
  <!-- 动态背景 -->
  <VitalityBackground />

  <div class="order-list-container">
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
      <!-- 左侧订单中心菜单 -->
      <div class="sidebar">
        <div class="sidebar-header">
          <h1>我的订单</h1>
          <span class="back-home-link" @click="goBackHome">返回首页</span>
        </div>
        <div class="sidebar-menu">
          <div
            class="menu-item"
            :class="{ active: currentTab === 'bought' }"
            @click="currentTab = 'bought'"
          >
            我买到的
          </div>
          <div
            class="menu-item"
            :class="{ active: currentTab === 'sold' }"
            @click="currentTab = 'sold'"
          >
            我卖出的
          </div>
          <div class="menu-item" @click="goToMyComments">我的评价</div>
          <div class="menu-item">代下单</div>
        </div>

        <div class="sidebar-title" style="margin-top: 20px;">关注中心</div>
        <div class="sidebar-menu">
          <div class="menu-item">关注的店铺</div>
          <div class="menu-item">关注的活动</div>
        </div>
      </div>

      <!-- 右侧订单列表 -->
      <div class="order-content">
        <!-- 订单状态筛选 -->
        <div class="order-tabs">
          <div
            class="tab-item"
            :class="{ active: currentStatus === '' }"
            @click="currentStatus = ''"
          >
            全部订单
          </div>
          <div
            class="tab-item"
            :class="{ active: currentStatus === 1 }"
            @click="currentStatus = 1"
          >
            待付款
            <span v-if="currentTab === 'bought' && unpaidCount > 0" class="badge">{{ unpaidCount }}</span>
          </div>
          <div
            class="tab-item"
            :class="{ active: currentStatus === 2 }"
            @click="currentStatus = 2"
          >
            待发货
          </div>
          <div
            class="tab-item"
            :class="{ active: currentStatus === 3 }"
            @click="currentStatus = 3"
          >
            待收货/使用
          </div>
          <div
            class="tab-item"
            :class="{ active: currentStatus === 4 }"
            @click="currentStatus = 4"
            v-if="currentTab === 'bought'"
          >
            待评价
          </div>
          <div
            class="tab-item"
            :class="{ active: currentStatus === 5 }"
            @click="currentStatus = 5"
          >
            已取消
          </div>
        </div>

        <!-- 时间筛选 -->
        <div class="filter-section">
          <div class="filter-group">
            <span class="filter-label">订单时间：</span>
            <el-select v-model="timeFilter" placeholder="选择时间范围" class="time-select" @change="handleTimeFilterChange">
              <el-option label="近三个月订单" value="3months" />
              <el-option label="今年内订单" value="thisYear" />
              <el-option label="2026 年订单" value="2026" />
              <el-option label="2025 年订单" value="2025" />
              <el-option label="2024 年订单" value="2024" />
              <el-option label="2023 年订单" value="2023" />
              <el-option label="2022 年订单" value="2022" />
              <el-option label="2021 年订单" value="2021" />
              <el-option label="2020 年订单" value="2020" />
            </el-select>
          </div>

          <div class="search-group">
            <el-input
              v-model="searchKeyword"
              placeholder="商品名称/商品编号/订单号"
              class="search-input"
              @keyup.enter="handleSearch"
            >
              <template #append>
                <el-button @click="handleSearch">搜索</el-button>
              </template>
            </el-input>
          </div>
        </div>

        <!-- 订单列表 -->
        <div class="order-list">
          <div v-if="loading" class="loading-container">
            <div class="loading-spinner"></div>
            <div class="loading-text">加载中...</div>
          </div>

          <div v-else-if="orders.length === 0" class="empty-container">
            <div class="empty-icon">📦</div>
            <div class="empty-text">暂无订单</div>
          </div>

          <div v-else class="order-items">
            <div v-for="order in orders" :key="order.id" class="order-item">
              <!-- 订单头部 -->
              <div class="order-header">
                <span class="order-date">{{ formatDateTime(order.createTime) }}</span>
                <span class="order-id">订单号：{{ order.id }}</span>
              </div>

              <!-- 订单内容 -->
              <div class="order-body">
                <!-- 商品列表 -->
                <div class="product-list">
                  <div v-for="item in order.orderItemVOList || []" :key="item.bookName" v-if="!item || order.status !== 4 || !item.isComment" class="product-item">
                    <div class="product-image">
                      <img :src="item.bookImg" :alt="item.bookName" />
                    </div>
                    <div class="product-info">
                      <div class="product-name">{{ item.bookName }}</div>
                      <div class="product-spec">
                        <span v-if="order.status === 1">商品规格中，数量有请，请尽快支付哦~</span>
                      </div>
                      <div class="product-quantity">x{{ item.num }}</div>
                    </div>
                  </div>
                </div>

                <!-- 收货人信息 -->
                <div class="receiver-info">
                  <el-popover
                    placement="right"
                    :width="300"
                    trigger="hover"
                  >
                    <template #reference>
                      <div class="receiver-name">
                        {{ order.receiverName }}
                        <el-icon class="popover-icon"><User /></el-icon>
                      </div>
                    </template>
                    <div class="receiver-detail">
                      <div class="receiver-name-detail">{{ order.receiverName }}</div>
                      <div class="receiver-address">{{ order.receiverAddress }}</div>
                      <div class="receiver-phone">{{ formatPhone(order.receiverPhone) }}</div>
                    </div>
                  </el-popover>
                </div>

                <!-- 金额信息 -->
                <div class="amount-info">
                  <div class="amount-label">应付</div>
                  <div class="amount-value">¥{{ order.totalPrice }}</div>
                  <div class="payment-status" v-if="order.status === 1">{{ getPayTypeText(order.payType) }}</div>
                </div>

                <!-- 订单状态 -->
                <div class="order-status">
                  <div class="status-text" :class="getStatusClass(order)">
                    {{ getStatusText(order) }}
                  </div>
                  <div class="status-time">
                    <el-icon class="clock-icon"><Clock /></el-icon>
                    <span v-if="getRemainingTime(order)" class="countdown-text">{{ getRemainingTime(order) }}</span>
                  </div>
                </div>

                <!-- 操作按钮 -->
                <div class="action-buttons">
                  <!-- 我买到的按钮 -->
                  <template v-if="currentTab === 'bought'">
                    <button v-if="order.status === 1" class="btn-cancel" @click="handleCancelOrder(order)">取消订单</button>
                    <button v-if="order.status === 1" class="btn-primary" @click="handlePay(order)">去支付</button>
                    <button v-if="order.status === 3" class="btn-confirm" @click="handleConfirmReceive(order)">确认收货</button>
                    <button v-if="order.status === 4 && (order.orderItemVOList || []).some(item => !item.isComment)" class="btn-review" @click="handleReview(order)">去评价</button>
                  </template>
                  <!-- 我卖出的按钮 -->
                  <template v-else>
                    <button v-if="order.status === 1" class="btn-cancel" @click="handleCancelOrder(order)">取消订单</button>
                    <button v-if="order.status === 2" class="btn-primary" @click="handleShip(order)">已发货</button>
                  </template>
                  <button class="btn-secondary" @click="viewOrderDetail(order)">订单详情</button>
                  <button v-if="order.status === 2 || order.status === 3" class="btn-secondary" @click="viewLogistics(order)">查看物流</button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 加载更多提示 -->
        <div class="load-more-container" v-if="orders.length > 0">
          <div v-if="isLoadingMore" class="loading-more">
            <div class="loading-spinner-small"></div>
            <span>加载中...</span>
          </div>
          <div v-else-if="!hasMore" class="no-more">没有更多订单了</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import UserMenu from './components/UserMenu.vue'
import GlitchText from './components/GlitchText.vue'
import VitalityBackground from './components/VitalityBackground.vue'
import { ref, reactive, onMounted, watch, onBeforeUnmount, h } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { User, Clock, UserFilled, Warning, CircleCheckFilled } from '@element-plus/icons-vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import request from './request'

const router = useRouter()
const route = useRoute()

// 用户信息
const nickName = ref('')
const avatarUrl = ref('')
const userInfo = ref(null)

// 状态管理
const currentTab = ref('bought')
const currentStatus = ref('')
const timeFilter = ref('')
const searchKeyword = ref('')
const loading = ref(false)
const orders = ref([])
const unpaidCount = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)
const hasLoadedUnpaidCount = ref(false)  // 标记是否已加载过待付款数量
const countdownMap = ref({})  // 存储每个订单的倒计时
const countdownTimers = ref({})  // 存储每个订单的定时器
const isLoadingMore = ref(false)  // 是否正在加载更多
const hasMore = ref(true)  // 是否还有更多数据

// 加载订单列表
const loadOrders = async () => {
  loading.value = true
  try {
    const params = {
      pageNo: currentPage.value,
      pageSize: pageSize.value,
      isAsc: false,
      sortBy: 'create_time'
    }
    
    // 添加状态筛选
    if (currentStatus.value) {
      params.status = currentStatus.value
    }
    
    // 添加时间筛选
    if (timeFilter.value) {
      const now = new Date()
      let beginDate = null
      let endDate = now
      
      if (timeFilter.value === '3months') {
        // 近三个月
        beginDate = new Date()
        beginDate.setMonth(beginDate.getMonth() - 3)
      } else if (timeFilter.value === 'thisYear') {
        // 今年内
        beginDate = new Date(now.getFullYear(), 0, 1)
      } else {
        // 指定年份
        const year = parseInt(timeFilter.value)
        beginDate = new Date(year, 0, 1)
        endDate = new Date(year, 11, 31, 23, 59, 59)
      }
      
      // 格式化时间为 YYYY-MM-DD HH:mm:ss
      params.beginTime = formatDate(beginDate)
      params.endTime = formatDate(endDate)
    }
    
    // 添加是否卖出筛选
    params.isSeller = currentTab.value === 'sold'

    // 添加搜索关键词
    if (searchKeyword.value && searchKeyword.value.trim()) {
      params.query = searchKeyword.value.trim()
    }
    
    console.log('发送请求参数:', params)
    
    const result = await request('/order/page', {
      method: 'GET',
      params
    })
    
    console.log('订单列表响应:', result)
    
    if (result && result.code === 200 && result.data) {
      const pageData = result.data
      // PageDTO 格式：{ total, pages, list }
      orders.value = pageData.list || []
      total.value = parseInt(pageData.total) || 0
      console.log('订单列表:', orders.value)
      console.log('总数量:', total.value)
      
      // 启动倒计时定时器
      startCountdownTimers()
      
      // 只有在全部订单状态下且未加载过待付款数量时才查询（仅适用于我买到的标签）
      if (currentStatus.value === '' && !hasLoadedUnpaidCount.value && currentTab.value === 'bought') {
        await loadUnpaidCount()
        hasLoadedUnpaidCount.value = true
      }
    }
  } catch (error) {
    console.error('加载订单列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载待付款订单数量
const loadUnpaidCount = async () => {
  try {
    const result = await request('/order/unpaid/count')
    if (result && result.code === 200) {
      unpaidCount.value = result.data || 0
    }
  } catch (error) {
    // 如果接口不存在，不显示错误，避免影响用户体验
    console.log('待付款数量接口未实现')
    unpaidCount.value = 0
  }
}

// 时间筛选变化
const handleTimeFilterChange = (value) => {
  currentPage.value = 1
  loadOrders()
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  loadOrders()
}

// 分页变化
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  orders.value = []
  hasMore.value = true
  loadOrders()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  orders.value = []
  hasMore.value = true
  loadOrders()
}

// 滚动到底部加载更多
const handleScroll = () => {
  const scrollTop = document.documentElement.scrollTop || document.body.scrollTop
  const clientHeight = document.documentElement.clientHeight
  const scrollHeight = document.documentElement.scrollHeight

  if (scrollTop + clientHeight >= scrollHeight - 100 && !isLoadingMore.value && hasMore.value && !loading.value) {
    loadMoreOrders()
  }
}

// 加载更多订单
const loadMoreOrders = async () => {
  if (isLoadingMore.value || !hasMore.value) return

  isLoadingMore.value = true
  currentPage.value++

  try {
    const params = {
      pageNo: currentPage.value,
      pageSize: pageSize.value,
      isAsc: false,
      sortBy: 'create_time'
    }

    if (currentStatus.value) {
      params.status = currentStatus.value
    }

    if (timeFilter.value) {
      const now = new Date()
      let beginDate = null
      let endDate = now

      if (timeFilter.value === '3months') {
        beginDate = new Date()
        beginDate.setMonth(beginDate.getMonth() - 3)
      } else if (timeFilter.value === 'thisYear') {
        beginDate = new Date(now.getFullYear(), 0, 1)
      } else {
        const year = parseInt(timeFilter.value)
        beginDate = new Date(year, 0, 1)
        endDate = new Date(year, 11, 31, 23, 59, 59)
      }

      params.beginTime = formatDate(beginDate)
      params.endTime = formatDate(endDate)
    }

    if (searchKeyword.value && searchKeyword.value.trim()) {
      params.query = searchKeyword.value.trim()
    }

    params.isSeller = currentTab.value === 'sold'

    const result = await request('/order/page', {
      method: 'GET',
      params
    })

    if (result && result.code === 200 && result.data) {
      const pageData = result.data
      const newOrders = pageData.list || []

      if (newOrders.length === 0) {
        hasMore.value = false
        currentPage.value--
      } else {
        orders.value = [...orders.value, ...newOrders]
        startCountdownTimers()

        if (orders.value.length >= total.value) {
          hasMore.value = false
        }
      }
    } else {
      currentPage.value--
    }
  } catch (error) {
    console.error('加载更多订单失败:', error)
    currentPage.value--
  } finally {
    isLoadingMore.value = false
  }
}

// 格式化时间为 YYYY-MM-DD HH:mm:ss
const formatDate = (date) => {
  if (!date) return ''
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

// 格式化时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 格式化手机号
const formatPhone = (phone) => {
  if (!phone) return ''
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

// 获取订单状态文本
const getStatusText = (order) => {
  // 后端返回的 status: 1 待付款 2 待发货 3 待收货 4 已完成 5 已取消 6 已评价
  if (order.status === 1) {
    return '待付款'
  } else if (order.status === 2) {
    return '待发货'
  } else if (order.status === 3) {
    return '待收货'
  } else if (order.status === 4) {
    return '已完成'
  } else if (order.status === 5) {
    return '已取消'
  } else if (order.status === 6) {
    return '已评价'
  }
  return '未知状态'
}

// 获取订单状态样式
const getStatusClass = (order) => {
  if (order.status === 1) {
    return 'status-waiting'
  } else if (order.status === 2 || order.status === 3) {
    return 'status-pending'
  } else if (order.status === 5) {
    return 'status-cancelled'
  } else if (order.status === 6) {
    return 'status-commented'
  }
  return 'status-normal'
}

// 获取支付方式文本
const getPayTypeText = (payType) => {
  if (payType === 1) {
    return '支付宝支付'
  } else if (payType === 2) {
    return '余额支付'
  }
  return '在线支付'
}

// 计算倒计时（从订单创建时间开始 1 天）
const calculateCountdown = (createTime) => {
  if (!createTime) return null
  
  const now = new Date().getTime()
  const createTimestamp = new Date(createTime).getTime()
  const oneDay = 24 * 60 * 60 * 1000  // 1 天的毫秒数
  const deadline = createTimestamp + oneDay
  
  const remaining = deadline - now
  
  if (remaining <= 0) {
    return null  // 已超时
  }
  
  const days = Math.floor(remaining / (1000 * 60 * 60 * 24))
  const hours = Math.floor((remaining % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60))
  const minutes = Math.floor((remaining % (1000 * 60 * 60)) / (1000 * 60))
  const seconds = Math.floor((remaining % (1000 * 60)) / 1000)
  
  // 如果天数为 0，不显示天数
  if (days === 0) {
    return `剩余${hours}小时${minutes}分${seconds}秒，请尽快支付`
  } else {
    return `剩余${days}天${hours}小时${minutes}分${seconds}秒，请尽快支付`
  }
}

// 取消订单（超时未支付）
const cancelOrder = async (orderId) => {
  try {
    await request('/order/status', {
      method: 'PUT',
      body: JSON.stringify({
        orderId: orderId,
        status: 5,  // 5 表示已取消
        cancelReason: '超时未支付，自动取消'
      })
    })
    console.log('订单已自动取消，订单 ID:', orderId)
    // 刷新订单列表
    loadOrders()
  } catch (error) {
    console.error('取消订单失败:', error)
  }
}

// 启动所有待付款订单的倒计时
const startCountdownTimers = () => {
  // 清除所有现有定时器
  Object.keys(countdownTimers.value).forEach(orderId => {
    if (countdownTimers.value[orderId]) {
      clearInterval(countdownTimers.value[orderId])
    }
  })
  
  // 为每个待付款订单启动定时器
  orders.value.forEach(order => {
    if (order.status === 1 && order.createTime) {
      // 立即计算一次
      countdownMap.value[order.id] = calculateCountdown(order.createTime)
      
      // 每秒更新一次
      countdownTimers.value[order.id] = setInterval(() => {
        const countdown = calculateCountdown(order.createTime)
        countdownMap.value[order.id] = countdown
        
        // 如果倒计时结束，清除定时器并自动取消订单
        if (!countdown) {
          if (countdownTimers.value[order.id]) {
            clearInterval(countdownTimers.value[order.id])
            delete countdownTimers.value[order.id]
          }
          // 自动取消订单
          cancelOrder(order.id)
        }
      }, 1000)
    }
  })
}

// 获取剩余时间（用于显示倒计时）
const getRemainingTime = (order) => {
  // 只有待付款订单才有剩余时间
  if (order.status === 1) {
    return countdownMap.value[order.id] || '剩余 0 小时 0 分 0 秒，请尽快支付'
  }
  return ''
}

// 付款操作
const handlePay = (order) => {
  router.push(`/payment?orderId=${order.id}`)
}

// 取消订单操作（用户主动取消）
const handleCancelOrder = (order) => {
  // 根据是买家还是卖家使用不同的取消原因
  const buyerCancelReasons = [
    '不想要了',
    '商品错选/多选',
    '商品无货',
    '地址/电话等填写错误',
    '商品降价',
    '商品价格高于其他平台',
    '发货/送达时间不符合需求',
    '没用/少用/错用优惠',
    '其他'
  ]

  const sellerCancelReasons = [
    '买家未付款',
    '买家申请退款取消',
    '商品已售罄',
    '商品损坏/缺货',
    '买家信息无效/不完整',
    '无法联系到买家',
    '订单信息有误',
    '其他'
  ]

  const cancelReasons = currentTab.value === 'sold' ? sellerCancelReasons : buyerCancelReasons
  
  const selectedReasons = []
  
  // 创建一个更新函数来重新渲染
  let updateCallback = null
  
  const toggleReason = (reason, element) => {
    const index = selectedReasons.indexOf(reason)
    if (index > -1) {
      selectedReasons.splice(index, 1)
      element.classList.remove('selected')
      const checkIcon = element.querySelector('.check-icon')
      if (checkIcon) checkIcon.remove()
    } else {
      selectedReasons.push(reason)
      element.classList.add('selected')
      if (!element.querySelector('.check-icon')) {
        const checkIcon = document.createElement('span')
        checkIcon.className = 'el-icon check-icon'
        checkIcon.innerHTML = '<svg viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg"><path d="M512 64C264.6 64 64 264.6 64 512s200.6 448 448 448 448-200.6 448-448S759.4 64 512 64zm-32 629.3l-45.3 45.3c-12.5 12.5-32.8 12.5-45.3 0L170.7 520c-12.5-12.5-12.5-32.8 0-45.3l45.3-45.3c12.5-12.5 32.8-12.5 45.3 0l205.4 205.4 333.3-333.3c12.5-12.5 32.8-12.5 45.3 0l45.3 45.3c12.5 12.5 12.5 32.8 0 45.3L480 693.3z" fill="currentColor"/></svg>'
        element.appendChild(checkIcon)
      }
    }
  }
  
  ElMessageBox({
    title: '取消订单',
    message: h('div', { class: 'cancel-reason-container' }, [
      h('div', { class: 'cancel-tip' }, [
        h('span', {}, '订单取消成功后将无法恢复；拆单后取消订单，其他子单也将一并取消')
      ]),
      h('div', { class: 'reason-grid' }, 
        cancelReasons.map((reason, idx) => 
          h('div', { 
            class: 'reason-item',
            onClick: function(e) {
              toggleReason(reason, e.currentTarget)
            }
          }, [
            h('span', {}, reason)
          ])
        )
      )
    ]),
    showCancelButton: true,
    confirmButtonText: '确认取消',
    cancelButtonText: '取消',
    type: 'warning',
    customClass: 'cancel-order-box',
    customStyle: {
      iconColor: 'transparent'
    },
    closeOnClickModal: false
  }).then(async () => {
    if (selectedReasons.length === 0) {
      ElMessage.warning('请至少选择一个取消原因')
      return
    }
    
    try {
      await request('/order/status', {
        method: 'PUT',
        body: JSON.stringify({
          orderId: order.id,
          status: 5,
          cancelReason: selectedReasons.join(',')
        })
      })
      ElMessage.success('订单已取消')
      // 刷新订单列表和待付款数量
      await loadOrders()
      await loadUnpaidCount()
    } catch (error) {
      console.error('取消订单失败:', error)
      ElMessage.error('取消失败，请重试')
    }
  }).catch(() => {
    ElMessage.info('已取消操作')
  })
}

// 确认收货操作
const handleConfirmReceive = (order) => {
  ElMessageBox.confirm(
    '您是否确认已收到商品？',
    '确认收货',
    {
      confirmButtonText: '确认收货',
      cancelButtonText: '取消',
      type: 'warning',
      customClass: 'confirm-receive-box'
    }
  ).then(async () => {
    try {
      await request('/order/status', {
        method: 'PUT',
        body: JSON.stringify({
          orderId: order.id,
          status: 4
        })
      })
      ElMessage.success('确认收货成功')
      // 刷新订单列表和待付款数量
      await loadOrders()
      await loadUnpaidCount()
    } catch (error) {
      console.error('确认收货失败:', error)
      ElMessage.error('确认收货失败，请重试')
    }
  }).catch(() => {
    // 用户取消
    ElMessage.info('已取消')
  })
}

// 评价操作
const handleReview = (order) => {
  // 获取未评价的商品 ID
  const unCommentedGoodsIds = (order.orderItemVOList || [])
    .filter(item => !item.isComment)
    .map(item => item.goodsId)

  router.push({
    path: '/comment',
    query: {
      orderId: order.id,
      goodsIds: unCommentedGoodsIds.join(',')
    }
  })
}

// 卖家发货操作
const handleShip = (order) => {
  ElMessageBox.confirm(
    '确定已发货给买家？',
    '确认发货',
    {
      confirmButtonText: '确认发货',
      cancelButtonText: '取消',
      type: 'warning',
      customClass: 'confirm-receive-box'
    }
  ).then(async () => {
    try {
      await request('/order/status', {
        method: 'PUT',
        body: JSON.stringify({
          orderId: order.id,
          status: 3
        })
      })
      ElMessage.success('发货成功')
      await loadOrders()
    } catch (error) {
      console.error('发货失败:', error)
      ElMessage.error('发货失败，请重试')
    }
  }).catch(() => {
    ElMessage.info('已取消')
  })
}

// 查看详情
const viewOrderDetail = (order) => {
  router.push({
    path: '/order-detail',
    query: {
      orderId: order.id,
      tab: currentTab.value
    }
  })
}

// 查看物流
const viewLogistics = (order) => {
  // TODO: 查看物流信息
  console.log('查看物流:', order)
}

// 导航方法
const goToHome = () => {
  router.push('/home')
}

const goBackHome = () => {
  router.push('/home')
}

const goToShoppingCart = () => {
  router.push('/shopping-cart')
}

const goToMyComments = () => {
  router.push('/my-comments')
}

const handleLogout = () => {
  ElMessage.success('已退出登录')
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  router.push('/login')
}

// 获取用户信息
const getUserInfo = () => {
  const userInfoData = localStorage.getItem('userInfo')
  if (userInfoData) {
    const user = JSON.parse(userInfoData)
    userInfo.value = user
    nickName.value = user.nickName || user.username || '用户'
    avatarUrl.value = user.avatar || ''
  }
}

// 监听状态变化
watch([currentStatus, timeFilter, currentTab], () => {
  currentPage.value = 1
  orders.value = []
  hasMore.value = true
  // 切换标签时重置待付款数量
  if (currentTab === 'sold') {
    unpaidCount.value = 0
    hasLoadedUnpaidCount.value = false
  } else if (currentTab === 'bought') {
    // 切换回买到的标签时重置，待付款数量会在切换到全部订单时重新加载
    unpaidCount.value = 0
    hasLoadedUnpaidCount.value = false
  }
  loadOrders()
  // 在我买到的标签下，切换到全部订单或待付款时刷新待付款数量
  if (currentTab === 'bought' && (currentStatus === '' || currentStatus === 1)) {
    loadUnpaidCount()
  }
}, {
  immediate: true
})

// 页面加载时获取用户信息和订单列表
onMounted(() => {
  getUserInfo()
  window.addEventListener('scroll', handleScroll)

  // 从 URL 参数读取 tab
  const tab = route.query.tab
  if (tab === 'bought' || tab === 'sold') {
    currentTab.value = tab
  }
})

// 组件卸载时清除所有定时器和监听器
onBeforeUnmount(() => {
  Object.keys(countdownTimers.value).forEach(orderId => {
    if (countdownTimers.value[orderId]) {
      clearInterval(countdownTimers.value[orderId])
    }
  })
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
.order-list-container {
  min-height: 100vh;
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

/* 主体内容 */
.main-content {
  max-width: 1200px;
  margin: 20px auto;
  display: flex;
  gap: 20px;
  padding: 0 20px;
  background: transparent;
}

/* 左侧边栏 */
.sidebar {
  position: sticky;
  top: 76px;
  width: 200px;
  background-color: #fff;
  border-radius: 8px;
  padding: 20px 0;
  height: fit-content;
  align-self: flex-start;
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

/* 右侧订单内容 */
.order-content {
  flex: 1;
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
}

/* 订单状态标签 */
.order-tabs {
  display: flex;
  gap: 30px;
  border-bottom: 2px solid #e5e5e5;
  padding-bottom: 15px;
  margin-bottom: 20px;
}

.tab-item {
  font-size: 16px;
  color: #666;
  cursor: pointer;
  position: relative;
  transition: all 0.3s;
}

.tab-item:hover {
  color: #2563eb;
}

.tab-item.active {
  color: #2563eb;
  font-weight: bold;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: -17px;
  left: 0;
  right: 0;
  height: 2px;
  background-color: #2563eb;
}

.badge {
  display: inline-block;
  background-color: #f23030;
  color: #fff;
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 10px;
  margin-left: 5px;
}

/* 筛选区域 */
.filter-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 8px;
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 10px;
}

.filter-label {
  color: #666;
  font-size: 14px;
}

.time-select {
  width: 150px;
}

.search-group {
  width: 300px;
}

.search-input {
  width: 100%;
}

/* 订单列表 */
.order-list {
  min-height: 400px;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #f3f3f3;
  border-top: 3px solid #2563eb;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.loading-text {
  margin-top: 15px;
  color: #999;
}

.empty-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
}

.empty-icon {
  font-size: 60px;
  margin-bottom: 20px;
}

.empty-text {
  color: #999;
  font-size: 16px;
}

.order-items {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.order-item {
  border: 1px solid #e5e5e5;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s;
}

.order-item:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.order-header {
  display: flex;
  justify-content: space-between;
  padding: 15px 20px;
  background-color: #f9f9f9;
  border-bottom: 1px solid #e5e5e5;
  font-size: 14px;
  color: #999;
}

.order-id {
  color: #999;
}

.order-body {
  display: flex;
  align-items: center;
  padding: 20px;
  gap: 20px;
}

.product-list {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.product-item {
  display: flex;
  gap: 15px;
}

.product-image {
  width: 80px;
  height: 80px;
  border: 1px solid #e5e5e5;
  border-radius: 4px;
  overflow: hidden;
  position: relative;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1;
}

.overlay-text {
  color: #fff;
  font-size: 12px;
  font-weight: bold;
  text-align: center;
  padding: 4px;
}

.product-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.product-name {
  font-size: 14px;
  color: #333;
  line-height: 1.5;
}

.product-spec {
  font-size: 12px;
  color: #999;
}

.product-quantity {
  font-size: 14px;
  color: #999;
  text-align: right;
}

.receiver-info {
  width: 120px;
  text-align: center;
}

.receiver-name {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  color: #333;
  cursor: pointer;
  padding: 8px;
  border-radius: 4px;
  transition: all 0.3s;
}

.receiver-name:hover {
  background-color: #f5f5f5;
}

.popover-icon {
  font-size: 14px;
  color: #999;
}

.receiver-detail {
  padding: 10px;
}

.receiver-name-detail {
  font-weight: bold;
  margin-bottom: 10px;
  color: #333;
}

.receiver-address {
  font-size: 13px;
  color: #666;
  margin-bottom: 8px;
  line-height: 1.5;
}

.receiver-phone {
  font-size: 13px;
  color: #666;
}

.amount-info {
  width: 100px;
  text-align: center;
}

.amount-label {
  font-size: 12px;
  color: #999;
  margin-bottom: 5px;
}

.amount-value {
  font-size: 18px;
  font-weight: bold;
  color: #2563eb;
  margin-bottom: 5px;
}

.payment-status {
  font-size: 12px;
  color: #999;
}

.order-status {
  width: 100px;
  text-align: center;
}

.status-text {
  font-size: 14px;
  font-weight: bold;
  margin-bottom: 5px;
}

.status-waiting {
  color: #f23030;
}

.status-pending {
  color: #3b82f6;
}

.status-normal {
  color: #666;
}

.status-cancelled {
  color: #999;
  font-style: italic;
}

.status-commented {
  color: #19be6b;
}

.status-time {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 3px;
  font-size: 12px;
  color: #999;
}

.clock-icon {
  font-size: 14px;
}

/* 倒计时文本样式 - 橙色字体，给予紧迫感 */
.countdown-text {
  color: #2563eb;
  font-weight: 600;
  font-size: 13px;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.btn-primary {
  padding: 8px 20px;
  background-color: #2563eb;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 14px;
}

.btn-primary:hover {
  background-color: #3b82f6;
}

.btn-secondary {
  padding: 8px 20px;
  background-color: #fff;
  color: #666;
  border: 1px solid #e5e5e5;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 14px;
}

.btn-secondary:hover {
  border-color: #2563eb;
  color: #2563eb;
}

/* 确认收货按钮 */
.btn-confirm {
  padding: 8px 20px;
  background-color: #2563eb;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 14px;
  font-weight: 500;
}

.btn-confirm:hover {
  background-color: #3b82f6;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(37,99,235, 0.3);
}

/* 取消订单按钮 */
.btn-cancel {
  padding: 8px 20px;
  background-color: #f23030;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 14px;
  font-weight: 500;
}

.btn-cancel:hover {
  background-color: #ff4d4f;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(242, 48, 48, 0.3);
}

/* 去评价按钮 */
.btn-review {
  padding: 8px 20px;
  background-color: #2563eb;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 14px;
  font-weight: 500;
}

.btn-review:hover {
  background-color: #3b82f6;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(37,99,235, 0.3);
}

/* 确认收货对话框样式 */
:deep(.confirm-receive-box.el-message-box) {
  border-radius: 16px !important;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  max-width: 420px;
}

:deep(.confirm-receive-box .el-message-box__header) {
  padding: 24px 24px 20px !important;
  background: linear-gradient(135deg, #2563eb 0%, #3b82f6 100%) !important;
  color: #fff;
  text-align: center;
}

:deep(.confirm-receive-box .el-message-box__title) {
  font-size: 18px !important;
  font-weight: 600 !important;
  color: #fff !important;
  letter-spacing: 0.5px;
}

:deep(.confirm-receive-box .el-message-box__content) {
  padding: 35px 24px 20px !important;
  background: linear-gradient(to bottom, #fff 0%, #fffbf7 100%) !important;
}

:deep(.confirm-receive-box .el-message-box__message) {
  font-size: 16px !important;
  color: #333 !important;
  text-align: center;
  line-height: 1.6;
}

:deep(.confirm-receive-box .el-message-box__footer) {
  padding: 20px 24px 28px !important;
  background-color: transparent !important;
  display: flex;
  justify-content: center;
  gap: 20px;
}

:deep(.confirm-receive-box .el-button--primary) {
  background: linear-gradient(135deg, #2563eb 0%, #3b82f6 100%) !important;
  border: none !important;
  padding: 12px 36px !important;
  font-size: 15px !important;
  font-weight: 500 !important;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(37,99,235, 0.3);
}

:deep(.confirm-receive-box .el-button--primary:hover) {
  background: linear-gradient(135deg, #3b82f6 0%, #3b82f6 100%) !important;
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(37,99,235, 0.4);
}

:deep(.confirm-receive-box .el-button--default) {
  padding: 12px 36px !important;
  font-size: 15px !important;
  border-radius: 8px;
  border: 1px solid #ddd;
}

:deep(.confirm-receive-box .el-button--default:hover) {
  border-color: #2563eb !important;
  color: #2563eb !important;
}

/* 取消订单对话框样式 */
:deep(.cancel-order-box.el-message-box) {
  border-radius: 16px !important;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  max-width: 450px;
}

:deep(.cancel-order-box .el-message-box__header) {
  padding: 24px 24px 20px !important;
  background: linear-gradient(135deg, #f23030 0%, #ff4d4f 100%) !important;
  color: #fff;
  text-align: center;
}

:deep(.cancel-order-box .el-message-box__title) {
  font-size: 18px !important;
  font-weight: 600 !important;
  color: #fff !important;
  letter-spacing: 0.5px;
}

:deep(.cancel-order-box .el-message-box__content) {
  padding: 30px 24px 20px !important;
  background: linear-gradient(to bottom, #fff 0%, #fff5f5 100%) !important;
}

.cancel-reason-container {
  padding: 5px 0 10px;
}

.cancel-reason-text {
  font-size: 15px;
  color: #333;
  margin-bottom: 18px;
  font-weight: 500;
  text-align: center;
}

.cancel-reason-select {
  width: 100%;
}

:deep(.cancel-order-box .el-select) {
  width: 100%;
}

:deep(.cancel-order-box .el-select .el-input__wrapper) {
  border-radius: 8px;
  padding: 8px 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

:deep(.cancel-order-box .el-select .el-input__inner) {
  font-size: 14px;
}

:deep(.cancel-order-box .el-message-box__footer) {
  padding: 20px 24px 28px !important;
  background-color: transparent !important;
  display: flex;
  justify-content: center;
  gap: 20px;
}

:deep(.cancel-order-box .el-button--primary) {
  background: linear-gradient(135deg, #f23030 0%, #ff4d4f 100%) !important;
  border: none !important;
  padding: 12px 36px !important;
  font-size: 15px !important;
  font-weight: 500 !important;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(242, 48, 48, 0.3);
}

:deep(.cancel-order-box .el-button--primary:hover) {
  background: linear-gradient(135deg, #ff4d4f 0%, #ff6b6b 100%) !important;
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(242, 48, 48, 0.4);
}

:deep(.cancel-order-box .el-button--default) {
  padding: 12px 36px !important;
  font-size: 15px !important;
  border-radius: 8px;
  border: 1px solid #ddd;
}

:deep(.cancel-order-box .el-button--default:hover) {
  border-color: #f23030 !important;
  color: #f23030 !important;
}

/* 分页 */
.pagination-container {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}

/* 加载更多 */
.load-more-container {
  text-align: center;
  padding: 20px 0;
}

.loading-more {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: #999;
  font-size: 14px;
}

.loading-spinner-small {
  width: 20px;
  height: 20px;
  border: 2px solid #f3f3f3;
  border-top: 2px solid #2563eb;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.no-more {
  color: #999;
  font-size: 14px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .main-content {
    flex-direction: column;
  }
  
  .sidebar {
    width: 100%;
  }
  
  .order-body {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .receiver-info,
  .amount-info,
  .order-status,
  .action-buttons {
    width: 100%;
    text-align: left;
  }
  
  .action-buttons {
    flex-direction: row;
    flex-wrap: wrap;
  }
}
</style>

<style>
/* 确认收货对话框全局样式 */
.confirm-receive-box.el-message-box {
  border-radius: 16px !important;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  max-width: 420px;
}

.confirm-receive-box .el-message-box__header {
  padding: 24px 24px 20px !important;
  background: linear-gradient(135deg, #2563eb 0%, #3b82f6 100%) !important;
  color: #fff;
  text-align: center;
}

.confirm-receive-box .el-message-box__title {
  font-size: 18px !important;
  font-weight: 600 !important;
  color: #fff !important;
  letter-spacing: 0.5px;
}

.confirm-receive-box .el-message-box__content {
  padding: 35px 24px 20px !important;
  background: linear-gradient(to bottom, #fff 0%, #fffbf7 100%) !important;
}

.confirm-receive-box .el-message-box__message {
  font-size: 16px !important;
  color: #333 !important;
  text-align: center;
  line-height: 1.6;
}

.confirm-receive-box .el-message-box__footer {
  padding: 20px 24px 28px !important;
  background-color: transparent !important;
  display: flex;
  justify-content: center;
  gap: 20px;
}

.confirm-receive-box .el-button--primary {
  background: linear-gradient(135deg, #2563eb 0%, #3b82f6 100%) !important;
  border: none !important;
  padding: 12px 36px !important;
  font-size: 15px !important;
  font-weight: 500 !important;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(37,99,235, 0.3);
}

.confirm-receive-box .el-button--primary:hover {
  background: linear-gradient(135deg, #3b82f6 0%, #3b82f6 100%) !important;
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(37,99,235, 0.4);
}

.confirm-receive-box .el-button--default {
  padding: 12px 36px !important;
  font-size: 15px !important;
  border-radius: 8px;
  border: 1px solid #ddd;
}

.confirm-receive-box .el-button--default:hover {
  border-color: #2563eb !important;
  color: #2563eb !important;
}

/* 取消订单对话框全局样式 */
.cancel-order-box.el-message-box {
  border-radius: 16px !important;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  max-width: 450px;
}

.cancel-order-box .el-message-box__header {
  padding: 24px 24px 20px !important;
  background: linear-gradient(135deg, #f23030 0%, #ff4d4f 100%) !important;
  color: #fff;
  text-align: center;
}

.cancel-order-box .el-message-box__title {
  font-size: 18px !important;
  font-weight: 600 !important;
  color: #fff !important;
  letter-spacing: 0.5px;
}

.cancel-order-box .el-message-box__content {
  padding: 30px 24px 20px !important;
  background: linear-gradient(to bottom, #fff 0%, #fff5f5 100%) !important;
}

.cancel-order-box .el-message-box__footer {
  padding: 20px 24px 28px !important;
  background-color: transparent !important;
  display: flex;
  justify-content: center;
  gap: 20px;
}

.cancel-order-box .el-button--primary {
  background: linear-gradient(135deg, #f23030 0%, #ff4d4f 100%) !important;
  border: none !important;
  padding: 12px 36px !important;
  font-size: 15px !important;
  font-weight: 500 !important;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(242, 48, 48, 0.3);
}

.cancel-order-box .el-button--primary:hover {
  background: linear-gradient(135deg, #ff4d4f 0%, #ff6b6b 100%) !important;
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(242, 48, 48, 0.4);
}

.cancel-order-box .el-button--default {
  padding: 12px 36px !important;
  font-size: 15px !important;
  border-radius: 8px;
  border: 1px solid #ddd;
}

.cancel-order-box .el-button--default:hover {
  border-color: #f23030 !important;
  color: #f23030 !important;
}

/* 取消原因选择器样式 */
.cancel-reason-container {
  padding: 15px 20px 5px;
  width: 950px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.cancel-tip {
  background-color: #fff7e6;
  padding: 12px 15px;
  margin-bottom: 20px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
  max-width: 800px;
  box-sizing: border-box;
}

.cancel-tip span {
  font-size: 13px;
  color: #666;
  line-height: 1.6;
}

.reason-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  width: 100%;
  max-width: 800px;
  box-sizing: border-box;
}

.reason-item {
  padding: 15px 20px;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
  background-color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 45px;
}

.reason-item span {
  font-size: 14px;
  color: #333;
  word-break: break-all;
}

.reason-item:hover {
  border-color: #f23030;
  color: #f23030;
  background-color: #fff5f5;
}

.reason-item.selected {
  border: 2px solid #f23030;
  background-color: #fff5f5;
}

.reason-item.selected span {
  color: #f23030;
  font-weight: 500;
}

.check-icon {
  position: absolute;
  right: 8px;
  bottom: 8px;
  color: #f23030;
  font-size: 14px;
}

/* 覆盖对话框默认样式 */
.cancel-order-box {
  width: 1000px !important;
  max-width: 95vw !important;
}

.cancel-order-box .el-message-box__content {
  padding: 0;
  width: 100%;
}

.cancel-order-box .el-message-box__footer {
  padding: 20px !important;
  justify-content: center;
  gap: 15px;
}

.cancel-order-box .el-button--primary {
  background: linear-gradient(135deg, #f23030 0%, #ff4d4f 100%) !important;
  border: none !important;
  padding: 12px 40px !important;
  font-size: 15px !important;
  font-weight: 500 !important;
  border-radius: 6px;
  box-shadow: 0 4px 12px rgba(242, 48, 48, 0.3);
}

.cancel-order-box .el-button--primary:hover {
  background: linear-gradient(135deg, #ff4d4f 0%, #ff6b6b 100%) !important;
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(242, 48, 48, 0.4);
}

.cancel-order-box .el-button--default {
  padding: 12px 40px !important;
  font-size: 15px !important;
  border-radius: 6px;
  border: 1px solid #ddd;
}

.cancel-order-box .el-button--default:hover {
  border-color: #f23030;
  color: #f23030;
}
</style>
