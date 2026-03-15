<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  NButton, NCard, NSpace, NEmpty, NInput, NSelect, NTag,
  NPagination, NEllipsis,
} from 'naive-ui'
import { getPosts, getCategories, getTopics, getTags } from '@/api/community'
import { useUserStore } from '@/store/user'
import { PenLine, MessageCircle } from 'lucide-vue-next'
import type { Post, PostCategory, PostTopic, PostTag } from '@/types'
import { PostStatus } from '@/types'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const posts = ref<Post[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)

const keyword = ref('')
const filterCategoryId = ref<number | null>(null)
const filterTopicId = ref<number | null>(null)
const filterTagId = ref<number | null>(null)

const categories = ref<PostCategory[]>([])
const topics = ref<PostTopic[]>([])
const tags = ref<PostTag[]>([])

onMounted(async () => {
  await Promise.all([loadFilters(), loadPosts()])
})

async function loadFilters() {
  try {
    const [catRes, topRes, tagRes] = await Promise.all([
      getCategories(),
      getTopics(),
      getTags(),
    ])
    categories.value = Array.isArray(catRes.data) ? catRes.data : []
    topics.value = Array.isArray(topRes.data) ? topRes.data : []
    tags.value = Array.isArray(tagRes.data) ? tagRes.data : []
  } catch { /* skip */ }
}

async function loadPosts() {
  loading.value = true
  try {
    const params: any = { page: page.value, size: pageSize.value }
    if (keyword.value) params.keyword = keyword.value
    if (filterCategoryId.value) params.categoryId = filterCategoryId.value
    if (filterTopicId.value) params.topicId = filterTopicId.value
    if (filterTagId.value) params.tagId = filterTagId.value
    const res = await getPosts(params)
    posts.value = (res.data.records || []).filter(
      (p: Post) => p.status === PostStatus.PUBLISHED,
    )
    total.value = res.data.total || 0
  } catch { /* skip */ } finally {
    loading.value = false
  }
}

function onSearch() {
  page.value = 1
  loadPosts()
}

function formatDate(dateStr: string) {
  const d = new Date(dateStr)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

function getCategoryName(id: number | null) {
  return categories.value.find((c) => c.id === id)?.name || ''
}
</script>

<template>
  <div class="container section">
    <div class="page-header">
      <div>
        <h1 class="page-title">育儿社区</h1>
        <p class="page-subtitle">分享育儿经验，交流成长心得</p>
      </div>
      <NButton
        v-if="userStore.isLoggedIn"
        type="primary"
        @click="router.push('/community/create')"
      >
        <PenLine :size="14" style="margin-right:4px" /> 发表帖子
      </NButton>
    </div>

    <!-- 筛选 -->
    <NCard :bordered="false" class="filter-card" style="margin-bottom: 24px">
      <NSpace wrap>
        <NInput
          v-model:value="keyword"
          placeholder="搜索帖子..."
          clearable
          style="width: 200px"
          @keyup.enter="onSearch"
          @clear="onSearch"
        />
        <NSelect
          v-model:value="filterCategoryId"
          :options="categories.map((c) => ({ label: c.name, value: c.id }))"
          placeholder="分类"
          clearable
          style="width: 140px"
          @update:value="onSearch"
        />
        <NSelect
          v-model:value="filterTopicId"
          :options="topics.map((t) => ({ label: t.name, value: t.id }))"
          placeholder="话题"
          clearable
          style="width: 140px"
          @update:value="onSearch"
        />
        <NSelect
          v-model:value="filterTagId"
          :options="tags.map((t) => ({ label: t.name, value: t.id }))"
          placeholder="标签"
          clearable
          style="width: 140px"
          @update:value="onSearch"
        />
        <NButton @click="onSearch">搜索</NButton>
      </NSpace>
    </NCard>

    <NEmpty v-if="!loading && posts.length === 0" description="暂无帖子" style="margin-top: 60px" />

    <div class="post-list" v-else>
      <NCard
        v-for="post in posts"
        :key="post.id"
        class="post-item"
        :bordered="false"
        hoverable
        @click="router.push(`/community/${post.id}`)"
      >
        <div class="post-item-top">
          <div class="post-author">
            <span class="post-date">{{ formatDate(post.createdAt) }}</span>
          </div>
          <NTag v-if="getCategoryName(post.categoryId)" :bordered="false" size="small" type="info">
            {{ getCategoryName(post.categoryId) }}
          </NTag>
        </div>
        <h3 class="post-item-title">{{ post.title }}</h3>
        <NEllipsis :line-clamp="2" class="post-item-content">
          {{ post.content }}
        </NEllipsis>
        <div class="post-item-footer">
          <NSpace :size="12">
            <span class="post-stat"><MessageCircle :size="14" style="vertical-align:middle;margin-right:2px" /> {{ post.commentCount }}</span>
          </NSpace>
        </div>
      </NCard>
    </div>

    <div class="pagination-wrapper" v-if="total > pageSize">
      <NPagination
        v-model:page="page"
        :page-size="pageSize"
        :item-count="total"
        @update:page="loadPosts"
      />
    </div>
  </div>
</template>

<style scoped>
.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: var(--space-xl);
}

.filter-card {
  border-radius: var(--radius-lg) !important;
}

.post-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-md);
}

.post-item {
  border-radius: var(--radius-lg) !important;
  cursor: pointer;
  transition: transform var(--transition-normal), box-shadow var(--transition-normal);
}

.post-item:hover {
  transform: translateY(-2px);
}

.post-item-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-md);
}

.post-author {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
}

.author-name {
  font-size: var(--font-size-sm);
  font-weight: 500;
  color: var(--color-text-primary);
}

.post-date {
  font-size: var(--font-size-xs);
  color: var(--color-text-tertiary);
}

.post-item-title {
  font-size: var(--font-size-md);
  font-weight: 600;
  margin-bottom: var(--space-sm);
  color: var(--color-text-primary);
}

.post-item-content {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  margin-bottom: var(--space-md);
}

.post-item-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.post-stat {
  font-size: var(--font-size-xs);
  color: var(--color-text-tertiary);
}

.post-tags {
  display: flex;
  gap: var(--space-xs);
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: var(--space-2xl);
}
</style>
