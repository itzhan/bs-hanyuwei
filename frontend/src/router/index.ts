import { createRouter, createWebHashHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/store/user'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/LoginPage.vue'),
    meta: { guest: true },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/RegisterPage.vue'),
    meta: { guest: true },
  },
  {
    path: '/',
    component: () => import('@/components/AppLayout.vue'),
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/home/HomePage.vue'),
      },
      {
        path: 'babies',
        name: 'BabyList',
        component: () => import('@/views/baby/BabyList.vue'),
        meta: { requiresAuth: true },
      },
      {
        path: 'babies/:id',
        name: 'BabyDetail',
        component: () => import('@/views/baby/BabyDetail.vue'),
        meta: { requiresAuth: true },
      },
      {
        path: 'growth/logs',
        name: 'GrowthLogs',
        component: () => import('@/views/growth/GrowthLogs.vue'),
        meta: { requiresAuth: true },
      },
      {
        path: 'growth/metrics',
        name: 'GrowthMetrics',
        component: () => import('@/views/growth/GrowthMetrics.vue'),
        meta: { requiresAuth: true },
      },
      {
        path: 'community',
        name: 'PostList',
        component: () => import('@/views/community/PostList.vue'),
      },
      {
        path: 'community/create',
        name: 'PostCreate',
        component: () => import('@/views/community/PostCreate.vue'),
        meta: { requiresAuth: true },
      },
      {
        path: 'community/:id',
        name: 'PostDetail',
        component: () => import('@/views/community/PostDetail.vue'),
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/profile/ProfilePage.vue'),
        meta: { requiresAuth: true },
      },
    ],
  },
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 }),
})

/* 导航守卫 */
router.beforeEach((to, _from, next) => {
  const userStore = useUserStore()

  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
  } else if (to.meta.guest && userStore.isLoggedIn) {
    next({ name: 'Home' })
  } else {
    next()
  }
})

export default router
