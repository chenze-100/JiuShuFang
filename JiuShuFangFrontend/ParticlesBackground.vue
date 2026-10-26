<template>
  <canvas ref="canvasRef" class="particles-canvas"></canvas>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch } from 'vue';

const props = defineProps({
  particleColors: {
    type: Array,
    default: () => ['#ffffff']
  },
  particleCount: {
    type: Number,
    default: 200
  },
  particleSpread: {
    type: Number,
    default: 10
  },
  speed: {
    type: Number,
    default: 0.1
  },
  particleBaseSize: {
    type: Number,
    default: 100
  },
  moveParticlesOnHover: {
    type: Boolean,
    default: false
  },
  alphaParticles: {
    type: Boolean,
    default: false
  },
  disableRotation: {
    type: Boolean,
    default: false
  },
  pixelRatio: {
    type: Number,
    default: 1
  },
  shape: {
    type: String,
    default: 'circle'
  }
});

const canvasRef = ref(null);
let animationId = null;
let particles = [];
let mouseX = -1000;
let mouseY = -1000;
let lastMouseX = -1000;
let lastMouseY = -1000;
let mouseSpeedX = 0;
let mouseSpeedY = 0;
let isMouseMoving = false;
let mouseMoveTimeout = null;

class Particle {
  constructor(canvas) {
    this.canvas = canvas;
    this.x = Math.random() * canvas.width;
    this.y = Math.random() * canvas.height;
    this.baseSize = Math.random() * 8 + 4;
    this.size = this.baseSize;
    this.speedX = (Math.random() - 0.5) * props.speed * 2;
    this.speedY = (Math.random() - 0.5) * props.speed * 2;
    this.color = props.particleColors[Math.floor(Math.random() * props.particleColors.length)];
    this.opacity = props.alphaParticles ? Math.random() * 0.6 + 0.2 : 0.8;
    this.angle = Math.random() * Math.PI * 2;
    this.rotationSpeed = (Math.random() - 0.5) * 0.01;
    this.originalX = this.x;
    this.originalY = this.y;
    this.offsetX = 0;
    this.offsetY = 0;
  }

  update() {
    if (props.moveParticlesOnHover) {
      if (isMouseMoving) {
        const dx = mouseX - this.originalX;
        const dy = mouseY - this.originalY;
        const distance = Math.sqrt(dx * dx + dy * dy);
        const maxDistance = 2000;
        
        if (distance < maxDistance && distance > 0) {
          const force = (maxDistance - distance) / maxDistance;
          const maxOffset = 20;
          const targetOffsetX = (dx / distance) * force * maxOffset;
          const targetOffsetY = (dy / distance) * force * maxOffset;
          
          this.offsetX += (targetOffsetX - this.offsetX) * 0.02;
          this.offsetY += (targetOffsetY - this.offsetY) * 0.02;
          
          const currentDist = Math.sqrt(
            Math.pow(this.offsetX, 2) + 
            Math.pow(this.offsetY, 2)
          );
          
          if (currentDist > maxOffset) {
            const angle = Math.atan2(this.offsetY, this.offsetX);
            this.offsetX = Math.cos(angle) * maxOffset;
            this.offsetY = Math.sin(angle) * maxOffset;
          }
        }
        
        this.x = this.originalX + this.offsetX;
        this.y = this.originalY + this.offsetY;
      } else {
        this.x += this.speedX;
        this.y += this.speedY;

        const distFromOriginal = Math.sqrt(
          Math.pow(this.x - this.originalX, 2) + 
          Math.pow(this.y - this.originalY, 2)
        );
        
        if (distFromOriginal > 20) {
          const angle = Math.atan2(this.y - this.originalY, this.x - this.originalX);
          this.x = this.originalX + Math.cos(angle) * 20;
          this.y = this.originalY + Math.sin(angle) * 20;
          this.speedX *= -0.5;
          this.speedY *= -0.5;
        }

        if (this.x < 0 || this.x > this.canvas.width) {
          this.speedX *= -1;
        }
        if (this.y < 0 || this.y > this.canvas.height) {
          this.speedY *= -1;
        }
      }
    } else {
      this.x += this.speedX;
      this.y += this.speedY;

      if (this.x < 0 || this.x > this.canvas.width) {
        this.speedX *= -1;
      }
      if (this.y < 0 || this.y > this.canvas.height) {
        this.speedY *= -1;
      }
    }

    this.size = this.baseSize;

    if (!props.disableRotation) {
      this.angle += this.rotationSpeed;
    }

    if (this.x < 0) {
      this.x = 0;
    }
    if (this.x > this.canvas.width) {
      this.x = this.canvas.width;
    }
    if (this.y < 0) {
      this.y = 0;
    }
    if (this.y > this.canvas.height) {
      this.y = this.canvas.height;
    }
  }

  draw(ctx) {
    ctx.save();
    ctx.globalAlpha = this.opacity;
    ctx.translate(this.x, this.y);
    if (!props.disableRotation) {
      ctx.rotate(this.angle);
    }
    
    if (props.shape === 'crescent') {
      const gradient = ctx.createRadialGradient(-this.size * 0.3, -this.size * 0.3, 0, 0, 0, this.size);
      gradient.addColorStop(0, '#ffffff');
      gradient.addColorStop(0.3, '#666666');
      gradient.addColorStop(1, '#000000');
      
      ctx.fillStyle = gradient;
      ctx.beginPath();
      ctx.arc(0, 0, this.size, 0, Math.PI * 2);
      ctx.fill();
      
      ctx.globalCompositeOperation = 'destination-out';
      ctx.beginPath();
      ctx.arc(this.size * 0.4, -this.size * 0.3, this.size * 0.7, 0, Math.PI * 2);
      ctx.fill();
    } else {
      ctx.fillStyle = this.color;
      ctx.beginPath();
      ctx.arc(0, 0, this.size, 0, Math.PI * 2);
      ctx.fill();
    }
    
    ctx.restore();
  }
}

const initParticles = () => {
  const canvas = canvasRef.value;
  if (!canvas) return;
  particles = [];
  for (let i = 0; i < props.particleCount; i++) {
    particles.push(new Particle(canvas));
  }
};

const animate = () => {
  const canvas = canvasRef.value;
  if (!canvas) return;
  const ctx = canvas.getContext('2d');
  ctx.clearRect(0, 0, canvas.width, canvas.height);

  particles.forEach(particle => {
    particle.update();
    particle.draw(ctx);
  });

  animationId = requestAnimationFrame(animate);
};

const handleResize = () => {
  const canvas = canvasRef.value;
  if (!canvas) return;
  const parent = canvas.parentElement;
  if (!parent) return;
  
  canvas.width = parent.clientWidth * props.pixelRatio;
  canvas.height = parent.clientHeight * props.pixelRatio;
  canvas.style.width = parent.clientWidth + 'px';
  canvas.style.height = parent.clientHeight + 'px';
  
  initParticles();
};

const handleMouseMove = (e) => {
  lastMouseX = mouseX;
  lastMouseY = mouseY;
  mouseX = e.clientX * props.pixelRatio;
  mouseY = e.clientY * props.pixelRatio;
  
  mouseSpeedX = mouseX - lastMouseX;
  mouseSpeedY = mouseY - lastMouseY;
  
  isMouseMoving = true;
  
  if (mouseMoveTimeout) {
    clearTimeout(mouseMoveTimeout);
  }
  
  mouseMoveTimeout = setTimeout(() => {
    isMouseMoving = false;
  }, 100);
};

onMounted(() => {
  handleResize();
  window.addEventListener('resize', handleResize);
  window.addEventListener('mousemove', handleMouseMove);
  
  animate();
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize);
  window.removeEventListener('mousemove', handleMouseMove);
  if (mouseMoveTimeout) {
    clearTimeout(mouseMoveTimeout);
  }
  if (animationId) {
    cancelAnimationFrame(animationId);
  }
});
</script>

<style scoped>
.particles-canvas {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
}
</style>
