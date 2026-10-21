<template>
  <!-- 动态背景 -->
  <VitalityBackground />

  <div class="order-detail-container">
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
     
      <!-- 右侧订单详情 -->
      <div class="order-detail-content">
        <!-- 订单状态卡片 -->
        <div class="order-status-card">
          <div class="status-left">
            <div class="status-icon" :class="getStatusIconClass(order.status)">
              <el-icon v-if="order.status === 1"><Clock /></el-icon>
              <el-icon v-else-if="order.status === 2"><Van /></el-icon>
              <el-icon v-else-if="order.status === 3"><Position /></el-icon>
              <el-icon v-else-if="order.status === 4"><CircleCheck /></el-icon>
              <el-icon v-else-if="order.status === 5"><CircleClose /></el-icon>
            </div>
            <div class="status-text">
              <div class="status-title" :style="{ color: getStatusColor(order.status) }">
                {{ getStatusText(order.status) }}
              </div>
              <div class="status-desc">
                <span v-if="order.status === 1">请尽快完成支付</span>
                <span v-else-if="order.status === 2">商家正在准备商品</span>
                <span v-else-if="order.status === 3">商品已发出，请耐心等待</span>
                <span v-else-if="order.status === 4">交易已完成</span>
                <span v-else-if="order.status === 5">订单已取消</span>
              </div>
            </div>
          </div>
          <div class="status-right">
            <div class="action-buttons">
              <!-- 买家视角 -->
              <template v-if="!isSeller">
                <button v-if="order.status === 1" class="btn-primary" @click="handlePay">立即支付</button>
                <button v-if="order.status === 1" class="btn-cancel" @click="handleCancelOrder">取消订单</button>
                <button v-if="order.status === 3" class="btn-confirm" @click="handleConfirmReceive">确认收货</button>
                <button v-if="order.status === 4 && filteredOrderItems.length > 0" class="btn-review" @click="handleReview">去评价</button>
              </template>
              <!-- 卖家视角 -->
              <template v-else>
                <button v-if="order.status === 1" class="btn-cancel-red" @click="handleCancelOrder">取消订单</button>
                <button v-if="order.status === 2" class="btn-primary" @click="handleShip">已发货</button>
              </template>
              <button class="btn-secondary" @click="goBack">返回订单列表</button>
            </div>
          </div>
        </div>

        <!-- 进度条 -->
        <div class="order-progress" v-if="order.status !== 5">
          <div class="progress-steps">
            <div class="progress-step" :class="{ active: order.status >= 1 && order.status < 4, completed: order.status >= 4 || order.status === 6 }">
              <div class="step-icon">
                <el-icon v-if="order.status >= 1"><Document /></el-icon>
                <el-icon v-else><Document /></el-icon>
              </div>
              <div class="step-text">下单</div>
            </div>
            <div class="progress-line" :class="{ active: order.status >= 2 && order.status < 4, completed: order.status >= 4 || order.status === 6 }"></div>
            <div class="progress-step" :class="{ active: order.status >= 2 && order.status < 4, completed: order.status >= 4 || order.status === 6 }">
              <div class="step-icon">
                <el-icon v-if="order.status >= 2"><Van /></el-icon>
                <el-icon v-else><Van /></el-icon>
              </div>
              <div class="step-text">配货</div>
            </div>
            <div class="progress-line" :class="{ active: order.status >= 3 && order.status < 4, completed: order.status >= 4 || order.status === 6 }"></div>
            <div class="progress-step" :class="{ active: order.status >= 3 && order.status < 4, completed: order.status >= 4 || order.status === 6 }">
              <div class="step-icon">
                <el-icon v-if="order.status >= 3"><Position /></el-icon>
                <el-icon v-else><Position /></el-icon>
              </div>
              <div class="step-text">运输</div>
            </div>
            <div class="progress-line" :class="{ active: order.status >= 4, completed: order.status >= 4 || order.status === 6 }"></div>
            <div class="progress-step" :class="{ active: order.status >= 4, completed: order.status >= 4 || order.status === 6 }">
              <div class="step-icon">
                <el-icon v-if="order.status >= 4"><CircleCheck /></el-icon>
                <el-icon v-else><CircleCheck /></el-icon>
              </div>
              <div class="step-text">完成</div>
            </div>
          </div>
        </div>

        <!-- 订单信息卡片 -->
        <div class="order-info-card">
          <div class="info-section">
            <div class="section-title">订单信息</div>
            <div class="info-content">
              <div class="info-row">
                <span class="info-label">订单编号：</span>
                <span class="info-value">{{ order.id }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">下单时间：</span>
                <span class="info-value">{{ formatTime(order.createTime) }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">支付方式：</span>
                <span class="info-value">{{ getPayTypeText(order.payType) }}</span>
              </div>
              <div class="info-row" v-if="order.payTime">
                <span class="info-label">支付时间：</span>
                <span class="info-value">{{ formatTime(order.payTime) }}</span>
              </div>
            </div>
          </div>

          <div class="info-section">
            <div class="section-title">配送信息</div>
            <div class="info-content">
              <div class="info-row">
                <span class="info-label">收货人：</span>
                <span class="info-value">{{ order.receiverName }} {{ order.receiverPhone }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">收货地址：</span>
                <span class="info-value">{{ order.receiverAddress }}</span>
              </div>
              <div class="info-row" v-if="order.deliveryTime">
                <span class="info-label">发货时间：</span>
                <span class="info-value">{{ formatTime(order.deliveryTime) }}</span>
              </div>
              <div class="info-row" v-if="order.receiveTime">
                <span class="info-label">收货时间：</span>
                <span class="info-value">{{ formatTime(order.receiveTime) }}</span>
              </div>
            </div>
          </div>

          <div class="info-section" v-if="order.status === 5 && order.cancelReason">
            <div class="section-title">取消信息</div>
            <div class="info-content">
              <div class="info-row">
                <span class="info-label">取消原因：</span>
                <span class="info-value">{{ order.cancelReason }}</span>
              </div>
              <div class="info-row" v-if="order.cancelTime">
                <span class="info-label">取消时间：</span>
                <span class="info-value">{{ formatTime(order.cancelTime) }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 商品列表 -->
        <div class="product-list-card">
          <div class="product-list-header">
            <span class="product-list-title">商品清单</span>
            <span class="product-count">共 {{ filteredOrderItems?.length || 0 }} 件商品</span>
          </div>
          <div class="product-list">
            <div 
              v-for="(item, index) in filteredOrderItems" 
              :key="index"
              class="product-item"
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
              <div class="product-price">
                <div class="price-row">
                  <span class="price-label">单价：</span>
                  <span class="price-value">¥{{ (item.price / item.num).toFixed(2) }}</span>
                </div>
                <div class="price-row">
                  <span class="price-label">小计：</span>
                  <span class="price-total">¥{{ item.price.toFixed(2) }}</span>
                </div>
              </div>
              <div class="product-status">
                <span v-if="item.goodStatus === 1" class="status-on-sale">在售</span>
                <span v-else class="status-off-sale">已下架</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 金额信息 -->
        <div class="amount-card">
          <div class="amount-row">
            <span class="amount-label">商品总额：</span>
            <span class="amount-value">¥{{ order.totalPrice.toFixed(2) }}</span>
          </div>
          <div class="amount-row" v-if="order.freightAmount > 0">
            <span class="amount-label">运费：</span>
            <span class="amount-value">¥{{ order.freightAmount.toFixed(2) }}</span>
          </div>
          <div class="amount-row" v-if="order.couponAmount > 0">
            <span class="amount-label">优惠券抵扣：</span>
            <span class="amount-value discount">-¥{{ order.couponAmount.toFixed(2) }}</span>
          </div>
          <div class="amount-row total">
            <span class="amount-label">实付款：</span>
            <span class="amount-total">¥{{ order.totalPrice.toFixed(2) }}</span>
          </div>
        </div>

        <!-- 服务支持 -->
        <div class="service-card">
          <div class="service-title">服务支持</div>
          <div class="service-items">
            <div class="service-item">
              <el-icon class="service-icon"><Service /></el-icon>
              <span class="service-text">7 天无理由退货</span>
            </div>
            <div class="service-item">
              <el-icon class="service-icon"><Lock /></el-icon>
              <span class="service-text">正品保障</span>
            </div>
            <div class="service-item">
              <el-icon class="service-icon"><Clock /></el-icon>
              <span class="service-text">极速退款</span>
            </div>
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
import { ref, computed, onMounted, h } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  UserFilled, Clock, Van, Position, CircleCheck, CircleClose, 
  Document, Service, Lock 
} from '@element-plus/icons-vue'
import request from './request'
import { convertToExternalUrl } from './utils/imageUtils'

const route = useRoute()
const router = useRouter()

// 判断是买家还是卖家视角
const isSeller = computed(() => route.query.tab === 'sold')

const userInfo = ref(null)
const nickName = ref('')
const avatarUrl = ref('')

const order = ref({
  id: '',
  status: 1,
  totalPrice: 0,
  payType: null,
  receiverName: '',
  receiverPhone: '',
  receiverAddress: '',
  createTime: null,
  payTime: null,
  deliveryTime: null,
  receiveTime: null,
  cancelTime: null,
  cancelReason: '',
  freightAmount: 0,
  couponAmount: 0,
  orderItemList: []
})

// 计算属性：过滤掉已经评价过的商品项
const filteredOrderItems = computed(() => {
  if (!order.value.orderItemList) return []
  return order.value.orderItemList
})

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
    if (result && result.code === 200 && result.data) {
      order.value = result.data
    } else {
      ElMessage.error('获取订单详情失败')
    }
  } catch (error) {
    console.error('获取订单详情失败:', error)
    ElMessage.error('获取订单详情失败')
  }
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    1: '待付款',
    2: '待发货',
    3: '待收货',
    4: '已完成',
    5: '已取消',
    6: '已评价'
  }
  return statusMap[status] || '未知状态'
}

// 获取状态颜色
const getStatusColor = (status) => {
  const colorMap = {
    1: '#2563eb',
    2: '#409EFF',
    3: '#67C23A',
    4: '#909399',
    5: '#F56C6C',
    6: '#19be6b'
  }
  return colorMap[status] || '#909399'
}

// 获取状态图标类名
const getStatusIconClass = (status) => {
  const classMap = {
    1: 'status-pending',
    2: 'status-shipping',
    3: 'status-delivering',
    4: 'status-completed',
    5: 'status-cancelled',
    6: 'status-commented'
  }
  return classMap[status] || ''
}

// 获取支付方式文本
const getPayTypeText = (payType) => {
  if (payType === 1) return '支付宝支付'
  if (payType === 2) return '余额支付'
  return '在线支付'
}

// 格式化时间
const formatTime = (timestamp) => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 返回订单列表
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

// 立即支付
const handlePay = () => {
  router.push({
    path: '/payment',
    query: {
      orderId: order.value.id,
      totalPrice: order.value.totalPrice
    }
  })
}

// 取消订单
const handleCancelOrder = () => {
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

  const cancelReasons = isSeller.value ? sellerCancelReasons : buyerCancelReasons

  const selectedReasons = []

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
        cancelReasons.map((reason) =>
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
          orderId: order.value.id,
          status: 5,
          cancelReason: selectedReasons.join(',')
        })
      })
      ElMessage.success('订单取消成功')
      getOrderDetail(order.value.id)
    } catch (error) {
      console.error('取消订单失败:', error)
      ElMessage.error('取消订单失败')
    }
  }).catch(() => {
    ElMessage.info('已取消')
  })
}

// 确认收货
const handleConfirmReceive = () => {
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
          orderId: order.value.id,
          status: 4
        })
      })
      ElMessage.success('确认收货成功')
      getOrderDetail(order.value.id)
    } catch (error) {
      console.error('确认收货失败:', error)
      ElMessage.error('确认收货失败')
    }
  }).catch(() => {
    ElMessage.info('已取消')
  })
}

// 评价晒单
const handleReview = () => {
  const unCommentedGoodsIds = filteredOrderItems.value.map(item => item.goodsId)

  router.push({
    path: '/comment',
    query: {
      orderId: order.value.id,
      goodsIds: unCommentedGoodsIds.join(',')
    }
  })
}

// 卖家发货
const handleShip = () => {
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
          orderId: order.value.id,
          status: 3
        })
      })
      ElMessage.success('发货成功')
      getOrderDetail(order.value.id)
    } catch (error) {
      console.error('发货失败:', error)
      ElMessage.error('发货失败，请重试')
    }
  }).catch(() => {
    ElMessage.info('已取消')
  })
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
.order-detail-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: transparent;
  position: relative;
  z-index: 1;
}

.top-nav {
  background: transparent; position: relative; z-index: 1;
  padding: 10px 0;
  border-bottom: 1px solid var(--color-neutral-100);
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
  color: var(--color-primary);
}

.nav-item a {
  color: #666;
  text-decoration: none;
}

.nav-item a:hover {
  color: var(--color-primary);
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
  background-color: var(--color-bg-tertiary);
}

.main-content {
  max-width: 1200px;
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
  color: var(--color-primary);
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
  background-color: var(--color-primary);
  color: #fff;
}

.sidebar-title {
  font-size: 16px;
  color: #666;
  font-weight: bold;
}

.order-detail-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

/* 订单状态卡片 */
.order-status-card {
  background-color: #fff;
  border-radius: 8px;
  padding: 25px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.status-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.status-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  color: #fff;
}

.status-pending {
  background-color: var(--color-primary);
}

.status-shipping {
  background-color: #409EFF;
}

.status-delivering {
  background-color: #67C23A;
}

.status-completed {
  background-color: #909399;
}

.status-cancelled {
  background-color: #F56C6C;
}

.status-commented {
  background-color: #19be6b;
}

.status-text {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.status-title {
  font-size: 24px;
  font-weight: bold;
}

.status-desc {
  font-size: 14px;
  color: #999;
}

.status-right {
  display: flex;
  gap: 10px;
}

.action-buttons {
  display: flex;
  gap: 10px;
}

.btn-primary {
  padding: 10px 25px;
  background-color: var(--color-primary);
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.btn-primary:hover {
  background-color: #1d4ed8;
}

.btn-cancel {
  padding: 10px 25px;
  background-color: #fff;
  color: #666;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.btn-cancel:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}

.btn-cancel-red {
  padding: 10px 25px;
  background-color: #F56C6C;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.btn-cancel-red:hover {
  background-color: #e54545;
}

.btn-confirm {
  padding: 10px 25px;
  background-color: #67C23A;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.btn-confirm:hover {
  background-color: #55a130;
}

.btn-review {
  padding: 10px 25px;
  background-color: var(--color-primary);
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.btn-review:hover {
  background-color: #1d4ed8;
}

.btn-secondary {
  padding: 10px 25px;
  background-color: #fff;
  color: #666;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.btn-secondary:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}

/* 进度条 */
.order-progress {
  background-color: #fff;
  border-radius: 8px;
  padding: 30px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.progress-steps {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.progress-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  flex: 0 0 auto;
}

.step-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: #e0e0e0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: #999;
  transition: all 0.3s;
}

.progress-step.active .step-icon {
  background-color: var(--color-primary);
  color: #fff;
}

.progress-step.completed .step-icon {
  background-color: #67C23A;
  color: #fff;
}

.step-text {
  font-size: 14px;
  color: #999;
}

.progress-step.active .step-text {
  color: var(--color-primary);
  font-weight: bold;
}

.progress-step.completed .step-text {
  color: #67C23A;
  font-weight: bold;
}

.progress-line {
  flex: 1;
  height: 2px;
  background-color: #e0e0e0;
  margin: 0 10px;
  transition: all 0.3s;
}

.progress-line.active {
  background-color: var(--color-primary);
}

.progress-line.completed {
  background-color: #67C23A;
}

/* 订单信息卡片 */
.order-info-card {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.info-section {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.section-title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  padding-bottom: 10px;
  border-bottom: 1px solid #e0e0e0;
}

.info-content {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.info-row {
  display: flex;
  gap: 10px;
  font-size: 14px;
}

.info-label {
  color: #999;
  min-width: 80px;
}

.info-value {
  color: #333;
  flex: 1;
}

/* 商品列表 */
.product-list-card {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.product-list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e0e0e0;
}

.product-list-title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
}

.product-count {
  font-size: 14px;
  color: #999;
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
}

.product-image {
  width: 100px;
  height: 100px;
  flex-shrink: 0;
  border-radius: 4px;
  overflow: hidden;
  background-color: var(--color-bg-tertiary);
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

.product-price {
  display: flex;
  flex-direction: column;
  gap: 10px;
  align-items: flex-end;
  min-width: 120px;
}

.price-row {
  display: flex;
  gap: 10px;
  font-size: 14px;
}

.price-label {
  color: #999;
}

.price-value {
  color: var(--color-primary);
  font-weight: bold;
}

.price-total {
  color: var(--color-primary);
  font-weight: bold;
  font-size: 16px;
}

.product-status {
  min-width: 60px;
}

.status-on-sale {
  display: inline-block;
  padding: 4px 10px;
  background-color: #e6f7e6;
  color: #67C23A;
  border-radius: 4px;
  font-size: 12px;
}

.status-off-sale {
  display: inline-block;
  padding: 4px 10px;
  background-color: var(--color-bg-tertiary);
  color: #999;
  border-radius: 4px;
  font-size: 12px;
}

/* 金额信息 */
.amount-card {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.amount-row {
  display: flex;
  justify-content: flex-end;
  gap: 20px;
  padding: 8px 0;
  font-size: 14px;
}

.amount-label {
  color: #999;
  min-width: 100px;
  text-align: right;
}

.amount-value {
  color: #333;
  font-weight: bold;
  min-width: 100px;
  text-align: right;
}

.amount-value.discount {
  color: #F56C6C;
}

.amount-total {
  color: var(--color-primary);
  font-size: 20px;
  font-weight: bold;
}

.amount-row.total {
  margin-top: 10px;
  padding-top: 15px;
  border-top: 1px solid #e0e0e0;
}

/* 服务支持 */
.service-card {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.service-title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #e0e0e0;
}

.service-items {
  display: flex;
  gap: 30px;
}

.service-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #666;
}

.service-icon {
  color: var(--color-primary);
  font-size: 18px;
}

/* 底部 */
.footer {
  background-color: var(--color-bg-tertiary);
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
  color: var(--color-primary);
}
</style>
