<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { fetchCommunityOverview } from '@/service/api';

const loading = ref(false);
const query = reactive({
  days: 7
});

const postDaily = ref<any[]>([]);
const commentDaily = ref<any[]>([]);

const columns = [
  { title: '日期', key: 'day', width: 140 },
  { title: '数量', key: 'total', width: 120 }
];

async function loadData() {
  loading.value = true;
  const { data: res, error } = await fetchCommunityOverview(query.days);
  if (!error && res) {
    postDaily.value = res.postDaily || [];
    commentDaily.value = res.commentDaily || [];
  }
  loading.value = false;
}

onMounted(() => {
  loadData();
});
</script>

<template>
  <NSpace vertical :size="16">
    <NCard :bordered="false" class="card-wrapper">
      <NForm inline label-placement="left">
        <NFormItem label="天数">
          <NInputNumber v-model:value="query.days" :min="1" :max="60" />
        </NFormItem>
        <NFormItem>
          <NButton type="primary" @click="loadData">刷新</NButton>
        </NFormItem>
      </NForm>
    </NCard>

    <NGrid :x-gap="16" :y-gap="16" responsive="screen" item-responsive>
      <NGi span="24 s:24 m:12">
        <NCard :bordered="false" class="card-wrapper" title="发帖趋势">
          <NDataTable :loading="loading" :columns="columns" :data="postDaily" />
        </NCard>
      </NGi>
      <NGi span="24 s:24 m:12">
        <NCard :bordered="false" class="card-wrapper" title="评论趋势">
          <NDataTable :loading="loading" :columns="columns" :data="commentDaily" />
        </NCard>
      </NGi>
    </NGrid>
  </NSpace>
</template>

<style scoped></style>
