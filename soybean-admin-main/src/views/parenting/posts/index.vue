<script setup lang="ts">
import { h, onMounted, reactive, ref } from 'vue';
import { NButton, NTag, NSpace } from 'naive-ui';
import { fetchAdminPosts, updatePostStatus } from '@/service/api';
import type { PostItem } from '@/service/api/parenting';

const loading = ref(false);
const data = ref<PostItem[]>([]);

const query = reactive({
  status: null as number | null,
  userId: null as number | null,
  categoryId: null as number | null,
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
  { label: '待审核', value: 2 },
  { label: '已隐藏', value: 3 },
  { label: '已驳回', value: 4 },
  { label: '已拉黑', value: 5 }
];

const statusModalVisible = ref(false);
const statusForm = reactive({
  id: null as number | null,
  status: 1,
  reason: ''
});

function openStatus(row: PostItem, status: number) {
  statusForm.id = row.id;
  statusForm.status = status;
  statusForm.reason = '';
  statusModalVisible.value = true;
}

async function submitStatus() {
  if (statusForm.id == null) return;
  const { error } = await updatePostStatus(statusForm.id, statusForm.status, statusForm.reason || undefined);
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
  { title: '用户ID', key: 'userId', width: 90 },
  { title: '标题', key: 'title', minWidth: 200 },
  {
    title: '状态',
    key: 'status',
    width: 120,
    render: (row: PostItem) => h(NTag, { type: row.status === 1 ? 'success' : 'warning' }, () => statusLabel(row.status))
  },
  { title: '评论数', key: 'commentCount', width: 100 },
  { title: '创建时间', key: 'createdAt', minWidth: 160 },
  {
    title: '操作',
    key: 'actions',
    width: 260,
    render: (row: PostItem) =>
      h(NSpace, { justify: 'center' }, () => [
        h(NButton, { size: 'small', type: 'success', onClick: () => openStatus(row, 1) }, { default: () => '通过' }),
        h(NButton, { size: 'small', onClick: () => openStatus(row, 3) }, { default: () => '隐藏' }),
        h(NButton, { size: 'small', type: 'warning', onClick: () => openStatus(row, 4) }, { default: () => '驳回' }),
        h(NButton, { size: 'small', type: 'error', onClick: () => openStatus(row, 5) }, { default: () => '拉黑' })
      ])
  }
];

async function loadData() {
  loading.value = true;
  const params = {
    page: pagination.page,
    size: pagination.pageSize,
    status: query.status ?? undefined,
    userId: query.userId ?? undefined,
    categoryId: query.categoryId ?? undefined,
    keyword: query.keyword || undefined
  };
  const { data: res, error } = await fetchAdminPosts(params);
  if (!error && res) {
    data.value = res.records || [];
    pagination.itemCount = res.total || 0;
  }
  loading.value = false;
}

function resetQuery() {
  query.status = null;
  query.userId = null;
  query.categoryId = null;
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
        <NFormItem label="状态">
          <NSelect v-model:value="query.status" :options="statusOptions" clearable />
        </NFormItem>
        <NFormItem label="用户ID">
          <NInputNumber v-model:value="query.userId" placeholder="用户ID" clearable />
        </NFormItem>
        <NFormItem label="分类ID">
          <NInputNumber v-model:value="query.categoryId" placeholder="分类ID" clearable />
        </NFormItem>
        <NFormItem label="关键词">
          <NInput v-model:value="query.keyword" placeholder="标题/内容" clearable />
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
      <div class="text-16px font-600 mb-12px">帖子审核</div>
      <NDataTable remote :loading="loading" :columns="columns" :data="data" :pagination="pagination" />
    </NCard>

    <NModal v-model:show="statusModalVisible" preset="card" title="审核操作" :block-scroll="false" style="width: 420px">
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
