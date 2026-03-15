<script setup lang="ts">
import { ref, watch, onMounted } from 'vue';
import { useAppStore } from '@/store/modules/app';
import { useEcharts } from '@/hooks/common/echarts';
import { fetchDashboardStats } from '@/service/api';
import { $t } from '@/locales';

defineOptions({ name: 'LineChart' });

const appStore = useAppStore();

const { domRef, updateOptions } = useEcharts(() => ({
  tooltip: {
    trigger: 'axis',
    axisPointer: { type: 'cross', label: { backgroundColor: '#6a7985' } }
  },
  legend: {
    data: [$t('page.home.downloadCount'), $t('page.home.registerCount')],
    top: '0'
  },
  grid: { left: '3%', right: '4%', bottom: '3%', top: '15%' },
  xAxis: { type: 'category', boundaryGap: false, data: [] as string[] },
  yAxis: { type: 'value' },
  series: [
    {
      color: '#8e9dff',
      name: $t('page.home.downloadCount'),
      type: 'line',
      smooth: true,
      stack: 'Total',
      areaStyle: {
        color: {
          type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [{ offset: 0.25, color: '#8e9dff' }, { offset: 1, color: '#fff' }]
        }
      },
      emphasis: { focus: 'series' },
      data: [] as number[]
    },
    {
      color: '#26deca',
      name: $t('page.home.registerCount'),
      type: 'line',
      smooth: true,
      stack: 'Total',
      areaStyle: {
        color: {
          type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [{ offset: 0.25, color: '#26deca' }, { offset: 1, color: '#fff' }]
        }
      },
      emphasis: { focus: 'series' },
      data: []
    }
  ]
}));

onMounted(async () => {
  const { data, error } = await fetchDashboardStats();
  if (!error && data) {
    // Fill the last 30 days
    const days: string[] = [];
    const postMap = new Map<string, number>();
    const commentMap = new Map<string, number>();

    (data.postDaily || []).forEach((d: any) => postMap.set(d.day, d.total));
    (data.commentDaily || []).forEach((d: any) => commentMap.set(d.day, d.total));

    for (let i = 29; i >= 0; i--) {
      const date = new Date();
      date.setDate(date.getDate() - i);
      const key = date.toISOString().slice(0, 10);
      days.push(key.slice(5)); // MM-DD
    }

    const postData = days.map(d => {
      const key = `2026-${d}`;
      return postMap.get(key) || 0;
    });
    const commentData = days.map(d => {
      const key = `2026-${d}`;
      return commentMap.get(key) || 0;
    });

    updateOptions(opts => {
      opts.xAxis.data = days;
      opts.series[0].data = postData;
      opts.series[1].data = commentData;
      return opts;
    });
  }
});

function updateLocale() {
  updateOptions((opts, factory) => {
    const originOpts = factory();
    opts.legend.data = originOpts.legend.data;
    opts.series[0].name = originOpts.series[0].name;
    opts.series[1].name = originOpts.series[1].name;
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
