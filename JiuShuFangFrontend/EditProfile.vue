<template>
  <!-- 动态背景 -->
  <VitalityBackground />

  <div class="edit-profile-container">
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

    <div class="main-wrapper">
      <div class="main-content">
        <div class="profile-header">
          <h1>编辑基本信息</h1>
        </div>

        <div class="profile-card">
          <div class="avatar-section">
            <div class="avatar-wrapper" @click="triggerAvatarUpload">
              <el-avatar :size="100" :src="form.avatar">
                <el-icon v-if="!form.avatar"><UserFilled /></el-icon>
              </el-avatar>
              <div class="avatar-overlay">
                <el-icon><Camera /></el-icon>
                <span>点击修改头像</span>
              </div>
            </div>
            <input 
              type="file" 
              ref="avatarInput" 
              accept="image/*" 
              style="display: none" 
              @change="handleAvatarChange"
            />
          </div>

          <div class="info-section">
            <el-form :model="form" :rules="rules" ref="formRef" label-width="100px" class="edit-form">
              <div class="form-row">
                <el-form-item label="用户名" prop="nickName">
                  <el-input v-model="form.nickName" placeholder="请输入用户名" maxlength="20"></el-input>
                </el-form-item>

                <el-form-item label="邮箱" prop="email">
                  <el-input v-model="form.email" placeholder="请输入邮箱"></el-input>
                </el-form-item>
              </div>

              <div class="form-row">
                <el-form-item label="性别" prop="sex">
                  <el-select v-model="form.sex" placeholder="请选择性别" style="width: 100%;">
                    <el-option label="男" :value="1"></el-option>
                    <el-option label="女" :value="2"></el-option>
                    <el-option label="未知" :value="0"></el-option>
                  </el-select>
                </el-form-item>

                <el-form-item label="所在城市" prop="city">
                  <el-cascader
                    v-model="selectedCity"
                    :options="regionData"
                    :props="{ value: 'value', label: 'label', children: 'children' }"
                    placeholder="请选择省/市/区"
                    style="width: 100%;"
                    @change="handleCityChange"
                  ></el-cascader>
                </el-form-item>
              </div>
            </el-form>
          </div>

          <div class="action-buttons">
            <button class="action-btn edit-btn" @click="handleSubmit" :disabled="submitting">
              {{ submitting ? '保存中...' : '保存修改' }}
            </button>
            <button class="action-btn cancel-btn" @click="goBack">取消</button>
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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { UserFilled, Camera } from '@element-plus/icons-vue'
import request from './request'
import { regionData, codeToText } from 'element-china-area-data'
import { convertImageUrl, convertToExternalUrl } from './utils/imageUtils'

const router = useRouter()
const formRef = ref(null)
const submitting = ref(false)
const selectedCity = ref([])
const avatarInput = ref(null)

const userInfo = ref({})
const nickName = ref('')
const avatarUrl = ref('')

const form = ref({
  nickName: '',
  email: '',
  sex: 0,
  city: '',
  avatar: ''
})

const uploadingAvatar = ref(false)

const triggerAvatarUpload = () => {
  avatarInput.value?.click()
}

const handleAvatarChange = async (event) => {
  const file = event.target.files[0]
  if (!file) return
  
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('头像大小不能超过 5MB')
    return
  }
  
  if (!file.type.startsWith('image/')) {
    ElMessage.error('请选择图片文件')
    return
  }
  
  uploadingAvatar.value = true
  
  try {
    const formData = new FormData()
    formData.append('file', file) // 修改参数名为 'file' 以匹配后端
    
    const result = await request('/image/picture/upload', {
      method: 'POST',
      body: formData
    })
    
    if (result && result.code === 200 && result.data) {
      form.value.avatar = convertToExternalUrl(result.data)
      ElMessage.success('头像上传成功')
    } else {
      ElMessage.error(result.message || '上传失败')
    }
  } catch (error) {
    console.error('上传头像失败:', error)
    ElMessage.error('上传失败，请重试')
  } finally {
    uploadingAvatar.value = false
    event.target.value = ''
  }
}

const rules = {
  nickName: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

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
  router.push('/profile')
}

const handleCityChange = (value) => {
  if (value && value.length > 0) {
    const province = codeToText[value[0]]
    const city = value.length > 1 ? codeToText[value[1]] : ''
    const district = value.length > 2 ? codeToText[value[2]] : ''
    
    if (province && city && district) {
      form.value.city = `${province}/${city}/${district}`
    } else if (province && city) {
      form.value.city = `${province}/${city}`
    } else if (province) {
      form.value.city = province
    }
  }
}

const getUserInfo = async (id) => {
  try {
    const result = await request(`/user/info/${id}`)
    
    if (result && result.code === 200 && result.data) {
      return result.data
    }
    return null
  } catch (error) {
    console.error('获取用户信息失败:', error)
    ElMessage.error('获取用户信息失败')
    return null
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    
    try {
      const updateData = {
        nickName: form.value.nickName,
        email: form.value.email,
        sex: form.value.sex,
        city: form.value.city,
        avatar: form.value.avatar
      }
      
      const result = await request('/user/update/simple', {
        method: 'PUT',
        body: JSON.stringify(updateData)
      })
      
      if (result && result.code === 200) {
        ElMessage.success('修改成功')
        
        const stored = localStorage.getItem('userInfo')
        if (stored) {
          const user = JSON.parse(stored)
          user.nickName = form.value.nickName
          user.email = form.value.email
          user.sex = form.value.sex
          user.city = form.value.city
          user.avatar = form.value.avatar
          localStorage.setItem('userInfo', JSON.stringify(user))
        }
        
        router.push('/profile')
      } else {
        ElMessage.error(result.message || '修改失败')
      }
    } catch (error) {
      console.error('修改失败:', error)
      ElMessage.error('修改失败')
    } finally {
      submitting.value = false
    }
  })
}

onMounted(async () => {
  const stored = localStorage.getItem('userInfo')
  if (stored) {
    const user = JSON.parse(stored)
    userInfo.value = user
    
    if (user && user.nickName) {
      nickName.value = user.nickName.substring(0, 5)
    }
    if (user && user.avatar) {
      avatarUrl.value = convertToExternalUrl(user.avatar)
    }
    
    if (user && user.id) {
      getUserInfo(user.id).then(latestInfo => {
        if (latestInfo) {
          form.value = {
            nickName: latestInfo.nickName || '',
            email: latestInfo.email || '',
            sex: latestInfo.sex || 0,
            city: latestInfo.city || '',
            avatar: latestInfo.avatar || ''
          }
          
          if (latestInfo.avatar) {
          form.value.avatar = convertToExternalUrl(latestInfo.avatar)
        }
        }
      })
    }
  } else {
    ElMessage.error('请先登录')
    router.push('/')
  }
})
</script>

<style scoped>
.edit-profile-container {
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

a:hover {
  color: #f23030;
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

.user-profile {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 5px 10px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: #f5f5f5;
}

.username {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

:deep(.user-popover) {
  padding: 10px;
}

.user-menu {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.menu-item {
  cursor: pointer;
  padding: 8px 12px;
  color: #666;
  font-size: 14px;
  transition: background-color 0.3s;
}

.menu-item:hover {
  background-color: #f5f5f5;
  color: #f23030;
}

.main-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.main-content {
  width: 1200px;
  margin: 30px auto;
  padding: 30px;
  background-color: #f5f5f5;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.profile-header {
  margin-bottom: 30px;
  padding-bottom: 15px;
  border-bottom: 2px solid #f5f5f5;
}

.profile-header h1 {
  font-size: 24px;
  color: #333;
  font-weight: 600;
}

.profile-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px;
  gap: 30px;
}

.avatar-section {
  margin-bottom: 20px;
  position: relative;
}

.avatar-wrapper {
  position: relative;
  display: inline-block;
  cursor: pointer;
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100px;
  height: 100px;
  background-color: rgba(0, 0, 0, 0.5);
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 12px;
  opacity: 0;
  transition: opacity 0.3s;
  gap: 5px;
}

.avatar-wrapper:hover .avatar-overlay {
  opacity: 1;
}

.avatar-overlay .el-icon {
  font-size: 24px;
}

.info-section {
  width: 100%;
  max-width: 800px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.edit-form {
  width: 100%;
}

.form-row {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

.form-row .el-form-item {
  flex: 1;
  margin-bottom: 0;
}

.action-buttons {
  display: flex;
  gap: 20px;
  margin-top: 30px;
}

.action-btn {
  padding: 12px 40px;
  border: none;
  border-radius: 25px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.edit-btn {
  background-color: #dbeafe;
  color: #f23030;
}

.edit-btn:hover {
  background-color: #c7d9f5;
}

.edit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.cancel-btn {
  background-color: #f5f5f5;
  color: #666;
}

.cancel-btn:hover {
  background-color: #e5e5e5;
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
  color: #666;
}
</style>
