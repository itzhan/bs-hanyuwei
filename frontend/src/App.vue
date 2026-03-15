<script setup lang="ts">
import { NConfigProvider, NMessageProvider, NDialogProvider, useMessage, type GlobalThemeOverrides } from 'naive-ui'
import { defineComponent } from 'vue'

const themeOverrides: GlobalThemeOverrides = {
  common: {
    primaryColor: '#7C6FBF',
    primaryColorHover: '#9B8FD4',
    primaryColorPressed: '#5A4E9A',
    primaryColorSuppl: '#9B8FD4',
    borderRadius: '10px',
    borderRadiusSmall: '6px',
    fontFamily: "'Inter', -apple-system, 'PingFang SC', 'Noto Sans SC', sans-serif",
  },
  Button: {
    borderRadiusMedium: '10px',
    borderRadiusLarge: '12px',
  },
  Card: {
    borderRadius: '16px',
  },
  Input: {
    borderRadius: '10px',
  },
}

/* 注入全局 $message 到 window 供 axios 拦截器使用 */
const MessageInjector = defineComponent({
  name: 'MessageInjector',
  setup() {
    const message = useMessage()
    window.$message = message
    return () => null
  },
})
</script>

<template>
  <NConfigProvider :theme-overrides="themeOverrides">
    <NMessageProvider>
      <NDialogProvider>
        <MessageInjector />
        <router-view />
      </NDialogProvider>
    </NMessageProvider>
  </NConfigProvider>
</template>
