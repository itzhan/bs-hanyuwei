<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { NButton, NCard, NStatistic, NGrid, NGi, NSpace, NTag, NEllipsis } from 'naive-ui'
import { Baby, Milk, BarChart3, Heart, PartyPopper, Star, Users, FileText, MessageCircle } from 'lucide-vue-next'
import http from '@/api/request'
import { getPosts } from '@/api/community'
import { useUserStore } from '@/store/user'
import type { Post } from '@/types'
import { PostStatus } from '@/types'

const router = useRouter()
const userStore = useUserStore()

const overview = ref({ userCount: 0, postCount: 0, commentCount: 0, babyCount: 0 })
const latestPosts = ref<Post[]>([])

onMounted(async () => {
  try {
    const [dashRes, postRes] = await Promise.all([
      http.get<any, any>('/api/stats/dashboard'),
      getPosts({ page: 1, size: 6 }),
    ])
    overview.value = dashRes.data
    latestPosts.value = (postRes.data?.records || []).filter(
      (p: Post) => p.status === PostStatus.PUBLISHED,
    )
  } catch {
    // 静默
  }
})

const features = [
  { icon: Baby, title: '宝宝档案', desc: '记录每个宝宝的基本信息' },
  { icon: BarChart3, title: '成长指标', desc: '身高体重可视化追踪' },
  { icon: FileText, title: '成长日志', desc: '记录喂养、睡眠、里程碑' },
  { icon: MessageCircle, title: '育儿社区', desc: '与其他家长交流分享' },
]

function formatDate(dateStr: string) {
  const d = new Date(dateStr)
  return `${d.getMonth() + 1}月${d.getDate()}日`
}
</script>

<template>
  <div class="home-page">
    <!-- Hero Section -->
    <section class="hero">
      <div class="container hero-inner">
        <div class="hero-content fade-in-up">
          <h1 class="hero-title">
            记录宝宝<span class="hero-highlight">每一步</span>成长
          </h1>
          <p class="hero-desc">
            用心记录每一个珍贵瞬间，科学追踪宝宝成长指标，与千万家长分享育儿经验
          </p>
          <NSpace :size="12">
            <NButton
              type="primary"
              size="large"
              @click="router.push(userStore.isLoggedIn ? '/babies' : '/register')"
            >
              {{ userStore.isLoggedIn ? '开始记录' : '免费注册' }}
            </NButton>
            <NButton size="large" @click="router.push('/community')"> 浏览社区 </NButton>
          </NSpace>
        </div>
        <div class="hero-visual fade-in-up" style="animation-delay: 0.2s">
          <div class="hero-emoji-grid">
            <span class="hero-emoji e1"><Baby :size="40" color="var(--color-primary)" /></span>
            <span class="hero-emoji e2"><Milk :size="40" color="var(--color-accent)" /></span>
            <span class="hero-emoji e3"><BarChart3 :size="40" color="var(--color-primary)" /></span>
            <span class="hero-emoji e4"><Heart :size="40" color="#E57373" /></span>
            <span class="hero-emoji e5"><PartyPopper :size="40" color="var(--color-accent)" /></span>
            <span class="hero-emoji e6"><Star :size="40" color="#FFD54F" /></span>
          </div>
        </div>
      </div>
    </section>

    <!-- 统计 -->
    <section class="section stats-section">
      <div class="container">
        <NGrid :cols="4" :x-gap="20" :y-gap="20" responsive="screen" :item-responsive="true">
          <NGi :span="4" class="stat-item-responsive">
            <NCard class="stat-card" :bordered="false">
              <NStatistic label="注册家长" :value="overview.userCount">
                <template #prefix><Users :size="20" class="stat-icon" /></template>
              </NStatistic>
            </NCard>
          </NGi>
          <NGi :span="4" class="stat-item-responsive">
            <NCard class="stat-card" :bordered="false">
              <NStatistic label="社区帖子" :value="overview.postCount">
                <template #prefix><FileText :size="20" class="stat-icon" /></template>
              </NStatistic>
            </NCard>
          </NGi>
          <NGi :span="4" class="stat-item-responsive">
            <NCard class="stat-card" :bordered="false">
              <NStatistic label="互动评论" :value="overview.commentCount">
                <template #prefix><MessageCircle :size="20" class="stat-icon" /></template>
              </NStatistic>
            </NCard>
          </NGi>
          <NGi :span="4" class="stat-item-responsive">
            <NCard class="stat-card" :bordered="false">
              <NStatistic label="宝宝档案" :value="overview.babyCount">
                <template #prefix><Baby :size="20" class="stat-icon" /></template>
              </NStatistic>
            </NCard>
          </NGi>
        </NGrid>
      </div>
    </section>

    <!-- 功能介绍 -->
    <section class="section features-section">
      <div class="container">
        <h2 class="section-title">平台核心功能</h2>
        <p class="section-desc">全方位陪伴宝宝成长的每一个阶段</p>
        <div class="features-grid">
          <div v-for="f in features" :key="f.title" class="feature-card card">
            <component :is="f.icon" :size="40" class="feature-icon" />
            <h3 class="feature-title">{{ f.title }}</h3>
            <p class="feature-desc">{{ f.desc }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- 最新社区帖子 -->
    <section class="section posts-section" v-if="latestPosts.length > 0">
      <div class="container">
        <div class="section-header">
          <div>
            <h2 class="section-title">最新分享</h2>
            <p class="section-desc">来自社区家长们的育儿经验</p>
          </div>
          <NButton text type="primary" @click="router.push('/community')"> 查看更多 → </NButton>
        </div>
        <div class="posts-grid">
          <NCard
            v-for="post in latestPosts"
            :key="post.id"
            class="post-preview"
            :bordered="false"
            hoverable
            @click="router.push(`/community/${post.id}`)"
          >
            <h4 class="post-title">{{ post.title }}</h4>
            <NEllipsis :line-clamp="2" class="post-excerpt">
              {{ post.content }}
            </NEllipsis>
            <div class="post-meta">
              <NTag size="small" :bordered="false" type="info">
                <template #icon><MessageCircle :size="12" /></template>
                {{ post.commentCount }}
              </NTag>
              <span class="post-date">{{ formatDate(post.createdAt) }}</span>
            </div>
          </NCard>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
/* ---- Hero ---- */
.hero {
  background: linear-gradient(135deg, #F0ECF9 0%, #FAF8F5 40%, #FFF5EB 100%);
  padding: var(--space-4xl) 0;
  overflow: hidden;
}

.hero-inner {
  display: flex;
  align-items: center;
  gap: var(--space-3xl);
}

.hero-content {
  flex: 1;
}

.hero-title {
  font-size: 42px;
  font-weight: 800;
  line-height: 1.3;
  color: var(--color-text-primary);
  margin-bottom: var(--space-lg);
}

.hero-highlight {
  color: var(--color-primary);
  position: relative;
}

.hero-highlight::after {
  content: '';
  position: absolute;
  bottom: 2px;
  left: 0;
  right: 0;
  height: 8px;
  background: var(--color-primary-lighter);
  border-radius: 4px;
  z-index: -1;
}

.hero-desc {
  font-size: var(--font-size-md);
  color: var(--color-text-secondary);
  margin-bottom: var(--space-2xl);
  max-width: 480px;
  line-height: 1.7;
}

.hero-visual {
  flex-shrink: 0;
  width: 320px;
  height: 280px;
  position: relative;
}

.hero-emoji-grid {
  position: relative;
  width: 100%;
  height: 100%;
}

.hero-emoji {
  position: absolute;
  font-size: 48px;
  animation: float 4s ease-in-out infinite;
}

.e1 { top: 10%; left: 20%; animation-delay: 0s; }
.e2 { top: 5%; right: 20%; animation-delay: 0.5s; }
.e3 { top: 45%; left: 5%; animation-delay: 1s; }
.e4 { top: 40%; right: 10%; animation-delay: 1.5s; }
.e5 { bottom: 10%; left: 25%; animation-delay: 2s; }
.e6 { bottom: 5%; right: 25%; animation-delay: 2.5s; }

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-12px); }
}

/* ---- Stats ---- */
.stats-section {
  margin-top: calc(-1 * var(--space-2xl));
}

.stat-card {
  text-align: center;
  border-radius: var(--radius-lg) !important;
  box-shadow: var(--shadow-card);
}

.stat-icon {
  font-size: 20px;
  margin-right: 4px;
}

.stat-item-responsive {
  grid-column: span 1 !important;
}

/* ---- Features ---- */
.section-title {
  font-size: var(--font-size-2xl);
  font-weight: 700;
  text-align: center;
  margin-bottom: var(--space-sm);
}

.section-desc {
  text-align: center;
  color: var(--color-text-secondary);
  margin-bottom: var(--space-2xl);
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-xl);
}

.feature-card {
  text-align: center;
  padding: var(--space-2xl) var(--space-xl);
  cursor: default;
}

.feature-icon {
  font-size: 40px;
  display: block;
  margin-bottom: var(--space-md);
}

.feature-title {
  font-size: var(--font-size-md);
  font-weight: 600;
  margin-bottom: var(--space-sm);
}

.feature-desc {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

/* ---- Posts ---- */
.section-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: var(--space-2xl);
}

.section-header .section-title,
.section-header .section-desc {
  text-align: left;
  margin-bottom: var(--space-xs);
}

.posts-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-lg);
}

.post-preview {
  cursor: pointer;
  border-radius: var(--radius-lg) !important;
  transition: transform var(--transition-normal), box-shadow var(--transition-normal);
}

.post-preview:hover {
  transform: translateY(-4px);
}

.post-title {
  font-size: var(--font-size-md);
  font-weight: 600;
  margin-bottom: var(--space-sm);
  color: var(--color-text-primary);
}

.post-excerpt {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  margin-bottom: var(--space-md);
}

.post-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.post-date {
  font-size: var(--font-size-xs);
  color: var(--color-text-tertiary);
}

/* ---- Responsive ---- */
@media (max-width: 768px) {
  .hero-inner {
    flex-direction: column;
    text-align: center;
  }

  .hero-title {
    font-size: 28px;
  }

  .hero-desc {
    max-width: 100%;
  }

  .hero-visual {
    width: 240px;
    height: 200px;
  }

  .features-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .posts-grid {
    grid-template-columns: 1fr;
  }
}
</style>
