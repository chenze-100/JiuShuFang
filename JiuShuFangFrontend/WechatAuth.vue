<template>
  <div class="auth-container">
    <div class="auth-content">
      <div class="pc-icon">
        <div class="pc-screen">PC</div>
      </div>
      <h2 class="auth-title">微信账号电脑端登录确认</h2>
      <p class="auth-desc">请在手机上确认是否登录电脑端</p>
      <button class="confirm-btn" :class="{ 'btn-success': loginSuccess }" @click="handleConfirm" :disabled="loginSuccess">
        {{ loginSuccess ? '登录成功' : '确认登录电脑端' }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

const loginSuccess = ref(false)

const handleConfirm = async () => {
  // Hash 模式下，参数在 hash 中
  const hash = window.location.hash
  const queryString = hash.split('?')[1] || ''
  const urlParams = new URLSearchParams(queryString)
  const loginId = urlParams.get('loginId')
  
  if (!loginId) {
    ElMessage.error('参数错误')
    return
  }
  
  try {
    // 调用确认登录接口
    const response = await fetch(`/user/login/confirm?loginId=${loginId}`)
    const result = await response.json()
    
    if (result && result.code === 200) {
      const userInfo = result.data
      localStorage.setItem('userInfo', JSON.stringify(userInfo))
      loginSuccess.value = true
      // 保存用户信息后跳转到首页
      ElMessage.success('登录成功')
      setTimeout(() => {
        window.location.href = '/#/home'
      }, 1000)
    } else {
      ElMessage.error(result.message || '登录失败')
    }
  } catch (error) {
    console.error('登录失败:', error)
    ElMessage.error('登录失败，请重试')
  }
}
</script>

<style scoped>
.auth-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.auth-content {
  background: #fff;
  border-radius: 12px;
  padding: 40px;
  text-align: center;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
  max-width: 400px;
  width: 90%;
}

.pc-icon {
  width: 120px;
  height: 80px;
  margin: 0 auto 30px;
  position: relative;
}

.pc-screen {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  font-weight: bold;
  color: #fff;
  border: 4px solid #3b82f6;
}

.pc-icon::after {
  content: '';
  position: absolute;
  bottom: -10px;
  left: 50%;
  transform: translateX(-50%);
  width: 40px;
  height: 4px;
  background: #3b82f6;
  border-radius: 2px;
}

.auth-title {
  font-size: 20px;
  color: #333;
  margin: 0 0 10px 0;
  font-weight: 600;
}

.auth-desc {
  font-size: 14px;
  color: #666;
  margin: 0 0 30px 0;
}

.confirm-btn {
  width: 100%;
  height: 48px;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  border: none;
  border-radius: 24px;
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(59,130,246, 0.3);
}

.confirm-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(59,130,246, 0.4);
}

.confirm-btn:active {
  transform: translateY(0);
}

.confirm-btn.btn-success {
  background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
  box-shadow: 0 4px 15px rgba(89, 196, 26, 0.3);
}

.confirm-btn.btn-success:hover {
  box-shadow: 0 6px 20px rgba(89, 196, 26, 0.4);
}
</style>
