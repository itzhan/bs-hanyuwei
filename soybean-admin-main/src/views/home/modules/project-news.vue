<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { fetchDashboardStats } from '@/service/api';
import { $t } from '@/locales';

defineOptions({ name: 'ProjectNews' });

interface NewsItem {
  id: number;
  content: string;
  time: string;
  icon: string;
}

const newses = ref<NewsItem[]>([]);
const loading = ref(true);

const statusMap: Record<number, string> = {
  1: '已发布',
  2: '待审核',
  3: '已隐藏',
  4: '已驳回',
  5: '已封禁'
};

onMounted(async () => {
  const { data, error } = await fetchDashboardStats();
  if (!error && data && data.recentPosts) {
    newses.value = data.recentPosts.map((post: any, idx: number) => ({
      id: post.id || idx,
      content: `${post.authorName || '用户'} 发布了「${post.title || '无标题'}」`,
      time: post.createdAt ? String(post.createdAt).slice(0, 16).replace('T', ' ') : '',
      icon: 'lucide:file-text'
    }));
  }
  if (newses.value.length === 0) {
    // Fallback to locale-based static list
    newses.value = [
      { id: 1, content: $t('page.home.projectNews.desc1'), time: '2026-03-14 10:00', icon: 'lucide:check-circle' },
      { id: 2, content: $t('page.home.projectNews.desc2'), time: '2026-03-13 14:20', icon: 'lucide:baby' },
      { id: 3, content: $t('page.home.projectNews.desc3'), time: '2026-03-12 09:15', icon: 'lucide:trending-up' },
      { id: 4, content: $t('page.home.projectNews.desc4'), time: '2026-03-11 16:30', icon: 'lucide:flag' },
      { id: 5, content: $t('page.home.projectNews.desc5'), time: '2026-03-10 08:00', icon: 'lucide:bar-chart-2' }
    ];
  }
  loading.value = false;
});
</script>

<template>
  <NCard :title="$t('page.home.projectNews.title')" :bordered="false" size="small" segmented class="card-wrapper">
    <template #header-extra>
      <a class="text-primary" href="javascript:;">{{ $t('page.home.projectNews.moreNews') }}</a>
    </template>
    <NSpin :show="loading">
      <NList>
        <NListItem v-for="item in newses" :key="item.id">
          <template #prefix>
            <div class="size-48px flex-center rd-1/2 bg-primary/10">
              <SvgIcon :icon="item.icon" class="text-24px text-primary" />
            </div>
          </template>
          <NThing :title="item.content" :description="item.time" />
        </NListItem>
      </NList>
    </NSpin>
  </NCard>
</template>

<style scoped></style>
