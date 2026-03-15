<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { NCard, NForm, NFormItem, NInput, NButton } from 'naive-ui'
import { Milk } from 'lucide-vue-next'
import { register } from '@/api/auth'
import type { RegisterDTO } from '@/types'

const router = useRouter()
const loading = ref(false)
const formData = ref<RegisterDTO>({ username: '', password: '', displayName: '' })
const confirmPassword = ref('')

async function handleRegister() {
  if (!formData.value.username || !formData.value.password || !formData.value.displayName) {
    window.$message?.warning('请填写完整信息')
    return
  }
  if (formData.value.password !== confirmPassword.value) {
    window.$message?.warning('两次输入的密码不一致')
    return
  }
  loading.value = true
  try {
    await register(formData.value)
    window.$message?.success('注册成功！请登录')
    router.push('/login')
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
        <p class="auth-subtitle">加入我们，一起分享育儿经验</p>
      </div>
      <NCard class="auth-card" :bordered="false">
        <h2 class="form-title">注册账号</h2>
        <NForm @submit.prevent="handleRegister">
          <NFormItem label="昵称">
            <NInput v-model:value="formData.displayName" placeholder="请输入您的昵称" size="large" />
          </NFormItem>
          <NFormItem label="用户名">
            <NInput v-model:value="formData.username" placeholder="请输入用户名" size="large" />
          </NFormItem>
          <NFormItem label="密码">
            <NInput
              v-model:value="formData.password"
              type="password"
              placeholder="请输入密码"
              size="large"
              show-password-on="click"
            />
          </NFormItem>
          <NFormItem label="确认密码">
            <NInput
              v-model:value="confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              size="large"
              show-password-on="click"
              @keyup.enter="handleRegister"
            />
          </NFormItem>
          <NButton
            type="primary"
            block
            size="large"
            :loading="loading"
            @click="handleRegister"
            style="margin-top: 8px"
          >
            注 册
          </NButton>
        </NForm>
        <div class="auth-footer">
          <span class="auth-footer-text">已有账号？</span>
          <router-link to="/login" class="auth-link">去登录</router-link>
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
</style>
