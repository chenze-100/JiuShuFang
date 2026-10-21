<template>
  <div class="footer">
    <div class="footer-content">
      <div class="footer-links">
        <span><a href="#">关于我们</a></span>
        <span><a href="#">联系我们</a></span>
        <span><a href="#">商家入驻</a></span>
        <span><a href="#">友情链接</a></span>
        <span><a href="#">帮助中心</a></span>
      </div>
    </div>
  </div>
</template>

<script setup>
</script>

<style scoped>
.footer {
  background-color: #f5f5f5;
  border-top: 1px solid #e5e5e5;
  padding: 30px 0;
  margin-top: auto;
}

.footer-content {
  width: 1200px;
  margin: 0 auto;
  text-align: center;
}

.footer-links {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-bottom: 20px;
}

.footer-links span {
  color: var(--color-neutral-500);
  font-size: 12px;
}

.footer-links span a {
  color: var(--color-neutral-500);
  text-decoration: none;
}

.footer-links span a:hover {
  color: var(--color-error);
}
</style>
