<template>
  <div class="login-container">
    <!-- 封面 - Page 0 -->
    <div v-if="!isBookOpen" class="book-cover" @click="openBook">
      <div class="cover-content">
        <div class="cover-decoration">
          <div class="cover-title">
            <span class="main-title">旧书坊</span>
            <span class="sub-title">二手书市</span>
          </div>
          <div class="cover-emoji">📚</div>
          <div class="cover-hint">
            <span>点击进入</span>
            <el-icon><ArrowRight /></el-icon>
          </div>
        </div>
      </div>
    </div>

    <!-- 打开的书本 -->
    <div v-else class="open-book-container">
      <div class="book-wrapper" ref="bookWrapper" :class="{ 'book-loading': !pageFlipInit }">
        <!-- Page 1: 左页 - 欢迎界面 -->
        <div class="book-page">
          <div class="page-content welcome-page-content">
            <div class="left-page-decoration">
              <div class="decorative-circle"></div>
              <div class="decorative-circle"></div>
              <div class="decorative-circle"></div>
              <div class="welcome-message">
                <h1 class="welcome-title">欢迎来到<br/>旧书坊</h1>
                <p class="welcome-desc">在这里，每一本二手书<br/>都等待着新的故事开始</p>
              </div>
              <div class="floating-books">
                <span class="floating-book">📖</span>
                <span class="floating-book">📕</span>
                <span class="floating-book">📗</span>
                <span class="floating-book">📘</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Page 2: 右页 - 账密登录 -->
        <div class="book-page">
          <div class="page-content login-page-content">
            <div class="login-page-wrapper">
              <div class="login-header">
                <GlitchText
                  text="旧书坊"
                  :speed="0.8"
                  :enableShadows="true"
                  :enableOnHover="true"
                  :className="'brand-name'"
                />
                <BlinkEmoji :size="40" :interval="2000" expression="happy" />
                <p class="welcome-text">{{ greetingText }}</p>
              </div>

              <el-form ref="passwordFormRef" :model="loginForm" :rules="passwordRules" class="login-form" @mousedown.stop @touchstart.stop>
                <el-form-item prop="phone">
                  <div class="input-group">
                    <el-select v-model="loginForm.countryCode" class="country-code" placeholder="+86">
                      <el-option label="+86" value="+86" />
                      <el-option label="+852" value="+852" />
                      <el-option label="+853" value="+853" />
                      <el-option label="+886" value="+886" />
                    </el-select>
                    <el-input
                      v-model="loginForm.phone"
                      class="phone-input"
                      placeholder="请输入手机号"
                      maxlength="11"

                    />
                  </div>
                </el-form-item>

                <el-form-item prop="password">
                  <el-input
                    v-model="loginForm.password"
                    type="password"
                    placeholder="请输入密码"

                    show-password
                  />
                </el-form-item>

                <el-form-item prop="captcha">
                  <div class="code-input-group">
                    <el-input
                      v-model="loginForm.captcha"
                      class="code-input"
                      placeholder="请输入图形验证码"
                      maxlength="6"
                    />
                    <img
                      v-if="captchaImageUrl"
                      :src="captchaImageUrl"
                      class="captcha-image"
                      @click="refreshCaptcha"
                      alt="验证码"
                    />
                    <div v-else class="captcha-image" @click="refreshCaptcha">
                      <span class="captcha-placeholder">点击获取</span>
                    </div>
                  </div>
                </el-form-item>

                <el-form-item>
                  <el-button class="login-btn" type="primary" @click="handleLogin">登录</el-button>
                </el-form-item>
              </el-form>

              <div class="login-footer">
                <div class="footer-links">
                  <span class="link" @click="openForgotPassword">忘记密码</span>
                  <span class="footer-divider">|</span>
                  <span class="switch-hint">切换登录方式 →</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Page 3: 左页 - 欢迎界面 -->
        <div class="book-page">
          <div class="page-content welcome-page-content">
            <div class="left-page-decoration">
              <div class="decorative-circle"></div>
              <div class="decorative-circle"></div>
              <div class="decorative-circle"></div>
              <div class="welcome-message">
                <h1 class="welcome-title">欢迎来到<br/>旧书坊</h1>
                <p class="welcome-desc">在这里，每一本二手书<br/>都等待着新的故事开始</p>
              </div>
              <div class="floating-books">
                <span class="floating-book">📖</span>
                <span class="floating-book">📕</span>
                <span class="floating-book">📗</span>
                <span class="floating-book">📘</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Page 4: 右页 - 验证码登录 -->
        <div class="book-page">
          <div class="page-content login-page-content">
            <div class="login-page-wrapper">
              <div class="login-header">
                <GlitchText
                  text="旧书坊"
                  :speed="0.8"
                  :enableShadows="true"
                  :enableOnHover="true"
                  :className="'brand-name'"
                />
                <BlinkEmoji :size="40" :interval="2000" expression="happy" />
                <p class="welcome-text">{{ greetingText }}</p>
              </div>

              <el-form ref="smsFormRef" :model="loginForm" :rules="smsRules" class="login-form" @mousedown.stop @touchstart.stop>
                <el-form-item prop="phone">
                  <div class="input-group">
                    <el-select v-model="loginForm.countryCode" class="country-code" placeholder="+86">
                      <el-option label="+86" value="+86" />
                      <el-option label="+852" value="+852" />
                      <el-option label="+853" value="+853" />
                      <el-option label="+886" value="+886" />
                    </el-select>
                    <el-input
                      v-model="loginForm.phone"
                      class="phone-input"
                      placeholder="请输入手机号"
                      maxlength="11"
                    />
                  </div>
                </el-form-item>

                <el-form-item prop="code">
                  <div class="code-input-group">
                    <el-input
                      v-model="loginForm.code"
                      class="code-input"
                      placeholder="请输入短信验证码"
                      maxlength="6"
                    />
                    <el-button
                      class="get-code-btn"
                      @click="getCode"
                      :disabled="isCountingDown"
                    >
                      {{ isCountingDown ? `${countdown}秒后重发` : '获取验证码' }}
                    </el-button>
                  </div>
                </el-form-item>

                <el-form-item prop="captcha">
                  <div class="code-input-group">
                    <el-input
                      v-model="loginForm.captcha"
                      class="code-input"
                      placeholder="请输入图形验证码"
                      maxlength="6"
                    />
                    <img
                      v-if="captchaImageUrl"
                      :src="captchaImageUrl"
                      class="captcha-image"
                      @click="refreshCaptcha"
                      alt="验证码"
                    />
                    <div v-else class="captcha-image" @click="refreshCaptcha">
                      <span class="captcha-placeholder">点击获取</span>
                    </div>
                  </div>
                </el-form-item>

                <el-form-item>
                  <el-button class="login-btn" type="primary" @click="handleLogin">登录</el-button>
                </el-form-item>
              </el-form>

              <div class="login-footer">
                <div class="footer-links">
                  <span class="link" @click="openForgotPassword">忘记密码</span>
                  <span class="footer-divider">|</span>
                  <span class="switch-hint">切换登录方式 →</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Page 5: 左页 - 欢迎界面 -->
        <div class="book-page">
          <div class="page-content welcome-page-content">
            <div class="left-page-decoration">
              <div class="decorative-circle"></div>
              <div class="decorative-circle"></div>
              <div class="decorative-circle"></div>
              <div class="welcome-message">
                <h1 class="welcome-title">欢迎来到<br/>旧书坊</h1>
                <p class="welcome-desc">在这里，每一本二手书<br/>都等待着新的故事开始</p>
              </div>
              <div class="floating-books">
                <span class="floating-book">📖</span>
                <span class="floating-book">📕</span>
                <span class="floating-book">📗</span>
                <span class="floating-book">📘</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Page 6: 右页 - 扫码登录 -->
        <div class="book-page">
          <div class="page-content login-page-content">
            <div class="login-page-wrapper">
              <div class="login-header">
                <GlitchText
                  text="旧书坊"
                  :speed="0.8"
                  :enableShadows="true"
                  :enableOnHover="true"
                  :className="'brand-name'"
                />
                <BlinkEmoji :size="40" :interval="2000" expression="happy" />
                <p class="welcome-text">{{ greetingText }}</p>
              </div>

              <div class="qr-login-content" @mousedown.stop @touchstart.stop>
                <div class="qr-login-header">
                  <h3>微信扫码登录</h3>
                  <p class="qr-login-desc">使用微信扫码二维码进行登录</p>
                </div>

                <div class="qr-code-container">
                  <img
                    v-if="qrCodeUrl && scanStatus === 'WAIT'"
                    :src="qrCodeUrl"
                    class="qr-code-image"
                    :class="{ 'qr-code-disabled': qrRefreshCooldown }"
                    alt=""
                    @click="refreshQRCode"
                  />
                  <div v-else-if="scanStatus === 'LOGINING'" class="qr-code-overlay">
                    <div class="loading-text">正在登录中...</div>
                    <div class="loading-spinner"></div>
                  </div>
                  <div v-else-if="scanStatus === null" class="qr-code-overlay">
                    <el-icon class="qr-refresh-icon" @click="loadQRCode"><Refresh /></el-icon>
                  </div>
                  <div v-else class="qr-code-overlay">
                    <el-icon class="qr-refresh-icon" @click="loadQRCode"><Refresh /></el-icon>
                  </div>
                </div>
              </div>

              <div class="login-footer">
                <div class="footer-links">
                  <span class="link" @click="openForgotPassword">忘记密码</span>
                  <span class="footer-divider">|</span>
                  <span class="switch-hint">切换登录方式 →</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 忘记密码覆盖层 -->
      <div v-if="showForgotPage" class="forgot-overlay-wrapper">
        <div class="forgot-overlay">
          <div class="forgot-password-wrapper">
            <div class="forgot-header">
              <GlitchText
                text="找回密码"
                :speed="0.8"
                :enableShadows="true"
                :enableOnHover="true"
                :className="'forgot-title'"
              />
              <p class="forgot-desc">请输入您的手机号和新密码</p>
            </div>

            <el-form ref="forgotFormRef" :model="forgotForm" :rules="forgotRules" class="forgot-form">
              <el-form-item prop="phone">
                <el-input
                  v-model="forgotForm.phone"
                  placeholder="请输入手机号"
                  maxlength="11"
                />
              </el-form-item>

              <el-form-item prop="code">
                <div class="code-input-group">
                  <el-input
                    v-model="forgotForm.code"
                    placeholder="请输入验证码"
                    maxlength="6"
                  />
                  <el-button
                    class="get-code-btn"
                    @click="getForgotCode"
                    :disabled="forgotIsCountingDown"
                  >
                    {{ forgotIsCountingDown ? `${forgotCountdown}秒后重发` : '获取验证码' }}
                  </el-button>
                </div>
              </el-form-item>

              <el-form-item prop="password">
                <el-input
                  v-model="forgotForm.password"
                  type="password"
                  placeholder="请输入 6-20 位新密码"
                  show-password
                />
              </el-form-item>

              <el-form-item prop="confirmPassword">
                <el-input
                  v-model="forgotForm.confirmPassword"
                  type="password"
                  placeholder="请再次输入新密码"
                  show-password
                />
              </el-form-item>

              <el-form-item>
                <el-button
                  type="primary"
                  class="submit-btn"
                  @click="handleForgotSubmit"
                  :loading="forgotLoading"
                >
                  确定
                </el-button>
              </el-form-item>

              <el-form-item>
                <div class="back-link" @click="backToLogin">
                  <el-link>返回登录</el-link>
                </div>
              </el-form-item>
            </el-form>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, computed, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowRight, ArrowLeft, Refresh } from '@element-plus/icons-vue'
import request from './request'
import { encryptPassword } from './crypto'
import BlinkEmoji from './components/BlinkEmoji.vue'
import GlitchText from './components/GlitchText.vue'
import { PageFlip } from 'page-flip'

const router = useRouter()

const isBookOpen = ref(false)
const bookWrapper = ref(null)

let pageFlip = null
const pageFlipInit = ref(false)
const currentPage = ref(0)
const showForgotPage = ref(false)

const greetingText = computed(() => {
  const hour = new Date().getHours()
  if (hour < 12) return '早上好，欢迎回来！☀️'
  if (hour < 18) return '下午好，欢迎回来！🌤️'
  return '晚上好，欢迎回来！🌙'
})

const openBook = async () => {
  isBookOpen.value = true
  await nextTick()
  setTimeout(() => {
    initPageFlip()
  }, 100)
}

const initPageFlip = () => {
  if (pageFlip) {
    try { pageFlip.destroy() } catch (e) {}
  }

  if (!bookWrapper.value) return

  const pages = Array.from(bookWrapper.value.querySelectorAll('.book-page') || [])

  pageFlip = new PageFlip(bookWrapper.value, {
    width: 420,
    height: 560,
    size: 'stretch',
    minWidth: 300,
    maxWidth: 450,
    minHeight: 400,
    maxHeight: 600,
    showCover: false,
    maxShadowOpacity: 0.6,
    flippingTime: 800,
    usePortrait: false,
    mobileScrollSupport: false,
    swipeDistance: 50,
    autoSize: true,
    useMouseEvents: true
  })

  pageFlip.loadFromHTML(pages)

  // flip 事件在翻页动画完成时触发
pageFlip.on('flip', (e) => {
    currentPage.value = e.data
    console.log('>>> flip event: e.data =', e.data, '=> currentPage =', currentPage.value)
    if (e.data === 4) {
      if (!qrCodeUrl.value) {
        loadQRCode()
      }
    } else {
      stopScanCheck()
    }
  })

  pageFlipInit.value = true

  // 初始化完成后翻到账密登录页（e.data = 0）
  currentPage.value = 0
  console.log('>>> init: setting currentPage = 0, calling turnToPage(1)')
  setTimeout(() => {
    if (pageFlip) {
      console.log('>>> init: getCurrentPageIndex before turnToPage =', pageFlip.getCurrentPageIndex())
      pageFlip.turnToPage(1)  // e.data = 0 是账密登录页
      console.log('>>> init: getCurrentPageIndex after turnToPage =', pageFlip.getCurrentPageIndex())
    }
  }, 300)
}

const passwordFormRef = ref(null)
const smsFormRef = ref(null)
const loginForm = ref({
  countryCode: '+86',
  phone: '',
  code: '',
  password: '',
  captcha: '',
  captchaKey: '',
  smsCaptcha: ''
})
const captchaImageUrl = ref('')
const qrCodeUrl = ref('')
const scanStatus = ref(null)
const qrRefreshCooldown = ref(false)
const qrCooldownTime = ref(0)
let qrCooldownTimer = null
const isCountingDown = ref(false)
const countdown = ref(60)
let countdownTimer = null

const forgotFormRef = ref(null)
const forgotForm = reactive({
  phone: '',
  code: '',
  password: '',
  confirmPassword: ''
})
const forgotIsCountingDown = ref(false)
const forgotCountdown = ref(60)
let forgotCountdownTimer = null
const forgotLoading = ref(false)

const openForgotPassword = () => {
  showForgotPage.value = true
}

const backToLogin = () => {
  showForgotPage.value = false
}

const loadQRCode = async () => {
  try {
    scanStatus.value = 'WAIT'
    const response = await request('/user/login/login/QRCode', { rawResponse: true })
    const loginId = response.headers?.get('loginId')

    if (loginId) {
      const blob = await response.blob()
      const imageUrl = URL.createObjectURL(blob)
      qrCodeUrl.value = imageUrl
      startScanCheck(loginId)
    }
  } catch (error) {
    console.error('生成二维码失败:', error)
  }
}

const refreshQRCode = () => {
  if (qrRefreshCooldown.value) return
  if (qrCodeUrl.value) URL.revokeObjectURL(qrCodeUrl.value)
  qrRefreshCooldown.value = true
  qrCooldownTime.value = 5
  qrCooldownTimer = setInterval(() => {
    qrCooldownTime.value--
    if (qrCooldownTime.value <= 0) {
      qrRefreshCooldown.value = false
      clearInterval(qrCooldownTimer)
    }
  }, 1000)
  loadQRCode()
}

const refreshCaptcha = () => {
  loginForm.value.captchaKey = 'login_' + Date.now()
  captchaImageUrl.value = `/api/capture/captcha?key=${loginForm.value.captchaKey}&t=${Date.now()}`
  console.log('>>> refreshCaptcha, new key:', loginForm.value.captchaKey)
}

let scanTimer = null
let currentLoginId = null

const stopScanCheck = () => {
  if (scanTimer) {
    clearInterval(scanTimer)
    scanTimer = null
  }
}

const startScanCheck = (loginId) => {
  currentLoginId = loginId
  stopScanCheck()
  scanTimer = setInterval(async () => {
    try {
      const result = await request(`/user/check?loginId=${loginId}`)
      if (result && result.code === 200) {
        const status = result.data
        if (status === '2' || status === 'LOGIN_SUCCESS' || status === 2) {
          clearInterval(scanTimer)
          scanStatus.value = 'LOGIN_SUCCESS'
          await handleLoginSuccess(loginId)
        } else if (status === '0' || status === 'WAIT' || status === 0) {
          scanStatus.value = 'WAIT'
        } else if (status === '1' || status === 'LOGINING' || status === 1) {
          scanStatus.value = 'LOGINING'
        } else {
          clearInterval(scanTimer)
          scanStatus.value = null
          ElMessage.error('二维码已失效，请点击重新生成')
        }
      }
    } catch (error) {
      clearInterval(scanTimer)
      scanStatus.value = null
    }
  }, 1000)
}

const handleLoginSuccess = async (loginId) => {
  try {
    const result = await request(`/user/login/check/QRCode?loginId=${loginId}`)
    if (result && result.data) {
      const userInfo = result.data
      if (userInfo.id) userInfo.id = userInfo.id.toString()
      if (userInfo.token) localStorage.setItem('token', userInfo.token)
      localStorage.setItem('userInfo', JSON.stringify(userInfo))
      ElMessage.success('扫码登录成功')
      window.location.href = '/#/home'
    } else {
      ElMessage.error(result?.message || '获取用户信息失败')
    }
  } catch (error) {
    ElMessage.error(error.message || '获取用户信息失败，请重试')
  }
}

const validatePhone = (rule, value, callback) => {
  const phoneReg = /^1[3-9]\d{9}$/
  if (!value) callback(new Error('请输入手机号'))
  else if (!phoneReg.test(value)) callback(new Error('请输入正确的手机号'))
  else callback()
}

const validateCode = (rule, value, callback) => {
  const codeReg = /^\d{6}$/
  if (!value) callback(new Error('请输入验证码'))
  else if (!codeReg.test(value)) callback(new Error('验证码为 6 位数字'))
  else callback()
}

const validatePassword = (rule, value, callback) => {
  if (!value) callback(new Error('请输入密码'))
  else if (value.length < 6 || value.length > 20) callback(new Error('密码长度为 6-20 位'))
  else callback()
}

const validateConfirmPassword = (rule, value, callback) => {
  if (!value) callback(new Error('请确认密码'))
  else if (value !== forgotForm.password) callback(new Error('两次输入的密码不一致'))
  else callback()
}

const forgotRules = reactive({
  phone: [{ validator: validatePhone, trigger: 'blur' }],
  code: [{ validator: validateCode, trigger: 'blur' }],
  password: [
    { validator: validatePassword, trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  confirmPassword: [{ validator: validateConfirmPassword, trigger: 'blur' }]
})

const getForgotCode = async () => {
  try {
    await forgotFormRef.value.validateField('phone')
    const result = await request('/code/send', {
      method: 'POST',
      body: JSON.stringify({ phone: forgotForm.phone, type: 2 })
    })
    if (result && result.code === 200) {
      ElMessage.success('验证码已发送')
      startForgotCountdown()
    } else {
      ElMessage.error(result.message || '发送失败，请重试')
    }
  } catch (error) {
    ElMessage.error(error.message || '发送失败，请重试')
  }
}

const startForgotCountdown = () => {
  forgotIsCountingDown.value = true
  forgotCountdown.value = 60
  if (forgotCountdownTimer) clearInterval(forgotCountdownTimer)
  forgotCountdownTimer = setInterval(() => {
    forgotCountdown.value--
    if (forgotCountdown.value <= 0) {
      clearInterval(forgotCountdownTimer)
      forgotIsCountingDown.value = false
    }
  }, 1000)
}

const handleForgotSubmit = async () => {
  try {
    await forgotFormRef.value.validate()
    forgotLoading.value = true
    const encryptedPassword = await encryptPassword(forgotForm.password, forgotForm.phone)
    const result = await request('/user/pwd/forget', {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        phone: forgotForm.phone,
        code: forgotForm.code,
        password: encryptedPassword
      })
    })
    if (result && result.code === 200) {
      ElMessage.success('密码重置成功')
      setTimeout(() => backToLogin(), 1500)
    } else {
      throw new Error(result?.message || '重置失败，请重试')
    }
  } catch (error) {
    if (error.message !== '验证码已发送') {
      ElMessage.error(error.message || '重置失败，请重试')
    }
  } finally {
    forgotLoading.value = false
  }
}

const passwordRules = reactive({
  phone: [{ validator: validatePhone, trigger: 'blur' }],
  password: [{ validator: validatePassword, trigger: 'blur' }],
  captcha: [{ required: true, message: '请输入图形验证码', trigger: 'blur' }]
})

const smsRules = reactive({
  phone: [{ validator: validatePhone, trigger: 'blur' }],
  captcha: [{ required: true, message: '请输入图形验证码', trigger: 'blur' }]
})

const getCode = async () => {
  try {
    await smsFormRef.value.validateField('phone')
    const result = await request('/code/send', {
      method: 'POST',
      body: JSON.stringify({ phone: loginForm.value.phone, type: 1 })
    })
    if (result && result.code === 200) {
      ElMessage.success('验证码已发送')
      startCountdown()
    } else {
      ElMessage.error(result.message || '发送失败，请重试')
    }
  } catch (error) {
    ElMessage.error(error.message || '发送失败，请重试')
  }
}

const startCountdown = () => {
  isCountingDown.value = true
  countdown.value = 60
  if (countdownTimer) clearInterval(countdownTimer)
  countdownTimer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(countdownTimer)
      isCountingDown.value = false
    }
  }, 1000)
}

const handleLogin = async () => {
  const page = currentPage.value
  console.log('handleLogin called, page:', page)
  try {
      // 账密登录（e.data = 0，type=0）
    if (page === 0) {
      console.log('password login')
      await passwordFormRef.value.validate()
      if (!loginForm.value.captcha) {
        ElMessage.warning('请输入图形验证码')
        return
      }
      if (!loginForm.value.password) {
        ElMessage.warning('请输入密码')
        return
      }
      let captchaValid = false
      try {
        const captchaResult = await request(`/api/capture/captcha/check?key=${loginForm.value.captchaKey}&code=${loginForm.value.captcha}`)
        if (!captchaResult || captchaResult.code !== 200) {
          ElMessage.error('图形验证码错误')
          refreshCaptcha()
          return
        }

      } catch (e) {
        ElMessage.error('图形验证码错误')
        refreshCaptcha()
        return
      }
      // const encryptedPassword = await encryptPassword(loginForm.value.password, loginForm.value.phone)
      const result = await request(`/user/login/check/code?phone=${loginForm.value.phone}&msg=${loginForm.value.password}&type=0`)
      if (result && result.data) {
        const userInfo = result.data
        if (userInfo.id) userInfo.id = userInfo.id.toString()
        localStorage.setItem('userInfo', JSON.stringify(userInfo))
        if (userInfo.token) localStorage.setItem('token', userInfo.token)
        ElMessage({ message: '登录成功', type: 'success', duration: 2000 })
        setTimeout(() => { window.location.href = '/home' }, 2000)
      }
    } else if (page === 2) {
      // 验证码登录（e.data = 2，type=1）
      console.log('sms login')
      await smsFormRef.value.validate()
      if (!loginForm.value.code) {
        ElMessage.warning('请输入短信验证码')
        return
      }
      const codeReg = /^\d{4}$/
      if (!codeReg.test(loginForm.value.code)) {
        ElMessage.warning('验证码为 4 位数字')
        return
      }
      let captchaValid = false
      try {
        const captchaResult = await request(`/api/capture/captcha/check?key=${loginForm.value.captchaKey}&code=${loginForm.value.captcha}`)
        if (!captchaResult || captchaResult.code !== 200) {
          ElMessage.error('图形验证码错误')
          refreshCaptcha()
          return
        }

      } catch (e) {
        console.error('图形验证码错误:', e)
        ElMessage.error('图形验证码错误')
        refreshCaptcha()
        return
      }
      const result = await request(`/user/login/check/code?phone=${loginForm.value.phone}&msg=${loginForm.value.code}&type=1`)
      if (result && result.data) {
        const userInfo = result.data
        if (userInfo.id) userInfo.id = userInfo.id.toString()
        localStorage.setItem('userInfo', JSON.stringify(userInfo))
        if (userInfo.token) localStorage.setItem('token', userInfo.token)
        ElMessage({ message: '登录成功', type: 'success', duration: 2000 })
        setTimeout(() => { window.location.href = '/home' }, 2000)
      }



    } else if (page === 4) {
      // 二维码登录（e.data = 4）
      ElMessage.info('请使用微信扫码登录')
      return
    } else {
      ElMessage.warning(`当前页面索引：${page}，无法登录`)
    }
  } catch (error) {
    console.error('登录验证失败:', error)
    ElMessage.error(error.message || '登录失败，请重试')
  }
}
onMounted(() => {
  refreshCaptcha()
})

onUnmounted(() => {
  stopScanCheck()
  if (countdownTimer) clearInterval(countdownTimer)
  if (forgotCountdownTimer) clearInterval(forgotCountdownTimer)
  if (qrCodeUrl.value) URL.revokeObjectURL(qrCodeUrl.value)
  if (pageFlip) {
    try { pageFlip.destroy() } catch (e) {}
  }
})
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #eaf1fb 0%, #dce8ff 50%, #c9d9fb 100%);
}

.book-cover {
  position: relative;
  width: 450px;
  height: 580px;
  background: linear-gradient(145deg, #2563eb 0%, #3b82f6 50%, #7aa5f0 100%);
  border-radius: 0 20px 20px 0;
  box-shadow: -10px 0 30px rgba(0, 0, 0, 0.2), 0 25px 80px rgba(37,99,235, 0.3), 0 0 0 8px rgba(255, 255, 255, 0.2), inset 0 0 60px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: all 0.5s cubic-bezier(0.68, -0.55, 0.265, 1.55);
  display: flex;
  justify-content: center;
  align-items: center;
}

.book-cover:hover {
  transform: translateX(-5px);
  box-shadow: -15px 0 40px rgba(0, 0, 0, 0.25), 0 35px 100px rgba(37,99,235, 0.4), 0 0 0 8px rgba(255, 255, 255, 0.3), inset 0 0 60px rgba(0, 0, 0, 0.15);
}

.cover-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
  padding-left: 60px;
}

.cover-decoration { text-align: center; }
.cover-title { display: flex; flex-direction: column; gap: 8px; margin-bottom: 40px; }
.main-title { font-size: 64px; font-weight: 900; letter-spacing: 8px; text-shadow: 0 4px 20px rgba(0, 0, 0, 0.2); }
.sub-title { font-size: 20px; letter-spacing: 12px; opacity: 0.9; }
.cover-emoji { font-size: 80px; margin: 30px 0; animation: bounce 2s ease-in-out infinite; }

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-15px); }
}

.cover-hint {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  opacity: 0.9;
  padding: 12px 24px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 30px;
  backdrop-filter: blur(10px);
  transition: all 0.3s;
}

.book-cover:hover .cover-hint {
  background: rgba(255, 255, 255, 0.3);
  gap: 15px;
}

.cover-hint .el-icon { transition: transform 0.3s; }
.book-cover:hover .cover-hint .el-icon { transform: translateX(5px); }

.open-book-container {
  width: 100%;
  max-width: 1000px;
  padding: 20px;
  position: relative;
}

.book-wrapper {
  box-shadow: 0 15px 50px rgba(0, 0, 0, 0.15);
  border-radius: 0 20px 20px 0;
  overflow: hidden;
  opacity: 0;
  transition: opacity 0.5s ease;
}

.book-wrapper.book-loading {
  opacity: 0;
  visibility: hidden;
}

.book-wrapper:not(.book-loading) {
  opacity: 1;
  visibility: visible;
}

.book-page {
  background: linear-gradient(135deg, #f7faff 0%, #eef4ff 100%);
}

.page-content {
  height: 100%;
  padding: 30px 25px;
  display: flex;
  flex-direction: column;
  position: relative;
}

/* 左页欢迎样式 */
.welcome-page-content {
  background: linear-gradient(135deg, #f7faff 0%, #eef4ff 100%);
}

/* 右页登录样式 */
.login-page-content {
  background: linear-gradient(135deg, #f7faff 0%, #eef4ff 100%);
}

.left-page-decoration {
  position: relative;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.decorative-circle {
  position: absolute;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(37,99,235, 0.1) 0%, rgba(59,130,246, 0.05) 100%);
  animation: float 6s ease-in-out infinite;
}

.decorative-circle:nth-child(1) { width: 180px; height: 180px; top: 12%; left: 12%; animation-delay: 0s; }
.decorative-circle:nth-child(2) { width: 140px; height: 140px; bottom: 22%; right: 12%; animation-delay: 2s; }
.decorative-circle:nth-child(3) { width: 90px; height: 90px; top: 50%; left: 50%; animation-delay: 4s; }

@keyframes float {
  0%, 100% { transform: translate(0, 0) scale(1); opacity: 0.6; }
  50% { transform: translate(20px, -30px) scale(1.1); opacity: 1; }
}

.welcome-message { text-align: center; z-index: 10; }
.welcome-title { font-size: 32px; font-weight: 700; color: #2563eb; line-height: 1.6; margin-bottom: 25px; text-shadow: 0 2px 10px rgba(37,99,235, 0.2); }
.welcome-desc { font-size: 14px; color: #666; line-height: 2.2; white-space: pre-line; }

.floating-books {
  position: absolute;
  bottom: 35px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 18px;
}

.floating-book {
  font-size: 28px;
  animation: bookFloat 3s ease-in-out infinite;
}

.floating-book:nth-child(1) { animation-delay: 0s; }
.floating-book:nth-child(2) { animation-delay: 0.5s; }
.floating-book:nth-child(3) { animation-delay: 1s; }
.floating-book:nth-child(4) { animation-delay: 1.5s; }

@keyframes bookFloat {
  0%, 100% { transform: translateY(0) rotate(8deg); }
  50% { transform: translateY(-15px) rotate(-8deg); }
}

/* 登录表单样式 */
.login-page-wrapper {
  height: 100%;
  display: flex;
  flex-direction: column;
  position: relative;
}

.login-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 15px;
}

.welcome-text { font-size: 12px; color: #999; margin: 0; }

.login-form { flex: 1; }

.input-group { display: flex; width: 100%; }
.country-code { width: 80px; }
.country-code :deep(.el-input__wrapper) { border-radius: 8px 0 0 8px !important; border-right: none !important; }
.phone-input { flex: 1; }
.phone-input :deep(.el-input__wrapper) { border-radius: 0 8px 8px 0 !important; }

.code-input-group { display: flex; gap: 8px; width: 100%; }
.code-input { flex: 1; }

.captcha-image {
  width: 105px;
  height: 40px;
  background: #f5f5f5;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
  border: 1.5px solid #e8e8e8;
}

.captcha-image:hover {
  border-color: #2563eb;
  background: #f0f6ff;
}

.captcha-image img {
  width: 100%;
  height: 100%;
  border-radius: 8px;
  object-fit: cover;
}

.captcha-placeholder {
  font-size: 11px;
  color: #999;
}

.get-code-btn {
  width: 105px;
  color: #2563eb;
  border-color: #2563eb;
  font-size: 12px;
  background: rgba(37,99,235, 0.08);
  transition: all 0.3s;
}

.get-code-btn:hover:not(:disabled) {
  color: white;
  background: linear-gradient(135deg, #2563eb 0%, #3b82f6 100%);
  border-color: transparent;
  box-shadow: 0 4px 15px rgba(37,99,235, 0.3);
}

.get-code-btn:disabled { opacity: 0.6; cursor: not-allowed; }

.login-btn {
  width: 100%;
  height: 42px;
  font-size: 14px;
  background: linear-gradient(135deg, #2563eb 0%, #3b82f6 100%);
  border: none;
  border-radius: 10px;
  font-weight: 600;
  transition: all 0.3s;
  box-shadow: 0 4px 15px rgba(37,99,235, 0.25);
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(37,99,235, 0.35);
}

.login-btn:active { transform: translateY(0); }

.login-footer { margin-top: auto; }
.footer-links { display: flex; gap: 12px; font-size: 12px; align-items: center; }
.link { color: #999; cursor: pointer; transition: color 0.3s; }
.link:hover { color: #2563eb; }
.footer-divider { color: #e0e0e0; }
.switch-hint { color: #ccc; font-style: italic; }

/* 二维码登录 */
.qr-login-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 5px 0;
}

.qr-login-header { text-align: center; margin-bottom: 15px; }
.qr-login-header h3 { font-size: 18px; color: #2563eb; margin: 0 0 6px 0; font-weight: 600; }
.qr-login-desc { font-size: 12px; color: #999; margin: 0; }

.qr-code-container {
  width: 180px;
  height: 180px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 18px rgba(0, 0, 0, 0.1);
  position: relative;
  overflow: hidden;
  transition: transform 0.3s;
}

.qr-code-container:hover {
  transform: scale(1.03);
  box-shadow: 0 6px 25px rgba(0, 0, 0, 0.15);
}

.qr-code-image { width: 100%; height: 100%; object-fit: contain; cursor: pointer; }
.qr-code-disabled { cursor: not-allowed; }

.qr-code-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.95);
}

.loading-text { color: #2563eb; font-size: 12px; margin-bottom: 10px; }

.loading-spinner {
  width: 28px;
  height: 28px;
  border: 3px solid rgba(37,99,235, 0.2);
  border-top-color: #2563eb;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin { to { transform: rotate(360deg); } }

.qr-refresh-icon {
  color: #2563eb;
  font-size: 60px;
  cursor: pointer;
  padding: 24px;
  background: rgba(37,99,235, 0.12);
  border-radius: 50%;
  transition: all 0.3s;
}

.qr-refresh-icon:hover {
  background: rgba(37,99,235, 0.25);
  transform: rotate(180deg);
  box-shadow: 0 4px 16px rgba(37,99,235, 0.3);
}

/* 忘记密码 */
.forgot-overlay-wrapper {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  pointer-events: none;
}

.forgot-overlay {
  background: rgba(255, 251, 245, 0.98);
  padding: 28px;
  border-radius: 18px;
  box-shadow: 0 10px 50px rgba(0, 0, 0, 0.2);
  pointer-events: auto;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn { from { opacity: 0; transform: translateY(-10px); } to { opacity: 1; transform: translateY(0); } }

.forgot-password-wrapper { width: 100%; max-width: 320px; }
.forgot-header { display: flex; flex-direction: column; align-items: center; margin-bottom: 20px; }
.forgot-title { font-size: 20px; font-weight: 700; color: #2563eb; margin-bottom: 5px; }
.forgot-desc { font-size: 12px; color: #999; margin: 0; }
.forgot-form { display: flex; flex-direction: column; }

.submit-btn {
  width: 100%;
  height: 42px;
  font-size: 14px;
  background: linear-gradient(135deg, #2563eb 0%, #3b82f6 100%);
  border: none;
  border-radius: 10px;
  font-weight: 600;
  transition: all 0.3s;
  box-shadow: 0 4px 15px rgba(37,99,235, 0.25);
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(37,99,235, 0.35);
}

.back-link { text-align: center; width: 100%; }
.back-link .el-link { color: #2563eb; font-size: 12px; }
.back-link .el-link:hover { color: #3b82f6; }

/* Element Plus */
:deep(.el-input__wrapper) {
  box-shadow: none;
  border: 1.5px solid #e8e8e8;
  border-radius: 8px;
  height: 40px;
  padding: 0 11px;
  transition: all 0.3s;
  background-color: white;
}

:deep(.el-input__wrapper:hover) { border-color: #3b82f6; }

:deep(.el-input.is-focus .el-input__wrapper) {
  box-shadow: 0 0 0 3px rgba(37,99,235, 0.12);
  border-color: #2563eb;
}

:deep(.el-input__inner) { height: 40px; line-height: 40px; font-size: 13px; }
:deep(.el-select .el-input__wrapper) { border-radius: 8px !important; }
:deep(.el-form-item) { margin-bottom: 16px; }
:deep(.el-button) { height: 42px; }

/* page-flip 容器样式 */
:deep(.page-flip-container) {
  border-radius: 0 20px 20px 0;
}

:deep(.page-flip--flipping) {
  cursor: grabbing;
}

/* 响应式 */
@media (max-width: 768px) {
  .book-cover {
    width: 320px;
    height: 450px;
  }

  .book-wrapper {
    border-radius: 15px;
  }

  .floating-books { display: none; }
}
</style>
