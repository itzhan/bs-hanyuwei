<script setup lang="ts">
import { h, onMounted, reactive, ref } from 'vue';
import { NButton, NPopconfirm, NSpace, NTag } from 'naive-ui';
import { createGrowthLog, deleteGrowthLog, fetchGrowthLogs, updateGrowthLog } from '@/service/api';
import type { GrowthLogItem } from '@/service/api/parenting';

const loading = ref(false);
const data = ref<GrowthLogItem[]>([]);

const query = reactive({
  userId: null as number | null,
  babyId: null as number | null,
  logType: null as number | null,
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

const logTypeOptions = [
  { label: '喂养', value: 1 },
  { label: '睡眠', value: 2 },
  { label: '疾病', value: 3 },
  { label: '里程碑', value: 4 },
  { label: '测量', value: 5 },
  { label: '其他', value: 6 }
];

const modalVisible = ref(false);
const modalTitle = ref('');
const isEdit = ref(false);
const formModel = reactive({
  id: null as number | null,
  babyId: null as number | null,
  logType: 1,
  title: '',
  content: '',
  logTime: null as string | null
});

function resetForm() {
  formModel.id = null;
  formModel.babyId = null;
  formModel.logType = 1;
  formModel.title = '';
  formModel.content = '';
  formModel.logTime = null;
}

function openCreate() {
  resetForm();
  isEdit.value = false;
  modalTitle.value = '新增成长日志';
  modalVisible.value = true;
}

function openEdit(row: GrowthLogItem) {
  resetForm();
  isEdit.value = true;
  modalTitle.value = '编辑成长日志';
  formModel.id = row.id;
  formModel.babyId = row.babyId;
  formModel.logType = row.logType;
  formModel.title = row.title || '';
  formModel.content = row.content || '';
  formModel.logTime = row.logTime || null;
  modalVisible.value = true;
}

async function submitForm() {
  const payload = {
    babyId: formModel.babyId,
    logType: formModel.logType,
    title: formModel.title,
    content: formModel.content,
    logTime: formModel.logTime
  };
  if (isEdit.value && formModel.id != null) {
    const { error } = await updateGrowthLog(formModel.id, payload);
    if (!error) {
      window.$message?.success('更新成功');
      modalVisible.value = false;
      loadData();
    }
    return;
  }
  const { error } = await createGrowthLog(payload);
  if (!error) {
    window.$message?.success('创建成功');
    modalVisible.value = false;
    loadData();
  }
}

async function remove(row: GrowthLogItem) {
  const { error } = await deleteGrowthLog(row.id);
  if (!error) {
    window.$message?.success('删除成功');
    loadData();
  }
}

function logTypeLabel(value: number) {
  return logTypeOptions.find(item => item.value === value)?.label || '-';
}

const columns = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '用户ID', key: 'userId', width: 90 },
  { title: '宝宝ID', key: 'babyId', width: 90 },
  {
    title: '类型',
    key: 'logType',
    width: 120,
    render: (row: GrowthLogItem) => h(NTag, { type: 'info' }, () => logTypeLabel(row.logType))
  },
  { title: '标题', key: 'title', minWidth: 140 },
  { title: '记录时间', key: 'logTime', minWidth: 160 },
  {
    title: '操作',
    key: 'actions',
    width: 160,
    render: (row: GrowthLogItem) =>
      h(NSpace, { justify: 'center' }, () => [
        h(NButton, { size: 'small', onClick: () => openEdit(row) }, { default: () => '编辑' }),
        h(
          NPopconfirm,
          { onPositiveClick: () => remove(row) },
          {
            default: () => '确定删除该日志？',
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
    logType: query.logType ?? undefined
  };
  if (query.timeRange) {
    params.startTime = query.timeRange[0];
    params.endTime = query.timeRange[1];
  }
  const { data: res, error } = await fetchGrowthLogs(params);
  if (!error && res) {
    data.value = res.records || [];
    pagination.itemCount = res.total || 0;
  }
  loading.value = false;
}

function resetQuery() {
  query.userId = null;
  query.babyId = null;
  query.logType = null;
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
        <NFormItem label="类型">
          <NSelect v-model:value="query.logType" :options="logTypeOptions" clearable />
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
        <div class="text-16px font-600">成长日志</div>
        <NButton type="primary" @click="openCreate">新增日志</NButton>
      </NSpace>
      <NDataTable remote :loading="loading" :columns="columns" :data="data" :pagination="pagination" />
    </NCard>

    <NModal v-model:show="modalVisible" preset="card" :title="modalTitle" :block-scroll="false" style="width: 560px">
      <NForm label-placement="left" label-width="90">
        <NFormItem label="宝宝ID">
          <NInputNumber v-model:value="formModel.babyId" placeholder="宝宝ID" />
        </NFormItem>
        <NFormItem label="类型">
          <NSelect v-model:value="formModel.logType" :options="logTypeOptions" />
        </NFormItem>
        <NFormItem label="标题">
          <NInput v-model:value="formModel.title" placeholder="标题" />
        </NFormItem>
        <NFormItem label="内容">
          <NInput v-model:value="formModel.content" type="textarea" placeholder="内容" />
        </NFormItem>
        <NFormItem label="记录时间">
          <NDatePicker
            v-model:formatted-value="formModel.logTime"
            type="datetime"
            value-format="yyyy-MM-dd'T'HH:mm:ss"
            clearable
          />
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
