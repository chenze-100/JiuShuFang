<template>
  <div class="blink-emoji" :class="{ 'blinking': isBlinking }" :style="containerStyle">
    <svg :width="size" :height="size" viewBox="0 0 100 100">
      <!-- 脸部 -->
      <circle cx="50" cy="50" r="45" :fill="faceColor" />
      
      <!-- 左眼 -->
      <g :class="{ 'eye-left': true, 'closed': isBlinking }">
        <ellipse 
          :cx="35" 
          :cy="40" 
          :rx="8" 
          :ry="isBlinking ? 1 : 8" 
          fill="#333"
        />
      </g>
      
      <!-- 右眼 -->
      <g :class="{ 'eye-right': true, 'closed': isBlinking }">
        <ellipse 
          :cx="65" 
          :cy="40" 
          :rx="8" 
          :ry="isBlinking ? 1 : 8" 
          fill="#333"
        />
      </g>
      
      <!-- 嘴巴 -->
      <path 
        :d="mouthPath" 
        stroke="#333" 
        stroke-width="3" 
        fill="none" 
        stroke-linecap="round"
      />
      
      <!-- 腮红 -->
      <ellipse cx="25" cy="55" rx="6" ry="4" fill="#FFB6C1" opacity="0.6" />
      <ellipse cx="75" cy="55" rx="6" ry="4" fill="#FFB6C1" opacity="0.6" />
    </svg>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'

const props = defineProps({
  size: {
    type: Number,
    default: 60
  },
  interval: {
    type: Number,
    default: 2000 // 眨眼间隔（毫秒）
  },
  faceColor: {
    type: String,
    default: '#93c5fd'
  },
  expression: {
    type: String,
    default: 'happy' // happy, excited, surprised
  }
})

const isBlinking = ref(false)
let blinkTimer = null
let blinkDuration = null

const mouthPath = computed(() => {
  switch (props.expression) {
    case 'happy':
      return 'M 35 60 Q 50 75 65 60'
    case 'excited':
      return 'M 35 58 Q 50 80 65 58'
    case 'surprised':
      return 'M 50 55 Q 50 70 50 55'
    default:
      return 'M 35 60 Q 50 75 65 60'
  }
})

const containerStyle = computed(() => ({
  width: `${props.size}px`,
  height: `${props.size}px`
}))

const blink = () => {
  isBlinking.value = true
  
  // 眨眼持续时间
  blinkDuration = setTimeout(() => {
    isBlinking.value = false
  }, 200)
}

const startBlinking = () => {
  blinkTimer = setInterval(() => {
    blink()
  }, props.interval)
}

const stopBlinking = () => {
  if (blinkTimer) {
    clearInterval(blinkTimer)
  }
  if (blinkDuration) {
    clearTimeout(blinkDuration)
  }
}

onMounted(() => {
  startBlinking()
})

onUnmounted(() => {
  stopBlinking()
})
</script>

<style scoped>
.blink-emoji {
  display: inline-block;
  animation: bounce 2s ease-in-out infinite;
}

.eye-left,
.eye-right {
  transition: all 0.1s ease;
}

@keyframes bounce {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}
</style>
