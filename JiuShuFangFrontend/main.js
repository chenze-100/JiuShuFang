import { createApp } from 'vue'
import { createRouter, createWebHashHistory } from 'vue-router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import App from './App.vue'
import Login from './Login.vue'
import Home from './Home.vue'
import WechatAuth from './WechatAuth.vue'
import ForgotPassword from './ForgotPassword.vue'
import SearchResults from './SearchResults.vue'
import ProductDetail from './ProductDetail.vue'
import Favorites from './Favorites.vue'
import Profile from './Profile.vue'
import EditProfile from './EditProfile.vue'
import ChangePassword from './ChangePassword.vue'
import ShoppingCart from './ShoppingCart.vue'
import AddressPage from './AddressPage.vue'
import History from './History.vue'
import OrderCheckout from './OrderCheckout.vue'
import Payment from './Payment.vue'
import OrderList from './OrderList.vue'
import OrderDetail from './OrderDetail.vue'
import Comment from './Comment.vue'
import MyComments from './MyComments.vue'
import EditComment from './EditComment.vue'
import MyShop from './MyShop.vue'
import AddShop from './AddShop.vue'
import UploadGoods from './UploadGoods.vue'
import ChatList from './ChatList.vue'
import ChatDetail from './ChatDetail.vue'
import AICompanion from './AICompanion.vue'

const app = createApp(App)

// 注册所有 Element Plus 图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 将 ElMessage 挂载到 window，方便全局使用
window.ElMessage = ElMessage

// 配置路由
const routes = [
  { path: '/', component: Login },
  { path: '/home', component: Home },
  { path: '/search', component: SearchResults },
  { path: '/product', component: ProductDetail },
  { path: '/favorites', component: Favorites },
  { path: '/profile', component: Profile },
  { path: '/user/profile', component: Profile },
  { path: '/edit-profile', component: EditProfile },
  { path: '/user/edit-profile', component: EditProfile },
  { path: '/change-password', component: ChangePassword },
  { path: '/user/change-password', component: ChangePassword },
  { path: '/shopping-cart', component: ShoppingCart },
  { path: '/address', component: AddressPage },
  { path: '/history', component: History },
  { path: '/order-checkout', component: OrderCheckout },
  { path: '/payment', component: Payment },// scrollTop：已经滚出去的距离
// windowHeight：屏幕可见高度
// documentHeight：整个页面的总高度


  { path: '/order-list', component: OrderList },
  { path: '/order-detail', component: OrderDetail },
  { path: '/comment', component: Comment },
  { path: '/my-comments', component: MyComments },
  { path: '/edit-comment', component: EditComment },
  { path: '/my-shop', component: MyShop },
  { path: '/add-shop', component: AddShop },
  { path: '/upload-goods', component: UploadGoods },
  { path: '/chat-list', component: ChatList },
  { path: '/chat-detail', component: ChatDetail },
  { path: '/ai-chat', component: AICompanion },
  { path: '/wechat-auth', component: WechatAuth },
  { path: '/forgot-password', component: ForgotPassword },
  { path: '/:pathMatch(.*)*', component: Login } // 通配符路由，匹配所有未定义的路径
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userInfo = localStorage.getItem('userInfo')
  
  // 如果已登录，访问登录页时跳转到首页
  if (userInfo && to.path === '/') {
    next('/home')
    return
  }
  
  next()
})



app.use(router)
app.use(ElementPlus)

// 全局确认对话框样式
const style = document.createElement('style')
style.textContent = `
.common-confirm-dialog.el-message-box {
  border-radius: 20px !important;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15), 0 0 0 1px rgba(0, 0, 0, 0.05);
  max-width: 380px;
  background: linear-gradient(180deg, #ffffff 0%, #fafafa 100%);
}
.common-confirm-dialog .el-message-box__header {
  padding: 28px 32px 0 !important;
  background: transparent !important;
  text-align: center;
}
.common-confirm-dialog .el-message-box__title {
  font-size: 20px !important;
  font-weight: 600 !important;
  color: #1a1a1a !important;
  letter-spacing: 0.5px;
}
.common-confirm-dialog .el-message-box__content {
  padding: 20px 32px 28px !important;
}
.common-confirm-dialog .el-message-box__message {
  font-size: 14px !important;
  color: #666666 !important;
  line-height: 1.7;
  text-align: center;
}
.common-confirm-dialog .el-message-box__status {
  display: none !important;
}
.common-confirm-dialog .el-message-box__footer {
  padding: 0 32px 28px !important;
  display: flex;
  justify-content: center;
  gap: 12px;
}
.common-confirm-dialog .el-button {
  min-width: 100px;
  height: 40px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.common-confirm-dialog .el-button--primary {
  background: linear-gradient(135deg, #2563eb 0%, #3b82f6 100%) !important;
  border: none !important;
  box-shadow: 0 4px 15px rgba(37,99,235, 0.35);
}
.common-confirm-dialog .el-button--primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(37,99,235, 0.45);
  background: linear-gradient(135deg, #3b82f6 0%, #93c5fd 100%) !important;
}
.common-confirm-dialog .el-button--default {
  background: #ffffff !important;
  border: 1.5px solid #e5e5e5 !important;
  color: #666666;
}
.common-confirm-dialog .el-button--default:hover {
  border-color: #2563eb !important;
  color: #2563eb !important;
  background: #f0f6ff !important;
}
`
document.head.appendChild(style)

app.mount('#app')
