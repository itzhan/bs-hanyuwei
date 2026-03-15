<script setup lang="ts">
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { NButton, NAvatar, NDropdown, NSpace } from 'naive-ui'
import { Milk } from 'lucide-vue-next'
import { useUserStore } from '@/store/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const navItems = [
  { label: '首页', key: 'Home', path: '/' },
  { label: '社区', key: 'PostList', path: '/community' },
  { label: '宝宝档案', key: 'BabyList', path: '/babies', auth: true },
  { label: '成长日志', key: 'GrowthLogs', path: '/growth/logs', auth: true },
  { label: '成长指标', key: 'GrowthMetrics', path: '/growth/metrics', auth: true },
]

const visibleNav = computed(() =>
  navItems.filter((item) => !item.auth || userStore.isLoggedIn),
)

const activeKey = computed(() => {
  const matched = navItems.find((item) => {
    if (item.path === '/') return route.path === '/'
    return route.path.startsWith(item.path)
  })
  return matched?.key || 'Home'
})

const dropdownOptions = [
  { label: '个人中心', key: 'profile' },
  { type: 'divider', key: 'd1' },
  { label: '退出登录', key: 'logout' },
]

function handleSelect(key: string) {
  if (key === 'profile') router.push('/profile')
  else if (key === 'logout') {
    userStore.logout()
    router.push('/')
  }
}
</script>

<template>
  <header class="app-header">
    <div class="header-inner container">
      <!-- Logo -->
      <router-link to="/" class="logo">
        <Milk :size="24" class="logo-icon" />
        <span class="logo-text">育儿社区</span>
      </router-link>

      <!-- 导航 -->
      <nav class="nav-links">
        <router-link
          v-for="item in visibleNav"
          :key="item.key"
          :to="item.path"
          class="nav-link"
          :class="{ active: activeKey === item.key }"
        >
          {{ item.label }}
        </router-link>
      </nav>

      <!-- 右侧 -->
      <div class="header-right">
        <template v-if="userStore.isLoggedIn">
          <NDropdown :options="dropdownOptions" @select="handleSelect" trigger="click">
            <div class="user-menu">
              <NAvatar
                :size="32"
                round
                :src="userStore.user?.avatarPath ? `/uploads/${userStore.user.avatarPath}` : undefined"
                style="background: var(--color-primary-lighter); color: var(--color-primary); cursor: pointer"
              >
                {{ userStore.displayName?.charAt(0) || '用' }}
              </NAvatar>
              <span class="user-name">{{ userStore.displayName }}</span>
            </div>
          </NDropdown>
        </template>
        <template v-else>
          <NSpace :size="8">
            <NButton quaternary size="small" @click="router.push('/login')"> 登录 </NButton>
            <NButton type="primary" size="small" @click="router.push('/register')"> 注册 </NButton>
          </NSpace>
        </template>
      </div>
    </div>
  </header>
</template>

<style scoped>
.app-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: var(--header-height);
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid var(--color-border-light);
  z-index: 100;
}

.header-inner {
  display: flex;
  align-items: center;
  height: 100%;
  gap: var(--space-2xl);
}

.logo {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  text-decoration: none;
  flex-shrink: 0;
}

.logo-icon {
  font-size: 28px;
}

.logo-text {
  font-size: var(--font-size-lg);
  font-weight: 700;
  color: var(--color-primary);
}

.nav-links {
  display: flex;
  align-items: center;
  gap: var(--space-xs);
  flex: 1;
}

.nav-link {
  padding: 6px 16px;
  border-radius: var(--radius-full);
  font-size: var(--font-size-base);
  font-weight: 500;
  color: var(--color-text-secondary);
  text-decoration: none;
  transition: all var(--transition-fast);
}

.nav-link:hover {
  color: var(--color-primary);
  background: var(--color-primary-lighter);
}

.nav-link.active {
  color: var(--color-primary);
  background: var(--color-primary-lighter);
  font-weight: 600;
}

.header-right {
  flex-shrink: 0;
}

.user-menu {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  cursor: pointer;
  padding: 4px 8px;
  border-radius: var(--radius-full);
  transition: background var(--transition-fast);
}

.user-menu:hover {
  background: var(--color-primary-lighter);
}

.user-name {
  font-size: var(--font-size-sm);
  font-weight: 500;
  color: var(--color-text-primary);
}
</style>
