<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  NButton, NCard, NAvatar,
  NDescriptions, NDescriptionsItem, NTabs, NTabPane,
  NTag, NEllipsis, NEmpty,
} from 'naive-ui'
import { getMe } from '@/api/auth'
import { getPosts } from '@/api/community'
import type { Post, User } from '@/types'
import { MessageCircle } from 'lucide-vue-next'
const router = useRouter()

const user = ref<User | null>(null)
const myPosts = ref<Post[]>([])
const loading = ref(true)

const postStatusLabels: Record<number, string> = {
  1: '已发布',
  2: '待审核',
  3: '已隐藏',
  4: '已拒绝',
  5: '已封禁',
}

const postStatusTypes: Record<number, string> = {
  1: 'success',
  2: 'warning',
  3: 'default',
  4: 'error',
  5: 'error',
}

onMounted(async () => {
  try {
    const [userRes, postsRes] = await Promise.all([
      getMe(),
      getPosts({ mine: true, page: 1, size: 100 }),
    ])
    user.value = userRes.data
    myPosts.value = postsRes.data?.records || []
  } catch { /* skip */ } finally {
    loading.value = false
  }
})

function formatDate(dateStr: string) {
  const d = new Date(dateStr)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}
</script>

<template>
  <div class="container section">
    <h1 class="page-title">个人中心</h1>

    <NTabs type="segment" animated>
      <NTabPane name="info" tab="个人信息">
        <NCard :bordered="false" class="profile-card" v-if="user">
          <div class="profile-header">
            <NAvatar
              :size="72"
              round
              :src="user.avatarPath ? `/uploads/${user.avatarPath}` : undefined"
              style="background: var(--color-primary-lighter); color: var(--color-primary); font-size: 32px"
            >
              {{ user.displayName?.charAt(0) || user.username?.charAt(0) || '用' }}
            </NAvatar>
            <div class="profile-info">
              <h2 class="profile-name">{{ user.displayName || user.username }}</h2>
              <NTag :bordered="false" size="small" type="info">
                {{ user.role === 2 ? '管理员' : '家长' }}
              </NTag>
            </div>
          </div>

          <NDescriptions :column="2" label-placement="left" bordered style="margin-top: 24px">
            <NDescriptionsItem label="用户名">{{ user.username }}</NDescriptionsItem>
            <NDescriptionsItem label="昵称">{{ user.displayName || '未设置' }}</NDescriptionsItem>
            <NDescriptionsItem label="账号状态">
              <NTag :bordered="false" :type="user.status === 1 ? 'success' : 'error'" size="small">
                {{ user.status === 1 ? '正常' : '已禁用' }}
              </NTag>
            </NDescriptionsItem>
            <NDescriptionsItem label="注册时间">{{ formatDate(user.createdAt) }}</NDescriptionsItem>
          </NDescriptions>
        </NCard>
      </NTabPane>

      <NTabPane name="posts" tab="我的帖子">
        <NEmpty v-if="myPosts.length === 0" description="还没有发布过帖子">
          <template #extra>
            <NButton type="primary" @click="router.push('/community/create')">去发帖</NButton>
          </template>
        </NEmpty>

        <div class="my-posts" v-else>
          <NCard
            v-for="post in myPosts"
            :key="post.id"
            :bordered="false"
            hoverable
            class="my-post-item"
            @click="router.push(`/community/${post.id}`)"
          >
            <div class="my-post-header">
              <h4 class="my-post-title">{{ post.title }}</h4>
              <NTag :bordered="false" size="small" :type="(postStatusTypes[post.status] as any) || 'default'">
                {{ postStatusLabels[post.status] || '未知' }}
              </NTag>
            </div>
            <NEllipsis :line-clamp="2" class="my-post-excerpt">
              {{ post.content }}
            </NEllipsis>
            <div class="my-post-meta">
              <span>{{ formatDate(post.createdAt) }}</span>
              <span><MessageCircle :size="14" style="vertical-align:middle;margin-right:2px" /> {{ post.commentCount }}</span>
            </div>
          </NCard>
        </div>
      </NTabPane>
    </NTabs>
  </div>
</template>

<style scoped>
.profile-card {
  border-radius: var(--radius-xl) !important;
  box-shadow: var(--shadow-md);
}

.profile-header {
  display: flex;
  align-items: center;
  gap: var(--space-xl);
}

.profile-name {
  font-size: var(--font-size-2xl);
  font-weight: 700;
  margin-bottom: var(--space-xs);
}

.profile-info {
  display: flex;
  flex-direction: column;
  gap: var(--space-xs);
}

.my-posts {
  display: flex;
  flex-direction: column;
  gap: var(--space-md);
}

.my-post-item {
  border-radius: var(--radius-lg) !important;
  cursor: pointer;
  transition: transform var(--transition-normal);
}

.my-post-item:hover {
  transform: translateY(-2px);
}

.my-post-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-sm);
}

.my-post-title {
  font-size: var(--font-size-md);
  font-weight: 600;
}

.my-post-excerpt {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  margin-bottom: var(--space-md);
}

.my-post-meta {
  display: flex;
  gap: var(--space-lg);
  font-size: var(--font-size-xs);
  color: var(--color-text-tertiary);
}
</style>
