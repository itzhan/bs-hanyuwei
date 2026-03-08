<script setup lang="ts">
import { h, onMounted, reactive, ref } from 'vue';
import { NButton, NPopconfirm, NSpace, NTag } from 'naive-ui';
import { createBaby, deleteBaby, fetchBabies, updateBaby } from '@/service/api';
import type { BabyItem } from '@/service/api/parenting';

const loading = ref(false);
const data = ref<BabyItem[]>([]);

const query = reactive({
  userId: null as number | null,
  name: ''
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

const genderOptions = [
  { label: '未知', value: 0 },
  { label: '男', value: 1 },
  { label: '女', value: 2 }
];

const modalVisible = ref(false);
const modalTitle = ref('');
const isEdit = ref(false);
const formModel = reactive({
  id: null as number | null,
  name: '',
  gender: 0,
  birthday: '' as string | null,
  relation: '',
  note: ''
});

function resetForm() {
  formModel.id = null;
  formModel.name = '';
  formModel.gender = 0;
  formModel.birthday = '' as string | null;
  formModel.relation = '';
  formModel.note = '';
}

function openCreate() {
  resetForm();
  isEdit.value = false;
  modalTitle.value = '新增宝宝档案';
  modalVisible.value = true;
}

function openEdit(row: BabyItem) {
  resetForm();
  isEdit.value = true;
  modalTitle.value = '编辑宝宝档案';
  formModel.id = row.id;
  formModel.name = row.name;
  formModel.gender = row.gender ?? 0;
  formModel.birthday = row.birthday || '';
  formModel.relation = row.relation || '';
  formModel.note = row.note || '';
  modalVisible.value = true;
}

async function submitForm() {
  const payload = {
    name: formModel.name,
    gender: formModel.gender,
    birthday: formModel.birthday || null,
    relation: formModel.relation,
    note: formModel.note
  };
  if (isEdit.value && formModel.id != null) {
    const { error } = await updateBaby(formModel.id, payload);
    if (!error) {
      window.$message?.success('更新成功');
      modalVisible.value = false;
      loadData();
    }
    return;
  }
  const { error } = await createBaby(payload);
  if (!error) {
    window.$message?.success('创建成功');
    modalVisible.value = false;
    loadData();
  }
}

async function remove(row: BabyItem) {
  const { error } = await deleteBaby(row.id);
  if (!error) {
    window.$message?.success('删除成功');
    loadData();
  }
}

function genderLabel(value: number | null) {
  const opt = genderOptions.find(item => item.value === (value ?? 0));
  return opt?.label || '-';
}

const columns = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '用户ID', key: 'userId', width: 100 },
  { title: '姓名', key: 'name', minWidth: 120 },
  {
    title: '性别',
    key: 'gender',
    width: 100,
    render: (row: BabyItem) => h(NTag, { type: 'info' }, () => genderLabel(row.gender))
  },
  { title: '生日', key: 'birthday', width: 140 },
  { title: '关系', key: 'relation', width: 120 },
  { title: '创建时间', key: 'createdAt', minWidth: 160 },
  {
    title: '操作',
    key: 'actions',
    width: 160,
    render: (row: BabyItem) =>
      h(NSpace, { justify: 'center' }, () => [
        h(NButton, { size: 'small', onClick: () => openEdit(row) }, { default: () => '编辑' }),
        h(
          NPopconfirm,
          { onPositiveClick: () => remove(row) },
          {
            default: () => '确定删除该档案？',
            trigger: () => h(NButton, { size: 'small', type: 'error' }, { default: () => '删除' })
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
    userId: query.userId ?? undefined
  };
  const { data: res, error } = await fetchBabies(params);
  if (!error && res) {
    data.value = res.records || [];
    pagination.itemCount = res.total || 0;
  }
  loading.value = false;
}

function resetQuery() {
  query.userId = null;
  query.name = '';
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
        <div class="text-16px font-600">宝宝档案</div>
        <NButton type="primary" @click="openCreate">新增档案</NButton>
      </NSpace>
      <NDataTable remote :loading="loading" :columns="columns" :data="data" :pagination="pagination" />
    </NCard>

    <NModal v-model:show="modalVisible" preset="card" :title="modalTitle" style="width: 520px">
      <NForm label-placement="left" label-width="90">
        <NFormItem label="姓名">
          <NInput v-model:value="formModel.name" placeholder="宝宝姓名" />
        </NFormItem>
        <NFormItem label="性别">
          <NSelect v-model:value="formModel.gender" :options="genderOptions" />
        </NFormItem>
        <NFormItem label="生日">
          <NDatePicker v-model:formatted-value="formModel.birthday" type="date" value-format="yyyy-MM-dd" />
        </NFormItem>
        <NFormItem label="关系">
          <NInput v-model:value="formModel.relation" placeholder="关系" />
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
