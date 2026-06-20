<template>
  <n-layout has-sider>
    <n-layout-sider
      :collapsed="collapsed"
      :collapsed-width="64"
      :width="240"
      show-trigger
      @update:collapsed="updateCollapsed"
    >
      <div class="logo">
        <span v-if="!collapsed" class="logo-text">除冰液管理</span>
        <span v-else class="logo-text">除冰</span>
      </div>
      <n-menu
        :collapsed="collapsed"
        :collapsed-width="64"
        :options="menuOptions"
        :value="activeMenu"
        @update:value="handleMenuClick"
      />
    </n-layout-sider>
    <n-layout>
      <n-layout-header bordered class="header">
        <div class="header-left">
          <span class="page-title">{{ currentPageTitle }}</span>
        </div>
        <div class="header-right">
          <n-tag v-if="hasRiskAlert" type="error" class="risk-tag">
            <n-icon size="16" style="margin-right: 4px;">
              <WarningFilled />
            </n-icon>
            存在安全风险
          </n-tag>
          <n-space>
            <n-button text @click="handleRefresh">
              <template #icon>
                <n-icon>
                  <ReloadOutlined />
                </n-icon>
              </template>
              刷新
            </n-button>
            <n-avatar round style="background: #3b82f6;">管</n-avatar>
          </n-space>
        </div>
      </n-layout-header>
      <n-layout-content content-style="padding: 16px;">
        <router-view />
      </n-layout-content>
    </n-layout>
  </n-layout>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { h } from 'vue'
import { NIcon } from 'naive-ui'
import {
  DashboardOutlined,
  AimOutlined,
  CarOutlined,
  ExperimentOutlined,
  SendOutlined,
  FilterOutlined,
  RetweetOutlined,
  EnvironmentOutlined,
  ReloadOutlined,
  WarningFilled
} from '@vicons/antd'
import { useDashboardStore } from '@/store/dashboard'
import { useMessage } from 'naive-ui'

const router = useRouter()
const route = useRoute()
const message = useMessage()
const dashboardStore = useDashboardStore()

const collapsed = ref(false)
const activeMenu = ref('dashboard')

const menuOptions = [
  {
    label: '实时看板',
    key: 'dashboard',
    icon: () => h(NIcon, null, { default: () => h(DashboardOutlined) })
  },
  {
    label: '航班管理',
    key: 'flight',
    icon: () => h(NIcon, null, { default: () => h(AimOutlined) })
  },
  {
    label: '车辆管理',
    key: 'vehicle',
    icon: () => h(NIcon, null, { default: () => h(CarOutlined) })
  },
  {
    label: '除冰液批次',
    key: 'batch',
    icon: () => h(NIcon, null, { default: () => h(ExperimentOutlined) })
  },
  {
    label: '调度管理',
    key: 'dispatch',
    icon: () => h(NIcon, null, { default: () => h(SendOutlined) })
  },
  {
    label: '喷洒记录',
    key: 'spray',
    icon: () => h(NIcon, null, { default: () => h(FilterOutlined) })
  },
  {
    label: '废液回收',
    key: 'waste',
    icon: () => h(NIcon, null, { default: () => h(RetweetOutlined) })
  },
  {
    label: '环保检查',
    key: 'env',
    icon: () => h(NIcon, null, { default: () => h(EnvironmentOutlined) })
  }
]

const currentPageTitle = computed(() => {
  const item = menuOptions.find(m => m.key === activeMenu.value)
  return item ? item.label : '实时看板'
})

const hasRiskAlert = computed(() => {
  if (!dashboardStore.realtimeData) return false
  const stats = dashboardStore.realtimeData.stats
  return stats && stats.riskFlights > 0
})

const updateCollapsed = (value) => {
  collapsed.value = value
}

const handleMenuClick = (key) => {
  activeMenu.value = key
  router.push(`/${key}`)
}

const handleRefresh = () => {
  dashboardStore.fetchRealtimeData()
  message.success('数据已刷新')
}

onMounted(() => {
  activeMenu.value = route.path.split('/')[1] || 'dashboard'
  dashboardStore.startAutoRefresh(30000)
})

onUnmounted(() => {
  dashboardStore.stopAutoRefresh()
})
</script>

<style scoped>
.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #3b82f6, #1d4ed8);
  color: white;
  font-weight: 600;
}

.logo-text {
  font-size: 18px;
  letter-spacing: 1px;
}

.header {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  background: #fff;
}

.header-left {
  display: flex;
  align-items: center;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.risk-tag {
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.6;
  }
}
</style>
