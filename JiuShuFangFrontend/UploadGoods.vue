<template>
  <!-- 动态背景 -->
  <VitalityBackground />

  <div class="upload-goods-container">
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
      <div class="form-header">
        <h2>{{ isEdit ? '修改商品' : '上架商品' }}</h2>
        <span class="back-link" @click="goBack">返回我的商铺</span>
      </div>

      <div class="upload-form">
        <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
          <el-form-item label="书籍名称" prop="bookName">
            <el-input v-model="form.bookName" placeholder="请输入书籍名称" />
          </el-form-item>

          <el-form-item label="作者" prop="author">
            <el-input v-model="form.author" placeholder="请输入作者" />
          </el-form-item>

          <el-form-item label="出版社" prop="publisher">
            <el-input v-model="form.publisher" placeholder="请输入出版社" />
          </el-form-item>

          <el-form-item label="出售价格" prop="price">
            <el-input-number v-model="form.price" :min="0.01" :precision="2" :step="1" />
          </el-form-item>

          <el-form-item label="原价" prop="originalPrice">
            <el-input-number v-model="form.originalPrice" :min="0.01" :precision="2" :step="1" />
          </el-form-item>

          <el-form-item label="库存数量" prop="stock">
            <el-input-number v-model="form.stock" :min="1" :step="1" />
          </el-form-item>

          <el-form-item label="成色" prop="degree">
            <el-select v-model="form.degree" placeholder="请选择成色">
              <el-option label="全新" :value="1" />
              <el-option label="九成新" :value="2" />
              <el-option label="八成新" :value="3" />
              <el-option label="七成新及以下" :value="4" />
            </el-select>
          </el-form-item>

          <el-form-item label="分类" prop="categoryId">
            <el-select v-model="form.categoryId" placeholder="请选择分类">
              <el-option
                v-for="item in categories"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="书籍封面" prop="bookImg">
            <el-upload
              class="avatar-uploader"
              action="/api/image/picture/upload"
              name="file"
              :headers="uploadHeaders"
              :show-file-list="false"
              :on-success="handleUploadSuccess"
              :before-upload="beforeUpload"
            >
              <img v-if="form.bookImg" :src="convertToExternalUrl(form.bookImg)" class="avatar" />
              <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
            </el-upload>
          </el-form-item>

          <el-form-item label="描述" prop="description">
            <el-input
              v-model="form.description"
              type="textarea"
              :rows="4"
              placeholder="请输入新旧程度、笔记、破损等描述信息"
            />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="handleSubmit" :loading="submitting">{{ isEdit ? '确认修改' : '提交上架' }}</el-button>
            <el-button @click="goBack">取消</el-button>
          </el-form-item>
        </el-form>
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
import VitalityBackground from './components/VitalityBackground.vue'
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { UserFilled, Plus } from '@element-plus/icons-vue'
import request from './request'
import { convertToExternalUrl } from './utils/imageUtils'

const router = useRouter()
const route = useRoute()

const userInfo = ref(null)
const nickName = ref('')
const avatarUrl = ref('')
const formRef = ref(null)
const submitting = ref(false)
const categories = ref([])
const isEdit = ref(false)
const goodsId = ref(null)

const form = ref({
  bookName: '物理',
  author: '陈泽钦',
  publisher: '泽',
  price: 100,
  originalPrice: 190,
  stock: 1,
  degree: null,
  categoryId: null,
  bookImg: '',
  description: 'dd'
})

const rules = {
  bookName: [{ required: true, message: '请输入书籍名称', trigger: 'blur' }],
  author: [{ required: true, message: '请输入作者', trigger: 'blur' }],
  publisher: [{ required: true, message: '请输入出版社', trigger: 'blur' }],
  price: [{ required: true, message: '请输入出售价格', trigger: 'blur' }],
  originalPrice: [{ required: true, message: '请输入原价', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存数量', trigger: 'blur' }],
  degree: [{ required: true, message: '请选择成色', trigger: 'change' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  bookImg: [{ required: true, message: '请上传书籍封面', trigger: 'change' }],
  description: [{ required: true, message: '请输入描述', trigger: 'blur' }]
}

const uploadHeaders = computed(() => ({
  token: localStorage.getItem('userInfo') ? JSON.parse(localStorage.getItem('userInfo')).token || '' : ''
}))

const getUserInfo = () => {
  const info = localStorage.getItem('userInfo')
  if (info) {
    const user = JSON.parse(info)
    userInfo.value = user
    nickName.value = user.nickName || user.username || '用户'
    avatarUrl.value = user.avatar || ''
  }
}

// 加载商品数据用于编辑回显
const loadGoods = async (id) => {
  try {
    const result = await request(`/goods/${id}`, { method: 'GET' })
    if (result && result.code === 200 && result.data) {
      const g = result.data
      form.value = {
        bookName: g.bookName,
        author: g.author,
        publisher: g.publisher,
        price: g.price,
        originalPrice: g.originalPrice,
        stock: g.stock,
        degree: g.degree,
        categoryId: g.categoryId,
        bookImg: g.bookImg,
        description: g.description
      }
    }
  } catch (error) {
    console.error('加载商品失败:', error)
    ElMessage.error('加载商品失败')
  }
}

const loadCategories = async () => {
  try {
    const result = await request('/category/list', { method: 'GET' })
    if (result && result.code === 200 && result.data && result.data.list) {
      categories.value = result.data.list
    }
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

const handleUploadSuccess = (response) => {
  if (response && response.code === 200 && response.data) {
    form.value.bookImg = response.data.url || response.data
  } else {
    ElMessage.error('上传失败')
  }
}

const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!')
  }
  return isImage && isLt5M
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        const payload = { ...form.value, id: goodsId.value }
        const url = isEdit.value ? '/api/goods/update' : '/api/goods/upload'
        const method = isEdit.value ? 'PUT' : 'POST'

        const result = await request(url, {
          method,
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(payload)
        })

        if (result && result.code === 200) {
          ElMessage.success(isEdit.value ? '商品修改成功' : '商品上架成功')
          router.push('/my-shop')
        } else {
          ElMessage.error(result.message || (isEdit.value ? '修改失败' : '上架失败'))
        }
      } catch (error) {
        console.error(isEdit.value ? '修改商品失败:' : '上架商品失败:', error)
        ElMessage.error(isEdit.value ? '修改商品失败' : '上架商品失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

const goBack = () => {
  router.push('/my-shop')
}

const goToShoppingCart = () => {
  router.push('/shopping-cart')
}

const goToProfile = () => {
  router.push('/profile')
}

const goToOrderList = () => {
  router.push('/order-list')
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

onMounted(() => {
  getUserInfo()
  loadCategories()

  const id = route.query.goodsId
  if (id) {
    goodsId.value = Number(id)
    isEdit.value = true
    loadGoods(goodsId.value)
  }
})
</script>

<style scoped>
.upload-goods-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: transparent; position: relative; z-index: 1;
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

.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 20px 40px;
  display: flex;
  flex-direction: column;
  gap: 24px;
  width: 100%;
  flex: 1;
}

.form-header {
  background: linear-gradient(135deg, var(--color-primary-soft) 0%, var(--color-bg-secondary) 100%);
  padding: 24px 30px;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  margin-bottom: 24px;
  border: 1px solid var(--color-primary-light);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.form-header h2 {
  font-size: 24px;
  font-weight: 600;
  color: var(--color-neutral-700);
  margin: 0;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-dark) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.back-link {
  font-size: 14px;
  color: var(--color-neutral-500);
  cursor: pointer;
  transition: all var(--transition-fast);
  padding: 8px 16px;
  background: linear-gradient(135deg, var(--color-bg-secondary) 0%, var(--color-primary-soft) 100%);
  border: 2px solid var(--color-primary-light);
  border-radius: var(--radius-pill);
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.back-link:hover {
  color: var(--color-primary);
  border-color: var(--color-primary);
  background: linear-gradient(135deg, var(--color-primary-soft) 0%, var(--color-primary-light) 100%);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.upload-form {
  background: white;
  padding: 30px;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--color-neutral-100);
  flex: 1;
}

.upload-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: var(--color-neutral-700);
}

.upload-form :deep(.el-input__wrapper) {
  border: 2px solid var(--color-neutral-100);
  border-radius: var(--radius-sm);
  box-shadow: none;
  transition: all var(--transition-fast);
  background: transparent; position: relative; z-index: 1;
}

.upload-form :deep(.el-input__wrapper:hover) {
  border-color: var(--color-primary);
}

.upload-form :deep(.el-input__wrapper.is-focus) {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(37,99,235, 0.1);
}

.upload-form :deep(.el-input-number__wrapper) {
  border: 2px solid var(--color-neutral-100);
  border-radius: var(--radius-sm);
  box-shadow: none;
  transition: all var(--transition-fast);
  background: transparent; position: relative; z-index: 1;
}

.upload-form :deep(.el-input-number__wrapper:hover) {
  border-color: var(--color-primary);
}

.upload-form :deep(.el-input-number__wrapper.is-focus) {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(37,99,235, 0.1);
}

.upload-form :deep(.el-select .el-input__wrapper) {
  border: 2px solid var(--color-neutral-100);
  border-radius: var(--radius-sm);
  box-shadow: none;
  transition: all var(--transition-fast);
  background: transparent; position: relative; z-index: 1;
}

.upload-form :deep(.el-select:hover .el-input__wrapper) {
  border-color: var(--color-primary);
}

.upload-form :deep(.el-select.is-focused .el-input__wrapper) {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(37,99,235, 0.1);
}

.upload-form :deep(.el-textarea__inner) {
  border: 2px solid var(--color-neutral-100);
  border-radius: var(--radius-sm);
  box-shadow: none;
  transition: all var(--transition-fast);
  background: transparent; position: relative; z-index: 1;
}

.upload-form :deep(.el-textarea__inner:hover) {
  border-color: var(--color-primary);
}

.upload-form :deep(.el-textarea__inner:focus) {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(37,99,235, 0.1);
}

.avatar-uploader :deep(.el-upload) {
  border: 2px dashed var(--color-neutral-200);
  border-radius: var(--radius-md);
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all var(--transition-fast);
  background: linear-gradient(135deg, var(--color-bg-secondary) 0%, var(--color-primary-soft) 100%);
}

.avatar-uploader :deep(.el-upload:hover) {
  border-color: var(--color-primary);
  box-shadow: var(--shadow-md);
}

.avatar-uploader-icon {
  font-size: 28px;
  color: var(--color-primary);
  width: 178px;
  height: 178px;
  line-height: 178px;
  text-align: center;
}

.avatar {
  width: 178px;
  height: 178px;
  display: block;
  object-fit: cover;
  border-radius: var(--radius-sm);
}

.upload-form :deep(.el-button--primary) {
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-dark) 100%);
  border: none;
  padding: 12px 32px;
  border-radius: var(--radius-sm);
  font-weight: 500;
  transition: all var(--transition-fast);
}

.upload-form :deep(.el-button--primary:hover) {
  background: linear-gradient(135deg, var(--color-primary-dark) 0%, var(--color-primary) 100%);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.upload-form :deep(.el-button) {
  padding: 12px 32px;
  border-radius: var(--radius-sm);
  border: 2px solid var(--color-neutral-100);
  color: var(--color-neutral-600);
  background: transparent; position: relative; z-index: 1;
  transition: all var(--transition-fast);
}

.upload-form :deep(.el-button:hover) {
  border-color: var(--color-primary);
  color: var(--color-primary);
  background-color: var(--color-primary-soft);
  transform: translateY(-2px);
}

.footer {
  background: white;
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
  font-size: 13px;
  transition: color var(--transition-fast);
}

.footer-links a:hover {
  color: var(--color-primary);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .nav-content {
    padding: 0 12px;
  }

  .main-content {
    padding: 16px 12px 32px;
    flex-direction: column;
  }

  .form-header {
    padding: 16px;
  }

  .upload-form {
    padding: 20px;
  }

  .back-link {
    padding: 6px 12px;
    font-size: 12px;
  }
}
</style>
