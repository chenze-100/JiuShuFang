<template>
  <div class="floating-bubbles">
    <div
      v-for="bubble in bubbles"
      :key="bubble.id"
      class="bubble"
      :style="bubble.style"
    ></div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  count: {
    type: Number,
    default: 15
  },
  colors: {
    type: Array,
    default: () => ['#FFE135', '#FFC107']
  },
  minSize: {
    type: Number,
    default: 50
  },
  maxSize: {
    type: Number,
    default: 100
  }
})

const bubbles = ref([])

const generateBubbles = () => {
  bubbles.value = []
  
  for (let i = 0; i < props.count; i++) {
    const size = Math.random() * (props.maxSize - props.minSize) + props.minSize
    const color = props.colors[Math.floor(Math.random() * props.colors.length)]
    
    const startX = Math.random() * window.innerWidth
    const startY = Math.random() * window.innerHeight
    
    const animationDuration = 15 + Math.random() * 10 // 15-25秒
    const delay = Math.random() * 5 // 0-5秒延迟
    
    bubbles.value.push({
      id: i,
      style: {
        left: `${startX}px`,
        top: `${startY}px`,
        width: `${size}px`,
        height: `${size}px`,
        backgroundColor: color,
        opacity: 0.2 + Math.random() * 0.2, // 0.2-0.4
        animation: `float ${animationDuration}s ease-in-out infinite`,
        animationDelay: `${delay}s`
      }
    })
  }
}

onMounted(() => {
  generateBubbles()
})
</script>

<style scoped>
.floating-bubbles {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  pointer-events: none;
  z-index: 0;
}

.bubble {
  position: absolute;
  border-radius: 50%;
  filter: blur(1px);
}

@keyframes float {
  0%, 100% {
    transform: translate(0, 0) scale(1);
  }
  25% {
    transform: translate(50px, -80px) scale(1.05);
  }
  50% {
    transform: translate(-30px, -120px) scale(0.95);
  }
  75% {
    transform: translate(80px, -60px) scale(1.02);
  }
}
</style>
