<template>
  <div class="sidebar" :class="{ 'gradient': gradient }">
    <div class="sidebar-header">
      <h1>{{ title }}</h1>
      <span v-if="backLink" class="back-home-link" @click="$router.push(backLink)">返回首页</span>
    </div>
    <div class="sidebar-menu">
      <div
        v-for="(item, index) in menuItems"
        :key="index"
        :class="['menu-item', { 'active': activeIndex === index }]"
        @click="handleClick(item)"
      >
        {{ item.label }}
      </div>
    </div>
    <slot></slot>
  </div>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    default: '菜单'
  },
  menuItems: {
    type: Array,
    default: () => []
  },
  activeIndex: {
    type: Number,
    default: -1
  },
  backLink: {
    type: String,
    default: '/home'
  },
  gradient: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['select'])

const handleClick = (item) => {
  if (item.action) {
    item.action()
  }
  emit('select', item)
}
</script>

<style scoped>
.sidebar {
  width: 200px;
  background-color: #fff;
  border-radius: 8px;
  padding: 20px 0;
  height: fit-content;
}

.sidebar.gradient {
  background: linear-gradient(180deg, #fff5f0 0%, #ffffff 100%);
}

.sidebar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px 15px;
  border-bottom: 1px solid #e5e5e5;
}

.sidebar-header h1 {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  margin: 0;
}

.back-home-link {
  font-size: 14px;
  color: #ff6600;
  cursor: pointer;
}

.back-home-link:hover {
  color: #ff8533;
}

.sidebar-menu {
  padding: 10px 0;
}

.menu-item {
  padding: 12px 20px;
  color: #666;
  cursor: pointer;
  transition: all 0.3s;
}

.menu-item:hover {
  background-color: #fff5f0;
  color: #ff6b00;
}

.menu-item.active {
  background-color: #fff5f0;
  color: #ff6b00;
  font-weight: bold;
}
</style>
