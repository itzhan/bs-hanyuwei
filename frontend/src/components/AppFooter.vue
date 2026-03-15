<script setup lang="ts">
import { Milk } from 'lucide-vue-next'
</script>

<template>
  <footer class="app-footer">
    <div class="container footer-inner">
      <div class="footer-brand">
        <span class="footer-logo"><Milk :size="18" style="vertical-align: middle; margin-right: 4px" /> 育儿社区</span>
        <p class="footer-desc">记录宝宝每一步成长，分享育儿经验与喜悦</p>
      </div>
      <div class="footer-links">
        <router-link to="/">首页</router-link>
        <router-link to="/community">社区</router-link>
        <router-link to="/babies">宝宝档案</router-link>
      </div>
      <div class="footer-copy">
        <p>© 2026 育儿社区. 毕业设计作品</p>
      </div>
    </div>
  </footer>
</template>

<style scoped>
.app-footer {
  background: var(--color-card);
  border-top: 1px solid var(--color-border-light);
  padding: var(--space-2xl) 0;
  margin-top: var(--space-3xl);
}

.footer-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: var(--space-lg);
}

.footer-brand {
  display: flex;
  flex-direction: column;
  gap: var(--space-xs);
}

.footer-logo {
  font-size: var(--font-size-md);
  font-weight: 700;
  color: var(--color-primary);
}

.footer-desc {
  font-size: var(--font-size-xs);
  color: var(--color-text-tertiary);
}

.footer-links {
  display: flex;
  gap: var(--space-lg);
}

.footer-links a {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

.footer-links a:hover {
  color: var(--color-primary);
}

.footer-copy p {
  font-size: var(--font-size-xs);
  color: var(--color-text-tertiary);
}
</style>
