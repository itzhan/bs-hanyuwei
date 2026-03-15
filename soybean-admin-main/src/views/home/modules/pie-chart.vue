<script setup lang="ts">
import { watch, onMounted } from 'vue';
import { useAppStore } from '@/store/modules/app';
import { useEcharts } from '@/hooks/common/echarts';
import { fetchDashboardStats } from '@/service/api';
import { $t } from '@/locales';

defineOptions({ name: 'PieChart' });

const appStore = useAppStore();

const { domRef, updateOptions } = useEcharts(() => ({
  tooltip: { trigger: 'item' },
  legend: {
    bottom: '1%',
    left: 'center',
    itemStyle: { borderWidth: 0 }
  },
  series: [
    {
      color: ['#5da8ff', '#8e9dff', '#fedc69', '#26deca', '#f68057'],
      name: $t('page.home.schedule'),
      type: 'pie',
      radius: ['45%', '75%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 1 },
      label: { show: false, position: 'center' },
      emphasis: { label: { show: true, fontSize: '12' } },
      labelLine: { show: false },
      data: [] as { name: string; value: number }[]
    }
  ]
}));

onMounted(async () => {
  const { data, error } = await fetchDashboardStats();
  if (!error && data && data.categoryStats) {
    updateOptions(opts => {
      opts.series[0].data = data.categoryStats.map((item: any) => ({
        name: item.name,
        value: Number(item.value) || 0
      }));
      return opts;
    });
  }
});

function updateLocale() {
  updateOptions((opts, factory) => {
    const originOpts = factory();
    opts.series[0].name = originOpts.series[0].name;
    return opts;
  });
}

watch(() => appStore.locale, () => { updateLocale(); });
</script>

<template>
  <NCard :bordered="false" class="card-wrapper">
    <div ref="domRef" class="h-360px overflow-hidden"></div>
  </NCard>
</template>

<style scoped></style>
