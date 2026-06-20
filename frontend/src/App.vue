<template>
  <n-config-provider :theme="theme">
    <n-message-provider>
      <n-dialog-provider>
        <n-notification-provider>
          <router-view />
        </n-notification-provider>
      </n-dialog-provider>
    </n-message-provider>
  </n-config-provider>
</template>

<script setup>
import { computed } from 'vue'
import { darkTheme, lightTheme } from 'naive-ui'
import { useThemeStore } from '@/store/theme'

const themeStore = useThemeStore()
const theme = computed(() => themeStore.isDark ? darkTheme : lightTheme)
</script>
