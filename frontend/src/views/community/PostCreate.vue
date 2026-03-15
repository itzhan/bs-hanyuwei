<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  NButton, NCard, NForm, NFormItem, NInput, NSelect, NSpace,
} from 'naive-ui'
import { createPost, updatePost, getPost, getCategories, getTopics, getTags } from '@/api/community'
import type { PostDTO, PostCategory, PostTopic, PostTag } from '@/types'

const router = useRouter()
const route = useRoute()

const editId = computed(() => route.query.edit ? Number(route.query.edit) : null)
const loading = ref(false)
const formData = ref<PostDTO>({
  title: '', content: '', categoryId: null, topicId: null, tagIds: [],
})

const categories = ref<PostCategory[]>([])
const topics = ref<PostTopic[]>([])
const tags = ref<PostTag[]>([])

onMounted(async () => {
  await loadFilters()
  if (editId.value) {
    try {
      const res = await getPost(editId.value)
      const p = res.data
      formData.value = {
        title: p.title,
        content: p.content,
        categoryId: p.categoryId,
        topicId: p.topicId,
        tagIds: p.tagIds || [],
      }
    } catch {
      window.$message?.error('加载帖子失败')
    }
  }
})

async function loadFilters() {
  try {
    const [catRes, topRes, tagRes] = await Promise.all([
      getCategories(), getTopics(), getTags(),
    ])
    categories.value = Array.isArray(catRes.data) ? catRes.data : []
    topics.value = Array.isArray(topRes.data) ? topRes.data : []
    tags.value = Array.isArray(tagRes.data) ? tagRes.data : []
  } catch { /* skip */ }
}

async function handleSubmit() {
  if (!formData.value.title || !formData.value.content) {
    window.$message?.warning('请填写标题和内容')
    return
  }
  loading.value = true
  try {
    if (editId.value) {
      await updatePost(editId.value, formData.value)
      window.$message?.success('更新成功，帖子将重新进入审核')
    } else {
      await createPost(formData.value)
      window.$message?.success('发布成功，等待审核')
    }
    router.push('/community')
  } catch { /* handled */ } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="container section">
    <NButton text @click="router.push('/community')" style="margin-bottom: 16px">← 返回社区</NButton>

    <NCard :bordered="false" class="create-card">
      <h2 class="form-title">{{ editId ? '编辑帖子' : '✏️ 发表新帖子' }}</h2>

      <NForm>
        <NFormItem label="标题" required>
          <NInput v-model:value="formData.title" placeholder="请输入帖子标题" size="large" maxlength="100" show-count />
        </NFormItem>
        <NFormItem label="分类">
          <NSelect
            v-model:value="formData.categoryId"
            :options="categories.map((c) => ({ label: c.name, value: c.id }))"
            placeholder="选择分类"
            clearable
          />
        </NFormItem>
        <NFormItem label="话题">
          <NSelect
            v-model:value="formData.topicId"
            :options="topics.map((t) => ({ label: t.name, value: t.id }))"
            placeholder="选择话题"
            clearable
          />
        </NFormItem>
        <NFormItem label="标签">
          <NSelect
            v-model:value="formData.tagIds"
            :options="tags.map((t) => ({ label: t.name, value: t.id }))"
            placeholder="选择标签"
            multiple
            clearable
          />
        </NFormItem>
        <NFormItem label="内容" required>
          <NInput
            v-model:value="formData.content"
            type="textarea"
            placeholder="分享你的育儿经验..."
            :rows="10"
          />
        </NFormItem>
      </NForm>
      <NSpace justify="end">
        <NButton @click="router.push('/community')">取消</NButton>
        <NButton type="primary" size="large" :loading="loading" @click="handleSubmit">
          {{ editId ? '保存修改' : '发布帖子' }}
        </NButton>
      </NSpace>
    </NCard>
  </div>
</template>

<style scoped>
.create-card {
  border-radius: var(--radius-xl) !important;
  box-shadow: var(--shadow-md);
  max-width: 720px;
  margin: 0 auto;
}

.form-title {
  font-size: var(--font-size-xl);
  font-weight: 600;
  margin-bottom: var(--space-xl);
}
</style>
