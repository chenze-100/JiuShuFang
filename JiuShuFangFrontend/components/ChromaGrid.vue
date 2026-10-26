<template>
  <div class="chroma-grid" ref="gridContainer">
    <div
      v-for="(item, index) in processedItems"
      :key="item.id || index"
      class="chroma-card"
      @mouseenter="handleMouseEnter($event, index)"
      @mouseleave="handleMouseLeave($event)"
      @click="handleClick(item)"
    >
      <div class="card-border"></div>
      <div class="card-image-wrapper">
        <img :src="item.bookImg || item.image" :alt="item.bookName || item.title" class="card-image" />
        <span v-if="item.isLike" class="like-badge">★</span>
      </div>
      <div class="card-content">
        <h3 class="card-title">{{ item.bookName || item.title }}</h3>
        <p class="card-subtitle">{{ item.author || item.subtitle }}</p>
        <div class="card-footer">
          <span class="card-price">¥{{ item.price }}</span>
          <span v-if="item.originalPrice" class="card-original-price">¥{{ item.originalPrice }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { gsap } from 'gsap'
import { convertToExternalUrl } from '../utils/imageUtils'

const props = defineProps({
  items: {
    type: Array,
    required: true
  },
  radius: {
    type: Number,
    default: 300
  },
  damping: {
    type: Number,
    default: 0.45
  },
  fadeOut: {
    type: Number,
    default: 0.6
  },
  ease: {
    type: String,
    default: 'power3.out'
  }
})

const emit = defineEmits(['item-click'])

// 转换图片URL为外网可访问地址
const processedItems = computed(() => {
  return props.items.map(item => ({
    ...item,
    bookImg: convertToExternalUrl(item.bookImg || item.image) || item.bookImg || item.image,
    image: convertToExternalUrl(item.bookImg || item.image) || item.bookImg || item.image
  }))
})

const gridContainer = ref(null)

let mouseTimeout = null

const handleMouseEnter = (e, index) => {
  const card = e.currentTarget
  const allCards = gridContainer.value.querySelectorAll('.chroma-card')

  // 清除所有卡片的动画
  allCards.forEach((c) => {
    gsap.killTweensOf(c)
  })

  // 重置所有卡片状态
  gsap.to(allCards, {
    scale: 1,
    opacity: 1,
    z: 0,
    duration: 0.3
  })

  // 悬停卡片放大
  gsap.to(card, {
    scale: 1.05,
    z: 20,
    duration: 0.4,
    ease: props.ease
  })

  // 获取鼠标位置
  const mouseX = e.clientX
  const mouseY = e.clientY

  // 处理周围卡片
  allCards.forEach((otherCard) => {
    if (otherCard === card) return

    const otherRect = otherCard.getBoundingClientRect()
    const otherCenterX = otherRect.left + otherRect.width / 2
    const otherCenterY = otherRect.top + otherRect.height / 2

    const distance = Math.sqrt(
      Math.pow(mouseX - otherCenterX, 2) + Math.pow(mouseY - otherCenterY, 2)
    )

    const influence = Math.max(0, 1 - distance / props.radius)
    const scaledInfluence = influence * props.damping

    // 周围卡片根据距离产生不同的缩放和透明度
    gsap.to(otherCard, {
      scale: 1 - scaledInfluence * 0.15,
      opacity: props.fadeOut + (1 - props.fadeOut) * (1 - scaledInfluence),
      z: -10 * scaledInfluence,
      duration: 0.5,
      ease: props.ease
    })
  })
}

const handleMouseLeave = (e) => {
  const allCards = gridContainer.value.querySelectorAll('.chroma-card')

  // 清除所有动画并恢复状态
  allCards.forEach((otherCard) => {
    gsap.killTweensOf(otherCard)
    gsap.to(otherCard, {
      scale: 1,
      opacity: 1,
      z: 0,
      duration: 0.4,
      ease: 'power2.out'
    })
  })
}

const handleClick = (item) => {
  emit('item-click', item)
}

// 初始化入场动画
onMounted(async () => {
  const cards = gridContainer.value.querySelectorAll('.chroma-card')

  cards.forEach((card, index) => {
    gsap.set(card, {
      opacity: 0,
      scale: 0.8,
      y: 30
    })

    gsap.to(card, {
      opacity: 1,
      scale: 1,
      y: 0,
      duration: 0.6,
      ease: 'back.out(1.7)',
      delay: index * 0.05
    })
  })
})

onUnmounted(() => {
  if (mouseTimeout) clearTimeout(mouseTimeout)
})
</script>

<style scoped>
.chroma-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  padding: 4px;
  perspective: 1000px;
  transform-style: preserve-3d;
}

.chroma-card {
  position: relative;
  background: white;
  border-radius: 16px;
  overflow: visible;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  transform-style: preserve-3d;
  will-change: transform, opacity;
  min-width: 0;
}

/* 渐变彩色边框层 */
.card-border {
  position: absolute;
  top: -3px;
  left: -3px;
  right: -3px;
  bottom: -3px;
  border-radius: 16px;
  background: linear-gradient(45deg, #ff6b6b, #ffd966, #66cc66, #66b3e6, #9999ff, #ff6699, #ff6b6b);
  background-size: 500% 500%;
  z-index: -1;
  opacity: 0;
  transition: opacity 0.3s ease;
  animation: gradientRotate 2s ease infinite;
  pointer-events: none;
}

.chroma-card:hover .card-border {
  opacity: 1;
}

@keyframes gradientRotate {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

.card-image-wrapper {
  width: 100%;
  height: 200px;
  overflow: hidden;
  background: #f5f5f5;
  position: relative;
  z-index: 1;
  border-radius: 16px 16px 0 0;
}

.card-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s ease;
}

.like-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  font-size: 20px;
  color: #3b82f6;
  text-shadow: 0 2px 4px rgba(0,0,0,0.3);
  z-index: 2;
}

.chroma-card:hover .card-image {
  transform: scale(1.1);
}

.card-content {
  padding: 16px;
  background: white;
  position: relative;
  z-index: 1;
  border-radius: 0 0 16px 16px;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin: 0 0 6px 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.card-subtitle {
  font-size: 12px;
  color: #888;
  margin: 0 0 10px 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.card-footer {
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-price {
  font-size: 18px;
  font-weight: 700;
  color: #2563eb;
}

.card-original-price {
  font-size: 13px;
  color: #aaa;
  text-decoration: line-through;
}

@media (max-width: 1200px) {
  .chroma-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 900px) {
  .chroma-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 500px) {
  .chroma-grid {
    grid-template-columns: repeat(1, 1fr);
  }
}
</style>
