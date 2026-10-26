<template>
  <span class="ai-typing-text">
    {{ displayedText }}<span v-if="showCursor && isTyping" class="typing-cursor">|</span>
  </span>
</template>

<script setup>
import { ref, watch, onUnmounted, nextTick } from 'vue'

const props = defineProps({
  text: {
    type: String,
    default: ''
  },
  speed: {
    type: Number,
    default: 30
  },
  showCursor: {
    type: Boolean,
    default: true
  }
})

const displayedText = ref('')
const charIndex = ref(0)
const isTyping = ref(true)
let timer = null

const typeText = () => {
  // 如果 text 还在增长，继续打字
  if (charIndex.value < props.text.length) {
    displayedText.value += props.text[charIndex.value]
    charIndex.value++
    timer = setTimeout(typeText, props.speed)
  } else if (charIndex.value >= props.text.length && charIndex.value === displayedText.value.length) {
    // 打字完成
    isTyping.value = false
  }
}

// 使用深度监听和 immediate
watch(() => props.text, (newText, oldText) => {
  if (!newText) return

  // 如果是新消息（从空到有内容），重置状态
  if (!oldText && newText) {
    displayedText.value = ''
    charIndex.value = 0
    isTyping.value = true
    if (timer) clearTimeout(timer)
    timer = setTimeout(typeText, 50)
  } else if (newText.length > oldText?.length || !oldText) {
    // 内容增长了，继续打字（不需要重置，继续之前的进度）
    if (!timer) {
      timer = setTimeout(typeText, props.speed)
    }
  }
}, { immediate: true })

onUnmounted(() => {
  if (timer) clearTimeout(timer)
})
</script>

<style scoped>
.ai-typing-text {
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.6;
}

.typing-cursor {
  display: inline-block;
  margin-left: 2px;
  color: var(--color-primary);
  animation: cursorBlink 0.8s ease-in-out infinite;
}

@keyframes cursorBlink {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0;
  }
}
</style>
