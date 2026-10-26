<template>
  <!-- 动态背景 -->
  <VitalityBackground />

  <div class="product-detail-container">
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
      <!-- 左侧商品图片 -->
      <div class="product-image-section">
        <div class="image-container">
          <div 
            class="main-image" 
            @mouseenter="showMagnifier" 
            @mouseleave="hideMagnifier" 
            @mousemove="moveMagnifier"
            ref="imageContainerRef"
          >
            <img
              v-if="product.bookImg"
              :src="convertToExternalUrl(product.bookImg)"
              :alt="product.bookName"
              class="product-img"
              ref="productImageRef"
            />
            <div v-else class="placeholder-image">商品图片</div>
          </div>
          <div 
            v-show="isMagnifierVisible && product.bookImg" 
            class="magnifier-preview"
            :style="{
              backgroundImage: `url(${product.bookImg ? convertToExternalUrl(product.bookImg) : ''})`,
              ...magnifierStyle
            }"
          ></div>
        </div>
      </div>

      <!-- 右侧商品信息 -->
      <div class="product-info-section">
        <div class="product-title-wrapper">
          <h1 class="product-title">{{ product.bookName }}</h1>
          <div class="product-like1" @click="toggleCollect">

            <span
              v-if="product.isLike"
              :class="['like-star', 'filled', { locked: collectLock }]"
              >★</span
            >
            <span
              v-else
              :class="['like-star', 'empty', { locked: collectLock }]"
              >★</span
            >
           <!--  <span class="like-text">{{
              product.isLike ? "已收藏" : "收藏商品"
            }}</span> -->
          </div>
            <div class="back-home-wrapper">
            <div class="back-home-btn" @click="goToHome" style="height: 40px;">
              <span class="back-home-icon">←</span>
              <span class="back-home-text" style="font-size: 16px;">返回首页</span>
            </div>
          </div>
        </div>

        <div class="product-meta">
          <span class="meta-item">作者：{{ product.author }}</span>
          <span class="meta-item">出版社：{{ product.publisher }}</span>
          <div
            v-if="isFromShop"
            class="back-shop-section"
            @click="goBackToShop"
          >
            <span class="back-shop-text">← 返回商铺</span>
          </div>
          <div
            v-else
            class="seller-section"
            @click="goToSellerShop(product.userId)"
          >
            <span class="seller-name">{{
              product.sellerName || "查看店铺"
            }}</span>
          </div>
        </div>
        <div class="price-section">
          <div class="current-price">
            <span class="price-label">售价</span>
            <span class="price-value">¥{{ product.price }}</span>
          </div>
          <div class="original-price" v-if="product.originalPrice">
            <span class="price-label">原价</span>
            <span class="price-value">¥{{ product.originalPrice }}</span>
          </div>
        </div>
        <div class="product-description">
          <div class="desc-label">商品描述</div>
          <div class="desc-content">
            {{ product.description || "暂无描述" }}
          </div>
        </div>
        <div v-if="productLoaded" class="product-actions">
          <button
            v-if="!isMyProduct"
            class="action-btn add-cart"
            @click="openCartDialog('cart')"
          >
            加入购物车
          </button>
          <button
            v-if="!isMyProduct"
            class="action-btn buy-now"
            @click="openCartDialog('buy')"
          >
            立即购买
          </button>
          <button
            v-if="!isMyProduct"
            class="action-btn contact-seller"
            @click="goToChatWithSeller"
          >
            联系卖家
          </button>
        </div>
      </div>
    </div>

    <!-- 评论区 -->
    <div class="review-section">
      <div class="review-header">
        <h2>买家评价</h2>
        <span class="review-count">({{ reviewTotal }}条)</span>
      </div>
      <div class="average-score-section" v-if="averageScore > 0">
        <div class="score-value">{{ averageScore.toFixed(2) }}分</div>
        <div class="score-stars">
          <span
            v-for="star in 5"
            :key="star"
            class="star"
            :class="getStarClass(star)"
            >★</span
          >
        </div>
      </div>
      <div class="score-filter">
        <span
          class="filter-item"
          :class="{ active: selectedScore === null }"
          @click="selectScoreFilter(null)"
          >全部</span
        >
        <span
          class="filter-item"
          :class="{ active: selectedScore === 4 || selectedScore === 5 }"
          @click="selectScoreFilter(4)"
          >只看好评</span
        >
        <span
          class="filter-item"
          :class="{ active: selectedScore === 3 }"
          @click="selectScoreFilter(3)"
          >只看中评</span
        >
        <span
          class="filter-item"
          :class="{ active: selectedScore === 1 || selectedScore === 2 }"
          @click="selectScoreFilter(1)"
          >只看差评</span
        >
      </div>
      <div class="review-list">
        <div v-for="item in reviewList" :key="item.id" class="review-item">
          <div class="review-user">
            <el-avatar :size="40" :src="item.avatar">
              <span class="user-initial">{{
                item.nickName?.charAt(0).toUpperCase()
              }}</span>
            </el-avatar>
            <span class="user-name">{{ item.nickName }}</span>
          </div>
          <div class="review-content">
            <div class="review-score">
              <span
                v-for="star in 5"
                :key="star"
                class="small-star"
                :class="getStarClass(star, item.score)"
                >★</span
              >
            </div>
            <div class="review-text">{{ item.content }}</div>
            <div v-if="hasValidImages(item.picture)" class="review-images">
              <img
                v-for="(pic, index) in getValidImages(item.picture)"
                :key="index"
                :src="convertToExternalUrl(pic)"
                class="review-image"
              />
            </div>
            <div class="review-time">{{ formatTime(item.createTime) }}</div>
          </div>
        </div>
      </div>
      <div v-if="reviewLoading" class="loading-text">加载中...</div>
      <div v-if="!reviewHasMore && reviewList.length > 0" class="no-more-text">
        已经到底了
      </div>
      <div v-if="reviewList.length === 0 && !reviewLoading" class="no-reviews">
        暂无评价
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

    <!-- 加入购物车对话框 -->
    <el-dialog
      v-model="cartDialogVisible"
      :title="cartDialogType === 'cart' ? '加入购物车' : '立即购买'"
      width="400px"
      :close-on-click-modal="false"
    >
      <div class="cart-dialog-content">
        <div class="cart-item-info">
          <div class="cart-item-image">
            <img
              v-if="product.bookImg"
              :src="convertToExternalUrl(product.bookImg)"
              :alt="product.bookName"
            />
            <div v-else class="placeholder-img">商品图片</div>
          </div>
          <div class="cart-item-details">
            <div class="cart-item-name">{{ product.bookName }}</div>
            <div class="cart-item-price">单价：¥{{ product.price }}</div>
          </div>
        </div>

        <div class="cart-quantity-section">
          <div class="quantity-label">购买数量</div>
          <div class="quantity-control">
            <button
              class="quantity-btn"
              @click="decreaseQuantity"
              :disabled="cartQuantity <= 1"
            >
              -
            </button>
            <span class="quantity-value">{{ cartQuantity }}</span>
            <button
              class="quantity-btn"
              @click="increaseQuantity"
              :disabled="cartQuantity >= (product.stock || 0)"
            >
              +
            </button>
          </div>
          <div class="quantity-tip" v-if="product.stock">
            库存：{{ product.stock }} 件
          </div>
        </div>

        <div class="cart-total-section">
          <span class="total-label">总价：</span>
          <span class="total-price"
            >¥{{ (product.price * cartQuantity).toFixed(2) }}</span
          >
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <button
            class="dialog-btn cancel-btn"
            @click="cartDialogVisible = false"
          >
            取消
          </button>
          <button
            class="dialog-btn confirm-btn"
            @click="
              cartDialogType === 'cart' ? confirmAddToCart() : confirmBuyNow()
            "
          >
            {{ cartDialogType === "cart" ? "确定" : "立即购买" }}
          </button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import UserMenu from './components/UserMenu.vue'
import GlitchText from './components/GlitchText.vue'
import VitalityBackground from './components/VitalityBackground.vue'
import { ref, onMounted, onUnmounted, computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { UserFilled } from "@element-plus/icons-vue";
import request from "./request";
import { convertToExternalUrl } from "./utils/imageUtils";

const route = useRoute();
const router = useRouter();

const isFromShop = ref(false);
const sellerId = ref(null);

const userInfo = ref(null);
const nickName = ref("");
const avatarUrl = ref("");
const currentUserId = ref(null);

const product = ref({});
const productLoaded = ref(false);
const reviewList = ref([]);
const reviewPage = ref(1);
const reviewHasMore = ref(false);
const reviewLoading = ref(false);
const reviewTotal = ref(0);
const averageScore = ref(0);
const selectedScore = ref(null);
const collectLock = ref(false);

const productImageRef = ref(null);
const imageContainerRef = ref(null);
const isMagnifierVisible = ref(false);
const magnifierStyle = ref({});

const showMagnifier = () => {
  if (product.value.bookImg) {
    isMagnifierVisible.value = true;
  }
};

const hideMagnifier = () => {
  isMagnifierVisible.value = false;
};

const moveMagnifier = (e) => {
  if (!isMagnifierVisible.value) return;
  
  const container = e.currentTarget;
  const rect = container.getBoundingClientRect();
  const x = e.clientX - rect.left;
  const y = e.clientY - rect.top;
  
  const containerWidth = rect.width;
  const containerHeight = rect.height;
  
  const percentX = (x / containerWidth) * 100;
  const percentY = (y / containerHeight) * 100;
  
  magnifierStyle.value = {
    backgroundPosition: `${percentX}% ${percentY}%`
  };
};

const isMyProduct = computed(() => {
  return (
    currentUserId.value &&
    product.value.userId &&
    currentUserId.value === product.value.userId
  );
});

// 购物车相关
const cartDialogVisible = ref(false);
const cartDialogType = ref("cart"); // 'cart' 或 'buy'
const cartQuantity = ref(1);
const cartLock = ref(false); // 防止重复点击

const REVIEW_PAGE_SIZE = 10;

const handleLogout = async () => {
  try {
    await request("/user/logout", {
      method: "POST",
    });
  } catch (error) {
    console.error("退出登录请求失败:", error);
  } finally {
    ElMessage.success("已退出登录");
    localStorage.removeItem("userInfo");
    localStorage.removeItem("token");
    window.location.href = "/";
  }
};

const goToProfile = () => {
  router.push("/profile");
};

const goToHome = () => {
  router.push("/home");
};

const goToShoppingCart = () => {
  router.push("/shopping-cart");
};

const goToSellerShop = (sellerId) => {
  console.log("点击查看店铺，sellerId:", sellerId);
  if (!sellerId) {
    ElMessage.warning("卖家信息不存在");
    return;
  }
  console.log("跳转到我的商铺，sellerId:", sellerId);
  router.push({
    path: "/my-shop",
    query: {
      isViewSellerStore: true,
      sellerId: sellerId,
    },
  });
};

const goBackToShop = () => {
  if (sellerId.value) {
    console.log("返回商铺，sellerId:", sellerId.value);
    router.push({
      path: "/my-shop",
      query: {
        isViewSellerStore: true,
        sellerId: sellerId.value,
      },
    });
  } else {
    ElMessage.warning("无法返回商铺");
  }
};

const goToAddress = () => {
  router.push("/address");
};

const goToFavorites = () => {
  router.push("/favorites");
};

const goToHistory = () => {
  router.push("/history");
};

const goToOrderList = () => {
  router.push("/order-list");
};

const goToChatWithSeller = () => {
  if (!userInfo.value) {
    ElMessage.warning("请先登录");
    router.push("/login");
    return;
  }

  if (!product.value.userId) {
    ElMessage.warning("卖家信息不存在");
    return;
  }

  router.push({
    path: "/chat-detail",
    query: {
      name: product.value.sellerName || "卖家",
      avator: product.value.sellerAvatar || "",
      userId: product.value.userId,
      productId: product.value.id,
      productName: product.value.goodsName || product.value.bookName || "",
      productImage: product.value.goodsImage || product.value.bookImg || "",
      productPrice: product.value.price || "",
    },
  });
};

const addHistory = async (bookId) => {
  try {
    await request("/history/add", {
      method: "POST",
      body: JSON.stringify({ bookId }),
    });
  } catch (error) {
    console.error("添加浏览历史失败:", error);
  }
};

const getProductDetail = async (id) => {
  try {
    const result = await request(`/goods/${id}`);

    console.log("商品详情数据:", result);

    if (result && result.code === 200 && result.data) {
      product.value = {
        ...result.data,
        sellerId: result.data.userId,
        isLike: result.data.isLike !== undefined ? result.data.isLike : false,
      };
      console.log("商品详情（处理后）:", product.value);
      console.log("sellerId:", product.value.sellerId);
      console.log("sellerName:", product.value.sellerName);
      productLoaded.value = true;
      // 添加浏览历史
      addHistory(id);
    }
  } catch (error) {
    console.error("获取商品详情失败:", error);
    ElMessage.error("获取商品详情失败");
  }
};

const getAverageScore = async (goodsId) => {
  try {
    const result = await request(`/comment/score/${goodsId}`);

    if (result && result.code === 200 && result.data !== undefined) {
      averageScore.value = result.data;
    }
  } catch (error) {
    console.error("获取平均评分失败:", error);
  }
};

const getReviewList = async (pageNo, pageSize, goodsId, score) => {
  try {
    const params = new URLSearchParams({
      pageNo: pageNo.toString(),
      pageSize: pageSize.toString(),
      isAsc: "false",
      sortBy: "create_time",
    });
    if (goodsId) {
      params.append("goodsId", goodsId);
    }
    if (score) {
      params.append("score", score);
    }
    const result = await request(`/comment?${params.toString()}`);

    if (result && result.code === 200 && result.data) {
      return result.data;
    }
    return null;
  } catch (error) {
    console.error("获取评论列表失败:", error);
    return null;
  }
};

const loadReviews = async () => {
  if (
    reviewLoading.value ||
    (!reviewHasMore.value && reviewList.value.length > 0)
  )
    return;

  reviewLoading.value = true;
  const currentPage = reviewPage.value;
  const pageData = await getReviewList(
    currentPage,
    REVIEW_PAGE_SIZE,
    route.query.id,
    selectedScore.value
  );

  if (pageData && pageData.list && pageData.list.length > 0) {
    reviewList.value = [...reviewList.value, ...pageData.list];
    reviewPage.value = currentPage + 1;
    reviewTotal.value = pageData.total;
    reviewHasMore.value = reviewList.value.length < pageData.total;
  } else {
    reviewHasMore.value = false;
  }
  reviewLoading.value = false;
};

const selectScoreFilter = (score) => {
  selectedScore.value = score;
  reviewList.value = [];
  reviewPage.value = 1;
  reviewHasMore.value = true;
  reviewLoading.value = false;
  loadReviews();
};

const getStarClass = (star, score = null) => {
  const currentScore = score !== null ? score : averageScore.value;

  if (currentScore >= star) {
    return "full";
  } else if (currentScore > star - 1) {
    return "half";
  } else {
    return "";
  }
};

const formatTime = (timestamp) => {
  if (!timestamp) return "";
  const date = new Date(timestamp);
  return date.toLocaleDateString("zh-CN");
};

// 判断是否有有效图片
const hasValidImages = (pictures) => {
  if (!pictures || !Array.isArray(pictures)) return false;
  return pictures.some((pic) => pic && pic.trim() !== "");
};

// 获取有效图片列表
const getValidImages = (pictures) => {
  if (!pictures || !Array.isArray(pictures)) return [];
  return pictures.filter((pic) => pic && pic.trim() !== "");
};

// 打开购物车对话框
const openCartDialog = (type) => {
  if (!userInfo.value) {
    ElMessage.warning("请先登录");
    router.push("/login");
    return;
  }
  cartDialogType.value = type;
  cartQuantity.value = 1;
  cartDialogVisible.value = true;
};

// 立即购买
const confirmBuyNow = () => {
  if (!product.value.stock || product.value.stock <= 0) {
    ElMessage.warning("该商品已售空");
    return;
  }
  if (cartQuantity.value > product.value.stock) {
    ElMessage.warning("购买数量不能超过库存");
    return;
  }
  cartDialogVisible.value = false;
  router.push({
    path: "/order-checkout",
    query: {
      goodsId: product.value.id,
      num: cartQuantity.value,
    },
  });
};

// 增加数量
const increaseQuantity = () => {
  const maxStock = product.value.stock || 0;
  if (cartQuantity.value < maxStock) {
    cartQuantity.value++;
  } else {
    ElMessage.warning("已达到库存上限");
  }
};

// 减少数量
const decreaseQuantity = () => {
  if (cartQuantity.value > 1) {
    cartQuantity.value--;
  }
};

// 确认添加到购物车
const confirmAddToCart = async () => {
  if (cartLock.value) return;

  cartLock.value = true;

  try {
    const result = await request("/shopping-cart/add", {
      method: "POST",
      body: JSON.stringify({
        goodsId: product.value.id,
        price: product.value.price,
        num: cartQuantity.value,
      }),
    });

    if (result && result.code === 200) {
      ElMessage.success("已添加到购物车");
      cartDialogVisible.value = false;
    } else {
      ElMessage.error(result.message || "添加失败");
    }
  } catch (error) {
    console.error("添加到购物车失败:", error);
    ElMessage.error("添加失败");
  } finally {
    cartLock.value = false;
  }
};

const toggleCollect = async (item) => {
  if (collectLock.value) return;

  collectLock.value = true;

  try {
    const newIsLike = !product.value.isLike;
    const result = await request("/collect", {
      method: "POST",
      body: JSON.stringify({
        goodsId: product.value.id,
        isCollect: newIsLike,
      }),
    });

    if (result && result.code === 200) {
      product.value.isLike = newIsLike;
      ElMessage.success(newIsLike ? "收藏成功" : "取消收藏");
    } else {
      ElMessage.error("操作失败");
    }
  } catch (error) {
    console.error("收藏操作失败:", error);
    ElMessage.error("操作失败");
  } finally {
    collectLock.value = false;
  }
};

const handleScroll = () => {
  const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
  const windowHeight = window.innerHeight;
  const documentHeight = document.documentElement.scrollHeight;

  if (
    scrollTop + windowHeight >= documentHeight - 100 &&
    !reviewLoading.value &&
    reviewHasMore.value
  ) {
    loadReviews();
  }
};

onMounted(async () => {
  const stored = localStorage.getItem("userInfo");
  if (stored) {
    userInfo.value = JSON.parse(stored);
    setUserInfo(userInfo.value);
  }

  const productId = route.query.id;
  const fromShop = route.query.fromShop === "true";
  const shopSellerId = route.query.sellerId;

  isFromShop.value = fromShop;
  sellerId.value = shopSellerId || null;

  if (productId) {
    // 不阻塞页面渲染，后台加载数据
    getProductDetail(productId);
    getAverageScore(productId);
    loadReviews();
  } else {
    ElMessage.error("商品 ID 不能为空");
  }

  window.addEventListener("scroll", handleScroll);
});

onUnmounted(() => {
  window.removeEventListener("scroll", handleScroll);
});

const setUserInfo = (data) => {
  if (data && data.nickName) {
    nickName.value = data.nickName.substring(0, 5);
  }
  if (data && data.avatar) {
    avatarUrl.value = convertToExternalUrl(data.avatar);
  }
  if (data && data.id) {
    currentUserId.value = data.id;
  }
};
</script>

<style scoped>
/* === 商品详情页 === */
.product-detail-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: transparent; position: relative; z-index: 1;
}

.welcome-text {
  color: var(--color-neutral-500);
  font-size: 14px;
}

/* === 导航栏 === */
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

/* === 主内容区 === */
.main-content {
  max-width: 1600px;
  margin: 24px auto;
  padding: 0 20px;
  display: flex;
  gap: 30px;
  background: transparent; position: relative; z-index: 1;
}

.product-image-section {
  flex: 1;
  display: flex;
  height: 450px;
}

.image-container {
  position: relative;
  width: 100%;
  height: 100%;
}

.main-image {
  position: relative;
  width: 100%;
  height: 450px;
  background-color: var(--color-bg-tertiary);
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-md);
  cursor: crosshair;
}

.main-image:hover {
  box-shadow: 0 0 0 2px var(--color-primary), var(--shadow-md);
}

.product-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.magnifier-preview {
  position: absolute;
  top: 0;
  right: -320px;
  width: 300px;
  height: 300px;
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-lg);
  border: 2px solid var(--color-neutral-200);
  background-color: white;
  background-size: 400%;
  background-repeat: no-repeat;
  pointer-events: none;
}

.placeholder-image {
  color: var(--color-neutral-300);
  font-size: 16px;
}

/* === 商品信息区 === */
.product-info-section {
  flex: 1;
  padding: 20px 0;
}

.product-title-wrapper {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 15px;
}

.back-home-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 15px;
}

.back-home-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 12px;
  background: linear-gradient(
    135deg,
    var(--color-primary-soft) 0%,
    #ffffff 100%
  );
  border-radius: var(--radius-pill);
  cursor: pointer;
  transition: all var(--transition-base);
  border: 1px solid var(--color-neutral-100);
  white-space: nowrap;
  font-size: 12px;
  color: var(--color-neutral-700);
  font-weight: 500;
}

.back-home-btn:hover {
  background: linear-gradient(
    135deg,
    var(--color-primary-light) 0%,
    var(--color-primary-soft) 100%
  );
  border-color: var(--color-primary);
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
  color: var(--color-primary);
}

.back-home-icon {
  font-size: 12px;
  transition: transform var(--transition-fast);
}

.back-home-btn:hover .back-home-icon {
  transform: translateX(-2px);
}

.product-title {
  font-size: 24px;
  color: var(--color-neutral-700);
  font-weight: 600;
  line-height: 1.4;
  flex: 1;
}

.product-like {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  padding: 8px 16px;

  border-radius: var(--radius-pill);
  transition: all var(--transition-base);
  border: 1px solid var(--color-neutral-100);
  white-space: nowrap;
}

.product-like:hover {

  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.like-star {
  font-size: 20px;
  transition: all var(--transition-fast);
}

.like-star.filled {
  color: var(--color-warning);
}

.like-star.empty {
  color: var(--color-neutral-300);
}

.like-star.locked {
  opacity: 0.5;
  cursor: not-allowed;
}

.like-text {
  font-size: 14px;
  color: var(--color-neutral-700);
  font-weight: 500;
}

.product-meta {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
  color: var(--color-neutral-500);
  font-size: 14px;
  flex-wrap: wrap;
}

.meta-item {
  padding: 6px 12px;
  background-color: var(--color-bg-tertiary);
  border-radius: var(--radius-sm);
}

.seller-section {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: linear-gradient(
    135deg,
    var(--color-primary-soft) 0%,
    #ffffff 100%
  );
  border-radius: var(--radius-pill);
  transition: all var(--transition-base);
  cursor: pointer;
  border: 1px solid var(--color-neutral-100);
  font-weight: 500;
}

.seller-section:hover {
  background: linear-gradient(
    135deg,
    var(--color-primary-light) 0%,
    var(--color-primary-soft) 100%
  );
  border-color: var(--color-primary);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.seller-name {
  color: var(--color-primary);
  font-weight: 500;
  font-size: 14px;
}

.back-shop-section {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: linear-gradient(
    135deg,
    var(--color-bg-tertiary) 0%,
    #ffffff 100%
  );
  border-radius: var(--radius-pill);
  transition: all var(--transition-base);
  cursor: pointer;
  border: 1px solid var(--color-neutral-100);
  font-weight: 500;
}

.back-shop-section:hover {
  background: linear-gradient(
    135deg,
    var(--color-neutral-100) 0%,
    var(--color-bg-tertiary) 100%
  );
  border-color: var(--color-neutral-200);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.back-shop-text {
  color: var(--color-neutral-500);
  font-weight: 500;
  font-size: 14px;
}

/* === 价格区 === */
.price-section {
  background: linear-gradient(
    135deg,
    var(--color-primary-soft) 0%,
    #ffffff 100%
  );
  padding: 20px;
  border-radius: var(--radius-md);
  margin-bottom: 20px;
  border: 1px solid var(--color-neutral-100);
}

.current-price {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.price-label {
  color: var(--color-neutral-400);
  font-size: 14px;
  font-weight: 500;
}

.price-value {
  color: var(--color-primary);
  font-size: 32px;
  font-weight: 700;
}

.original-price {
  display: flex;
  align-items: center;
  gap: 12px;
}

.original-price .price-value {
  color: var(--color-neutral-400);
  font-size: 18px;
  text-decoration: line-through;
  font-weight: 400;
}

/* === 描述区 === */
.product-description {
  margin-bottom: 24px;
}

.desc-label {
  color: var(--color-neutral-400);
  font-size: 14px;
  margin-bottom: 10px;
  font-weight: 500;
}

.desc-content {
  color: var(--color-neutral-700);
  font-size: 15px;
  line-height: 1.6;
  padding: 16px;
  background-color: white;
  border-radius: var(--radius-sm);
  border: 1px solid var(--color-neutral-100);
}

/* === 操作按钮 === */
.product-actions {
  display: flex;
  gap: 12px;
  margin: 30px 0;
}

.action-btn {
  flex: 1;
  height: 48px;
  border: none;
  border-radius: var(--radius-md);
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition-base);
  display: flex;
  align-items: center;
  justify-content: center;
}

.add-cart {
  background: white;
  color: var(--color-primary);
  border: 1px solid var(--color-neutral-200);
}

.add-cart:hover {
  background-color: var(--color-primary-soft);
  border-color: var(--color-primary);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.buy-now {
  background: var(--gradient-primary);
  color: white;
  border: none;
}

.buy-now:hover {
  background: var(--gradient-primary);
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
}

.contact-seller {
  background: white;
  color: var(--color-info);
  border: 1px solid var(--color-neutral-200);
}

.contact-seller:hover {
  background-color: rgba(24, 144, 255, 0.05);
  border-color: var(--color-info);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

/* === 收藏 === */
.product-like {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 20px;
  padding: 15px 20px;
  background-color: white;
  border-radius: var(--radius-md);
  border: 1px dashed var(--color-neutral-200);
  cursor: pointer;
  transition: all var(--transition-base);
  width: fit-content;
}

.product-like:hover {
  border-color: var(--color-primary-light);
  transform: scale(1.02);
}

.like-star {
  font-size: 24px;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.like-star.filled {
  color: #3b82f6;
}

.like-star.empty {
  color: var(--color-neutral-200);
}

.like-star:hover {
  transform: scale(1.2);
}

.like-text {
  font-size: 14px;
  color: var(--color-neutral-500);
}

/* === 评论区 === */
.review-section {
  max-width: 1200px;
  margin: 40px auto;
  padding: 30px;
  background-color: white;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--color-neutral-100);
  width: 100%;
}

.review-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 25px;
  padding-bottom: 15px;
  border-bottom: 1px solid var(--color-neutral-100);
}

.review-header h2 {
  font-size: 20px;
  color: var(--color-neutral-700);
  font-weight: 600;
}

.review-count {
  color: var(--color-neutral-400);
  font-size: 14px;
}

.average-score-section {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 20px 0;
  margin-bottom: 20px;
  border-bottom: 1px solid var(--color-neutral-100);
}

.score-value {
  font-size: 28px;
  font-weight: 600;
  color: var(--color-primary);
}

.score-stars {
  display: flex;
  align-items: center;
}

.star {
  font-size: 24px;
  color: var(--color-neutral-200);
  margin-right: 4px;
}

.star.full {
  color: #3b82f6;
}

.star.half {
  color: #3b82f6;
  background: linear-gradient(90deg, #3b82f6 50%, var(--color-neutral-200) 50%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.score-filter {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.filter-item {
  padding: 8px 20px;
  border: 1px solid var(--color-neutral-200);
  border-radius: var(--radius-pill);
  color: var(--color-neutral-500);
  font-size: 14px;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.filter-item:hover {
  background-color: var(--color-bg-tertiary);
  border-color: var(--color-primary);
  color: var(--color-primary);
}

.filter-item.active {
  background: var(--gradient-primary);
  border-color: var(--color-primary);
  color: white;
}

.review-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.review-item {
  display: flex;
  gap: 15px;
  padding: 20px 0;
  border-bottom: 1px solid var(--color-neutral-100);
}

.review-user {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  min-width: 80px;
}

.user-name {
  color: var(--color-neutral-500);
  font-size: 13px;
}

.review-content {
  flex: 1;
}

.review-text {
  color: var(--color-neutral-700);
  font-size: 15px;
  line-height: 1.6;
  margin-bottom: 10px;
}

.review-images {
  display: flex;
  gap: 10px;
  margin-bottom: 10px;
  flex-wrap: wrap;
}

.review-image {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: transform var(--transition-fast);
}

.review-image:hover {
  transform: scale(1.1);
}

.review-time {
  color: var(--color-neutral-400);
  font-size: 13px;
}

.small-star {
  font-size: 14px;
  color: var(--color-neutral-200);
  margin-right: 2px;
}

.small-star.full {
  color: #3b82f6;
}

.small-star.half {
  color: #3b82f6;
  background: linear-gradient(90deg, #3b82f6 50%, var(--color-neutral-200) 50%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.loading-text,
.no-more-text,
.no-reviews {
  text-align: center;
  color: var(--color-neutral-400);
  padding: 40px;
  font-size: 14px;
}

/* === 购物车对话框 === */
.cart-dialog-content {
  padding: 10px 0;
}

.cart-item-info {
  display: flex;
  gap: 15px;
  padding: 15px;
  background-color: var(--color-bg-tertiary);
  border-radius: var(--radius-md);
  margin-bottom: 20px;
}

.cart-item-image {
  width: 80px;
  height: 80px;
  flex-shrink: 0;
  border-radius: var(--radius-sm);
  overflow: hidden;
  background: transparent; position: relative; z-index: 1;
}

.cart-item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.placeholder-img {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--color-neutral-100);
  color: var(--color-neutral-300);
  font-size: 12px;
}

.cart-item-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 8px;
}

.cart-item-name {
  font-size: 14px;
  color: var(--color-neutral-700);
  font-weight: 500;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.cart-item-price {
  font-size: 14px;
  color: var(--color-primary);
  font-weight: 500;
}

.cart-quantity-section {
  margin-bottom: 20px;
}

.quantity-label {
  font-size: 14px;
  color: var(--color-neutral-500);
  margin-bottom: 10px;
  font-weight: 500;
}

.quantity-control {
  display: flex;
  align-items: center;
  gap: 15px;
}

.quantity-btn {
  width: 36px;
  height: 36px;
  border: 1px solid var(--color-neutral-200);
  background-color: white;
  border-radius: var(--radius-sm);
  font-size: 20px;
  color: var(--color-neutral-500);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--transition-fast);
}

.quantity-btn:hover:not(:disabled) {
  border-color: var(--color-primary);
  color: var(--color-primary);
}

.quantity-btn:disabled {
  background-color: var(--color-bg-tertiary);
  color: var(--color-neutral-300);
  cursor: not-allowed;
}

.quantity-value {
  font-size: 18px;
  color: var(--color-neutral-700);
  font-weight: 600;
  min-width: 40px;
  text-align: center;
}

.quantity-tip {
  font-size: 12px;
  color: var(--color-neutral-400);
  margin-top: 8px;
}

.cart-total-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 15px;
  background-color: var(--color-primary-soft);
  border-radius: var(--radius-md);
  border: 1px solid var(--color-neutral-100);
}

.total-label {
  font-size: 14px;
  color: var(--color-neutral-500);
  font-weight: 500;
}

.total-price {
  font-size: 24px;
  color: var(--color-primary);
  font-weight: 600;
}

.dialog-footer {
  display: flex;
  justify-content: center;
  gap: 20px;
  padding: 10px 0;
}

.dialog-btn {
  padding: 10px 30px;
  border: none;
  border-radius: var(--radius-md);
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition-fast);
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cancel-btn {
  background-color: var(--color-bg-tertiary);
  color: var(--color-neutral-500);
  border: 1px solid var(--color-neutral-200);
}

.cancel-btn:hover {
  background-color: var(--color-neutral-100);
}

.confirm-btn {
  background: var(--gradient-primary);
  color: white;
  border: none;
}

.confirm-btn:hover {
  background: var(--gradient-primary);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

/* === 底部 === */
.footer {
  background-color: white;
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
  font-size: 14px;
  transition: color var(--transition-fast);
}

.footer-links a:hover {
  color: var(--color-primary);
}

/* === Responsive === */
@media (max-width: 1024px) {
  .main-content {
    grid-template-columns: 1fr;
    gap: 20px;
  }

  .product-image-section {
    position: static;
  }

  .main-image {
    height: 350px;
  }
}

@media (max-width: 768px) {
  .nav-content {
    padding: 0 12px;
  }

  .main-content {
    padding: 0 12px;
    margin: 16px auto;
  }

  .product-actions {
    flex-direction: column;
    gap: 12px;
  }

  .review-section {
    padding: 20px;
    margin: 24px auto;
  }

  .review-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .score-filter {
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .main-image {
    height: 250px;
  }

  .product-title {
    font-size: 20px;
  }

  .price-value {
    font-size: 24px;
  }

  .cart-quantity-section {
    flex-direction: column;
    gap: 10px;
  }

  .quantity-control {
    justify-content: center;
  }
}
</style>
