<template>
  <!-- 动态背景 -->
  <VitalityBackground />

  <div class="payment-page">
    <!-- 顶部导航 -->
    <div class="top-nav">
      <div class="nav-content">
        <div class="nav-left">
          <GlitchText
            text="旧书坊"
            :speed="0.8"
            :enableShadows="true"
            :enableOnHover="true"
            :className="'logo'"
            @click="goHome"
          />
          <span class="nav-text">收银台</span>
        </div>
        <div class="nav-right">
          <span class="help-link"><a href="#">我的订单</a></span>
          <span class="divider">|</span>
          <span class="help-link"><a href="#">帮助中心</a></span>
        </div>
      </div>
    </div>

    <!-- 支付内容 -->
    <div class="payment-container">
      <!-- 订单信息 -->
      <div class="order-summary">
        <div class="summary-row">
          <span class="label">订单编号：</span>
          <span class="value">{{ orderId }}</span>
        </div>
        <div class="summary-row">
          <span class="label">应付金额：</span>
          <span class="amount">¥{{ totalPrice }}</span>
        </div>
      </div>

      <!-- 支付区域 -->
      <div class="payment-section">
        <div class="payment-header">
          <span class="payment-method">选择支付方式</span>
        </div>

        <div class="payment-methods">
          <!-- 支付宝支付已禁用
          <div
            class="method-card"
            :class="{ active: selectedMethod === 'alipay' }"
            @click="selectedMethod = 'alipay'"
          >
            <div class="method-icon">💳</div>
            <div class="method-info">
              <div class="method-name">支付宝支付</div>
              <div class="method-desc">使用支付宝账户快捷支付</div>
            </div>
            <div class="method-radio">
              <el-radio :model-value="selectedMethod" label="alipay"></el-radio>
            </div>
          </div>
          -->

          <div 
            class="method-card" 
            :class="{ active: selectedMethod === 'balance', disabled: balanceInsufficient }"
            @click="!balanceInsufficient && (selectedMethod = 'balance')"
          >
            <div class="method-icon">💰</div>
            <div class="method-info">
              <div class="method-name">
                余额支付
                <span class="balance-text">（当前余额：¥{{ userBalance }}）</span>
              </div>
              <div class="method-desc">
                使用账户余额支付
                <span class="insufficient-tip" v-if="balanceInsufficient">余额不足</span>
              </div>
            </div>
            <div class="method-radio">
              <el-radio :model-value="selectedMethod" label="balance" :disabled="balanceInsufficient"></el-radio>
            </div>
          </div>
        </div>

        <div class="payment-action">
          <button class="pay-btn" @click="handlePayment">确认支付</button>
        </div>
      </div>

      <!-- 支付状态 -->
      <div class="payment-status">
        <div class="status-item">
          <span class="status-label">订单状态：</span>
          <span class="status-value waiting">等待支付</span>
        </div>
        <div class="status-actions">
          <button class="action-btn back-btn" @click="goHome">返回首页</button>
        </div>
      </div>
    </div>

    <!-- 底部 -->
    <div class="footer">
      <div class="footer-content">
        <div class="footer-links">
          <a href="#">关于我们</a> |
          <a href="#">联系我们</a> |
          <a href="#">商家入驻</a> |
          <a href="#">营销中心</a> |
          <a href="#">友情链接</a>
        </div>
        <div class="footer-copyright">
          Copyright © 2024-2026 网上书城 版权所有
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElLoading } from 'element-plus'
import request from './request.js'
import GlitchText from './components/GlitchText.vue'
import VitalityBackground from './components/VitalityBackground.vue'

const router = useRouter()
const route = useRoute()

const orderId = ref('')
const totalPrice = ref('0.00')
const selectedMethod = ref('balance') // alipay 或 balance（支付宝已禁用，默认余额支付）
const userBalance = ref('0.00') // 用户余额
const balanceInsufficient = ref(false) // 余额是否不足

// 处理支付
const handlePayment = async () => {
  console.log('点击确认支付，当前状态:', {
    orderId: orderId.value,
    selectedMethod: selectedMethod.value,
    totalPrice: totalPrice.value
  })

  if (!orderId.value) {
    ElMessage.error('订单信息不完整')
    return
  }

  // if (selectedMethod.value === 'alipay') {
  //   console.log('开始支付宝支付，订单 ID:', orderId.value)
  //   // 支付宝支付
  //   await handleAlipay()
  // } else if (selectedMethod.value === 'balance') {
  if (selectedMethod.value === 'balance') {
    console.log('开始余额支付，订单 ID:', orderId.value)
    // 余额支付
    await handleBalance()
  } else {
    ElMessage.warning('请选择支付方式')
  }
}

// 查询用户余额
const getUserBalance = async () => {
  try {
    const result = await request('/user/banance', {
      method: 'GET'
    })
    
    if (result && result.code === 200) {
      userBalance.value = result.data.toFixed(2)
      // 判断余额是否充足
      balanceInsufficient.value = result.data < parseFloat(totalPrice.value)
    }
  } catch (error) {
    console.error('查询余额失败:', error)
  }
}

// 支付宝支付（已禁用）
// const handleAlipay = async () => {
//   let loadingInstance = null
//   try {
//     console.log('支付宝支付请求 URL:', `/api/pay/pc?orderId=${orderId.value}`)
//
//     // 显示加载提示
//     loadingInstance = ElLoading.service({
//       lock: true,
//       text: '正在跳转支付宝支付...',
//       background: 'rgba(0, 0, 0, 0.7)'
//     })
//
//     // 从 localStorage 获取 token
//     const userInfo = localStorage.getItem('userInfo')
//     let token = ''
//     if (userInfo) {
//       const user = JSON.parse(userInfo)
//       token = user.token || ''
//     }
//
//     // 使用 fetch 调用后端接口，携带 token
//     const response = await fetch(`/api/pay/pc?orderId=${orderId.value}`, {
//       method: 'GET',
//       headers: {
//         'Content-Type': 'application/json',
//         'token': token  // 添加 token 到请求头
//       },
//       credentials: 'include' // 携带 cookie
//     })
//
//     console.log('支付宝支付响应状态:', response.status)
//
//     if (!response.ok) {
//       // 如果是 401，跳转到登录页
//       if (response.status === 401) {
//         throw new Error('请先登录')
//       }
//       throw new Error(`支付请求失败，状态码：${response.status}`)
//     }
//
//     // 获取后端返回的 HTML 表单
//     const html = await response.text()
//     console.log('后端返回的完整 HTML:', html)
//
//     // 关闭加载
//     if (loadingInstance) {
//       loadingInstance.close()
//     }
//
//     // 直接将 HTML 插入到 body
//     document.body.insertAdjacentHTML('beforeend', html)
//
//     // 找到表单并提交
//     const form = document.querySelector('form[name="punchout_form"]')
//     if (form) {
//       console.log('找到表单，准备提交:', form.action)
//       // 确保表单在 body 中
//       if (!form.parentNode) {
//         document.body.appendChild(form)
//       }
//       // 自动提交表单，跳转到支付宝
//       form.submit()
//     } else {
//       // 尝试查找其他表单
//       const anyForm = document.querySelector('form')
//       if (anyForm) {
//         console.log('找到其他表单，准备提交:', anyForm.action)
//         anyForm.submit()
//       } else {
//         console.error('未找到任何表单，返回的 HTML:', html)
//         throw new Error('未找到支付表单，请联系客服')
//       }
//     }
//   } catch (error) {
//     // 关闭加载
//     if (loadingInstance) {
//       loadingInstance.close()
//     }
//     console.error('支付宝支付失败:', error)
//     ElMessage.error('支付宝支付失败：' + error.message)
//   }
// }

// 余额支付
const handleBalance = async () => {
  let loadingInstance = null
  try {
    // 余额不足提示
    if (balanceInsufficient.value) {
      ElMessage.error('账户余额不足，请选择其他支付方式')
      return
    }
    
    // 显示加载提示
    loadingInstance = ElLoading.service({
      lock: true,
      text: '正在处理余额支付...',
      background: 'rgba(0, 0, 0, 0.7)'
    })
    
    const result = await request('/pay/balance', {
      method: 'POST',
      body: JSON.stringify({
        orderId: orderId.value,
        amount: totalPrice.value
      })
    })
    
    // 关闭加载
    if (loadingInstance) {
      loadingInstance.close()
    }
    
    if (result && result.code === 200) {
      ElMessage.success('支付成功')
      // 跳转到支付成功页面或首页
      setTimeout(() => {
        router.push('/home')
      }, 1500)
    } else {
      ElMessage.error(result.message || '余额支付失败')
    }
  } catch (error) {
    // 关闭加载
    if (loadingInstance) {
      loadingInstance.close()
    }
    console.error('余额支付失败:', error)
    ElMessage.error('余额支付失败：' + (error.message || '未知错误'))
  }
}

const goHome = () => {
  // 替换历史记录，防止用户返回支付页面
  router.replace('/home')
}

onMounted(() => {
  // 从路由参数获取订单信息
  orderId.value = route.query.orderId || ''
  totalPrice.value = route.query.totalPrice || '0.00'

  console.log('支付页面收到的参数:', { orderId: orderId.value, totalPrice: totalPrice.value })

  if (!orderId.value) {
    ElMessage.error('订单信息不完整')
    // 替换历史记录，防止返回
    router.replace('/home')
    return
  }

  // 如果没有 totalPrice，从后端获取订单详情
  if (!route.query.totalPrice) {
    loadOrderDetail()
  }

  // 禁止 body 滚动
  document.body.style.overflow = 'hidden'

  // 查询用户余额
  getUserBalance()
})

// 加载订单详情获取金额
const loadOrderDetail = async () => {
  try {
    const result = await request(`/order/${orderId.value}`)
    console.log('订单详情返回:', result)
    if (result && result.code === 200 && result.data) {
      const price = result.data.totalPrice
      console.log('订单金额:', price, typeof price)
      if (price !== undefined && price !== null) {
        totalPrice.value = Number(price).toFixed(2)
      }
    }
  } catch (e) {
    console.error('获取订单详情失败:', e)
  }
}

onUnmounted(() => {
  // 恢复 body 滚动
  document.body.style.overflow = ''
  window.removeEventListener('popstate', handleBackButton)
})

const handleBackButton = () => {
  // 用户尝试后退时，强制返回首页
  router.replace('/home')
}
</script>

<style scoped>
.payment-page {
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
  gap: 20px;
}

.logo {
  font-size: 24px;
  font-weight: bold;
  color: var(--color-primary);
  cursor: pointer;
}

.nav-text {
  font-size: 18px;
  color: #333;
  font-weight: 500;
}

.nav-right {
  display: flex;
  gap: 10px;
  font-size: 14px;
  color: #666;
}

.nav-right a {
  color: #666;
  text-decoration: none;
}

.nav-right a:hover {
  color: var(--color-primary);
}

.divider {
  color: #ccc;
}

.payment-container {
  flex: 1;
  width: 1200px;
  margin: 30px auto;
  background-color: var(--color-bg-tertiary);
  padding: 30px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.order-summary {
  padding: 20px;
  background-color: #f9f9f9;
  margin-bottom: 30px;
  border-radius: 4px;
}

.summary-row {
  margin-bottom: 10px;
  font-size: 14px;
}

.summary-row:last-child {
  margin-bottom: 0;
}

.label {
  color: #666;
  margin-right: 10px;
}

.value {
  color: #333;
  font-weight: 500;
}

.amount {
  color: var(--color-primary);
  font-size: 20px;
  font-weight: bold;
}

.payment-section {
  margin-bottom: 30px;
}

.payment-header {
  padding: 15px 20px;
  background-color: #f9f9f9;
  border-bottom: 1px solid #e0e0e0;
}

.payment-method {
  font-size: 18px;
  font-weight: 500;
  color: #333;
}

.payment-methods {
  padding: 30px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.method-card {
  display: flex;
  align-items: center;
  padding: 20px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  background-color: #fff;
}

.method-card:hover {
  border-color: #2563eb;
  background-color: #f0f6ff;
}

.method-card.active {
  border-color: #2563eb;
  background-color: #f0f6ff;
}

.method-card.disabled {
  opacity: 0.6;
  cursor: not-allowed;
  border-color: #ddd;
  background-color: #f9f9f9;
}

.method-card.disabled:hover {
  border-color: #ddd;
  background-color: #f9f9f9;
}

.method-icon {
  font-size: 36px;
  margin-right: 20px;
}

.method-info {
  flex: 1;
}

.method-name {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  margin-bottom: 5px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.balance-text {
  font-size: 13px;
  color: #666;
  font-weight: 400;
}

.insufficient-tip {
  color: #ff0000;
  font-size: 12px;
  font-weight: 500;
  margin-left: 5px;
}

.method-desc {
  font-size: 13px;
  color: #999;
}

.method-radio {
  margin-left: 15px;
}

.method-radio :deep(.el-radio__input.is-checked .el-radio__inner) {
  background-color: var(--color-primary);
  border-color: #2563eb;
}

.method-radio :deep(.el-radio__input.is-checked .el-radio__inner::after) {
  background-color: #fff;
}

.method-radio :deep(.el-radio__input.is-checked .el-radio__label) {
  color: #2563eb;
}

.payment-action {
  padding: 20px;
  text-align: center;
  border-top: 1px solid #e0e0e0;
}

.pay-btn {
  padding: 15px 80px;
  font-size: 18px;
  font-weight: 500;
  color: #fff;
  background-color: var(--color-primary);
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.pay-btn:hover {
  background-color: #1d4ed8;
}

.pay-btn:active {
  background-color: #1e40af;
}

.payment-status {
  border-top: 1px solid #e0e0e0;
  padding-top: 20px;
}

.status-item {
  margin-bottom: 20px;
  font-size: 14px;
}

.status-label {
  color: #666;
  margin-right: 10px;
}

.status-value {
  color: #2563eb;
  font-weight: 500;
}

.status-value.waiting {
  color: #2563eb;
}

.status-actions {
  display: flex;
  gap: 15px;
}

.action-btn {
  padding: 10px 30px;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
  background-color: #fff;
  color: #666;
}

.action-btn:hover {
  border-color: #2563eb;
  color: #2563eb;
}

.back-btn {
  background-color: #fff;
  color: #666;
  border: 1px solid #ddd;
}

.back-btn:hover {
  border-color: #2563eb;
  color: #2563eb;
}

.footer {
  background-color: #eaeaea;
  padding: 20px 0;
  margin-top: auto;
}

.footer-content {
  width: 1200px;
  margin: 0 auto;
  text-align: center;
}

.footer-links {
  margin-bottom: 15px;
  font-size: 14px;
  color: #666;
}

.footer-links a {
  color: #666;
  text-decoration: none;
  margin: 0 10px;
}

.footer-links a:hover {
  color: var(--color-primary);
}

.footer-copyright {
  font-size: 12px;
  color: #999;
}
</style>
