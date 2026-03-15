<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NButton, NCard, NSpace, NDescriptions, NDescriptionsItem, NAvatar, NTag } from 'naive-ui'
import { FileText, BarChart3 } from 'lucide-vue-next'
import { getBaby } from '@/api/baby'
import type { Baby } from '@/types'

const route = useRoute()
const router = useRouter()
const baby = ref<Baby | null>(null)
const loading = ref(true)

const genderLabel: Record<number, string> = { 0: '未知', 1: '男宝', 2: '女宝' }

onMounted(async () => {
  const id = Number(route.params.id)
  try {
    const res = await getBaby(id)
    baby.value = res.data
  } catch {
    window.$message?.error('宝宝信息加载失败')
  } finally {
    loading.value = false
  }
})

function calcAge(birthday: string | null): string {
  if (!birthday) return '未设置'
  const born = new Date(birthday)
  const now = new Date()
  const months = (now.getFullYear() - born.getFullYear()) * 12 + now.getMonth() - born.getMonth()
  if (months < 12) return `${months}个月`
  const years = Math.floor(months / 12)
  const rest = months % 12
  return rest > 0 ? `${years}岁${rest}个月` : `${years}岁`
}
</script>

<template>
  <div class="container section">
    <NButton text @click="router.push('/babies')" style="margin-bottom: 16px">← 返回宝宝列表</NButton>

    <NCard v-if="baby" :bordered="false" class="detail-card">
      <div class="detail-header">
        <NAvatar
          :size="80"
          round
          :src="baby.avatarPath ? `/uploads/${baby.avatarPath}` : undefined"
          style="background: var(--color-primary-lighter); font-size: 40px"
        >
          {{ genderLabel[baby.gender ?? 0] }}
        </NAvatar>
        <div class="detail-title-area">
          <h1 class="detail-name">{{ baby.name }}</h1>
          <NTag :bordered="false" size="small" type="info">
            {{ genderLabel[baby.gender ?? 0] }}
          </NTag>
        </div>
      </div>

      <NDescriptions :column="2" label-placement="left" bordered style="margin-top: 24px">
        <NDescriptionsItem label="出生日期">
          {{ baby.birthday || '未设置' }}
        </NDescriptionsItem>
        <NDescriptionsItem label="年龄">
          {{ calcAge(baby.birthday) }}
        </NDescriptionsItem>
        <NDescriptionsItem label="与宝宝关系">
          {{ baby.relation || '未设置' }}
        </NDescriptionsItem>
        <NDescriptionsItem label="备注">
          {{ baby.note || '无' }}
        </NDescriptionsItem>
      </NDescriptions>

      <NSpace style="margin-top: 24px">
        <NButton type="primary" @click="router.push('/growth/logs')"><FileText :size="14" style="margin-right:4px" /> 查看成长日志</NButton>
        <NButton @click="router.push('/growth/metrics')"><BarChart3 :size="14" style="margin-right:4px" /> 查看成长指标</NButton>
      </NSpace>
    </NCard>
  </div>
</template>

<style scoped>
.detail-card {
  border-radius: var(--radius-xl) !important;
  box-shadow: var(--shadow-md);
}

.detail-header {
  display: flex;
  align-items: center;
  gap: var(--space-xl);
}

.detail-name {
  font-size: var(--font-size-2xl);
  font-weight: 700;
  margin-bottom: var(--space-xs);
}

.detail-title-area {
  display: flex;
  flex-direction: column;
  gap: var(--space-xs);
}
</style>
