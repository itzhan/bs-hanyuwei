<script setup lang="ts">
import { h, onMounted, reactive, ref } from 'vue';
import { NButton, NPopconfirm, NSpace, NTag } from 'naive-ui';
import { createTopic, deleteTopic, fetchAdminTopics, updateTopic } from '@/service/api';
import type { TopicItem } from '@/service/api/parenting';

const loading = ref(false);
const data = ref<TopicItem[]>([]);

const statusOptions = [
  { label: '启用', value: 1 },
  { label: '禁用', value: 2 }
];

const modalVisible = ref(false);
const modalTitle = ref('');
const isEdit = ref(false);
const formModel = reactive({
  id: null as number | null,
  name: '',
  description: '',
  status: 1
});

function resetForm() {
  formModel.id = null;
  formModel.name = '';
  formModel.description = '';
  formModel.status = 1;
}

function openCreate() {
  resetForm();
  isEdit.value = false;
  modalTitle.value = '新增话题';
  modalVisible.value = true;
}

function openEdit(row: TopicItem) {
  resetForm();
  isEdit.value = true;
  modalTitle.value = '编辑话题';
  formModel.id = row.id;
  formModel.name = row.name;
  formModel.description = row.description || '';
  formModel.status = row.status ?? 1;
  modalVisible.value = true;
}

async function submitForm() {
  const payload = {
    name: formModel.name,
    description: formModel.description,
    status: formModel.status
  };
  if (isEdit.value && formModel.id != null) {
    const { error } = await updateTopic(formModel.id, payload);
    if (!error) {
      window.$message?.success('更新成功');
      modalVisible.value = false;
      loadData();
    }
    return;
  }
  const { error } = await createTopic(payload);
  if (!error) {
    window.$message?.success('创建成功');
    modalVisible.value = false;
    loadData();
  }
}

async function remove(row: TopicItem) {
  const { error } = await deleteTopic(row.id);
  if (!error) {
    window.$message?.success('删除成功');
    loadData();
  }
}

function statusLabel(value: number) {
  return statusOptions.find(item => item.value === value)?.label || '-';
}

const columns = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '名称', key: 'name', minWidth: 160 },
  { title: '描述', key: 'description', minWidth: 200 },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render: (row: TopicItem) => h(NTag, { type: row.status === 1 ? 'success' : 'warning' }, () => statusLabel(row.status))
  },
  {
    title: '操作',
    key: 'actions',
    width: 160,
    render: (row: TopicItem) =>
      h(NSpace, { justify: 'center' }, () => [
        h(NButton, { size: 'small', onClick: () => openEdit(row) }, { default: () => '编辑' }),
        h(
          NPopconfirm,
          { onPositiveClick: () => remove(row) },
          {
            default: () => '确定删除该话题？',
            trigger: () => h(NButton, { size: 'small', type: 'error' }, { default: () => '删除' })
          }
        )
      ])
  }
];

async function loadData() {
  loading.value = true;
  const { data: res, error } = await fetchAdminTopics();
  if (!error && res) {
    data.value = res || [];
  }
  loading.value = false;
}

onMounted(() => {
  loadData();
});
</script>

<template>
  <NSpace vertical :size="16">
    <NCard :bordered="false" class="card-wrapper">
      <NSpace justify="space-between" align="center" class="mb-12px">
        <div class="text-16px font-600">话题管理</div>
        <NButton type="primary" @click="openCreate">新增话题</NButton>
      </NSpace>
      <NDataTable :loading="loading" :columns="columns" :data="data" />
    </NCard>

    <NModal v-model:show="modalVisible" preset="card" :title="modalTitle" :block-scroll="false" style="width: 480px">
      <NForm label-placement="left" label-width="90">
        <NFormItem label="名称">
          <NInput v-model:value="formModel.name" placeholder="话题名称" />
        </NFormItem>
        <NFormItem label="描述">
          <NInput v-model:value="formModel.description" placeholder="描述" />
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
