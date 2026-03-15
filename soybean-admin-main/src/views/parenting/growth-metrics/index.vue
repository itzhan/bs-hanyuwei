<script setup lang="ts">
import { h, onMounted, reactive, ref } from 'vue';
import { NButton, NPopconfirm, NSpace, NTag } from 'naive-ui';
import { createGrowthMetric, deleteGrowthMetric, fetchGrowthMetrics, updateGrowthMetric } from '@/service/api';
import type { GrowthMetricItem } from '@/service/api/parenting';

const loading = ref(false);
const data = ref<GrowthMetricItem[]>([]);

const query = reactive({
  userId: null as number | null,
  babyId: null as number | null,
  metricType: null as number | null,
  timeRange: null as [string, string] | null
});

const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  onUpdatePage: (page: number) => {
    pagination.page = page;
    loadData();
  },
  onUpdatePageSize: (size: number) => {
    pagination.pageSize = size;
    pagination.page = 1;
    loadData();
  }
});

const metricTypeOptions = [
  { label: '身高', value: 1 },
  { label: '体重', value: 2 },
  { label: '头围', value: 3 },
  { label: '体温', value: 4 },
  { label: '其他', value: 5 }
];

const modalVisible = ref(false);
const modalTitle = ref('');
const isEdit = ref(false);
const formModel = reactive({
  id: null as number | null,
  babyId: null as number | null,
  metricType: 1,
  metricValue: null as number | null,
  unit: '',
  recordedAt: null as string | null,
  note: ''
});

function resetForm() {
  formModel.id = null;
  formModel.babyId = null;
  formModel.metricType = 1;
  formModel.metricValue = null;
  formModel.unit = '';
  formModel.recordedAt = null;
  formModel.note = '';
}

function openCreate() {
  resetForm();
  isEdit.value = false;
  modalTitle.value = '新增成长指标';
  modalVisible.value = true;
}

function openEdit(row: GrowthMetricItem) {
  resetForm();
  isEdit.value = true;
  modalTitle.value = '编辑成长指标';
  formModel.id = row.id;
  formModel.babyId = row.babyId;
  formModel.metricType = row.metricType;
  formModel.metricValue = Number(row.metricValue);
  formModel.unit = row.unit || '';
  formModel.recordedAt = row.recordedAt || null;
  formModel.note = row.note || '';
  modalVisible.value = true;
}

async function submitForm() {
  const payload = {
    babyId: formModel.babyId,
    metricType: formModel.metricType,
    metricValue: formModel.metricValue,
    unit: formModel.unit,
    recordedAt: formModel.recordedAt,
    note: formModel.note
  };
  if (isEdit.value && formModel.id != null) {
    const { error } = await updateGrowthMetric(formModel.id, payload);
    if (!error) {
      window.$message?.success('更新成功');
      modalVisible.value = false;
      loadData();
    }
    return;
  }
  const { error } = await createGrowthMetric(payload);
  if (!error) {
    window.$message?.success('创建成功');
    modalVisible.value = false;
    loadData();
  }
}

async function remove(row: GrowthMetricItem) {
  const { error } = await deleteGrowthMetric(row.id);
  if (!error) {
    window.$message?.success('删除成功');
    loadData();
  }
}

function metricTypeLabel(value: number) {
  return metricTypeOptions.find(item => item.value === value)?.label || '-';
}

const columns = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '用户ID', key: 'userId', width: 90 },
  { title: '宝宝ID', key: 'babyId', width: 90 },
  {
    title: '指标类型',
    key: 'metricType',
    width: 120,
    render: (row: GrowthMetricItem) => h(NTag, { type: 'info' }, () => metricTypeLabel(row.metricType))
  },
  {
    title: '值',
    key: 'metricValue',
    width: 120,
    render: (row: GrowthMetricItem) => `${row.metricValue}${row.unit || ''}`
  },
  { title: '记录时间', key: 'recordedAt', minWidth: 160 },
  {
    title: '操作',
    key: 'actions',
    width: 160,
    render: (row: GrowthMetricItem) =>
      h(NSpace, { justify: 'center' }, () => [
        h(NButton, { size: 'small', onClick: () => openEdit(row) }, { default: () => '编辑' }),
        h(
          NPopconfirm,
          { onPositiveClick: () => remove(row) },
          {
            default: () => '确定删除该指标？',
            trigger: () => h(NButton, { size: 'small', type: 'error' }, { default: () => '删除' })
          }
        )
      ])
  }
];

async function loadData() {
  loading.value = true;
  const params: Record<string, any> = {
    page: pagination.page,
    size: pagination.pageSize,
    userId: query.userId ?? undefined,
    babyId: query.babyId ?? undefined,
    metricType: query.metricType ?? undefined
  };
  if (query.timeRange) {
    params.startTime = query.timeRange[0];
    params.endTime = query.timeRange[1];
  }
  const { data: res, error } = await fetchGrowthMetrics(params);
  if (!error && res) {
    data.value = res.records || [];
    pagination.itemCount = res.total || 0;
  }
  loading.value = false;
}

function resetQuery() {
  query.userId = null;
  query.babyId = null;
  query.metricType = null;
  query.timeRange = null;
  pagination.page = 1;
  loadData();
}

onMounted(() => {
  loadData();
});
</script>

<template>
  <NSpace vertical :size="16">
    <NCard :bordered="false" class="card-wrapper">
      <NForm inline :model="query" label-placement="left">
        <NFormItem label="用户ID">
          <NInputNumber v-model:value="query.userId" placeholder="用户ID" clearable />
        </NFormItem>
        <NFormItem label="宝宝ID">
          <NInputNumber v-model:value="query.babyId" placeholder="宝宝ID" clearable />
        </NFormItem>
        <NFormItem label="指标类型">
          <NSelect v-model:value="query.metricType" :options="metricTypeOptions" clearable />
        </NFormItem>
        <NFormItem label="时间范围">
          <NDatePicker
            v-model:formatted-value="query.timeRange"
            type="datetimerange"
            value-format="yyyy-MM-dd'T'HH:mm:ss"
            clearable
          />
        </NFormItem>
        <NFormItem>
          <NSpace>
            <NButton type="primary" @click="loadData">查询</NButton>
            <NButton @click="resetQuery">重置</NButton>
          </NSpace>
        </NFormItem>
      </NForm>
    </NCard>

    <NCard :bordered="false" class="card-wrapper">
      <NSpace justify="space-between" align="center" class="mb-12px">
        <div class="text-16px font-600">成长指标</div>
        <NButton type="primary" @click="openCreate">新增指标</NButton>
      </NSpace>
      <NDataTable remote :loading="loading" :columns="columns" :data="data" :pagination="pagination" />
    </NCard>

    <NModal v-model:show="modalVisible" preset="card" :title="modalTitle" :block-scroll="false" style="width: 560px">
      <NForm label-placement="left" label-width="90">
        <NFormItem label="宝宝ID">
          <NInputNumber v-model:value="formModel.babyId" placeholder="宝宝ID" />
        </NFormItem>
        <NFormItem label="指标类型">
          <NSelect v-model:value="formModel.metricType" :options="metricTypeOptions" />
        </NFormItem>
        <NFormItem label="值">
          <NInputNumber v-model:value="formModel.metricValue" placeholder="数值" />
        </NFormItem>
        <NFormItem label="单位">
          <NInput v-model:value="formModel.unit" placeholder="单位" />
        </NFormItem>
        <NFormItem label="记录时间">
          <NDatePicker
            v-model:formatted-value="formModel.recordedAt"
            type="datetime"
            value-format="yyyy-MM-dd'T'HH:mm:ss"
            clearable
          />
        </NFormItem>
        <NFormItem label="备注">
          <NInput v-model:value="formModel.note" type="textarea" placeholder="备注" />
        </NFormItem>
      </NForm>
      <template #footer>
        <NSpace justify="end">
          <NButton @click="modalVisible = false">取消</NButton>
          <NButton type="primary" @click="submitForm">保存</NButton>
        </NSpace>
      </template>
    </NModal>
  </NSpace>
</template>

<style scoped></style>
