<template>
  <div class="forgot-password-page">
    <div class="page-container">
      <div class="page-header">
        <h1>找回密码</h1>
      </div>
      
      <div class="page-body">
        <el-form 
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="80px"
          class="forgot-form"
        >
          <el-form-item label="手机号" prop="phone">
            <el-input 
              v-model="form.phone" 
              placeholder="请输入手机号"
              maxlength="11"
            />
          </el-form-item>
          
          <el-form-item label="验证码" prop="code">
            <div class="code-input-group">
              <el-input 
                v-model="form.code" 
                placeholder="请输入验证码"
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
          
          <el-form-item label="新密码" prop="password">
            <el-input 
              v-model="form.password" 
              type="password"
              placeholder="请输入 6-20 位新密码"
              maxlength="20"
              show-password
            />
          </el-form-item>
          
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input 
              v-model="form.confirmPassword" 
              type="password"
              placeholder="请再次输入新密码"
              maxlength="20"
              show-password
            />
          </el-form-item>
          
          <el-form-item>
            <el-button 
              type="primary" 
              class="submit-btn" 
              @click="handleSubmit"
              :loading="loading"
              style="width: 100%"
            >
              确定
            </el-button>
          </el-form-item>
          
          <el-form-item>
            <div class="back-link">
              <el-link @click="handleBack">返回登录</el-link>
            </div>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from './request'
import { encryptPassword } from './crypto'

const formRef = ref(null)
const loading = ref(false)
const isCountingDown = ref(false)
const countdown = ref(60)
let countdownTimer = null

const form = reactive({
  phone: '',
  code: '',
  password: '',
  confirmPassword: ''
})

const validatePhone = (rule, value, callback) => {
  const phoneReg = /^1[3-9]\d{9}$/
  if (!value) {
    callback(new Error('请输入手机号'))
  } else if (!phoneReg.test(value)) {
    callback(new Error('请输入正确的手机号'))
  } else {
    callback()
  }
}

const validateCode = (rule, value, callback) => {
  const codeReg = /^\d{6}$/
  if (!value) {
    callback(new Error('请输入验证码'))
  } else if (!codeReg.test(value)) {
    callback(new Error('验证码为 6 位数字'))
  } else {
    callback()
  }
}

const validatePassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入密码'))
  } else if (value.length < 6 || value.length > 20) {
    callback(new Error('密码长度为 6-20 位'))
  } else {
    callback()
  }
}

const validateConfirmPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请确认密码'))
  } else if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = reactive({
  phone: [{ validator: validatePhone, trigger: 'blur' }],
  code: [{ validator: validateCode, trigger: 'blur' }],
  password: [
    { validator: validatePassword, trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  confirmPassword: [{ validator: validateConfirmPassword, trigger: 'blur' }]
})

const getCode = async () => {
  try {
    await formRef.value.validateField('phone')
    
    // 调用后端发送验证码接口
    // type: 2-忘记密码
    const result = await request('/code/send', {
      method: 'POST',
      body: JSON.stringify({
        phone: form.phone,
        type: 2  // 忘记密码验证码
      })
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
  
  if (countdownTimer) {
    clearInterval(countdownTimer)
  }
  
  countdownTimer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(countdownTimer)
      isCountingDown.value = false
    }
  }, 1000)
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    loading.value = true
    
    // 加密密码：MD5(password + phone)
    console.log('原始密码:', form.password)
    console.log('手机号:', form.phone)
    const encryptedPassword = await encryptPassword(form.password, form.phone)
    console.log('加密后密码:', encryptedPassword)
    
    const requestData = {
      phone: form.phone,
      code: form.code,
      password: encryptedPassword
    }
    console.log('请求数据:', requestData)
    
    // 调用后端重置密码接口
    const result = await request('/user/pwd/forget', {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(requestData)
    })
    
    if (result && result.code === 200) {
      ElMessage.success('密码重置成功')
      // 在当前页面中，重置成功后跳转回登录页面
      setTimeout(() => {
        window.location.href = '/'
      }, 1500)
    } else {
      throw new Error(result?.message || '重置失败，请重试')
    }
  } catch (error) {
    if (error.message !== '验证码已发送') {
      ElMessage.error(error.message || '重置失败，请重试')
    }
  } finally {
    loading.value = false
  }
}

const handleBack = () => {
  window.location.href = '/'
}

onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
  }
})
</script>

<style scoped>
.forgot-password-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
}

.page-container {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 450px;
  padding: 40px;
}

.page-header {
  text-align: center;
  margin-bottom: 40px;
}

.page-header h1 {
  margin: 0;
  font-size: 24px;
  color: #333;
  font-weight: 500;
}

.page-body {
  padding: 0 20px;
}

.forgot-form {
  max-width: 400px;
  margin: 0 auto;
}

.code-input-group {
  display: flex;
  gap: 10px;
}

.code-input-group .el-input {
  flex: 1;
}

.get-code-btn {
  white-space: nowrap;
}

.submit-btn {
  margin-top: 20px;
  background: #2563eb;
  border: 1px solid #2563eb;
  color: #fff;
}

.submit-btn:hover {
  background: #1d4ed8;
  border-color: #1d4ed8;
}

.back-link {
  text-align: center;
  margin-top: 20px;
}

.back-link .el-link {
  color: #2563eb;
}

.back-link .el-link:hover {
  color: #1d4ed8;
}
</style>
