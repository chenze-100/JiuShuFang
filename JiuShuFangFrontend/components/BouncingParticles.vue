<template>
  <canvas ref="canvasRef" class="bouncing-particles" :width="canvasWidth" :height="canvasHeight"></canvas>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'

const props = defineProps({
  count: {
    type: Number,
    default: 30
  },
  colors: {
    type: Array,
    default: () => ['#d4a373', '#b45309', '#fdf6ec', '#c2703e', '#8a9a5b']
  },
  minSize: {
    type: Number,
    default: 5
  },
  maxSize: {
    type: Number,
    default: 15
  },
  speed: {
    type: Number,
    default: 0.5
  }
})

const canvasRef = ref(null)
const canvasWidth = ref(window.innerWidth)
const canvasHeight = ref(window.innerHeight)

let animationId = null
let particles = []

class Particle {
  constructor(canvas) {
    this.x = Math.random() * canvas.width
    this.y = Math.random() * canvas.height
    this.size = Math.random() * (props.maxSize - props.minSize) + props.minSize
    this.speedX = (Math.random() - 0.5) * props.speed
    this.speedY = (Math.random() - 0.5) * props.speed
    this.color = props.colors[Math.floor(Math.random() * props.colors.length)]
    this.bounceFactor = 0.8
    this.gravity = 0.02
    this.verticalOffset = 0
    this.bouncePhase = Math.random() * Math.PI * 2
  }

  update() {
    // 上下跳动效果
    this.verticalOffset = Math.sin(this.bouncePhase) * 20
    this.bouncePhase += 0.05

    this.x += this.speedX
    this.y += this.speedY + this.verticalOffset * 0.01

    // 边界反弹
    if (this.x < 0 || this.x > canvasWidth.value) {
      this.speedX *= -1
    }
    if (this.y < 0 || this.y > canvasHeight.value) {
      this.speedY *= -this.bounceFactor
      this.y = Math.max(0, Math.min(canvasHeight.value, this.y))
    }
  }

  draw(ctx) {
    ctx.save()
    ctx.fillStyle = this.color
    ctx.globalAlpha = 0.7
    ctx.beginPath()
    ctx.arc(this.x, this.y, this.size, 0, Math.PI * 2)
    ctx.fill()
    ctx.restore()
  }
}

const initParticles = () => {
  particles = []
  for (let i = 0; i < props.count; i++) {
    particles.push(new Particle({
      width: canvasWidth.value,
      height: canvasHeight.value
    }))
  }
}

const animate = () => {
  const canvas = canvasRef.value
  const ctx = canvas.getContext('2d')
  
  ctx.clearRect(0, 0, canvasWidth.value, canvasHeight.value)
  
  particles.forEach(particle => {
    particle.update()
    particle.draw(ctx)
  })
  
  animationId = requestAnimationFrame(animate)
}

const handleResize = () => {
  canvasWidth.value = window.innerWidth
  canvasHeight.value = window.innerHeight
}

onMounted(() => {
  initParticles()
  animate()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  if (animationId) {
    cancelAnimationFrame(animationId)
  }
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.bouncing-particles {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 1;
}
</style>
