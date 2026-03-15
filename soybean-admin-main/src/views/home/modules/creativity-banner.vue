<script setup lang="ts">
import { $t } from '@/locales';

defineOptions({ name: 'CreativityBanner' });
</script>

<template>
  <NCard :title="$t('page.home.creativity')" :bordered="false" size="small" class="h-full card-wrapper">
    <div class="h-full flex-col flex-center gap-16px py-24px">
      <div class="flex-center gap-12px">
        <SvgIcon icon="lucide:heart" class="text-48px text-primary" />
        <SvgIcon icon="lucide:baby" class="text-64px text-primary" />
        <SvgIcon icon="lucide:heart" class="text-48px text-primary" />
      </div>
      <h2 class="text-24px font-bold text-primary">育儿社区管理平台</h2>
      <p class="text-14px text-#999 text-center px-24px">
        关注宝宝成长 · 分享育儿经验 · 构建温馨社区
      </p>
      <div class="flex gap-16px mt-8px">
        <NTag type="success" round>
          <template #icon><SvgIcon icon="lucide:users" class="text-14px" /></template>
          家长交流
        </NTag>
        <NTag type="info" round>
          <template #icon><SvgIcon icon="lucide:book-open" class="text-14px" /></template>
          知识分享
        </NTag>
        <NTag type="warning" round>
          <template #icon><SvgIcon icon="lucide:trending-up" class="text-14px" /></template>
          成长记录
        </NTag>
      </div>
    </div>
  </NCard>
</template>

<style scoped></style>
