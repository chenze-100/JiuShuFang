<template>
  <slot></slot>
</template>

<script setup>
import { ElMessageBox } from 'element-plus'

const props = defineProps({
  title: {
    type: String,
    default: '确认删除'
  },
  message: {
    type: String,
    default: '确定要删除吗？删除后将无法恢复。'
  },
  confirmText: {
    type: String,
    default: '删除'
  },
  cancelText: {
    type: String,
    default: '取消'
  },
  confirmType: {
    type: String,
    default: 'warning' // warning, danger, primary
  }
})

const emit = defineEmits(['confirm', 'cancel'])

const show = async () => {
  try {
    await ElMessageBox.confirm(
      props.message,
      props.title,
      {
        confirmButtonText: props.confirmText,
        cancelButtonText: props.cancelText,
        type: props.confirmType,
        customClass: 'common-confirm-dialog'
      }
    )
    emit('confirm')
  } catch (error) {
    emit('cancel', error)
  }
}

defineExpose({ show })
</script>

<style scoped>
/* ConfirmDialog styles are in global CSS */
</style>
