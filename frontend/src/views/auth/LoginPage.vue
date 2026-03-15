<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { NCard, NForm, NFormItem, NInput, NButton } from 'naive-ui'
import { Milk } from 'lucide-vue-next'
import { login } from '@/api/auth'
import { useUserStore } from '@/store/user'
import type { LoginDTO } from '@/types'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)
const formData = ref<LoginDTO>({ username: '', password: '' })

async function handleLogin() {
  if (!formData.value.username || !formData.value.password) {
    window.$message?.warning('请填写用户名和密码')
    return
  }
  loading.value = true
  try {
    const res = await login(formData.value)
    userStore.setAuth(res.data.token, res.data.user)
    window.$message?.success('登录成功！')
    const redirect = (route.query.redirect as string) || '/'
    router.push(redirect)
  } catch {
    // 错误已在拦截器处理
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <div class="auth-container fade-in-up">
      <div class="auth-brand">
        <Milk :size="48" class="auth-icon" />
        <h1 class="auth-title">育儿社区</h1>
        <p class="auth-subtitle">记录宝宝每一步成长</p>
      </div>
      <NCard class="auth-card" :bordered="false">
        <h2 class="form-title">欢迎回来</h2>
        <NForm @submit.prevent="handleLogin">
          <NFormItem label="用户名">
            <NInput
              v-model:value="formData.username"
              placeholder="请输入用户名"
              size="large"
              @keyup.enter="handleLogin"
            />
          </NFormItem>
          <NFormItem label="密码">
            <NInput
              v-model:value="formData.password"
              type="password"
              placeholder="请输入密码"
              size="large"
              show-password-on="click"
              @keyup.enter="handleLogin"
            />
          </NFormItem>
          <NButton
            type="primary"
            block
            size="large"
            :loading="loading"
            @click="handleLogin"
            style="margin-top: 8px"
          >
            登 录
          </NButton>
        </NForm>
        <div class="auth-footer">
          <span class="auth-footer-text">还没有账号？</span>
          <router-link to="/register" class="auth-link">立即注册</router-link>
        </div>
      </NCard>
    </div>
  </div>
</template>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #F0ECF9 0%, #FAF8F5 50%, #FFF5EB 100%);
  padding: var(--space-xl);
}

.auth-container {
  width: 100%;
  max-width: 420px;
}

.auth-brand {
  text-align: center;
  margin-bottom: var(--space-2xl);
}

.auth-icon {
  font-size: 48px;
  display: block;
  margin-bottom: var(--space-sm);
}

.auth-title {
  font-size: var(--font-size-3xl);
  font-weight: 700;
  color: var(--color-primary);
  margin-bottom: var(--space-xs);
}

.auth-subtitle {
  font-size: var(--font-size-md);
  color: var(--color-text-secondary);
}

.auth-card {
  border-radius: var(--radius-xl) !important;
  box-shadow: var(--shadow-lg);
  padding: var(--space-lg);
}

.form-title {
  font-size: var(--font-size-xl);
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: var(--space-xl);
  text-align: center;
}

.auth-footer {
  text-align: center;
  margin-top: var(--space-xl);
}

.auth-footer-text {
  color: var(--color-text-tertiary);
  font-size: var(--font-size-sm);
}

.auth-link {
  color: var(--color-primary);
  font-weight: 500;
  font-size: var(--font-size-sm);
  margin-left: 4px;
}

.auth-link:hover {
  color: var(--color-primary-dark);
}
</style>
