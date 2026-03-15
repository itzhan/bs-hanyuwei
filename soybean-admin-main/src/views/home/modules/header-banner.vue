<script setup lang="ts">
import { computed, ref, onMounted } from 'vue';
import { useAppStore } from '@/store/modules/app';
import { useAuthStore } from '@/store/modules/auth';
import { fetchDashboardStats } from '@/service/api';
import { $t } from '@/locales';

defineOptions({ name: 'HeaderBanner' });

const appStore = useAppStore();
const authStore = useAuthStore();
const gap = computed(() => (appStore.isMobile ? 0 : 16));

const stats = ref({ userCount: 0, pendingPosts: 0, pendingReports: 0 });

onMounted(async () => {
  const { data, error } = await fetchDashboardStats();
  if (!error && data) {
    stats.value.userCount = data.userCount || 0;
    stats.value.pendingPosts = data.postCount || 0;
    stats.value.pendingReports = data.commentCount || 0;
  }
});

interface StatisticData {
  id: number;
  label: string;
  value: string;
}

const statisticData = computed<StatisticData[]>(() => [
  { id: 0, label: '注册家长', value: String(stats.value.userCount) },
  { id: 1, label: '社区帖子', value: String(stats.value.pendingPosts) },
  { id: 2, label: '互动评论', value: String(stats.value.pendingReports) }
]);
</script>

<template>
  <NCard :bordered="false" class="card-wrapper">
    <NGrid :x-gap="gap" :y-gap="16" responsive="screen" item-responsive>
      <NGi span="24 s:24 m:18">
        <div class="flex-y-center">
          <div class="size-72px shrink-0 flex-center rd-1/2 bg-primary/10">
            <SvgIcon icon="lucide:baby" class="text-36px text-primary" />
          </div>
          <div class="pl-12px">
            <h3 class="text-18px font-semibold">
              {{ $t('page.home.greeting', { userName: authStore.userInfo.userName }) }}
            </h3>
            <p class="text-#999 leading-30px">{{ $t('page.home.weatherDesc') }}</p>
          </div>
        </div>
      </NGi>
      <NGi span="24 s:24 m:6">
        <NSpace :size="24" justify="end">
          <NStatistic v-for="item in statisticData" :key="item.id" class="whitespace-nowrap" v-bind="item" />
        </NSpace>
      </NGi>
    </NGrid>
  </NCard>
</template>

<style scoped></style>
