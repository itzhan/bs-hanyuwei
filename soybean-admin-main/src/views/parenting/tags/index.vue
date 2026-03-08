<script setup lang="ts">
import { h, onMounted, reactive, ref } from 'vue';
import { NButton, NPopconfirm, NSpace } from 'naive-ui';
import { createTag, deleteTag, fetchAdminTags, updateTag } from '@/service/api';
import type { TagItem } from '@/service/api/parenting';

const loading = ref(false);
const data = ref<TagItem[]>([]);

const modalVisible = ref(false);
const modalTitle = ref('');
const isEdit = ref(false);
const formModel = reactive({
  id: null as number | null,
  name: ''
});

function resetForm() {
  formModel.id = null;
  formModel.name = '';
}

function openCreate() {
  resetForm();
  isEdit.value = false;
  modalTitle.value = '新增标签';
  modalVisible.value = true;
}

function openEdit(row: TagItem) {
  resetForm();
  isEdit.value = true;
  modalTitle.value = '编辑标签';
  formModel.id = row.id;
  formModel.name = row.name;
  modalVisible.value = true;
}

async function submitForm() {
  const payload = { name: formModel.name };
  if (isEdit.value && formModel.id != null) {
    const { error } = await updateTag(formModel.id, payload);
    if (!error) {
      window.$message?.success('更新成功');
      modalVisible.value = false;
      loadData();
    }
    return;
  }
  const { error } = await createTag(payload);
  if (!error) {
    window.$message?.success('创建成功');
    modalVisible.value = false;
    loadData();
  }
}

async function remove(row: TagItem) {
  const { error } = await deleteTag(row.id);
  if (!error) {
    window.$message?.success('删除成功');
    loadData();
  }
}

const columns = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '名称', key: 'name', minWidth: 160 },
  {
    title: '操作',
    key: 'actions',
    width: 160,
    render: (row: TagItem) =>
      h(NSpace, { justify: 'center' }, () => [
        h(NButton, { size: 'small', onClick: () => openEdit(row) }, { default: () => '编辑' }),
        h(
          NPopconfirm,
          { onPositiveClick: () => remove(row) },
          {
            default: () => '确定删除该标签？',
            trigger: () => h(NButton, { size: 'small', type: 'error' }, { default: () => '删除' })
          }
        )
      ])
  }
];

async function loadData() {
  loading.value = true;
  const { data: res, error } = await fetchAdminTags();
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
        <div class="text-16px font-600">标签管理</div>
        <NButton type="primary" @click="openCreate">新增标签</NButton>
      </NSpace>
      <NDataTable :loading="loading" :columns="columns" :data="data" />
    </NCard>

    <NModal v-model:show="modalVisible" preset="card" :title="modalTitle" style="width: 420px">
      <NForm label-placement="left" label-width="90">
        <NFormItem label="名称">
          <NInput v-model:value="formModel.name" placeholder="标签名称" />
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
