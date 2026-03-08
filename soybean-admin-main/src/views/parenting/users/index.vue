<script setup lang="ts">
import { h, onMounted, reactive, ref } from 'vue';
import { NButton, NPopconfirm, NSpace, NTag } from 'naive-ui';
import { createUser, fetchUsers, updateUser, updateUserStatus } from '@/service/api';
import type { PageResult, UserItem } from '@/service/api/parenting';

const loading = ref(false);
const data = ref<UserItem[]>([]);

const query = reactive({
  keyword: '',
  role: null as number | null,
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

const roleOptions = [
  { label: '家长', value: 1 },
  { label: '管理员', value: 2 }
];

const statusOptions = [
  { label: '正常', value: 1 },
  { label: '禁用', value: 2 }
];

const modalVisible = ref(false);
const modalTitle = ref('');
const isEdit = ref(false);
const formModel = reactive({
  id: null as number | null,
  username: '',
  password: '',
  role: 1,
  status: 1,
  displayName: ''
});

function resetForm() {
  formModel.id = null;
  formModel.username = '';
  formModel.password = '';
  formModel.role = 1;
  formModel.status = 1;
  formModel.displayName = '';
}

function openCreate() {
  resetForm();
  isEdit.value = false;
  modalTitle.value = '新增用户';
  modalVisible.value = true;
}

function openEdit(row: UserItem) {
  resetForm();
  isEdit.value = true;
  modalTitle.value = '编辑用户';
  formModel.id = row.id;
  formModel.username = row.username;
  formModel.role = row.role;
  formModel.status = row.status;
  formModel.displayName = row.displayName || '';
  modalVisible.value = true;
}

async function submitForm() {
  if (isEdit.value && formModel.id != null) {
    const { error } = await updateUser(formModel.id, {
      displayName: formModel.displayName,
      role: formModel.role,
      status: formModel.status
    });
    if (!error) {
      window.$message?.success('更新成功');
      modalVisible.value = false;
      loadData();
    }
    return;
  }

  const { error } = await createUser({
    username: formModel.username,
    password: formModel.password,
    role: formModel.role,
    status: formModel.status,
    displayName: formModel.displayName
  });
  if (!error) {
    window.$message?.success('创建成功');
    modalVisible.value = false;
    loadData();
  }
}

async function toggleStatus(row: UserItem) {
  const nextStatus = row.status === 1 ? 2 : 1;
  const { error } = await updateUserStatus(row.id, nextStatus);
  if (!error) {
    window.$message?.success('状态已更新');
    loadData();
  }
}

function roleLabel(value: number) {
  return roleOptions.find(item => item.value === value)?.label || '-';
}

function statusLabel(value: number) {
  return statusOptions.find(item => item.value === value)?.label || '-';
}

const columns = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '用户名', key: 'username', minWidth: 120 },
  { title: '显示名', key: 'displayName', minWidth: 120 },
  {
    title: '角色',
    key: 'role',
    width: 120,
    render: (row: UserItem) => h(NTag, { type: row.role === 2 ? 'success' : 'info' }, () => roleLabel(row.role))
  },
  {
    title: '状态',
    key: 'status',
    width: 120,
    render: (row: UserItem) =>
      h(NTag, { type: row.status === 1 ? 'success' : 'warning' }, () => statusLabel(row.status))
  },
  { title: '创建时间', key: 'createdAt', minWidth: 160 },
  {
    title: '操作',
    key: 'actions',
    width: 200,
    render: (row: UserItem) =>
      h(NSpace, { justify: 'center' }, () => [
        h(NButton, { size: 'small', onClick: () => openEdit(row) }, { default: () => '编辑' }),
        h(
          NPopconfirm,
          { onPositiveClick: () => toggleStatus(row) },
          {
            default: () => (row.status === 1 ? '确定禁用该用户？' : '确定启用该用户？'),
            trigger: () =>
              h(
                NButton,
                { size: 'small', type: row.status === 1 ? 'warning' : 'success' },
                { default: () => (row.status === 1 ? '禁用' : '启用') }
              )
          }
        )
      ])
  }
];

async function loadData() {
  loading.value = true;
  const params = {
    page: pagination.page,
    size: pagination.pageSize,
    keyword: query.keyword || undefined,
    role: query.role ?? undefined,
    status: query.status ?? undefined
  };
  const { data: res, error } = await fetchUsers(params);
  if (!error && res) {
    data.value = res.records || [];
    pagination.itemCount = res.total || 0;
  }
  loading.value = false;
}

function resetQuery() {
  query.keyword = '';
  query.role = null;
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
        <NFormItem label="关键词">
          <NInput v-model:value="query.keyword" placeholder="用户名/显示名" clearable />
        </NFormItem>
        <NFormItem label="角色">
          <NSelect v-model:value="query.role" :options="roleOptions" clearable />
        </NFormItem>
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
      <NSpace justify="space-between" align="center" class="mb-12px">
        <div class="text-16px font-600">用户列表</div>
        <NButton type="primary" @click="openCreate">新增用户</NButton>
      </NSpace>
      <NDataTable
        remote
        :loading="loading"
        :columns="columns"
        :data="data"
        :pagination="pagination"
      />
    </NCard>

    <NModal v-model:show="modalVisible" preset="card" :title="modalTitle" style="width: 520px">
      <NForm label-placement="left" label-width="90">
        <NFormItem label="用户名" v-if="!isEdit">
          <NInput v-model:value="formModel.username" placeholder="输入用户名" />
        </NFormItem>
        <NFormItem label="密码" v-if="!isEdit">
          <NInput v-model:value="formModel.password" type="password" placeholder="输入密码" />
        </NFormItem>
        <NFormItem label="显示名">
          <NInput v-model:value="formModel.displayName" placeholder="显示名" />
        </NFormItem>
        <NFormItem label="角色">
          <NSelect v-model:value="formModel.role" :options="roleOptions" />
        </NFormItem>
        <NFormItem label="状态">
          <NSelect v-model:value="formModel.status" :options="statusOptions" />
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
