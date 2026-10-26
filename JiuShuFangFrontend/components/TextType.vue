<template>
  <span ref="containerRef" class="text-type">
    <span class="text-type__content" :style="{ color: currentTextColor }">
      {{ displayedText }}
    </span>
    <span
      v-if="showCursor"
      ref="cursorRef"
      :class="['text-type__cursor', { 'text-type__cursor--hidden': shouldHideCursor }]"
    >
      {{ cursorCharacter }}
    </span>
  </span>
</template>

<script setup>
import { ref, computed, watch, onMounted, nextTick } from 'vue'
import gsap from 'gsap'

const props = defineProps({
  text: {
    type: [String, Array],
    default: ''
  },
  typingSpeed: {
    type: Number,
    default: 50
  },
  initialDelay: {
    type: Number,
    default: 0
  },
  pauseDuration: {
    type: Number,
    default: 2000
  },
  deletingSpeed: {
    type: Number,
    default: 30
  },
  loop: {
    type: Boolean,
    default: true
  },
  showCursor: {
    type: Boolean,
    default: true
  },
  hideCursorWhileTyping: {
    type: Boolean,
    default: false
  },
  cursorCharacter: {
    type: String,
    default: '|'
  },
  cursorBlinkDuration: {
    type: Number,
    default: 0.5
  },
  textColors: {
    type: Array,
    default: () => []
  },
  variableSpeed: {
    type: Object,
    default: null
  },
  startOnVisible: {
    type: Boolean,
    default: false
  },
  reverseMode: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['sentence-complete'])

const containerRef = ref(null)
const cursorRef = ref(null)
const displayedText = ref('')
const currentCharIndex = ref(0)
const isDeleting = ref(false)
const currentTextIndex = ref(0)
const isVisible = ref(!props.startOnVisible)

const textArray = computed(() => {
  return Array.isArray(props.text) ? props.text : [props.text]
})

const currentTextColor = computed(() => {
  if (props.textColors.length === 0) return 'inherit'
  return props.textColors[currentTextIndex.value % props.textColors.length]
})

const shouldHideCursor = computed(() => {
  return props.hideCursorWhileTyping && (
    currentCharIndex.value < textArray.value[currentTextIndex.value].length || isDeleting.value
  )
})

const getRandomSpeed = () => {
  if (!props.variableSpeed) return props.typingSpeed
  const { min, max } = props.variableSpeed
  return Math.random() * (max - min) + min
}

let timeout = null

const executeTypingAnimation = () => {
  const currentText = textArray.value[currentTextIndex.value]
  const processedText = props.reverseMode
    ? currentText.split('').reverse().join('')
    : currentText

  if (isDeleting.value) {
    if (displayedText.value === '') {
      isDeleting.value = false
      if (currentTextIndex.value === textArray.value.length - 1 && !props.loop) {
        return
      }

      emit('sentence-complete', textArray.value[currentTextIndex.value], currentTextIndex.value)

      currentTextIndex.value = (currentTextIndex.value + 1) % textArray.value.length
      currentCharIndex.value = 0
      timeout = setTimeout(() => {}, props.pauseDuration)
    } else {
      timeout = setTimeout(() => {
        displayedText.value = displayedText.value.slice(0, -1)
      }, props.deletingSpeed)
    }
  } else {
    if (currentCharIndex.value < processedText.length) {
      timeout = setTimeout(
        () => {
          const char = processedText[currentCharIndex.value]
          if (char) {
            displayedText.value = displayedText.value + char
            currentCharIndex.value = currentCharIndex.value + 1
          }
        },
        props.variableSpeed ? getRandomSpeed() : props.typingSpeed
      )
    } else if (textArray.value.length >= 1) {
      if (!props.loop && currentTextIndex.value === textArray.value.length - 1) return
      timeout = setTimeout(() => {
        isDeleting.value = true
      }, props.pauseDuration)
    }
  }
}

const startAnimation = () => {
  if (currentCharIndex.value === 0 && !isDeleting.value && displayedText.value === '') {
    timeout = setTimeout(executeTypingAnimation, props.initialDelay)
  } else {
    executeTypingAnimation()
  }
}

watch([currentCharIndex, displayedText, isDeleting, currentTextIndex], () => {
  if (isVisible.value) {
    startAnimation()
  }
}, { immediate: true })

watch(() => [isVisible.value, props.text], () => {
  if (isVisible.value) {
    startAnimation()
  }
}, { immediate: true })

onMounted(() => {
  if (props.showCursor && cursorRef.value) {
    gsap.set(cursorRef.value, { opacity: 1 })
    gsap.to(cursorRef.value, {
      opacity: 0,
      duration: props.cursorBlinkDuration,
      repeat: -1,
      yoyo: true,
      ease: 'power2.inOut'
    })
  }

  if (props.startOnVisible && containerRef.value) {
    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            isVisible.value = true
          }
        })
      },
      { threshold: 0.1 }
    )
    observer.observe(containerRef.value)
  }
})
</script>

<style scoped>
.text-type {
  display: inline-block;
  white-space: pre-wrap;
  word-break: break-word;
}

.text-type__content {
  display: inline;
}

.text-type__cursor {
  margin-left: 0.25rem;
  display: inline-block;
  opacity: 1;
}

.text-type__cursor--hidden {
  display: none;
}
</style>
