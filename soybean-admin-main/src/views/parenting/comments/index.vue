<script setup lang="ts">
import { h, onMounted, reactive, ref } from 'vue';
import { NButton, NTag, NSpace } from 'naive-ui';
import { fetchAdminComments, updateCommentStatus } from '@/service/api';
import type { CommentItem } from '@/service/api/parenting';

const loading = ref(false);
const data = ref<CommentItem[]>([]);

const query = reactive({
  postId: null as number | null,
  userId: null as number | null,
  status: null as number | null,
  keyword: ''
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
  { label: '已发布', value: 1 },
  { label: '已隐藏', value: 2 },
  { label: '已删除', value: 3 }
];

const statusModalVisible = ref(false);
const statusForm = reactive({
  id: null as number | null,
  status: 1,
  reason: ''
});

function openStatus(row: CommentItem, status: number) {
  statusForm.id = row.id;
  statusForm.status = status;
  statusForm.reason = '';
  statusModalVisible.value = true;
}

async function submitStatus() {
  if (statusForm.id == null) return;
  const { error } = await updateCommentStatus(statusForm.id, statusForm.status, statusForm.reason || undefined);
  if (!error) {
    window.$message?.success('操作成功');
    statusModalVisible.value = false;
    loadData();
  }
}

function statusLabel(value: number) {
  return statusOptions.find(item => item.value === value)?.label || '-';
}

const columns = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '帖子ID', key: 'postId', width: 90 },
  { title: '用户ID', key: 'userId', width: 90 },
  {
    title: '状态',
    key: 'status',
    width: 120,
    render: (row: CommentItem) => h(NTag, { type: row.status === 1 ? 'success' : 'warning' }, () => statusLabel(row.status))
  },
  { title: '内容', key: 'content', minWidth: 220 },
  { title: '创建时间', key: 'createdAt', minWidth: 160 },
  {
    title: '操作',
    key: 'actions',
    width: 220,
    render: (row: CommentItem) =>
      h(NSpace, { justify: 'center' }, () => [
        h(NButton, { size: 'small', type: 'success', onClick: () => openStatus(row, 1) }, { default: () => '通过' }),
        h(NButton, { size: 'small', onClick: () => openStatus(row, 2) }, { default: () => '隐藏' }),
        h(NButton, { size: 'small', type: 'error', onClick: () => openStatus(row, 3) }, { default: () => '删除' })
      ])
  }
];

async function loadData() {
  loading.value = true;
  const params = {
    page: pagination.page,
    size: pagination.pageSize,
    postId: query.postId ?? undefined,
    userId: query.userId ?? undefined,
    status: query.status ?? undefined,
    keyword: query.keyword || undefined
  };
  const { data: res, error } = await fetchAdminComments(params);
  if (!error && res) {
    data.value = res.records || [];
    pagination.itemCount = res.total || 0;
  }
  loading.value = false;
}

function resetQuery() {
  query.postId = null;
  query.userId = null;
  query.status = null;
  query.keyword = '';
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
        <NFormItem label="帖子ID">
          <NInputNumber v-model:value="query.postId" placeholder="帖子ID" clearable />
        </NFormItem>
        <NFormItem label="用户ID">
          <NInputNumber v-model:value="query.userId" placeholder="用户ID" clearable />
        </NFormItem>
        <NFormItem label="状态">
          <NSelect v-model:value="query.status" :options="statusOptions" clearable />
        </NFormItem>
        <NFormItem label="关键词">
          <NInput v-model:value="query.keyword" placeholder="评论内容" clearable />
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
      <div class="text-16px font-600 mb-12px">评论审核</div>
      <NDataTable remote :loading="loading" :columns="columns" :data="data" :pagination="pagination" />
    </NCard>

    <NModal v-model:show="statusModalVisible" preset="card" title="评论处理" :block-scroll="false" style="width: 420px">
      <NForm label-placement="left" label-width="90">
        <NFormItem label="状态">
          <NSelect v-model:value="statusForm.status" :options="statusOptions" />
        </NFormItem>
        <NFormItem label="原因">
          <NInput v-model:value="statusForm.reason" placeholder="可选填写" />
        </NFormItem>
      </NForm>
      <template #footer>
        <NSpace justify="end">
          <NButton @click="statusModalVisible = false">取消</NButton>
          <NButton type="primary" @click="submitStatus">确认</NButton>
        </NSpace>
      </template>
    </NModal>
  </NSpace>
</template>

<style scoped></style>
