<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import {
  NButton, NCard, NSpace, NEmpty, NModal, NForm, NFormItem,
  NInput, NSelect, NDatePicker, NTimeline, NTimelineItem,
  NTag, NPopconfirm, NPagination,
} from 'naive-ui'
import { getGrowthLogs, createGrowthLog, updateGrowthLog, deleteGrowthLog } from '@/api/growth'
import { getBabies } from '@/api/baby'
import type { GrowthLog, GrowthLogDTO, Baby } from '@/types'
import { LogTypeLabels } from '@/types'

const loading = ref(false)
const logs = ref<GrowthLog[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const babies = ref<Baby[]>([])
const filterBabyId = ref<number | null>(null)
const filterLogType = ref<number | null>(null)

const showModal = ref(false)
const editingId = ref<number | null>(null)
const formData = ref<GrowthLogDTO>({
  babyId: 0, logType: 1, title: '', content: '', logTime: '',
})

const logTypeOptions = Object.entries(LogTypeLabels).map(([k, v]) => ({
  label: v,
  value: Number(k),
}))

const babyOptions = computed(() =>
  babies.value.map((b) => ({ label: b.name, value: b.id })),
)

const logTypeColor: Record<number, string> = {
  1: 'info', 2: 'success', 3: 'error', 4: 'warning', 5: 'info', 6: 'default',
}

onMounted(async () => {
  await loadBabies()
  await loadLogs()
})

async function loadBabies() {
  try {
    const res = await getBabies({ page: 1, size: 100 })
    babies.value = res.data.records || []
    if (babies.value.length > 0 && !formData.value.babyId) {
      formData.value.babyId = babies.value[0]!.id
    }
  } catch { /* skip */ }
}

async function loadLogs() {
  loading.value = true
  try {
    const params: any = { page: page.value, size: pageSize.value }
    if (filterBabyId.value) params.babyId = filterBabyId.value
    if (filterLogType.value) params.logType = filterLogType.value
    const res = await getGrowthLogs(params)
    logs.value = res.data.records || []
    total.value = res.data.total || 0
  } catch { /* skip */ } finally {
    loading.value = false
  }
}

function openCreate() {
  editingId.value = null
  const now = new Date()
  formData.value = {
    babyId: filterBabyId.value || (babies.value[0]?.id ?? 0),
    logType: 1,
    title: '',
    content: '',
    logTime: now.toISOString().slice(0, 16).replace('T', ' ') + ':00',
  }
  showModal.value = true
}

function openEdit(log: GrowthLog) {
  editingId.value = log.id
  formData.value = {
    babyId: log.babyId,
    logType: log.logType,
    title: log.title || '',
    content: log.content || '',
    logTime: log.logTime,
  }
  showModal.value = true
}

async function handleSave() {
  if (!formData.value.babyId) {
    window.$message?.warning('请选择宝宝')
    return
  }
  try {
    if (editingId.value) {
      await updateGrowthLog(editingId.value, formData.value)
      window.$message?.success('更新成功')
    } else {
      await createGrowthLog(formData.value)
      window.$message?.success('记录成功')
    }
    showModal.value = false
    loadLogs()
  } catch { /* handled */ }
}

async function handleDelete(id: number) {
  try {
    await deleteGrowthLog(id)
    window.$message?.success('已删除')
    loadLogs()
  } catch { /* handled */ }
}

function formatTime(t: string) {
  const d = new Date(t)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

function getBabyName(id: number) {
  return babies.value.find((b) => b.id === id)?.name || ''
}
</script>

<template>
  <div class="container section">
    <div class="page-header">
      <div>
        <h1 class="page-title">成长日志</h1>
        <p class="page-subtitle">记录宝宝的每一个珍贵瞬间</p>
      </div>
      <NButton type="primary" @click="openCreate">+ 新增记录</NButton>
    </div>

    <!-- 筛选 -->
    <NCard :bordered="false" class="filter-card" style="margin-bottom: 24px">
      <NSpace>
        <NSelect
          v-model:value="filterBabyId"
          :options="babyOptions"
          placeholder="选择宝宝"
          clearable
          style="width: 160px"
          @update:value="() => { page = 1; loadLogs() }"
        />
        <NSelect
          v-model:value="filterLogType"
          :options="logTypeOptions"
          placeholder="日志类型"
          clearable
          style="width: 140px"
          @update:value="() => { page = 1; loadLogs() }"
        />
      </NSpace>
    </NCard>

    <NEmpty v-if="!loading && logs.length === 0" description="还没有成长日志，快来记录一下吧！">
      <template #extra>
        <NButton type="primary" @click="openCreate">开始记录</NButton>
      </template>
    </NEmpty>

    <!-- 时间线 -->
    <NTimeline v-else>
      <NTimelineItem
        v-for="log in logs"
        :key="log.id"
        :time="formatTime(log.logTime)"
        :type="(logTypeColor[log.logType] as any) || 'default'"
      >
        <NCard :bordered="false" size="small" class="log-card">
          <div class="log-header">
            <div class="log-title-area">
              <NTag :bordered="false" size="small" :type="(logTypeColor[log.logType] as any) || 'default'">
                {{ LogTypeLabels[log.logType] || '其他' }}
              </NTag>
              <span class="log-baby-name">{{ getBabyName(log.babyId) }}</span>
              <h4 class="log-title" v-if="log.title">{{ log.title }}</h4>
            </div>
            <NSpace :size="4">
              <NButton text size="tiny" @click="openEdit(log)">编辑</NButton>
              <NPopconfirm @positive-click="handleDelete(log.id)">
                <template #trigger>
                  <NButton text size="tiny" type="error">删除</NButton>
                </template>
                确定删除这条日志吗？
              </NPopconfirm>
            </NSpace>
          </div>
          <p class="log-content" v-if="log.content">{{ log.content }}</p>
        </NCard>
      </NTimelineItem>
    </NTimeline>

    <div class="pagination-wrapper" v-if="total > pageSize">
      <NPagination
        v-model:page="page"
        :page-size="pageSize"
        :item-count="total"
        @update:page="loadLogs"
      />
    </div>

    <!-- 新增/编辑弹窗 -->
    <NModal
      v-model:show="showModal"
      :title="editingId ? '编辑日志' : '新增成长日志'"
      preset="card"
      style="width: 520px; border-radius: 16px"
    >
      <NForm>
        <NFormItem label="宝宝" required>
          <NSelect v-model:value="formData.babyId" :options="babyOptions" placeholder="请选择宝宝" />
        </NFormItem>
        <NFormItem label="日志类型" required>
          <NSelect v-model:value="formData.logType" :options="logTypeOptions" />
        </NFormItem>
        <NFormItem label="标题">
          <NInput v-model:value="formData.title" placeholder="可选标题" />
        </NFormItem>
        <NFormItem label="记录时间" required>
          <NDatePicker
            v-model:formatted-value="formData.logTime"
            type="datetime"
            value-format="yyyy-MM-dd HH:mm:ss"
            style="width: 100%"
          />
        </NFormItem>
        <NFormItem label="内容">
          <NInput v-model:value="formData.content" type="textarea" placeholder="详细记录" :rows="4" />
        </NFormItem>
      </NForm>
      <template #footer>
        <NSpace justify="end">
          <NButton @click="showModal = false">取消</NButton>
          <NButton type="primary" @click="handleSave">保存</NButton>
        </NSpace>
      </template>
    </NModal>
  </div>
</template>

<style scoped>
.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: var(--space-xl);
}

.filter-card {
  border-radius: var(--radius-lg) !important;
}

.log-card {
  border-radius: var(--radius-md) !important;
  box-shadow: var(--shadow-sm);
}

.log-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--space-sm);
}

.log-title-area {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  flex-wrap: wrap;
}

.log-baby-name {
  font-size: var(--font-size-xs);
  color: var(--color-text-tertiary);
}

.log-title {
  font-size: var(--font-size-base);
  font-weight: 600;
}

.log-content {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  line-height: 1.6;
  white-space: pre-wrap;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: var(--space-2xl);
}
</style>
