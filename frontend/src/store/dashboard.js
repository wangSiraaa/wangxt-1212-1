import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getRealtimeStatus } from '@/api/dashboard'

export const useDashboardStore = defineStore('dashboard', () => {
  const realtimeData = ref(null)
  const loading = ref(false)
  let refreshTimer = null

  const fetchRealtimeData = async () => {
    loading.value = true
    try {
      const data = await getRealtimeStatus()
      realtimeData.value = data
    } catch (error) {
      console.error('获取实时数据失败:', error)
    } finally {
      loading.value = false
    }
  }

  const startAutoRefresh = (interval = 30000) => {
    stopAutoRefresh()
    fetchRealtimeData()
    refreshTimer = setInterval(fetchRealtimeData, interval)
  }

  const stopAutoRefresh = () => {
    if (refreshTimer) {
      clearInterval(refreshTimer)
      refreshTimer = null
    }
  }

  return {
    realtimeData,
    loading,
    fetchRealtimeData,
    startAutoRefresh,
    stopAutoRefresh
  }
})
