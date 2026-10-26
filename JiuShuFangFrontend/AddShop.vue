<template>
  <!-- 动态背景 -->
  <VitalityBackground />

  <div class="add-shop-container">
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
        <div class="page-header">
          <h1>开设商铺</h1>
          <span class="back-link" @click="goBackHome">返回首页</span>
        </div>

        <!-- 前置检查结果展示 -->
        <div v-if="checkResult" class="check-panel">
          <div class="check-item" :class="{ passed: checkResult.realNameVerified, failed: !checkResult.realNameVerified }">
            <el-icon v-if="checkResult.realNameVerified"><CircleCheck /></el-icon>
            <el-icon v-else><CircleClose /></el-icon>
            <span>实名认证：{{ checkResult.realNameVerified ? '已认证' : '未认证' }}</span>
          </div>
          <div class="check-item" :class="{ passed: checkResult.myShopCount < checkResult.maxShopCount, failed: checkResult.myShopCount >= checkResult.maxShopCount }">
            <el-icon v-if="checkResult.myShopCount < checkResult.maxShopCount"><CircleCheck /></el-icon>
            <el-icon v-else><CircleClose /></el-icon>
            <span>商铺数量：{{ checkResult.myShopCount }} / {{ checkResult.maxShopCount }}</span>
          </div>
          <div class="check-item" :class="{ passed: !checkResult.hasPendingShop, failed: checkResult.hasPendingShop }">
            <el-icon v-if="!checkResult.hasPendingShop"><CircleCheck /></el-icon>
            <el-icon v-else><CircleClose /></el-icon>
            <span>审核中商铺：{{ checkResult.hasPendingShop ? '有' : '无' }}</span>
          </div>
        </div>

        <!-- 表单 -->
        <div class="form-card">
          <el-form
            :model="form"
            :rules="rules"
            ref="formRef"
            label-width="100px"
            class="add-shop-form"
            v-loading="submitting"
          >
            <el-form-item label="商铺名称" prop="name">
              <el-input
                v-model="form.name"
                placeholder="请输入商铺名称"
                maxlength="30"
                show-word-limit
              ></el-input>
            </el-form-item>

            <el-form-item label="商铺Logo" prop="logoUrl">
              <el-input
                v-model="form.logoUrl"
                placeholder="请输入 Logo 图片 URL"
              ></el-input>
              <el-image
                v-if="form.logoUrl"
                :src="form.logoUrl"
                fit="cover"
                style="width: 80px; height: 80px; margin-top: 8px; border-radius: 8px;"
              ></el-image>
            </el-form-item>

            <el-form-item label="商铺简介" prop="intro">
              <el-input
                v-model="form.intro"
                type="textarea"
                :rows="4"
                placeholder="请输入商铺简介（选填）"
                maxlength="200"
                show-word-limit
              ></el-input>
            </el-form-item>

            <el-form-item label="联系人" prop="contactPerson">
              <el-input
                v-model="form.contactPerson"
                placeholder="请输入联系人姓名"
                maxlength="20"
              ></el-input>
            </el-form-item>

            <el-form-item label="联系电话" prop="contactPhone">
              <el-input
                v-model="form.contactPhone"
                placeholder="请输入手机号"
                maxlength="11"
              ></el-input>
            </el-form-item>

            <el-form-item>
              <el-button
                type="primary"
                @click="handleSubmit"
                :loading="submitting"
                :disabled="!canSubmit"
              >提交申请</el-button>
              <el-button @click="goBackHome">取消</el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 已有商铺列表 -->
        <div v-if="myShops.length > 0" class="my-shops-section">
          <h2>我的商铺</h2>
          <div class="shop-list">
            <div v-for="shop in myShops" :key="shop.id" class="shop-card">
              <div class="shop-logo">
                <el-image v-if="shop.logoUrl" :src="shop.logoUrl" fit="cover" style="width: 60px; height: 60px; border-radius: 8px;" />
                <el-icon v-else :size="30"><Shop /></el-icon>
              </div>
              <div class="shop-info-box">
                <h3>{{ shop.name }}</h3>
                <p class="shop-status" :class="'status-' + shop.status">{{ shop.statusDesc }}</p>
                <p class="shop-contact">{{ shop.contactPerson }} · {{ shop.contactPhone }}</p>
                <p v-if="shop.rejectReason" class="shop-reject">拒绝原因：{{ shop.rejectReason }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import request from './request.js'
import { checkBeforeAdd, addShop, listMyShops } from './config/shopApi.js'
import UserMenu from './components/UserMenu.vue'

export default {
  name: 'AddShop',
  components: { UserMenu },
  data() {
    return {
      userInfo: {},
      checkResult: null,
      submitting: false,
      myShops: [],
      form: {
        name: '',
        logoUrl: '',
        intro: '',
        contactPerson: '',
        contactPhone: ''
      },
      rules: {
        name: [
          { required: true, message: '请输入商铺名称', trigger: 'blur' },
          { min: 2, max: 30, message: '名称长度 2-30 个字符', trigger: 'blur' }
        ],
        contactPerson: [
          { required: true, message: '请输入联系人', trigger: 'blur' }
        ],
        contactPhone: [
          { required: true, message: '请输入联系电话', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    canSubmit() {
      if (!this.checkResult) {
        console.log('checkResult:检查结果', this.checkResult)
        this.$message.error('请先检查商铺状态')
        return false
      }

      return this.checkResult.realNameVerified &&
             this.checkResult.myShopCount < this.checkResult.maxShopCount &&
             !this.checkResult.hasPendingShop
    }
  },
  async mounted() {
    const userInfoStr = localStorage.getItem('userInfo')
    if (userInfoStr) {
      this.userInfo = JSON.parse(userInfoStr)
      console.log(this.userInfo)
    } else {
      this.$message.error('请先登录')
      window.location.href = '/#/'
      return
    }

    // 加载前置检查 + 已有商铺列表
    await this.loadCheckResult()
    await this.loadMyShops()
  },
  methods: {
    async loadCheckResult() {
      try {
        const res = await checkBeforeAdd()
        console.log(res)
        this.checkResult = res
        console.log('checkResult(add):检查结果', this.checkResult)
      } catch (e) {
        console.error('检查失败:', e)
      }
    },
    async loadMyShops() {
      try {
        const res = await listMyShops()
        this.myShops = res || []
      } catch (e) {
        console.error('加载商铺列表失败:', e)
      }
    },
    async handleSubmit() {
      try {
        await this.$refs.formRef.validate()
      } catch {
        return
      }

      this.submitting = true
      try {
        await addShop(this.form)
        this.$message.success('提交成功，等待审核')
        // 刷新数据
        await this.loadCheckResult()
        await this.loadMyShops()
        // 重置表单
        this.$refs.formRef.resetFields()
      } catch (e) {
        // request.js 已统一弹错误消息
        console.error('添加商铺失败:', e)
      } finally {
        this.submitting = false
      }
    },
    goBackHome() {
      this.$router.push('/home')
    },
    goToShoppingCart() {
      this.$router.push('/shopping-cart')
    },
    handleLogout() {
      localStorage.removeItem('userInfo')
      localStorage.removeItem('token')
      this.$router.push('/')
    }
  }
}
</script>

<style scoped>
.add-shop-container {
  min-height: 100vh;
  background: #f5f5f5;
}

.top-nav {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  position: sticky;
  top: 0;
  z-index: 100;
}

.nav-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  height: 60px;
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.nav-item {
  font-size: 14px;
  color: #666;
  cursor: pointer;
}

.nav-item a {
  color: #666;
  text-decoration: none;
}

.nav-item a:hover {
  color: #409eff;
}

.main-wrapper {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}

.main-content {
  background: #fff;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-header h1 {
  font-size: 24px;
  color: #333;
  margin: 0;
}

.back-link {
  color: #409eff;
  cursor: pointer;
  font-size: 14px;
}

.back-link:hover {
  text-decoration: underline;
}

/* 前置检查面板 */
.check-panel {
  display: flex;
  gap: 24px;
  padding: 16px 20px;
  background: #f9f9f9;
  border-radius: 8px;
  margin-bottom: 24px;
}

.check-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
}

.check-item.passed {
  color: #67c23a;
}

.check-item.failed {
  color: #f56c6c;
}

/* 表单 */
.form-card {
  padding: 20px;
}

.add-shop-form {
  max-width: 500px;
}

/* 我的商铺列表 */
.my-shops-section {
  margin-top: 32px;
  border-top: 1px solid #eee;
  padding-top: 20px;
}

.my-shops-section h2 {
  font-size: 18px;
  color: #333;
  margin-bottom: 16px;
}

.shop-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.shop-card {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: #f9f9f9;
  border-radius: 8px;
  align-items: center;
}

.shop-logo {
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #e9ecef;
  border-radius: 8px;
  flex-shrink: 0;
}

.shop-info-box {
  flex: 1;
}

.shop-info-box h3 {
  font-size: 16px;
  color: #333;
  margin: 0 0 4px 0;
}

.shop-status {
  font-size: 13px;
  margin: 0 0 4px 0;
}

.shop-status.status-0 { color: #e6a23c; }
.shop-status.status-1 { color: #67c23a; }
.shop-status.status-2 { color: #f56c6c; }
.shop-status.status-3 { color: #909399; }

.shop-contact {
  font-size: 13px;
  color: #999;
  margin: 0;
}

.shop-reject {
  font-size: 13px;
  color: #f56c6c;
  margin: 4px 0 0 0;
}
</style>
