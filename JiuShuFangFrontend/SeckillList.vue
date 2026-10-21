<template>
  <!-- 动态背景 -->
  <VitalityBackground />

  <div class="seckill-container">
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
          <UserMenu :userInfo="userInfo" @logout="handleLogout" />
        </div>
      </div>
    </div>

    <!-- 页面主体 -->
    <div class="main-content">
      <div class="seckill-header">
        <h1>限量书秒杀</h1>
        <span class="count-text">共 {{ seckillList.length }} 场活动</span>
        <span class="back-home-link" @click="goBackHome">返回首页</span>
      </div>

      <div v-if="loading" class="loading-text">加载中...</div>
      <div v-else-if="seckillList.length === 0" class="no-data">暂无秒杀活动</div>

      <div v-else class="seckill-grid">
        <div v-for="item in seckillList" :key="item.id" class="seckill-card">
          <div class="card-img">
            <img :src="resolveImg(item.bookImg)" :alt="item.bookName" @error="onImgError" />
            <div class="card-badge">秒杀</div>
          </div>

          <div class="card-body">
            <div class="book-name" :title="item.bookName">{{ item.bookName }}</div>

            <div class="price-row">
              <span class="seckill-price">￥{{ formatPrice(item.price) }}</span>
              <span class="origin-price">￥{{ formatPrice(item.originalPrice) }}</span>
            </div>

            <div class="stock-row">
              <span class="stock-label">剩余库存</span>
              <span class="stock-value" :class="{ 'stock-low': item.remainStock <= 0 }">
                {{ item.remainStock }} / {{ item.initialStock }}
              </span>
            </div>

            <div class="countdown-row">
              <span class="countdown-text">{{ countdownText(item) }}</span>
            </div>

            <button
              class="seckill-btn"
              :class="{ 'is-disabled': btnDisabled(item), 'is-success': item.joined }"
              :disabled="btnDisabled(item)"
              @click="handleSeckill(item)"
            >
              {{ btnText(item) }}
            </button>
          </div>
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
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { convertToExternalUrl } from './utils/imageUtils'
import {
  listSeckill,
  getSeckillDetail,
  doSeckill,
  getSeckillResult
} from './config/seckillApi.js'

const router = useRouter()

const userInfo = ref(null)
const seckillList = ref([])
const loading = ref(false)

// 每秒刷新的当前时间（驱动倒计时与按钮状态）
const now = ref(Date.now())
let tickTimer = null
// 轮询定时器：seckillId -> intervalId
const pollTimers = {}
// 轮询最大次数
const MAX_POLL = 10

const handleLogout = () => {
  localStorage.removeItem('userInfo')
  localStorage.removeItem('token')
  router.push('/')
}

const goBackHome = () => {
  router.push('/home')
}

const goToShoppingCart = () => {
  router.push('/shopping-cart')
}

const formatPrice = (val) => {
  if (val === null || val === undefined || val === '') return '--'
  return Number(val).toFixed(2)
}

const resolveImg = (url) => convertToExternalUrl(url)

const onImgError = (e) => {
  e.target.style.visibility = 'hidden'
}

// 兼容 LocalDateTime 的多种序列化形式（ISO 字符串 / 空格分隔 / 数组）
const toTs = (val) => {
  if (val === null || val === undefined) return NaN
  if (Array.isArray(val)) {
    const [y, mo, d, h = 0, mi = 0, s = 0] = val
    return new Date(y, mo - 1, d, h, mi, s).getTime()
  }
  if (typeof val === 'number') return val
  return new Date(String(val).replace(' ', 'T')).getTime()
}

const fmtDuration = (ms) => {
  const total = Math.max(0, Math.floor(ms / 1000))
  const h = String(Math.floor(total / 3600)).padStart(2, '0')
  const m = String(Math.floor((total % 3600) / 60)).padStart(2, '0')
  const s = String(total % 60).padStart(2, '0')
  return `${h}:${m}:${s}`
}

const countdownText = (item) => {
  const t = now.value
  const start = toTs(item.startTime)
  const end = toTs(item.endTime)
  if (Number.isNaN(start) || Number.isNaN(end)) return ''
  if (item.status === 2 || t > end) return '活动已结束'
  if (t < start) return '距开始 ' + fmtDuration(start - t)
  return '距结束 ' + fmtDuration(end - t)
}

const btnText = (item) => {
  if (item.processing) return '排队中…'
  if (item.joined) return '已抢到'
  if (!item.remainStock || item.remainStock <= 0) return '已售罄'
  if (item.status === 2) return '已结束'
  const t = now.value
  if (t < toTs(item.startTime)) return '未开始'
  if (t > toTs(item.endTime)) return '已结束'
  return '立即抢购'
}

const btnDisabled = (item) => {
  if (item.processing || item.joined) return true
  if (!item.remainStock || item.remainStock <= 0) return true
  if (item.status === 2) return true
  const t = now.value
  return t < toTs(item.startTime) || t > toTs(item.endTime)
}

// 拉取单个活动的最新库存与参与状态
const refreshItem = async (item) => {
  try {
    const d = await getSeckillDetail(item.id)
    if (!d) return
    item.remainStock = d.remainStock === null || d.remainStock === undefined ? 0 : d.remainStock
    item.joined = !!d.joined
    // processing 由本地轮询接管，避免与详情里的瞬时值互相覆盖
    if (d.processing && !item.processing) {
      item.processing = true
      startPolling(item)
    }
  } catch (e) {
    // 详情刷新失败不打断页面
  }
}

const loadSeckill = async () => {
  if (loading.value) return
  loading.value = true
  try {
    const list = await listSeckill()
    if (!Array.isArray(list)) {
      seckillList.value = []
      return
    }
    const details = await Promise.all(
      list.map(async (it) => {
        const base = {
          ...it,
          remainStock: it.seckillStock,
          joined: false,
          processing: false
        }
        try {
          const d = await getSeckillDetail(it.id)
          if (d) {
            base.remainStock = d.remainStock === null || d.remainStock === undefined
              ? it.seckillStock
              : d.remainStock
            base.joined = !!d.joined
            base.processing = !!d.processing
          }
        } catch (e) {
          // 单个活动详情失败不影响整体列表
        }
        return base
      })
    )
    seckillList.value = details
    // 上次未处理完的请求，刷新后继续轮询
    details.forEach((it) => {
      if (it.processing) startPolling(it)
    })
  } catch (e) {
    console.error('加载秒杀列表失败:', e)
  } finally {
    loading.value = false
  }
}

const stopPolling = (item) => {
  const timer = pollTimers[item.id]
  if (timer) {
    clearInterval(timer)
    delete pollTimers[item.id]
  }
}

const startPolling = (item) => {
  if (pollTimers[item.id]) return
  let count = 0
  item.processing = true
  pollTimers[item.id] = setInterval(async () => {
    count++
    if (count > MAX_POLL) {
      stopPolling(item)
      item.processing = false
      ElMessage.warning('结果查询超时，请稍后重试或查看我的订单')
      return
    }
    try {
      const res = await getSeckillResult(item.id)
      if (!res) return
      if (res.status === 'SUCCESS') {
        stopPolling(item)
        item.processing = false
        item.joined = true
        ElMessage.success(res.message || '抢购成功！')
        refreshItem(item)
      } else if (res.status === 'FAIL' || res.status === 'SOLD_OUT' || res.status === 'REPEAT') {
        stopPolling(item)
        item.processing = false
        ElMessage.warning(res.message || '抢购失败')
        refreshItem(item)
      }
      // PROCESSING / NONE：继续轮询
    } catch (e) {
      // 单次查询失败忽略，继续轮询
    }
  }, 1000)
}

const handleSeckill = async (item) => {
  if (item.processing) return
  item.processing = true
  try {
    await doSeckill(item.id)
    ElMessage.info('排队中，请稍候查看结果')
    startPolling(item)
  } catch (e) {
    // 失败原因（已售罄 / 已抢过 / 系统繁忙）已由请求层统一提示
    item.processing = false
    refreshItem(item)
  }
}

onMounted(() => {
  const stored = localStorage.getItem('userInfo')
  if (stored) {
    userInfo.value = JSON.parse(stored)
  }
  tickTimer = setInterval(() => {
    now.value = Date.now()
  }, 1000)
  loadSeckill()
})

onUnmounted(() => {
  if (tickTimer) clearInterval(tickTimer)
  Object.keys(pollTimers).forEach((k) => {
    clearInterval(pollTimers[k])
    delete pollTimers[k]
  })
})
</script>

<style scoped>
.seckill-container {
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

a {
  text-decoration: none;
  color: #333;
}

.top-nav {
  position: sticky;
  top: 0;
  z-index: 300;
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  box-shadow: 0 1px 0 rgba(37, 99, 235, 0.06);
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
  color: #2563eb;
}

.main-content {
  width: 1200px;
  margin: 30px auto;
  padding: 30px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  min-height: 600px;
}

.seckill-header {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 30px;
  padding-bottom: 15px;
  border-bottom: 2px solid #f5f5f5;
}

.seckill-header h1 {
  font-size: 24px;
  color: #333;
  font-weight: 600;
}

.count-text {
  font-size: 14px;
  color: #999;
}

.back-home-link {
  margin-left: auto;
  font-size: 14px;
  color: #2563eb;
  cursor: pointer;
  transition: color 0.3s;
}

.back-home-link:hover {
  color: #3b82f6;
}

.loading-text,
.no-data {
  text-align: center;
  color: #999;
  font-size: 14px;
  padding: 40px 0;
}

.seckill-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 20px;
}

.seckill-card {
  border: 1px solid #eee;
  border-radius: 10px;
  overflow: hidden;
  background: #fff;
  transition: box-shadow 0.25s, transform 0.25s;
}

.seckill-card:hover {
  box-shadow: 0 8px 24px rgba(37, 99, 235, 0.14);
  transform: translateY(-3px);
}

.card-img {
  position: relative;
  width: 100%;
  height: 180px;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
}

.card-img img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.card-badge {
  position: absolute;
  top: 10px;
  left: 10px;
  padding: 2px 10px;
  font-size: 12px;
  color: #fff;
  background: linear-gradient(135deg, #2563eb 0%, #3b82f6 100%);
  border-radius: 10px;
}

.card-body {
  padding: 14px 16px 18px;
}

.book-name {
  font-size: 15px;
  color: #333;
  font-weight: 500;
  line-height: 1.4;
  height: 42px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.price-row {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-top: 10px;
}

.seckill-price {
  font-size: 20px;
  font-weight: 700;
  color: #2563eb;
}

.origin-price {
  font-size: 13px;
  color: #bbb;
  text-decoration: line-through;
}

.stock-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 10px;
  font-size: 13px;
}

.stock-label {
  color: #999;
}

.stock-value {
  color: #333;
  font-weight: 600;
}

.stock-value.stock-low {
  color: #f23030;
}

.countdown-row {
  margin-top: 8px;
}

.countdown-text {
  font-size: 13px;
  color: #666;
  font-variant-numeric: tabular-nums;
}

.seckill-btn {
  width: 100%;
  margin-top: 14px;
  height: 38px;
  border: none;
  border-radius: 6px;
  font-size: 15px;
  color: #fff;
  cursor: pointer;
  background: linear-gradient(135deg, #2563eb 0%, #3b82f6 100%);
  transition: opacity 0.2s, transform 0.2s;
}

.seckill-btn:hover:not(.is-disabled) {
  opacity: 0.9;
  transform: translateY(-1px);
}

.seckill-btn.is-disabled {
  background: #d8dde6;
  color: #8a93a3;
  cursor: not-allowed;
  transform: none;
}

.seckill-btn.is-success {
  background: #16a34a;
  color: #fff;
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

.footer-links span {
  font-size: 14px;
  color: #999;
}

.footer-links a {
  color: #999;
}
</style>
