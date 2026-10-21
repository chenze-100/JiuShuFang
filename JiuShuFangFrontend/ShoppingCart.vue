<template>
  <!-- 动态背景 -->
  <VitalityBackground />

  <div class="shopping-cart-page">
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
    <div class="main-wrapper">
      <div class="main-content">
        <div class="cart-header">
          <h1>我的购物车</h1>
          <span class="back-home-link" @click="goBackHome">返回首页</span>
        </div>

        <div class="cart-container" v-if="cartItems && cartItems.length > 0">
          <!-- 购物车商品列表 -->
          <div class="cart-items">
            <div class="cart-item" v-for="item in cartItems" :key="item.id">
              <div class="item-image" @click="goToProductDetail(item.goodsId)">
                <img v-if="item.bookImg" :src="convertToExternalUrl(item.bookImg)" :alt="item.bookName" style="cursor: pointer;" />
                <div v-else class="placeholder-img" style="cursor: pointer;">商品图片</div>
              </div>

              <div class="item-info">
                <div class="item-name" style="cursor: pointer;" @click="goToProductDetail(item.goodsId)">{{ item.bookName }}</div>
                <div class="item-price">
                  <span class="price-label">单价</span>
                  <span class="price-value">¥{{ item.price }}</span>
                </div>
              </div>

              <div class="item-quantity">
                <div class="quantity-control">
                  <button
                    class="quantity-btn"
                    @click="decreaseQuantity(item)"
                    :disabled="item.num <= 1"
                  >
                    -
                  </button>
                  <span class="quantity-value">{{ item.num }}</span>
                  <button
                    class="quantity-btn"
                    @click="increaseQuantity(item)"
                  >
                    +
                  </button>
                </div>
              </div>

              <div class="item-subtotal">
                <span class="subtotal-label">小计</span>
                <span class="subtotal-value">¥{{ item.totalPrice }}</span>
              </div>

              <div class="item-actions">
                <button class="delete-btn" @click="deleteItem(item.id)">删除</button>
              </div>
            </div>
          </div>

          <!-- 结算栏 -->
          <div class="cart-summary">
            <div class="summary-row">
              <span class="summary-label">商品总价：</span>
              <span class="summary-price">¥{{ totalPrice }}</span>
            </div>
            <div class="summary-actions">
              <button class="action-btn continue-btn" @click="goBackHome">继续购物</button>
              <button class="action-btn checkout-btn" @click="goToCheckout">去结算</button>
            </div>
          </div>
        </div>

        <!-- 空购物车提示 -->
        <div class="empty-cart" v-else>
          <div class="empty-cart-icon">🛒</div>
          <div class="empty-cart-text">购物车还是空的</div>
          <button class="go-shopping-btn" @click="goBackHome">去逛逛</button>
        </div>
      </div>
    </div>

    <!-- 底部 -->
    <div class="footer">
      <div class="footer-content">
        <div class="footer-links">
          <span><a href="#">关于我们</a></span>
          <span><a href="#">联系我们</a></span>
          <span><a href="#">商家入驻</a></span>
          <span><a href="#">友情链接</a></span>
          <span><a href="#">帮助中心</a></span>
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
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { UserFilled } from '@element-plus/icons-vue'
import request from './request'
import { convertToExternalUrl } from './utils/imageUtils'

const router = useRouter()

const userInfo = ref(null)
const nickName = ref('')
const avatarUrl = ref('')

const cartItems = ref([])
const loading = ref(false)

// 计算总价
const totalPrice = computed(() => {
  if (!cartItems.value || cartItems.value.length === 0) return '0.00'

  const total = cartItems.value.reduce((sum, item) => {
    return sum + (parseFloat(item.totalPrice) || 0)
  }, 0)

  return total.toFixed(2)
})

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

const goBackHome = () => {
  router.push('/home')
}

const goToProductDetail = (goodsId) => {
  router.push(`/product?id=${goodsId}`)
}

const goToCheckout = () => {
  if (!cartItems.value || cartItems.value.length === 0) {
    ElMessage.warning('购物车为空')
    return
  }
  router.push({
    path: '/order-checkout',
    query: {
      type: 'cart'
    }
  })
}

// 获取购物车列表
const getCartList = async () => {
  if (loading.value) return

  loading.value = true

  try {
    const result = await request('/shopping-cart/list')

    if (result && result.code === 200 && result.data) {
      cartItems.value = result.data.items || []
    } else {
      ElMessage.error(result.message || '获取购物车失败')
    }
  } catch (error) {
    console.error('获取购物车失败:', error)
    ElMessage.error('获取购物车失败')
  } finally {
    loading.value = false
  }
}

// 增加数量
const increaseQuantity = async (item) => {
  try {
    const result = await request('/shopping-cart/num', {
      method: 'PUT',
      body: JSON.stringify({
        cartId: item.id,
        isPlus: true
      })
    })

    if (result && result.code === 200) {
      item.num++
      item.totalPrice = (parseFloat(item.price) * item.num).toFixed(2)
      ElMessage.success('数量已更新')
    } else {
      ElMessage.error(result.message || '更新失败')
    }
  } catch (error) {
    console.error('更新数量失败:', error)
    ElMessage.error('更新失败')
  }
}

// 减少数量
const decreaseQuantity = async (item) => {
  if (item.num <= 1) return

  try {
    const result = await request('/shopping-cart/num', {
      method: 'PUT',
      body: JSON.stringify({
        cartId: item.id,
        isPlus: false
      })
    })

    if (result && result.code === 200) {
      item.num--
      item.totalPrice = (parseFloat(item.price) * item.num).toFixed(2)
      ElMessage.success('数量已更新')
    } else {
      ElMessage.error(result.message || '更新失败')
    }
  } catch (error) {
    console.error('更新数量失败:', error)
    ElMessage.error('更新失败')
  }
}

// 删除商品
const deleteItem = async (id) => {
  try {
    const result = await request(`/shopping-cart/delete/${id}`, {
      method: 'DELETE'
    })

    if (result && result.code === 200) {
      ElMessage.success('删除成功')
      cartItems.value = cartItems.value.filter(item => item.id !== id)
    } else {
      ElMessage.error(result.message || '删除失败')
    }
  } catch (error) {
    console.error('删除失败:', error)
    ElMessage.error('删除失败')
  }
}

const setUserInfo = (user) => {
  userInfo.value = user
  if (user) {
    nickName.value = user.nickName || '用户'
    avatarUrl.value = user.avatar || ''
  }
}

onMounted(async () => {
  const stored = localStorage.getItem('userInfo')
  if (stored) {
    userInfo.value = JSON.parse(stored)
    setUserInfo(userInfo.value)
    await getCartList()
  } else {
    ElMessage.warning('请先登录')
    router.push('/login')
  }
})
</script>

<style scoped>
/* === 购物车页 === */
.shopping-cart-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: transparent; position: relative; z-index: 1;
}

/* === 导航栏 === */
.top-nav {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid var(--color-neutral-100);
}

.nav-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  height: 56px;
  display: flex;
  justify-content: space-between;
  align-items: center;
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

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 10px;
  border-radius: var(--radius-pill);
  transition: background-color var(--transition-fast);
}

.user-info:hover {
  background-color: var(--color-bg-tertiary);
}

.username {
  font-size: 13px;
  color: var(--color-neutral-700);
  font-weight: 500;
}

:deep(.user-popover) {
  padding: 8px;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-lg);
}

.user-menu {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.menu-item {
  cursor: pointer;
  padding: 10px 16px;
  color: var(--color-neutral-500);
  font-size: 13px;
  border-radius: var(--radius-sm);
  transition: all var(--transition-fast);
}

.menu-item:hover {
  background-color: var(--color-bg-tertiary);
  color: var(--color-primary);
}

/* === 主内容 === */
.main-wrapper {
  display: flex;
  flex-direction: column;
  min-height: calc(100vh - 200px);
}

.main-content {
  flex: 1;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
  padding: 30px 20px;
}

.cart-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 30px;
}

.cart-header h1 {
  font-size: 24px;
  color: var(--color-neutral-700);
  margin: 0;
  font-weight: 600;
}

.back-home-link {
  color: var(--color-neutral-400);
  font-size: 14px;
  cursor: pointer;
  padding: 8px 16px;
  border-radius: var(--radius-pill);
  transition: all var(--transition-fast);
}

.back-home-link:hover {
  color: var(--color-primary);
  background-color: var(--color-primary-soft);
}

/* === 购物车容器 === */
.cart-container {
  background-color: white;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
  overflow: hidden;
  border: 1px solid var(--color-neutral-100);
}

/* === 购物车项 === */
.cart-items {
  padding: 20px;
}

.cart-item {
  display: flex;
  align-items: center;
  padding: 20px 0;
  border-bottom: 1px solid var(--color-neutral-100);
  gap: 20px;
}

.cart-item:last-child {
  border-bottom: none;
}

.item-image {
  width: 100px;
  height: 100px;
  flex-shrink: 0;
  border-radius: var(--radius-sm);
  overflow: hidden;
  background-color: var(--color-bg-tertiary);
  cursor: pointer;
  transition: transform var(--transition-fast);
}

.item-image:hover {
  transform: scale(1.05);
}

.item-image img {
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
  background-color: var(--color-neutral-100);
  color: var(--color-neutral-300);
  font-size: 12px;
}

.item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.item-name {
  font-size: 15px;
  color: var(--color-neutral-700);
  font-weight: 500;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  cursor: pointer;
  transition: color var(--transition-fast);
}

.item-name:hover {
  color: var(--color-primary);
}

.item-price {
  display: flex;
  align-items: center;
  gap: 10px;
}

.price-label {
  font-size: 14px;
  color: var(--color-neutral-400);
}

.price-value {
  font-size: 18px;
  color: var(--color-primary);
  font-weight: 600;
}

.item-quantity {
  width: 150px;
}

.quantity-control {
  display: flex;
  align-items: center;
  gap: 10px;
}

.quantity-btn {
  width: 32px;
  height: 32px;
  border: 1px solid var(--color-neutral-200);
  background-color: white;
  border-radius: var(--radius-sm);
  font-size: 18px;
  color: var(--color-neutral-500);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--transition-fast);
}

.quantity-btn:hover:not(:disabled) {
  border-color: var(--color-primary);
  color: var(--color-primary);
}

.quantity-btn:disabled {
  background-color: var(--color-bg-tertiary);
  color: var(--color-neutral-300);
  cursor: not-allowed;
}

.quantity-value {
  font-size: 16px;
  color: var(--color-neutral-700);
  font-weight: 600;
  min-width: 30px;
  text-align: center;
}

.item-subtotal {
  width: 120px;
  text-align: right;
}

.subtotal-label {
  display: block;
  font-size: 13px;
  color: var(--color-neutral-400);
  margin-bottom: 5px;
}

.subtotal-value {
  font-size: 20px;
  color: var(--color-primary);
  font-weight: 600;
}

.item-actions {
  width: 80px;
}

.delete-btn {
  width: 100%;
  padding: 8px 15px;
  background-color: white;
  border: 1px solid var(--color-neutral-200);
  border-radius: var(--radius-sm);
  color: var(--color-neutral-500);
  font-size: 14px;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.delete-btn:hover {
  border-color: var(--color-error);
  color: var(--color-error);
  background-color: rgba(245, 34, 45, 0.05);
}

/* === 结算栏 === */
.cart-summary {
  background: linear-gradient(135deg, var(--color-primary-soft) 0%, #ffffff 100%);
  padding: 20px 30px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid var(--color-neutral-100);
}

.summary-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.summary-label {
  font-size: 16px;
  color: var(--color-neutral-500);
  font-weight: 500;
}

.summary-price {
  font-size: 28px;
  color: var(--color-primary);
  font-weight: 700;
}

.summary-actions {
  display: flex;
  gap: 15px;
}

.action-btn {
  padding: 12px 40px;
  border: none;
  border-radius: var(--radius-md);
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition-base);
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.continue-btn {
  background-color: white;
  color: var(--color-neutral-500);
  border: 1px solid var(--color-neutral-200);
}

.continue-btn:hover {
  background-color: var(--color-bg-tertiary);
  border-color: var(--color-neutral-300);
}

.checkout-btn {
  background: var(--gradient-primary);
  color: white;
  border: none;
}

.checkout-btn:hover {
  background: var(--gradient-primary);
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
}

/* === 空购物车 === */
.empty-cart {
  background-color: white;
  border-radius: var(--radius-lg);
  padding: 80px 20px;
  text-align: center;
  box-shadow: var(--shadow-md);
  border: 1px solid var(--color-neutral-100);
}

.empty-cart-icon {
  font-size: 80px;
  margin-bottom: 20px;
  color: var(--color-neutral-200);
}

.empty-cart-text {
  font-size: 18px;
  color: var(--color-neutral-400);
  margin-bottom: 30px;
  font-weight: 500;
}

.go-shopping-btn {
  padding: 12px 40px;
  background: var(--gradient-primary);
  color: white;
  border: none;
  border-radius: var(--radius-md);
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition-base);
}

.go-shopping-btn:hover {
  background: var(--gradient-primary);
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
}

/* === 底部 === */
.footer {
  background-color: white;
  border-top: 1px solid var(--color-neutral-100);
  padding: 32px 0;
  margin-top: auto;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  text-align: center;
}

.footer-links {
  display: flex;
  justify-content: center;
  gap: 32px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.footer-links a {
  color: var(--color-neutral-400);
  text-decoration: none;
  font-size: 14px;
  transition: color var(--transition-fast);
}

.footer-links a:hover {
  color: var(--color-primary);
}

/* === Responsive === */
@media (max-width: 1024px) {
  .main-content {
    padding: 24px 16px;
  }

  .cart-item {
    gap: 16px;
  }

  .item-quantity {
    width: 120px;
  }

  .item-subtotal {
    width: 100px;
  }

  .item-actions {
    width: 70px;
  }
}

@media (max-width: 768px) {
  .nav-content {
    padding: 0 12px;
  }

  .cart-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .cart-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .item-image {
    width: 80px;
    height: 80px;
  }

  .item-info {
    width: 100%;
  }

  .cart-summary {
    flex-direction: column;
    gap: 20px;
    align-items: stretch;
    padding: 24px;
  }

  .summary-actions {
    flex-direction: column;
  }

  .action-btn {
    width: 100%;
  }
}

@media (max-width: 480px) {
  .main-content {
    padding: 16px 12px;
  }

  .cart-header h1 {
    font-size: 20px;
  }

  .item-image {
    width: 70px;
    height: 70px;
  }

  .item-name {
    font-size: 14px;
  }

  .price-value {
    font-size: 16px;
  }

  .quantity-btn {
    width: 30px;
    height: 30px;
    font-size: 16px;
  }

  .subtotal-value {
    font-size: 18px;
  }

  .action-btn {
    padding: 10px 20px;
    font-size: 15px;
  }
}
</style>
