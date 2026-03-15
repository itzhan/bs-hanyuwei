<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  NButton, NCard, NSpace, NEmpty, NModal, NForm, NFormItem,
  NInput, NSelect, NDatePicker, NAvatar, NPopconfirm,
} from 'naive-ui'
import { getBabies, createBaby, updateBaby, deleteBaby } from '@/api/baby'
import type { Baby, BabyDTO } from '@/types'
import { Gender } from '@/types'

const router = useRouter()
const loading = ref(false)
const babies = ref<Baby[]>([])
const showModal = ref(false)
const editingId = ref<number | null>(null)
const formData = ref<BabyDTO>({ name: '', gender: null, birthday: null, relation: null, note: null })

const genderOptions = [
  { label: '未知', value: Gender.UNKNOWN },
  { label: '男宝', value: Gender.MALE },
  { label: '女宝', value: Gender.FEMALE },
]

const genderLabel: Record<number, string> = { 0: '未知', 1: '男宝', 2: '女宝' }

onMounted(() => loadBabies())

async function loadBabies() {
  loading.value = true
  try {
    const res = await getBabies({ page: 1, size: 100 })
    babies.value = res.data.records || []
  } catch {
    // 静默
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editingId.value = null
  formData.value = { name: '', gender: null, birthday: null, relation: null, note: null }
  showModal.value = true
}

function openEdit(baby: Baby) {
  editingId.value = baby.id
  formData.value = {
    name: baby.name,
    gender: baby.gender,
    birthday: baby.birthday,
    relation: baby.relation,
    note: baby.note,
  }
  showModal.value = true
}

async function handleSave() {
  if (!formData.value.name) {
    window.$message?.warning('请输入宝宝名字')
    return
  }
  try {
    if (editingId.value) {
      await updateBaby(editingId.value, formData.value)
      window.$message?.success('更新成功')
    } else {
      await createBaby(formData.value)
      window.$message?.success('添加成功')
    }
    showModal.value = false
    loadBabies()
  } catch {
    // 错误已处理
  }
}

async function handleDelete(id: number) {
  try {
    await deleteBaby(id)
    window.$message?.success('已删除')
    loadBabies()
  } catch {
    // 错误已处理
  }
}

function calcAge(birthday: string | null): string {
  if (!birthday) return ''
  const born = new Date(birthday)
  const now = new Date()
  const months = (now.getFullYear() - born.getFullYear()) * 12 + now.getMonth() - born.getMonth()
  if (months < 12) return `${months}个月`
  const years = Math.floor(months / 12)
  const remainMonths = months % 12
  return remainMonths > 0 ? `${years}岁${remainMonths}个月` : `${years}岁`
}
</script>

<template>
  <div class="container section">
    <div class="page-header">
      <div>
        <h1 class="page-title">宝宝档案</h1>
        <p class="page-subtitle">管理您的宝宝信息</p>
      </div>
      <NButton type="primary" @click="openCreate">+ 添加宝宝</NButton>
    </div>

    <NEmpty v-if="!loading && babies.length === 0" description="还没有添加宝宝哦～" style="margin-top: 80px">
      <template #extra>
        <NButton type="primary" @click="openCreate">立即添加</NButton>
      </template>
    </NEmpty>

    <div class="baby-grid" v-else>
      <NCard
        v-for="baby in babies"
        :key="baby.id"
        class="baby-card"
        :bordered="false"
        hoverable
      >
        <div class="baby-card-inner">
          <NAvatar
            :size="56"
            round
            :src="baby.avatarPath ? `/uploads/${baby.avatarPath}` : undefined"
            style="background: var(--color-primary-lighter); font-size: 28px"
          >
            {{ genderLabel[baby.gender ?? 0] }}
          </NAvatar>
          <div class="baby-info">
            <h3 class="baby-name">{{ baby.name }}</h3>
            <p class="baby-meta" v-if="baby.birthday">
              {{ calcAge(baby.birthday) }}
              <span v-if="baby.relation"> · {{ baby.relation }}</span>
            </p>
            <p class="baby-note" v-if="baby.note">{{ baby.note }}</p>
          </div>
        </div>
        <div class="baby-actions">
          <NButton text size="small" type="primary" @click="router.push(`/babies/${baby.id}`)">
            查看详情
          </NButton>
          <NButton text size="small" @click="openEdit(baby)">编辑</NButton>
          <NPopconfirm @positive-click="handleDelete(baby.id)">
            <template #trigger>
              <NButton text size="small" type="error">删除</NButton>
            </template>
            确定要删除宝宝 "{{ baby.name }}" 吗？
          </NPopconfirm>
        </div>
      </NCard>
    </div>

    <!-- 新增/编辑弹窗 -->
    <NModal
      v-model:show="showModal"
      :title="editingId ? '编辑宝宝信息' : '添加宝宝'"
      preset="card"
      style="width: 480px; border-radius: 16px"
    >
      <NForm>
        <NFormItem label="宝宝名字" required>
          <NInput v-model:value="formData.name" placeholder="请输入宝宝名字" />
        </NFormItem>
        <NFormItem label="性别">
          <NSelect v-model:value="formData.gender" :options="genderOptions" placeholder="请选择" clearable />
        </NFormItem>
        <NFormItem label="出生日期">
          <NDatePicker
            v-model:formatted-value="formData.birthday"
            type="date"
            value-format="yyyy-MM-dd"
            clearable
            style="width: 100%"
          />
        </NFormItem>
        <NFormItem label="与宝宝的关系">
          <NInput v-model:value="formData.relation" placeholder="如：妈妈、爸爸、奶奶" />
        </NFormItem>
        <NFormItem label="备注">
          <NInput v-model:value="formData.note" type="textarea" placeholder="可选备注" :rows="2" />
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
  margin-bottom: var(--space-2xl);
}

.baby-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: var(--space-lg);
}

.baby-card {
  border-radius: var(--radius-lg) !important;
}

.baby-card-inner {
  display: flex;
  gap: var(--space-lg);
  align-items: flex-start;
  margin-bottom: var(--space-md);
}

.baby-info {
  flex: 1;
}

.baby-name {
  font-size: var(--font-size-lg);
  font-weight: 600;
  margin-bottom: var(--space-xs);
}

.baby-meta {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

.baby-note {
  font-size: var(--font-size-xs);
  color: var(--color-text-tertiary);
  margin-top: var(--space-xs);
}

.baby-actions {
  display: flex;
  gap: var(--space-md);
  border-top: 1px solid var(--color-border-light);
  padding-top: var(--space-md);
}
</style>
