<script setup lang="ts">
import { ref, onMounted, computed, watch, h } from 'vue'
import {
  NButton, NCard, NSpace, NEmpty, NModal, NForm, NFormItem,
  NInputNumber, NSelect, NDatePicker, NDataTable, NInput,
  NPopconfirm, NPagination,
} from 'naive-ui'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { getGrowthMetrics, createGrowthMetric, updateGrowthMetric, deleteGrowthMetric, getBabyMetricStats } from '@/api/growth'
import { getBabies } from '@/api/baby'
import type { GrowthMetric, GrowthMetricDTO, Baby } from '@/types'
import { MetricTypeLabels, MetricType } from '@/types'

use([LineChart, GridComponent, TooltipComponent, LegendComponent, CanvasRenderer])

const loading = ref(false)
const metrics = ref<GrowthMetric[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const babies = ref<Baby[]>([])
const selectedBabyId = ref<number | null>(null)
const selectedMetricType = ref<number>(MetricType.HEIGHT)

const showModal = ref(false)
const editingId = ref<number | null>(null)
const formData = ref<GrowthMetricDTO>({
  babyId: 0, metricType: 1, metricValue: 0, unit: 'cm', recordedAt: '', note: '',
})

const chartData = ref<GrowthMetric[]>([])

const metricTypeOptions = Object.entries(MetricTypeLabels).map(([k, v]) => ({
  label: v as string, value: Number(k),
}))

const unitMap: Record<number, string> = { 1: 'cm', 2: 'kg', 3: 'cm', 4: '°C' }

const babyOptions = computed(() =>
  babies.value.map((b) => ({ label: b.name, value: b.id })),
)

const chartOption = computed(() => {
  const sorted = [...chartData.value].sort(
    (a, b) => new Date(a.recordedAt).getTime() - new Date(b.recordedAt).getTime(),
  )
  return {
    tooltip: { trigger: 'axis' as const },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category' as const,
      data: sorted.map((m) => {
        const d = new Date(m.recordedAt)
        return `${d.getMonth() + 1}/${d.getDate()}`
      }),
      axisLabel: { color: '#64748B' },
    },
    yAxis: {
      type: 'value' as const,
      name: MetricTypeLabels[selectedMetricType.value] || '',
      axisLabel: { color: '#64748B' },
    },
    series: [
      {
        name: MetricTypeLabels[selectedMetricType.value] || '',
        type: 'line' as const,
        smooth: true,
        data: sorted.map((m) => m.metricValue),
        lineStyle: { color: '#7C6FBF', width: 3 },
        itemStyle: { color: '#7C6FBF' },
        areaStyle: {
          color: {
            type: 'linear' as const,
            x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(124,111,191,0.3)' },
              { offset: 1, color: 'rgba(124,111,191,0.02)' },
            ],
          },
        },
      },
    ],
  }
})

onMounted(async () => {
  await loadBabies()
  await loadMetrics()
})

watch([selectedBabyId, selectedMetricType], () => {
  page.value = 1
  loadMetrics()
  loadChart()
})

async function loadBabies() {
  try {
    const res = await getBabies({ page: 1, size: 100 })
    babies.value = res.data.records || []
    if (babies.value.length > 0) {
      selectedBabyId.value = babies.value[0]!.id
      formData.value.babyId = babies.value[0]!.id
    }
  } catch { /* skip */ }
}

async function loadMetrics() {
  loading.value = true
  try {
    const params: any = { page: page.value, size: pageSize.value }
    if (selectedBabyId.value) params.babyId = selectedBabyId.value
    params.metricType = selectedMetricType.value
    const res = await getGrowthMetrics(params)
    metrics.value = res.data.records || []
    total.value = res.data.total || 0
  } catch { /* skip */ } finally {
    loading.value = false
  }
  loadChart()
}

async function loadChart() {
  if (!selectedBabyId.value) return
  try {
    const res = await getBabyMetricStats(selectedBabyId.value, {
      metricType: selectedMetricType.value,
    })
    chartData.value = Array.isArray(res.data) ? res.data : []
  } catch {
    chartData.value = []
  }
}

function openCreate() {
  editingId.value = null
  const now = new Date()
  formData.value = {
    babyId: selectedBabyId.value || (babies.value[0]?.id ?? 0),
    metricType: selectedMetricType.value,
    metricValue: 0,
    unit: unitMap[selectedMetricType.value] || '',
    recordedAt: now.toISOString().slice(0, 16).replace('T', ' ') + ':00',
    note: '',
  }
  showModal.value = true
}

function openEdit(m: GrowthMetric) {
  editingId.value = m.id
  formData.value = {
    babyId: m.babyId,
    metricType: m.metricType,
    metricValue: m.metricValue,
    unit: m.unit || '',
    recordedAt: m.recordedAt,
    note: m.note || '',
  }
  showModal.value = true
}

async function handleSave() {
  if (!formData.value.babyId || !formData.value.metricValue) {
    window.$message?.warning('请填写完整信息')
    return
  }
  try {
    if (editingId.value) {
      await updateGrowthMetric(editingId.value, formData.value)
      window.$message?.success('更新成功')
    } else {
      await createGrowthMetric(formData.value)
      window.$message?.success('记录成功')
    }
    showModal.value = false
    loadMetrics()
  } catch { /* handled */ }
}

async function handleDelete(id: number) {
  try {
    await deleteGrowthMetric(id)
    window.$message?.success('已删除')
    loadMetrics()
  } catch { /* handled */ }
}

const columns = [
  {
    title: '记录时间',
    key: 'recordedAt',
    render: (row: GrowthMetric) => {
      const d = new Date(row.recordedAt)
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
    },
  },
  {
    title: '数值',
    key: 'metricValue',
    render: (row: GrowthMetric) => `${row.metricValue} ${row.unit || ''}`,
  },
  { title: '备注', key: 'note', ellipsis: { tooltip: true } },
  {
    title: '操作',
    key: 'actions',
    width: 120,
    render: (row: GrowthMetric) => {
      return [
        h(NButton, { text: true, size: 'small', onClick: () => openEdit(row) }, { default: () => '编辑' }),
        h(NPopconfirm, { onPositiveClick: () => handleDelete(row.id) }, {
          trigger: () => h(NButton, { text: true, size: 'small', type: 'error', style: 'margin-left:8px' }, { default: () => '删除' }),
          default: () => '确定删除？',
        }),
      ]
    },
  },
]

</script>

<template>
  <div class="container section">
    <div class="page-header">
      <div>
        <h1 class="page-title">成长指标</h1>
        <p class="page-subtitle">科学追踪宝宝成长数据</p>
      </div>
      <NButton type="primary" @click="openCreate">+ 记录指标</NButton>
    </div>

    <!-- 筛选 -->
    <NCard :bordered="false" class="filter-card" style="margin-bottom: 24px">
      <NSpace>
        <NSelect
          v-model:value="selectedBabyId"
          :options="babyOptions"
          placeholder="选择宝宝"
          style="width: 160px"
        />
        <NSelect
          v-model:value="selectedMetricType"
          :options="metricTypeOptions"
          placeholder="指标类型"
          style="width: 160px"
        />
      </NSpace>
    </NCard>

    <!-- 图表 -->
    <NCard :bordered="false" class="chart-card" style="margin-bottom: 24px" v-if="chartData.length > 0">
      <h3 style="margin-bottom: 12px; font-weight: 600">成长曲线</h3>
      <VChart :option="chartOption" style="height: 320px" autoresize />
    </NCard>

    <NEmpty v-if="!loading && metrics.length === 0" description="还没有记录成长指标">
      <template #extra>
        <NButton type="primary" @click="openCreate">开始记录</NButton>
      </template>
    </NEmpty>

    <NCard :bordered="false" v-else>
      <NDataTable :columns="columns" :data="metrics" :loading="loading" size="small" />
      <div class="pagination-wrapper" v-if="total > pageSize">
        <NPagination
          v-model:page="page"
          :page-size="pageSize"
          :item-count="total"
          @update:page="loadMetrics"
          style="margin-top: 16px; justify-content: center"
        />
      </div>
    </NCard>

    <!-- 弹窗 -->
    <NModal
      v-model:show="showModal"
      :title="editingId ? '编辑指标' : '记录成长指标'"
      preset="card"
      style="width: 520px; border-radius: 16px"
    >
      <NForm>
        <NFormItem label="宝宝" required>
          <NSelect v-model:value="formData.babyId" :options="babyOptions" />
        </NFormItem>
        <NFormItem label="指标类型" required>
          <NSelect
            v-model:value="formData.metricType"
            :options="metricTypeOptions"
            @update:value="(v: number) => { formData.unit = unitMap[v] || '' }"
          />
        </NFormItem>
        <NFormItem label="数值" required>
          <NInputNumber v-model:value="formData.metricValue" :min="0" :precision="2" style="width: 100%">
            <template #suffix>{{ formData.unit }}</template>
          </NInputNumber>
        </NFormItem>
        <NFormItem label="记录时间" required>
          <NDatePicker
            v-model:formatted-value="formData.recordedAt"
            type="datetime"
            value-format="yyyy-MM-dd HH:mm:ss"
            style="width: 100%"
          />
        </NFormItem>
        <NFormItem label="备注">
          <NInput v-model:value="formData.note" placeholder="可选备注" />
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

.filter-card, .chart-card {
  border-radius: var(--radius-lg) !important;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
}
</style>
