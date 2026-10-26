<template>
  <!-- 动态背景 -->
  <VitalityBackground />

  <div class="order-checkout-page">
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

    <div class="main-wrapper">
      <div class="main-content">
        <div class="checkout-header">
          <h1>商品结算</h1>
          <span class="back-link" @click="goBack">返回</span>
        </div>

        <div class="checkout-container">
          <div class="checkout-left">
            <div class="section-title">收货人信息</div>
            <div class="address-section">
              <div class="address-list" v-if="addressList.length > 0">
                <div 
                  class="address-card" 
                  v-for="item in addressList" 
                  :key="item.id" 
                  :class="{ 'selected': selectedAddressId === item.id, 'default-address': item.isDefault }"
                  @click="selectAddress(item.id)"
                >
                  <div class="address-info">
                    <div class="address-top">
                      <span class="receiver-name">{{ item.receiver }}</span>
                      <span class="receiver-phone">{{ item.phone }}</span>
                      <span class="default-tag" v-if="item.isDefault">默认</span>
                    </div>
                    <div class="address-detail">
                      {{ item.province }}{{ item.city }}{{ item.district }}{{ item.detail }}
                    </div>
                  </div>
                  <div class="address-selector" v-if="selectedAddressId === item.id">
                    <el-icon><Check /></el-icon>
                  </div>
                </div>
              </div>
              <div class="empty-address" v-else>
                <div class="empty-text">暂无收货地址</div>
                <button class="add-addr-btn" @click="goToAddress">添加地址</button>
              </div>
              <div class="manage-address" v-if="addressList.length > 0">
                <button class="manage-btn" @click="goToAddress">管理收货地址</button>
              </div>
            </div>

            <div class="section-title">订单信息</div>
            <div class="order-goods-section">
              <div class="goods-list">
                <div class="goods-item" v-for="item in orderItems" :key="item.goodsId">
                  <div class="goods-image">
                    <img v-if="item.bookImg" :src="convertToExternalUrl(item.bookImg)" :alt="item.bookName" />
                    <div v-else class="placeholder-img">商品图片</div>
                  </div>
                  <div class="goods-info">
                    <div class="goods-name">{{ item.bookName }}</div>
                    <div class="goods-spec">作者：{{ item.author }}</div>
                  </div>
                  <div class="goods-price">
                    <div class="price">¥{{ item.price }}</div>
                    <div class="original-price" v-if="item.originalPrice">¥{{ item.originalPrice }}</div>
                  </div>
                  <div class="goods-quantity">x{{ item.num }}</div>
                  <div class="goods-subtotal">¥{{ (item.price * item.num).toFixed(2) }}</div>
                </div>
              </div>
            </div>
          </div>

          <div class="checkout-right">
            <div class="summary-card">
              <div class="summary-title">订单结算</div>
              <div class="summary-body">
                <div class="summary-row">
                  <span class="label">商品总额</span>
                  <span class="value">¥{{ totalPrice }}</span>
                </div>
                <div class="summary-row">
                  <span class="label">运费</span>
                  <span class="value">¥0.00</span>
                </div>
                <div class="divider"></div>
                <div class="summary-row total-row">
                  <span class="label">应付金额</span>
                  <span class="value total-price">¥{{ totalPrice }}</span>
                </div>
              </div>
              <button 
                class="submit-btn" 
                @click="submitOrder"
                :disabled="submitting || !selectedAddressId"
              >
                {{ submitting ? '提交中...' : '提交订单' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

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
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { UserFilled, Check } from '@element-plus/icons-vue'
import request from './request'
import { convertToExternalUrl } from './utils/imageUtils'

const router = useRouter()
const route = useRoute()

const userInfo = ref(null)
const nickName = ref('')
const avatarUrl = ref('')
const addressList = ref([])
const selectedAddressId = ref(null)
const orderItems = ref([])
const submitting = ref(false)
const isClearCart = ref(true)

const totalPrice = computed(() => {
  if (!orderItems.value || orderItems.value.length === 0) return '0.00'
  const total = orderItems.value.reduce((sum, item) => {
    return sum + (parseFloat(item.price) * item.num)
  }, 0)
  return total.toFixed(2)
})

const handleLogout = () => {
  localStorage.removeItem('userInfo')
  localStorage.removeItem('token')
  router.push('/')
}

const goToProfile = () => {
  router.push('/profile')
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

const goToOrderList = () => {
  router.push('/order-list')
}

const goBack = () => {
  router.back()
}

const getAddressList = async () => {
  try {
    const result = await request('/address/list')
    if (result && result.code === 200 && result.data) {
      addressList.value = result.data
      const defaultAddr = result.data.find(addr => addr.isDefault)
      if (defaultAddr) {
        selectedAddressId.value = defaultAddr.id
      } else if (result.data.length > 0) {
        selectedAddressId.value = result.data[0].id
      }
    }
  } catch (error) {
    console.error('获取地址列表失败:', error)
  }
}

const selectAddress = (id) => {
  selectedAddressId.value = id
}

const submitOrder = async () => {
  if (!selectedAddressId.value) {
    ElMessage.warning('请选择收货地址')
    return
  }

  submitting.value = true
  try {
    const orderData = {
      totalPrice: totalPrice.value,
      addressId: selectedAddressId.value,
      isClearCart: isClearCart.value,
      orderItemDTOList: orderItems.value.map(item => ({
        goodsId: item.goodsId,
        num: item.num
      }))
    }

    const result = await request('/order/create', {
      method: 'POST',
      body: JSON.stringify(orderData)
    })

    if (result && result.code === 200) {
      ElMessage.success('订单创建成功')
      // 如果清空购物车，需要刷新购物车数量
      if (isClearCart.value) {
        window.dispatchEvent(new Event('cart-updated'))
      }
      // 获取订单 ID（后端返回 Long 型订单 ID）
      const orderId = result.data
      console.log('订单创建成功，订单 ID:', orderId, '类型:', typeof orderId, '返回数据:', result)
      // 跳转到支付页面
      router.push({
        path: '/payment',
        query: {
          orderId: orderId,
          totalPrice: totalPrice.value
        }
      })
    } else {
      ElMessage.error(result.message || '创建订单失败')
    }
  } catch (error) {
    console.error('创建订单失败:', error)
    ElMessage.error('创建订单失败')
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  const stored = localStorage.getItem('userInfo')
  if (stored) {
    userInfo.value = JSON.parse(stored)
    nickName.value = userInfo.value.nickName || '用户'
    avatarUrl.value = userInfo.value.avatar || ''
  } else {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  if (route.query.type === 'cart') {
    isClearCart.value = true
    try {
      const result = await request('/shopping-cart/list')
      if (result && result.code === 200 && result.data && result.data.items) {
        orderItems.value = result.data.items.map(item => ({
          goodsId: item.goodsId,
          bookName: item.bookName,
          bookImg: item.bookImg,
          author: item.author,
          price: item.price,
          originalPrice: item.originalPrice,
          num: item.num
        }))
      }
    } catch (error) {
      console.error('获取购物车数据失败:', error)
    }
  } else if (route.query.goodsId && route.query.num) {
    isClearCart.value = false
    try {
      const result = await request(`/goods/${route.query.goodsId}`)
      if (result && result.code === 200 && result.data) {
        const goods = result.data
        orderItems.value = [{
          goodsId: goods.id,
          bookName: goods.bookName,
          bookImg: goods.bookImg,
          author: goods.author,
          price: goods.price,
          originalPrice: goods.originalPrice,
          num: parseInt(route.query.num)
        }]
      }
    } catch (error) {
      console.error('获取商品详情失败:', error)
    }
  }

  await getAddressList()
})
</script>

<style scoped>
.order-checkout-page {
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
  width: 1200px;
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
  color: #999;
  font-size: 14px;
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.nav-item {
  font-size: 14px;
  color: #333;
}

.nav-item a {
  color: #333;
  text-decoration: none;
}

.nav-item a:hover {
  color: #2563eb;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 10px;
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

.main-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.main-content {
  width: 1200px;
  margin: 30px auto;
  flex: 1;
}

.checkout-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.checkout-header h1 {
  font-size: 24px;
  color: #333;
  font-weight: 600;
  margin: 0;
}

.back-link {
  color: #999;
  font-size: 14px;
  cursor: pointer;
  transition: color 0.3s;
}

.back-link:hover {
  color: #2563eb;
}

.checkout-container {
  display: flex;
  gap: 20px;
  background-color: #f5f5f5;
}

.checkout-left {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 15px;
  padding-left: 10px;
  border-left: 3px solid #2563eb;
}

.address-section {
  background-color: #fff;
  padding: 20px;
  border-radius: 8px;
}

.address-list {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 15px;
}

.address-card {
  border: 2px solid #e5e5e5;
  border-radius: 8px;
  padding: 15px;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
}

.address-card:hover {
  border-color: #2563eb;
}

.address-card.selected {
  border-color: #2563eb;
  background-color: #f0f6ff;
}

.address-card.default-address::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  border-top: 25px solid #2563eb;
  border-right: 25px solid transparent;
}

.address-top {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.receiver-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.receiver-phone {
  font-size: 14px;
  color: #666;
}

.default-tag {
  background-color: #2563eb;
  color: #fff;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
}

.address-detail {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
}

.address-selector {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 24px;
  color: #2563eb;
}

.empty-address {
  text-align: center;
  padding: 40px;
}

.empty-text {
  color: #999;
  font-size: 14px;
  margin-bottom: 20px;
}

.add-addr-btn {
  padding: 10px 30px;
  background-color: #dbeafe;
  color: #2563eb;
  border: none;
  border-radius: 25px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.add-addr-btn:hover {
  background-color: #c7d9f5;
}

.manage-address {
  margin-top: 15px;
  text-align: center;
}

.manage-btn {
  padding: 8px 20px;
  background-color: #fff;
  color: #2563eb;
  border: 1px solid #2563eb;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.3s;
}

.manage-btn:hover {
  background-color: #2563eb;
  color: #fff;
}

.order-goods-section {
  background-color: #fff;
  padding: 20px;
  border-radius: 8px;
}

.goods-list {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.goods-item {
  display: flex;
  align-items: center;
  padding: 20px 0;
  border-bottom: 1px solid #f5f5f5;
}

.goods-item:last-child {
  border-bottom: none;
}

.goods-image {
  width: 80px;
  height: 80px;
  flex-shrink: 0;
  border-radius: 8px;
  overflow: hidden;
  background-color: #f5f5f5;
}

.goods-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.placeholder-img {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #e5e5e5;
  color: #999;
  font-size: 12px;
}

.goods-info {
  flex: 1;
  margin-left: 20px;
}

.goods-name {
  font-size: 15px;
  color: #333;
  font-weight: 500;
  margin-bottom: 8px;
}

.goods-spec {
  font-size: 13px;
  color: #999;
}

.goods-price {
  width: 120px;
  text-align: right;
}

.goods-price .price {
  font-size: 16px;
  color: #2563eb;
  font-weight: 600;
}

.goods-price .original-price {
  font-size: 13px;
  color: #999;
  text-decoration: line-through;
}

.goods-quantity {
  width: 80px;
  text-align: center;
  color: #666;
  font-size: 14px;
}

.goods-subtotal {
  width: 100px;
  text-align: right;
  font-size: 16px;
  color: #2563eb;
  font-weight: 600;
}

.checkout-right {
  width: 320px;
  flex-shrink: 0;
}

.summary-card {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  position: sticky;
  top: 20px;
}

.summary-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 2px solid #f5f5f5;
}

.summary-body {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
}

.summary-row .label {
  color: #666;
}

.summary-row .value {
  color: #333;
  font-weight: 500;
}

.divider {
  height: 1px;
  background-color: #f5f5f5;
  margin: 5px 0;
}

.summary-row.total-row {
  font-size: 16px;
  margin-top: 10px;
}

.summary-row.total-row .label {
  font-weight: 600;
}

.summary-row.total-row .value.total-price {
  font-size: 24px;
  color: #2563eb;
  font-weight: 700;
}

.submit-btn {
  width: 100%;
  margin-top: 20px;
  padding: 15px;
  background-color: #2563eb;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 18px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.submit-btn:hover:not(:disabled) {
  background-color: #1d4ed8;
}

.submit-btn:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
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
  gap: 30px;
  margin-bottom: 15px;
}

.footer-links a {
  color: #666;
  text-decoration: none;
  font-size: 14px;
}

.footer-links a:hover {
  color: #2563eb;
}

.user-popover {
  padding: 0;
}

.user-menu {
  display: flex;
  flex-direction: column;
}

.menu-item {
  padding: 12px 20px;
  cursor: pointer;
  color: #333;
  font-size: 14px;
  transition: background-color 0.3s;
}

.menu-item:hover {
  background-color: #f5f5f5;
}

.menu-item a {
  color: #333;
  text-decoration: none;
  display: block;
}

.menu-item a:hover {
  color: #2563eb;
}
</style>
