<script setup lang="ts">
import { h, onMounted, reactive, ref } from 'vue';
import { NButton, NTag, NSpace } from 'naive-ui';
import { fetchAdminReports, handleReport } from '@/service/api';
import type { ReportItem } from '@/service/api/parenting';

const loading = ref(false);
const data = ref<ReportItem[]>([]);

const query = reactive({
  status: null as number | null
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

const statusOptions = [
  { label: '待处理', value: 1 },
  { label: '已处理', value: 2 },
  { label: '已驳回', value: 3 }
];

const handleResultOptions = [
  { label: '忽略', value: 1 },
  { label: '删除', value: 2 },
  { label: '拉黑', value: 3 },
  { label: '警告', value: 4 }
];

const reasonOptions = [
  { label: '垃圾广告', value: 1 },
  { label: '辱骂攻击', value: 2 },
  { label: '隐私泄露', value: 3 },
  { label: '违法违规', value: 4 },
  { label: '虚假信息', value: 5 },
  { label: '其他', value: 6 }
];

const handleModalVisible = ref(false);
const handleForm = reactive({
  id: null as number | null,
  status: 2,
  handleResult: 1,
  handleNote: ''
});

function openHandle(row: ReportItem) {
  handleForm.id = row.id;
  handleForm.status = 2;
  handleForm.handleResult = 1;
  handleForm.handleNote = '';
  handleModalVisible.value = true;
}

async function submitHandle() {
  if (handleForm.id == null) return;
  const { error } = await handleReport(handleForm.id, {
    status: handleForm.status,
    handleResult: handleForm.handleResult,
    handleNote: handleForm.handleNote
  });
  if (!error) {
    window.$message?.success('处理成功');
    handleModalVisible.value = false;
    loadData();
  }
}

function statusLabel(value: number) {
  return statusOptions.find(item => item.value === value)?.label || '-';
}

function reasonLabel(value: number) {
  return reasonOptions.find(item => item.value === value)?.label || '-';
}

const columns = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '举报人', key: 'reporterId', width: 90 },
  { title: '帖子ID', key: 'postId', width: 90 },
  { title: '评论ID', key: 'commentId', width: 90 },
  {
    title: '原因',
    key: 'reason',
    width: 120,
    render: (row: ReportItem) => h(NTag, { type: 'warning' }, () => reasonLabel(row.reason))
  },
  {
    title: '状态',
    key: 'status',
    width: 120,
    render: (row: ReportItem) => h(NTag, { type: row.status === 2 ? 'success' : 'info' }, () => statusLabel(row.status))
  },
  { title: '说明', key: 'reasonDesc', minWidth: 160 },
  { title: '创建时间', key: 'createdAt', minWidth: 160 },
  {
    title: '操作',
    key: 'actions',
    width: 140,
    render: (row: ReportItem) =>
      h(NSpace, { justify: 'center' }, () => [
        h(NButton, { size: 'small', type: 'primary', onClick: () => openHandle(row) }, { default: () => '处理' })
      ])
  }
];

async function loadData() {
  loading.value = true;
  const params = {
    page: pagination.page,
    size: pagination.pageSize,
    status: query.status ?? undefined
  };
  const { data: res, error } = await fetchAdminReports(params);
  if (!error && res) {
    data.value = res.records || [];
    pagination.itemCount = res.total || 0;
  }
  loading.value = false;
}

function resetQuery() {
  query.status = null;
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
        <NFormItem label="状态">
          <NSelect v-model:value="query.status" :options="statusOptions" clearable />
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
      <div class="text-16px font-600 mb-12px">举报处理</div>
      <NDataTable remote :loading="loading" :columns="columns" :data="data" :pagination="pagination" />
    </NCard>

    <NModal v-model:show="handleModalVisible" preset="card" title="举报处理" :block-scroll="false" style="width: 420px">
      <NForm label-placement="left" label-width="90">
        <NFormItem label="状态">
          <NSelect v-model:value="handleForm.status" :options="statusOptions" />
        </NFormItem>
        <NFormItem label="处理结果">
          <NSelect v-model:value="handleForm.handleResult" :options="handleResultOptions" />
        </NFormItem>
        <NFormItem label="备注">
          <NInput v-model:value="handleForm.handleNote" placeholder="可选填写" />
        </NFormItem>
      </NForm>
      <template #footer>
        <NSpace justify="end">
          <NButton @click="handleModalVisible = false">取消</NButton>
          <NButton type="primary" @click="submitHandle">确认</NButton>
        </NSpace>
      </template>
    </NModal>
  </NSpace>
</template>

<style scoped></style>
