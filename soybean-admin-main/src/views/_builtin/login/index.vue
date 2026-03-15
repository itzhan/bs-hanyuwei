<script setup lang="ts">
import { computed } from 'vue';
import type { Component } from 'vue';
import { getPaletteColorByNumber, mixColor } from '@sa/color';
import { loginModuleRecord } from '@/constants/app';
import { useAppStore } from '@/store/modules/app';
import { useThemeStore } from '@/store/modules/theme';
import { $t } from '@/locales';
import PwdLogin from './modules/pwd-login.vue';
import CodeLogin from './modules/code-login.vue';
import Register from './modules/register.vue';
import ResetPwd from './modules/reset-pwd.vue';
import BindWechat from './modules/bind-wechat.vue';

interface Props {
  module?: UnionKey.LoginModule;
}

const props = defineProps<Props>();

const appStore = useAppStore();
const themeStore = useThemeStore();

interface LoginModule {
  label: App.I18n.I18nKey;
  component: Component;
}

const moduleMap: Record<UnionKey.LoginModule, LoginModule> = {
  'pwd-login': { label: loginModuleRecord['pwd-login'], component: PwdLogin },
  'code-login': { label: loginModuleRecord['code-login'], component: CodeLogin },
  register: { label: loginModuleRecord.register, component: Register },
  'reset-pwd': { label: loginModuleRecord['reset-pwd'], component: ResetPwd },
  'bind-wechat': { label: loginModuleRecord['bind-wechat'], component: BindWechat }
};

const activeModule = computed(() => moduleMap[props.module || 'pwd-login']);

const bgThemeColor = computed(() =>
  themeStore.darkMode ? getPaletteColorByNumber(themeStore.themeColor, 600) : themeStore.themeColor
);

const bgColor = computed(() => {
  const COLOR_WHITE = '#ffffff';
  const ratio = themeStore.darkMode ? 0.5 : 0.2;
  return mixColor(COLOR_WHITE, themeStore.themeColor, ratio);
});
</script>

<template>
  <div class="relative size-full flex-center overflow-hidden" :style="{ backgroundColor: bgColor }">
    <WaveBg :theme-color="bgThemeColor" />
    <NCard :bordered="false" class="relative z-4 w-auto rd-12px">
      <div class="w-400px lt-sm:w-300px">
        <!-- Login header with parenting branding -->
        <header class="flex-col flex-center gap-8px pb-8px">
          <div class="flex-y-center gap-12px">
            <div class="size-56px flex-center rd-1/2 bg-primary/10">
              <SvgIcon icon="lucide:baby" class="text-32px text-primary" />
            </div>
            <div>
              <h2 class="text-24px text-primary font-700 lt-sm:text-20px m-0">{{ $t('system.title') }}</h2>
              <p class="text-12px text-#999 m-0">关注宝宝成长 · 分享育儿经验</p>
            </div>
          </div>
          <div class="flex-y-center gap-8px">
            <ThemeSchemaSwitch
              :theme-schema="themeStore.themeScheme"
              :show-tooltip="false"
              class="text-18px"
              @switch="themeStore.toggleThemeScheme"
            />
            <LangSwitch
              v-if="themeStore.header.multilingual.visible"
              :lang="appStore.locale"
              :lang-options="appStore.localeOptions"
              :show-tooltip="false"
              @change-lang="appStore.changeLocale"
            />
          </div>
        </header>
        <!-- Feature icons bar -->
        <div class="flex justify-center gap-24px py-12px border-t border-b border-primary/10 mb-16px">
          <div class="flex-col flex-center gap-4px">
            <SvgIcon icon="lucide:heart" class="text-20px text-primary" />
            <span class="text-11px text-#999">成长记录</span>
          </div>
          <div class="flex-col flex-center gap-4px">
            <SvgIcon icon="lucide:users" class="text-20px text-primary" />
            <span class="text-11px text-#999">社区交流</span>
          </div>
          <div class="flex-col flex-center gap-4px">
            <SvgIcon icon="lucide:bar-chart-2" class="text-20px text-primary" />
            <span class="text-11px text-#999">数据统计</span>
          </div>
          <div class="flex-col flex-center gap-4px">
            <SvgIcon icon="lucide:shield" class="text-20px text-primary" />
            <span class="text-11px text-#999">内容管理</span>
          </div>
        </div>
        <main>
          <h3 class="text-18px text-primary font-medium">{{ $t(activeModule.label) }}</h3>
          <div class="pt-24px">
            <Transition :name="themeStore.page.animateMode" mode="out-in" appear>
              <component :is="activeModule.component" />
            </Transition>
          </div>
        </main>
      </div>
    </NCard>
  </div>
</template>

<style scoped></style>
