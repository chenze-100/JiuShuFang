<template>
  <!-- 动态背景 -->
  <VitalityBackground />

  <div class="address-page">
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
        <div class="address-header">
          <h1>收货地址</h1>
          <span class="back-home-link" @click="goBackHome">返回首页</span>
        </div>

        <div class="address-list" v-if="addressList.length > 0">
          <div class="address-item" v-for="item in addressList" :key="item.id" :class="{ 'default-address': item.isDefault }">
            <div class="address-top">
              <div class="address-info">
                <span class="receiver-name">{{ item.receiver }}</span>
                <span class="receiver-phone">{{ item.phone }}</span>
              </div>
              <div class="address-actions">
                <button class="action-btn delete-btn" @click="deleteAddress(item.id)">×</button>
              </div>
            </div>

            <div class="address-detail">
              <div class="address-tags" v-if="item.isDefault">
                <span class="tag default-tag">已设默认</span>
              </div>
              <div class="address-text">
                <span class="province">{{ item.province }}</span>
                <span class="city">{{ item.city }}</span>
                <span class="district">{{ item.district }}</span>
                <span class="detail">{{ item.detail }}</span>
              </div>
            </div>

            <div class="address-bottom">
              <div class="bottom-left">
                <el-radio 
                  v-model="defaultAddressId" 
                  :label="item.id"
                  @change="setDefaultAddress(item)"
                  :disabled="item.isDefault"
                >
                  默认
                </el-radio>
              </div>
              <div class="bottom-right">
                <button class="link-btn" @click="setTopAddress(item.id)">置顶</button>
                <button class="link-btn" @click="copyAddress(item)">复制</button>
                <button class="link-btn" @click="editAddress(item)">修改</button>
              </div>
            </div>
          </div>
        </div>

        <!-- 空地址提示 -->
        <div class="empty-address" v-else>
          <div class="empty-icon">📍</div>
          <div class="empty-text">暂无收货地址</div>
          <button class="add-address-btn" @click="addNewAddress">新增地址</button>
        </div>

        <!-- 新增地址按钮（有地址时显示） -->
        <div class="add-new-btn-container" v-if="addressList.length > 0">
          <button class="add-new-btn" @click="addNewAddress">
            <span class="plus-icon">+</span> 新增地址
          </button>
        </div>
      </div>
    </div>

    <!-- 新增/编辑地址对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEditMode ? '修改收货地址' : '新增收货地址'"
      width="600px"
      :close-on-click-modal="false"
      @close="resetForm"
    >
      <el-form
        ref="addressFormRef"
        :model="addressForm"
        :rules="addressRules"
        label-width="80px"
      >
        <el-form-item label="收件人" prop="receiver">
          <el-input v-model="addressForm.receiver" placeholder="请输入收件人姓名" />
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input v-model="addressForm.phone" placeholder="请输入手机号" maxlength="11" />
        </el-form-item>

        <el-form-item label="所在地区" prop="region">
          <el-cascader
            v-model="addressForm.region"
            :options="regionOptions"
            placeholder="请选择省市区"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="详细地址" prop="detail">
          <el-input
            v-model="addressForm.detail"
            type="textarea"
            :rows="3"
            placeholder="请输入详细地址，如街道、小区、门牌号等"
          />
        </el-form-item>

        <el-form-item label="设为默认">
          <el-switch v-model="addressForm.isDefault" />
          <span style="margin-left: 10px; color: var(--color-neutral-500); font-size: 13px;">设为默认后，该地址将作为默认收货地址</span>
        </el-form-item>
      </el-form>

      <template #footer>
        <div style="text-align: center; padding: 10px 0;">
          <el-button 
            @click="dialogVisible = false" 
            style="border-radius: 20px; padding: 10px 30px; font-size: 15px;"
          >
            取消
          </el-button>
          <el-button 
            type="primary" 
            @click="submitAddress" 
            :loading="submitting" 
            style="border-radius: 20px; padding: 10px 30px; font-size: 15px; background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-error) 100%); border: none;"
          >
            确定
          </el-button>
        </div>
      </template>
    </el-dialog>

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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UserFilled } from '@element-plus/icons-vue'
import request from './request'
import { regionData, codeToText } from 'element-china-area-data'

const router = useRouter()

const userInfo = ref(null)
const nickName = ref('')
const avatarUrl = ref('')

const addressList = ref([])
const defaultAddressId = ref(null)
const loading = ref(false)

// 对话框相关
const dialogVisible = ref(false)
const submitting = ref(false)
const addressFormRef = ref(null)
const isEditMode = ref(false)
const editingAddressId = ref(null)
const addressForm = ref({
  receiver: '',
  phone: '',
  region: [],
  detail: '',
  isDefault: false
})

// 表单验证规则
const addressRules = {
  receiver: [
    { required: true, message: '请输入收件人姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  region: [
    { required: true, message: '请选择省市区', trigger: 'change' }
  ],
  detail: [
    { required: true, message: '请输入详细地址', trigger: 'blur' }
  ]
}

// 地区选项（使用 element-china-area-data 的完整数据）
const regionOptions = regionData

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

// 获取地址列表
const getAddressList = async () => {
  if (loading.value) return
  
  loading.value = true
  
  try {
    const result = await request('/address/list')
    
    if (result && result.code === 200 && result.data) {
      addressList.value = result.data
      // 找到默认地址
      const defaultAddr = addressList.value.find(item => item.isDefault)
      if (defaultAddr) {
        defaultAddressId.value = defaultAddr.id
      }
    } else {
      ElMessage.error(result.message || '获取地址列表失败')
    }
  } catch (error) {
    console.error('获取地址列表失败:', error)
    ElMessage.error('获取地址列表失败')
  } finally {
    loading.value = false
  }
}

// 设置默认地址
const setDefaultAddress = async (item) => {
  try {
    const result = await request(`/address/${item.id}/default`, {
      method: 'PUT'
    })
    
    if (result && result.code === 200) {
      ElMessage.success('默认地址设置成功')
      // 更新列表
      addressList.value.forEach(addr => {
        addr.isDefault = (addr.id === item.id)
      })
      defaultAddressId.value = item.id
    } else {
      ElMessage.error(result.message || '设置失败')
    }
  } catch (error) {
    console.error('设置默认地址失败:', error)
    ElMessage.error('设置失败')
  }
}

// 置顶地址
const setTopAddress = async (id) => {
  ElMessage.info('置顶功能暂未实现')
}

// 复制地址
const copyAddress = async (item) => {
  const addressText = `${item.receiver} ${item.phone}\n${item.province}${item.city}${item.district}${item.detail}`
  
  try {
    await navigator.clipboard.writeText(addressText)
    ElMessage.success('地址已复制到剪贴板')
  } catch (error) {
    ElMessage.error('复制失败')
  }
}

// 修改地址
const editAddress = (item) => {
  isEditMode.value = true
  editingAddressId.value = item.id
  
  // 将省市区文本转换为代码（需要反向查找）
  let regionCode = []
  
  // 遍历regionData找到对应的代码
  for (const province of regionOptions) {
    if (province.label === item.province) {
      regionCode.push(province.value)
      if (province.children) {
        for (const city of province.children) {
          if (city.label === item.city) {
            regionCode.push(city.value)
            if (city.children) {
              for (const district of city.children) {
                if (district.label === item.district) {
                  regionCode.push(district.value)
                  break
                }
              }
            }
            break
          }
        }
      }
      break
    }
  }
  
  addressForm.value = {
    receiver: item.receiver,
    phone: item.phone,
    region: regionCode,
    detail: item.detail,
    isDefault: item.isDefault
  }
  
  dialogVisible.value = true
}

// 删除地址
const deleteAddress = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该地址吗？删除后无法恢复', '删除确认', {
      confirmButtonText: '确定删除',
      cancelButtonText: '再想想',
      type: 'warning',
      confirmButtonClass: 'el-button--danger',
      customClass: 'delete-confirm-dialog'
    })
    
    const result = await request(`/address/${id}`, {
      method: 'DELETE'
    })
    
    if (result && result.code === 200) {
      ElMessage.success('地址删除成功')
      addressList.value = addressList.value.filter(item => item.id !== id)
      if (defaultAddressId.value === id) {
        defaultAddressId.value = null
      }
    } else {
      ElMessage.error(result.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 新增地址
const addNewAddress = () => {
  isEditMode.value = false
  editingAddressId.value = null
  dialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  if (addressFormRef.value) {
    addressFormRef.value.resetFields()
  }
  isEditMode.value = false
  editingAddressId.value = null
  addressForm.value = {
    receiver: '',
    phone: '',
    region: [],
    detail: '',
    isDefault: false
  }
}

// 提交地址
const submitAddress = async () => {
  if (!addressFormRef.value) return
  
  await addressFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    
    try {
      // region 数组是代码，需要转换为文本名称
      const [provinceCode, cityCode, districtCode] = addressForm.value.region
      const province = codeToText[provinceCode]
      const city = codeToText[cityCode]
      const district = codeToText[districtCode]
      
      const requestData = {
        receiver: addressForm.value.receiver,
        phone: addressForm.value.phone,
        province: province,
        city: city,
        district: district,
        detail: addressForm.value.detail,
        isDefault: addressForm.value.isDefault
      }
      
      let result
      if (isEditMode.value && editingAddressId.value) {
        // 编辑模式
        requestData.id = editingAddressId.value
        result = await request('/address', {
          method: 'PUT',
          body: JSON.stringify(requestData)
        })
      } else {
        // 新增模式
        result = await request('/address', {
          method: 'POST',
          body: JSON.stringify(requestData)
        })
      }
      
      if (result && result.code === 200) {
        ElMessage.success(isEditMode.value ? '地址修改成功' : '地址添加成功')
        dialogVisible.value = false
        // 重新加载地址列表
        await getAddressList()
      } else {
        ElMessage.error(result.message || (isEditMode.value ? '修改失败' : '添加失败'))
      }
    } catch (error) {
      console.error('操作失败:', error)
      ElMessage.error(isEditMode.value ? '修改失败' : '添加失败')
    } finally {
      submitting.value = false
    }
  })
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
    await getAddressList()
  } else {
    ElMessage.warning('请先登录')
    router.push('/login')
  }
})
</script>

<style>
/* 删除确认对话框样式 */
.delete-confirm-dialog .el-message-box__header {
  background: linear-gradient(135deg, #fff5f5 0%, #ffecec 100%);
  border-bottom: 1px solid #ffe0e0;
}

.delete-confirm-dialog .el-message-box__title {
  color: var(--color-error);
  font-weight: 600;
}

.delete-confirm-dialog .el-message-box__content {
  padding: 30px 20px;
}

.delete-confirm-dialog .el-message-box__message {
  color: #666;
  font-size: 15px;
}

.delete-confirm-dialog .el-button--danger {
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-error) 100%);
  border: none;
  border-radius: 20px;
  padding: 10px 30px;
  font-size: 15px;
  transition: all 0.3s;
}

.delete-confirm-dialog .el-button--danger:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(242, 48, 48, 0.4);
}

.delete-confirm-dialog .el-button--default {
  border-radius: 20px;
  padding: 10px 30px;
  font-size: 15px;
  border-color: #e5e5e5;
  color: #666;
}

.delete-confirm-dialog .el-button--default:hover {
  border-color: #2563eb;
  color: #2563eb;
  background-color: #f0f6ff;
}
</style>

<style scoped>
.address-page {
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
  color: var(--color-neutral-500);
  font-size: 14px;
}

.nav-right {
  display: flex;
  gap: 20px;
  align-items: center;
}

.nav-item {
  color: var(--color-neutral-500);
  font-size: 14px;
}

.nav-item a {
  color: var(--color-neutral-500);
  text-decoration: none;
}

.nav-item a:hover {
  color: var(--color-error);
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
  background-color: var(--color-bg-tertiary);
}

.username {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.main-wrapper {
  display: flex;
  flex-direction: column;
  min-height: calc(100vh - 200px);
}

.main-content {
  flex: 1;
  max-width: 1200px;
  margin: 30px auto;
  width: 100%;
  padding: 30px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.address-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 30px;
}

.address-header h1 {
  font-size: 24px;
  color: #333;
  margin: 0;
  font-weight: 600;
}

.back-home-link {
  color: var(--color-neutral-500);
  font-size: 14px;
  cursor: pointer;
  transition: color 0.3s;
}

.back-home-link:hover {
  color: var(--color-error);
}

.address-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.address-item {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
  position: relative;
}

.address-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.address-item.default-address {
  border: 1px solid #2563eb;
}

.address-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.address-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.receiver-name {
  font-size: 16px;
  color: #333;
  font-weight: 600;
}

.receiver-phone {
  font-size: 15px;
  color: #666;
}

.address-actions {
  display: flex;
  gap: 10px;
}

.delete-btn {
  width: 32px;
  height: 32px;
  border: 2px solid #e5e5e5;
  background-color: #fff;
  color: var(--color-neutral-500);
  font-size: 18px;
  cursor: pointer;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
  padding: 0;
  line-height: 1;
}

.delete-btn:hover {
  border-color: var(--color-error);
  background-color: #fff5f5;
  color: var(--color-error);
  transform: scale(1.1);
}

.delete-btn:active {
  transform: scale(0.95);
}

.address-detail {
  margin-bottom: 15px;
}

.address-tags {
  margin-bottom: 8px;
}

.default-tag {
  display: inline-block;
  padding: 3px 10px;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-error) 100%);
  color: #fff;
  font-size: 12px;
  border-radius: 12px;
  font-weight: 600;
  box-shadow: 0 2px 6px rgba(242, 48, 48, 0.3);
}

.common-tag {
  display: inline-block;
  padding: 3px 10px;
  background: linear-gradient(135deg, #66b3ff 0%, #3399ff 100%);
  color: #fff;
  font-size: 12px;
  border-radius: 12px;
  font-weight: 600;
  box-shadow: 0 2px 6px rgba(51, 153, 255, 0.3);
  margin-right: 8px;
}

.address-text {
  font-size: 15px;
  color: #666;
  line-height: 1.6;
}

.address-text span {
  margin-right: 5px;
}

.address-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 15px;
  border-top: 1px solid #f5f5f5;
}

.bottom-left {
  display: flex;
  align-items: center;
}

.bottom-right {
  display: flex;
  gap: 15px;
}

.link-btn {
  background: none;
  border: none;
  color: var(--color-neutral-500);
  font-size: 14px;
  cursor: pointer;
  padding: 0;
  transition: color 0.3s;
  position: relative;
}

.link-btn::after {
  content: '|';
  position: absolute;
  right: -10px;
  color: #e5e5e5;
}

.link-btn:last-child::after {
  display: none;
}

.link-btn:hover {
  color: #2563eb;
}

.empty-address {
  background-color: #fff;
  border-radius: 8px;
  padding: 80px 20px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.empty-icon {
  font-size: 80px;
  margin-bottom: 20px;
  animation: bounce 2s infinite;
}

@keyframes bounce {
  0%, 20%, 50%, 80%, 100% {
    transform: translateY(0);
  }
  40% {
    transform: translateY(-20px);
  }
  60% {
    transform: translateY(-10px);
  }
}

.empty-text {
  font-size: 18px;
  color: var(--color-neutral-500);
  margin-bottom: 30px;
}

.add-address-btn {
  padding: 12px 50px;
  background: linear-gradient(135deg, var(--color-primary-soft) 0%, var(--color-primary-light) 100%);
  color: var(--color-error);
  border: none;
  border-radius: 25px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(242, 48, 48, 0.15);
}

.add-address-btn:hover {
  background: linear-gradient(135deg, #c7d9f5 0%, #a8c4f0 100%);
  box-shadow: 0 4px 12px rgba(242, 48, 48, 0.25);
  transform: translateY(-2px);
}

.add-address-btn:active {
  transform: translateY(0);
}

.add-new-btn-container {
  margin-top: 20px;
  text-align: center;
  padding-bottom: 30px;
}

.add-new-btn {
  padding: 12px 50px;
  background: linear-gradient(135deg, var(--color-primary-soft) 0%, var(--color-primary-light) 100%);
  color: var(--color-error);
  border: none;
  border-radius: 25px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  display: inline-flex;
  align-items: center;
  gap: 10px;
  box-shadow: 0 2px 8px rgba(242, 48, 48, 0.15);
}

.add-new-btn:hover {
  background: linear-gradient(135deg, #c7d9f5 0%, #a8c4f0 100%);
  box-shadow: 0 4px 12px rgba(242, 48, 48, 0.25);
  transform: translateY(-2px);
}

.add-new-btn:active {
  transform: translateY(0);
}

.plus-icon {
  font-size: 22px;
  font-weight: bold;
  line-height: 1;
}

.footer {
  background-color: var(--color-bg-tertiary);
  border-top: 1px solid #e5e5e5;
  padding: 30px 0;
  margin-top: auto;
}

.footer-content {
  max-width: 1200px;
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
  color: var(--color-error);
}

.copyright {
  color: var(--color-neutral-500);
  font-size: 13px;
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
  background-color: var(--color-bg-tertiary);
  color: var(--color-error);
}
</style>
