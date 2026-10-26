<template>
  <span
    ref="containerRef"
    :class="['glitch-text', className]"
    :style="containerStyle"
    @mouseenter="handleHoverStart"
    @mouseleave="handleHoverEnd"
    @click="handleClick"
  >
    <span class="glitch-text__content">{{ displayedText || text }}</span>
    <span v-if="enableShadows && !isHovering" class="glitch-text__shadow glitch-text__shadow--1"></span>
    <span v-if="enableShadows && !isHovering" class="glitch-text__shadow glitch-text__shadow--2"></span>
  </span>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  speed: {
    type: Number,
    default: 1
  },
  enableShadows: {
    type: Boolean,
    default: true
  },
  enableOnHover: {
    type: Boolean,
    default: false
  },
  className: {
    type: String,
    default: ''
  },
  glitchChars: {
    type: String,
    default: '!@#$%^&*<>[]{}?/\\|'
  },
  text: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['click'])

const containerRef = ref(null)
const displayedText = ref('')
const originalText = ref('')
const isHovering = ref(false)
const isGlitching = ref(false)

let glitchInterval = null
let timeout = null

const handleClick = (e) => {
  emit('click', e)
}

const containerStyle = computed(() => ({
  position: 'relative',
  display: 'inline-block',
  cursor: props.enableOnHover ? 'pointer' : 'default'
}))

const getRandomChar = () => {
  const chars = props.glitchChars
  return chars[Math.floor(Math.random() * chars.length)]
}

const glitchText = () => {
  if (!originalText.value) return

  const textArr = originalText.value.split('')
  const glitchedArr = textArr.map((char, index) => {
    if (char === ' ') return ' '
    if (Math.random() < 0.3 * props.speed) {
      return getRandomChar()
    }
    return char
  })

  displayedText.value = glitchedArr.join('')
}

const resetText = () => {
  displayedText.value = originalText.value
}

const startGlitch = () => {
  if (isGlitching.value) return
  isGlitching.value = true

  const interval = Math.max(50, 200 / props.speed)

  glitchInterval = setInterval(() => {
    glitchText()
  }, interval)
}

const stopGlitch = () => {
  if (glitchInterval) {
    clearInterval(glitchInterval)
    glitchInterval = null
  }
  isGlitching.value = false
  resetText()
}

const handleHoverStart = () => {
  if (props.enableOnHover) {
    isHovering.value = true
    startGlitch()
  }
}

const handleHoverEnd = () => {
  if (props.enableOnHover) {
    isHovering.value = false
    stopGlitch()
  }
}

const startContinuousGlitch = () => {
  if (props.enableOnHover) return

  const triggerGlitch = () => {
    if (isGlitching.value) return

    isGlitching.value = true
    startGlitch()

    const duration = 100 + Math.random() * 200
    timeout = setTimeout(() => {
      stopGlitch()

      const nextTrigger = 2000 / props.speed + Math.random() * 3000 / props.speed
      timeout = setTimeout(triggerGlitch, nextTrigger)
    }, duration)
  }

  triggerGlitch()
}

onMounted(() => {
  // 使用 props.text
  if (props.text) {
    originalText.value = props.text
    displayedText.value = props.text
  }

  // 延迟启动持续故障效果
  timeout = setTimeout(() => {
    if (!props.enableOnHover) {
      startContinuousGlitch()
    }
  }, 500)
})

onUnmounted(() => {
  if (glitchInterval) {
    clearInterval(glitchInterval)
  }
  if (timeout) {
    clearTimeout(timeout)
  }
})
</script>

<style scoped>
.glitch-text {
  position: relative;
  display: inline-block;
}

.glitch-text__content {
  position: relative;
  z-index: 1;
}

.glitch-text__shadow {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 0;
  opacity: 0.5;
}

.glitch-text__shadow--1 {
  animation: glitch-shadow-1 2s infinite linear alternate-reverse;
}

.glitch-text__shadow--2 {
  animation: glitch-shadow-2 3s infinite linear alternate-reverse;
}

@keyframes glitch-shadow-1 {
  0%, 100% {
    transform: translate(0);
    clip-path: inset(0 0 0 0);
  }
  20% {
    transform: translate(-2px, 1px);
    clip-path: inset(20% 0 60% 0);
  }
  40% {
    transform: translate(1px, -1px);
    clip-path: inset(40% 0 40% 0);
  }
  60% {
    transform: translate(-1px, 2px);
    clip-path: inset(60% 0 20% 0);
  }
  80% {
    transform: translate(2px, -2px);
    clip-path: inset(10% 0 80% 0);
  }
}

@keyframes glitch-shadow-2 {
  0%, 100% {
    transform: translate(0);
    clip-path: inset(0 0 0 0);
  }
  20% {
    transform: translate(2px, -1px);
    clip-path: inset(80% 0 10% 0);
  }
  40% {
    transform: translate(-1px, 2px);
    clip-path: inset(30% 0 50% 0);
  }
  60% {
    transform: translate(1px, -2px);
    clip-path: inset(50% 0 30% 0);
  }
  80% {
    transform: translate(-2px, 1px);
    clip-path: inset(10% 0 70% 0);
  }
}
</style>
