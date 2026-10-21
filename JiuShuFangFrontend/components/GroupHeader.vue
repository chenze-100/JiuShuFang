<template>
  <div class="group-header">{{ label }}</div>
</template>

<script setup>
defineProps({
  label: {
    type: String,
    required: true
  }
})
</script>

<style scoped>
.group-header {
  font-size: 12px;
  color: #999;
  padding: 10px 15px 4px;
  background-color: #fafafa;
  font-weight: 500;
}
</style>
