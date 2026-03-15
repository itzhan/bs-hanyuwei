<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  NButton, NCard, NSpace, NInput,
  NPopconfirm, NPagination, NModal, NForm, NFormItem, NRadioGroup, NRadio,
} from 'naive-ui'
import { getPost, deletePost, getComments, createComment, createReport } from '@/api/community'
import { useUserStore } from '@/store/user'
import type { Post, PostComment, CommentDTO, ReportDTO } from '@/types'
import { MessageCircle } from 'lucide-vue-next'
import { ReportReasonLabels } from '@/types'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const post = ref<Post | null>(null)
const comments = ref<PostComment[]>([])
const commentTotal = ref(0)
const commentPage = ref(1)
const commentPageSize = ref(20)
const loading = ref(true)

const newComment = ref('')
const replyingTo = ref<PostComment | null>(null)

const showReport = ref(false)
const reportData = ref<ReportDTO>({
  postId: null, commentId: null, reason: 1, reasonDesc: '',
})

const reportReasonOptions = Object.entries(ReportReasonLabels).map(([k, v]) => ({
  label: v, value: Number(k),
}))

onMounted(async () => {
  const id = Number(route.params.id)
  try {
    const [postRes, commentRes] = await Promise.all([
      getPost(id),
      getComments(id, { page: 1, size: commentPageSize.value }),
    ])
    post.value = postRes.data
    comments.value = commentRes.data?.records || []
    commentTotal.value = commentRes.data?.total || 0
  } catch {
    window.$message?.error('加载失败')
  } finally {
    loading.value = false
  }
})

async function loadComments() {
  if (!post.value) return
  try {
    const res = await getComments(post.value.id, { page: commentPage.value, size: commentPageSize.value })
    comments.value = res.data?.records || []
    commentTotal.value = res.data?.total || 0
  } catch { /* skip */ }
}

async function submitComment() {
  if (!newComment.value.trim()) {
    window.$message?.warning('请输入评论内容')
    return
  }
  if (!post.value) return

  const data: CommentDTO = { content: newComment.value }
  if (replyingTo.value) {
    data.parentId = replyingTo.value.id
    data.replyToUserId = replyingTo.value.userId
  }

  try {
    await createComment(post.value.id, data)
    window.$message?.success('评论成功')
    newComment.value = ''
    replyingTo.value = null
    loadComments()
  } catch { /* handled */ }
}

function startReply(comment: PostComment) {
  replyingTo.value = comment
  newComment.value = ''
}

function cancelReply() {
  replyingTo.value = null
}

async function handleDeletePost() {
  if (!post.value) return
  try {
    await deletePost(post.value.id)
    window.$message?.success('删除成功')
    router.push('/community')
  } catch { /* handled */ }
}

function openReport(postId?: number, commentId?: number) {
  reportData.value = {
    postId: postId || null,
    commentId: commentId || null,
    reason: 1,
    reasonDesc: '',
  }
  showReport.value = true
}

async function submitReport() {
  try {
    await createReport(reportData.value)
    window.$message?.success('举报已提交')
    showReport.value = false
  } catch { /* handled */ }
}

function formatDate(dateStr: string) {
  const d = new Date(dateStr)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

const isOwnPost = computed(() => post.value?.userId === userStore.user?.id)
</script>

<template>
  <div class="container section">
    <NButton text @click="router.push('/community')" style="margin-bottom: 16px">← 返回社区</NButton>

    <template v-if="post">
      <NCard :bordered="false" class="post-card">
        <div class="post-header">
          <div class="post-author">
            <div class="post-time">{{ formatDate(post.createdAt) }}</div>
          </div>
          <NSpace :size="8">
            <NButton v-if="isOwnPost" text size="small" @click="router.push(`/community/create?edit=${post.id}`)">
              编辑
            </NButton>
            <NPopconfirm v-if="isOwnPost" @positive-click="handleDeletePost">
              <template #trigger>
                <NButton text size="small" type="error">删除</NButton>
              </template>
              确定删除这篇帖子吗？
            </NPopconfirm>
            <NButton v-if="userStore.isLoggedIn && !isOwnPost" text size="small" type="warning" @click="openReport(post.id)">
              举报
            </NButton>
          </NSpace>
        </div>

        <h1 class="post-title">{{ post.title }}</h1>
        <div class="post-content" v-html="post.content.replace(/\n/g, '<br />')"></div>
      </NCard>

      <!-- 评论区 -->
      <div class="comments-section">
        <h3 class="comments-title"><MessageCircle :size="18" style="vertical-align:middle;margin-right:4px" /> 评论 ({{ commentTotal }})</h3>

        <!-- 评论输入 -->
        <NCard v-if="userStore.isLoggedIn" :bordered="false" class="comment-input-card">
          <div v-if="replyingTo" class="reply-hint">
            回复 <strong>用户#{{ replyingTo.userId }}</strong>
            <NButton text size="tiny" @click="cancelReply">取消</NButton>
          </div>
          <NSpace vertical>
            <NInput
              v-model:value="newComment"
              type="textarea"
              :placeholder="replyingTo ? `回复 用户#${replyingTo.userId}...` : '写下你的评论...'"
              :rows="3"
            />
            <NButton type="primary" size="small" @click="submitComment" style="align-self: flex-end">
              发表评论
            </NButton>
          </NSpace>
        </NCard>

        <!-- 评论列表 -->
        <div class="comment-list">
          <div v-for="comment in comments" :key="comment.id" class="comment-item">
            <div class="comment-body">
              <div class="comment-meta">
                <span class="comment-author">用户#{{ comment.userId }}</span>
                <span class="comment-time">{{ formatDate(comment.createdAt) }}</span>
              </div>
              <p class="comment-content">{{ comment.content }}</p>
              <NSpace :size="8">
                <NButton v-if="userStore.isLoggedIn" text size="tiny" @click="startReply(comment)">
                  回复
                </NButton>
                <NButton
                  v-if="userStore.isLoggedIn && comment.userId !== userStore.user?.id"
                  text size="tiny"
                  type="warning"
                  @click="openReport(undefined, comment.id)"
                >
                  举报
                </NButton>
              </NSpace>
            </div>
          </div>
        </div>

        <div class="pagination-wrapper" v-if="commentTotal > commentPageSize">
          <NPagination
            v-model:page="commentPage"
            :page-size="commentPageSize"
            :item-count="commentTotal"
            @update:page="loadComments"
          />
        </div>
      </div>
    </template>

    <!-- 举报弹窗 -->
    <NModal v-model:show="showReport" title="举报内容" preset="card" style="width: 440px; border-radius: 16px">
      <NForm>
        <NFormItem label="举报原因">
          <NRadioGroup v-model:value="reportData.reason">
            <NSpace vertical>
              <NRadio v-for="opt in reportReasonOptions" :key="opt.value" :value="opt.value">
                {{ opt.label }}
              </NRadio>
            </NSpace>
          </NRadioGroup>
        </NFormItem>
        <NFormItem label="详细描述">
          <NInput v-model:value="reportData.reasonDesc" type="textarea" placeholder="请描述举报原因" :rows="3" />
        </NFormItem>
      </NForm>
      <template #footer>
        <NSpace justify="end">
          <NButton @click="showReport = false">取消</NButton>
          <NButton type="warning" @click="submitReport">提交举报</NButton>
        </NSpace>
      </template>
    </NModal>
  </div>
</template>

<style scoped>
.post-card {
  border-radius: var(--radius-xl) !important;
  box-shadow: var(--shadow-md);
  margin-bottom: var(--space-2xl);
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--space-xl);
}

.post-author {
  display: flex;
  align-items: center;
  gap: var(--space-md);
}

.author-name {
  font-weight: 600;
  font-size: var(--font-size-base);
}

.post-time {
  font-size: var(--font-size-xs);
  color: var(--color-text-tertiary);
}

.post-title {
  font-size: var(--font-size-2xl);
  font-weight: 700;
  margin-bottom: var(--space-xl);
  line-height: 1.4;
}

.post-content {
  font-size: var(--font-size-base);
  line-height: 1.8;
  color: var(--color-text-primary);
  margin-bottom: var(--space-xl);
}

.post-tags {
  display: flex;
  gap: var(--space-xs);
  flex-wrap: wrap;
}

/* ---- Comments ---- */
.comments-section {
  margin-top: var(--space-lg);
}

.comments-title {
  font-size: var(--font-size-lg);
  font-weight: 600;
  margin-bottom: var(--space-lg);
}

.comment-input-card {
  border-radius: var(--radius-lg) !important;
  margin-bottom: var(--space-lg);
}

.reply-hint {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  margin-bottom: var(--space-sm);
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-md);
}

.comment-item {
  display: flex;
  gap: var(--space-md);
  padding: var(--space-lg);
  background: var(--color-card);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
}

.comment-body {
  flex: 1;
}

.comment-meta {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  margin-bottom: var(--space-xs);
}

.comment-author {
  font-weight: 600;
  font-size: var(--font-size-sm);
}

.comment-reply-to {
  font-size: var(--font-size-xs);
  color: var(--color-text-tertiary);
}

.comment-time {
  font-size: var(--font-size-xs);
  color: var(--color-text-tertiary);
}

.comment-content {
  font-size: var(--font-size-sm);
  color: var(--color-text-primary);
  line-height: 1.6;
  margin-bottom: var(--space-xs);
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: var(--space-xl);
}
</style>
