<template>
  <div class="vitality-background">
    <!-- 呼吸渐变背景 -->
    <div class="breathing-bg"></div>
    
    <!-- 漂浮圆点 -->
    <FloatingBubbles 
      :count="15" 
      :colors="['#93c5fd', '#2563eb']" 
      :minSize="50" 
      :maxSize="100" 
    />
    
    <!-- 跳动粒子 -->
    <BouncingParticles 
      :count="30" 
      :colors="['#93c5fd', '#2563eb', '#eff6ff', '#FF6B6B', '#4ECDC4']" 
      :minSize="5" 
      :maxSize="15" 
      :speed="0.5" 
    />
  </div>
</template>

<script setup>
import FloatingBubbles from './FloatingBubbles.vue'
import BouncingParticles from './BouncingParticles.vue'
</script>

<style scoped>
.vitality-background {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  z-index: 0;
}

.breathing-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #eff6ff, #FFFFFF, #eff6ff, #FFFFFF);
  background-size: 400% 400%;
  animation: breathe 10s ease infinite;
}

@keyframes breathe {
  0%, 100% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
}
</style>
