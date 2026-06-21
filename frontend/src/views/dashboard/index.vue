<template>
  <div class="dashboard-page">
    <div class="stats-grid">
      <div v-for="(stat, index) in statCards" :key="index" class="stat-card">
        <div class="stat-card-header">
          <span class="stat-card-title">{{ stat.title }}</span>
          <div class="stat-card-icon" :style="{ background: stat.color + '20', color: stat.color }">
            <n-icon size="24">
              <component :is="stat.icon" />
            </n-icon>
          </div>
        </div>
        <div class="stat-card-value" :style="{ color: stat.color }">
          {{ stat.value }}
        </div>
        <div class="stat-card-desc">{{ stat.desc }}</div>
      </div>
    </div>

    <div class="dashboard-content">
      <div class="dashboard-main">
        <div class="dashboard-section">
          <div class="dashboard-section-header">
            <span class="dashboard-section-title">今日航班除冰状态</span>
            <n-tag size="small" type="info" @click="handleRefresh">
              <template #icon>
                <n-icon size="12">
                  <ClockCircleOutlined />
                </n-icon>
              </template>
              每30秒自动刷新
            </n-tag>
          </div>
          <div v-if="loading" class="loading-container">
            <n-spin size="large" />
          </div>
          <div v-else-if="flightList.length === 0" class="empty-container">
            <n-empty description="暂无航班数据" />
          </div>
          <div v-else class="flight-list">
            <div
              v-for="flight in flightList"
              :key="flight.id"
              class="flight-card"
              :class="getFlightCardClass(flight)"
            >
              <div class="flight-card-header">
                <span class="flight-no">{{ flight.flightNo }}</span>
                <div class="flight-status">
                  <n-tag :type="getFlightStatusType(flight)" size="small">
                    {{ getFlightStatusText(flight) }}
                  </n-tag>
                  <span v-if="hasDeicingRisk(flight)" class="risk-badge">
                    <n-icon size="12" style="margin-right: 2px;">
                      <WarningFilled />
                    </n-icon>
                    有风险
                  </span>
                </div>
              </div>
              <div class="flight-info">
                <span><n-icon size="14"><AimOutlined /></n-icon> {{ flight.airline || '未知' }}</span>
                <span><n-icon size="14"><ClockCircleOutlined /></n-icon> {{ formatTime(flight.scheduledDepartureTime) }}</span>
                <span><n-icon size="14"><EnvironmentOutlined /></n-icon> {{ flight.standNo || '未分配' }}</span>
              </div>
              <div class="flight-deicing-info">
                <span v-if="flight.deicingRequired">
                  除冰: 
                  <n-tag size="small" :type="flight.deicingCompleted ? 'success' : 'warning'">
                    {{ flight.deicingCompleted ? '已完成' : '待除冰' }}
                  </n-tag>
                </span>
                <span v-else>
                  无需除冰
                </span>
              </div>
            </div>
          </div>
        </div>

        <div class="dashboard-section">
          <div class="dashboard-section-header">
            <span class="dashboard-section-title">进行中的喷洒作业</span>
          </div>
          <div v-if="inProgressSprays.length === 0" class="empty-container">
            <n-empty description="暂无进行中的喷洒作业" />
          </div>
          <div v-else class="spray-list">
            <div v-for="spray in inProgressSprays" :key="spray.id" class="spray-card">
              <div class="spray-header">
                <span class="spray-no">{{ spray.sprayNo }}</span>
                <n-tag type="info" size="small">进行中</n-tag>
              </div>
              <div class="spray-info">
                <span>航班: {{ spray.flightNo }}</span>
                <span>车辆: {{ spray.vehicleNo }}</span>
                <span>批次: {{ spray.batchNo }}</span>
                <span>驾驶员: {{ spray.driverName || '未知' }}</span>
              </div>
              <div class="spray-progress">
                <n-progress type="line" :percentage="getSprayProgress(spray)" status="info" />
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="dashboard-side">
        <div class="dashboard-section">
          <div class="dashboard-section-header">
            <span class="dashboard-section-title">车辆状态</span>
          </div>
          <div class="vehicle-status-list">
            <div v-for="vehicle in vehicleList" :key="vehicle.id" class="vehicle-item">
              <div class="vehicle-header">
                <span class="vehicle-no">{{ vehicle.vehicleNo }}</span>
                <n-tag :type="getVehicleStatusType(vehicle)" size="small">
                  {{ getVehicleStatusText(vehicle) }}
                </n-tag>
              </div>
              <div class="vehicle-info">
                <span>类型: {{ vehicle.vehicleType === 'TYPE1' ? '除冰车' : '防冰车' }}</span>
                <span>液体: {{ vehicle.currentFluidVolume || 0 }}L</span>
              </div>
            </div>
          </div>
        </div>

        <div class="dashboard-section">
          <div class="dashboard-section-header">
            <span class="dashboard-section-title">环保检查提醒</span>
          </div>
          <div v-if="openEnvChecks.length === 0" class="empty-container">
            <n-empty description="暂无待处理环保检查" />
          </div>
          <div v-else class="env-check-list">
            <div
              v-for="check in openEnvChecks"
              :key="check.id"
              class="env-check-item"
              :class="{ 'env-check-warning': check.checkResult === 'UNQUALIFIED' }"
            >
              <div class="env-check-header">
                <span class="env-check-no">{{ check.checkNo }}</span>
                <n-tag :type="getEnvCheckResultType(check)" size="small">
                  {{ getEnvCheckResultText(check) }}
                </n-tag>
              </div>
              <div class="env-check-info">
                <span>回收池: {{ check.poolNo || '未知' }}</span>
                <span>
                  浓度: {{ check.concentration || '--' }}%
                  <span v-if="check.standardConcentration && check.concentration > check.standardConcentration" class="text-danger">
                    (超标)
                  </span>
                </span>
              </div>
              <div v-if="check.recheckRequired" class="recheck-hint">
                <n-icon size="12" color="#f59e0b">
                  <WarningOutlined />
                </n-icon>
                需要复检
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useDashboardStore } from '@/store/dashboard'
import { useRouter } from 'vue-router'
import {
  DashboardOutlined,
  AimOutlined,
  CarOutlined,
  ExperimentOutlined,
  EnvironmentOutlined,
  WarningFilled,
  WarningOutlined,
  ClockCircleOutlined
} from '@vicons/antd'
import dayjs from 'dayjs'

const router = useRouter()
const dashboardStore = useDashboardStore()

const loading = computed(() => dashboardStore.loading)

const flightList = computed(() => {
  if (!dashboardStore.realtimeData || !dashboardStore.realtimeData.flights) return []
  return dashboardStore.realtimeData.flights.slice(0, 8)
})

const vehicleList = computed(() => {
  if (!dashboardStore.realtimeData || !dashboardStore.realtimeData.vehicles) return []
  return dashboardStore.realtimeData.vehicles.list || []
})

const inProgressSprays = computed(() => {
  if (!dashboardStore.realtimeData || !dashboardStore.realtimeData.sprays) return []
  return dashboardStore.realtimeData.sprays
})

const openEnvChecks = computed(() => {
  if (!dashboardStore.realtimeData || !dashboardStore.realtimeData.envChecks) return []
  return dashboardStore.realtimeData.envChecks
})

const statCards = computed(() => [
  {
    title: '今日航班',
    value: getStatValue('totalFlights', 0),
    desc: '架次',
    color: '#3b82f6',
    icon: AimOutlined
  },
  {
    title: '待除冰',
    value: getStatValue('pendingDeicing', 0),
    desc: '架次',
    color: '#f59e0b',
    icon: ClockCircleOutlined
  },
  {
    title: '除冰中',
    value: getStatValue('deicingInProgress', 0),
    desc: '架次',
    color: '#06b6d4',
    icon: DashboardOutlined
  },
  {
    title: '已完成',
    value: getStatValue('completedDeicing', 0),
    desc: '架次',
    color: '#10b981',
    icon: ExperimentOutlined
  },
  {
    title: '可用车辆',
    value: getStatValue('availableVehicles', 0),
    desc: '辆',
    color: '#6366f1',
    icon: CarOutlined
  },
  {
    title: '风险航班',
    value: getStatValue('riskFlights', 0),
    desc: '架次',
    color: '#ef4444',
    icon: WarningFilled
  }
])

function getStatValue(key, defaultValue) {
  if (!dashboardStore.realtimeData || !dashboardStore.realtimeData.stats) {
    return defaultValue
  }
  return dashboardStore.realtimeData.stats[key] || defaultValue
}

function formatTime(time) {
  if (!time) return '--'
  return dayjs(time).format('HH:mm')
}

function getFlightCardClass(flight) {
  if (hasDeicingRisk(flight)) return 'flight-card-danger'
  if (flight.deicingCompleted) return 'flight-card-success'
  if (flight.flightStatus === 'DEICING') return 'flight-card-warning'
  return ''
}

function getFlightStatusType(flight) {
  const map = {
    SCHEDULED: 'default',
    DEICING: 'info',
    DEICED: 'success',
    DEPARTED: 'success',
    CANCELLED: 'error'
  }
  return map[flight.flightStatus] || 'default'
}

function getFlightStatusText(flight) {
  const map = {
    SCHEDULED: '计划中',
    DEICING: '除冰中',
    DEICED: '已除冰',
    DEPARTED: '已起飞',
    CANCELLED: '已取消'
  }
  return map[flight.flightStatus] || '未知'
}

function hasDeicingRisk(flight) {
  if (!flight.deicingRequired) return false
  if (flight.deicingCompleted) return false
  if (flight.flightStatus === 'DEPARTED') return true
  if (flight.flightStatus === 'CANCELLED') return false
  return false
}

function getVehicleStatusType(vehicle) {
  const map = {
    IDLE: 'success',
    DISPATCHED: 'warning',
    SPRAYING: 'info',
    MAINTENANCE: 'error'
  }
  return map[vehicle.vehicleStatus] || 'default'
}

function getVehicleStatusText(vehicle) {
  const map = {
    IDLE: '空闲',
    DISPATCHED: '已调度',
    SPRAYING: '作业中',
    MAINTENANCE: '维护中'
  }
  return map[vehicle.vehicleStatus] || '未知'
}

function getSprayProgress(spray) {
  if (!spray || !spray.estimatedSprayVolume || !spray.sprayVolume) return 30
  return Math.min(100, Math.round((spray.sprayVolume / spray.estimatedSprayVolume) * 100))
}

function getEnvCheckResultType(check) {
  const map = {
    PENDING: 'default',
    QUALIFIED: 'success',
    UNQUALIFIED: 'error'
  }
  return map[check.checkResult] || 'default'
}

function getEnvCheckResultText(check) {
  const map = {
    PENDING: '待检测',
    QUALIFIED: '合格',
    UNQUALIFIED: '不合格'
  }
  return map[check.checkResult] || '未知'
}

function handleRefresh() {
  dashboardStore.fetchRealtimeData()
}

onMounted(() => {
  dashboardStore.fetchRealtimeData()
  dashboardStore.startAutoRefresh(30000)
})

onUnmounted(() => {
  dashboardStore.stopAutoRefresh()
})
</script>

<style scoped>
.dashboard-page {
  padding: 0;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}

.stat-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s, box-shadow 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.stat-card-title {
  font-size: 14px;
  color: #6b7280;
}

.stat-card-icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-card-value {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 4px;
}

.stat-card-desc {
  font-size: 13px;
  color: #9ca3af;
}

.dashboard-content {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 16px;
}

.dashboard-main,
.dashboard-side {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.dashboard-section {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.dashboard-section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e5e7eb;
}

.dashboard-section-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.flight-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 400px;
  overflow-y: auto;
}

.flight-card {
  background: #f9fafb;
  border-radius: 8px;
  padding: 12px;
  border-left: 4px solid #3b82f6;
  transition: all 0.2s;
}

.flight-card:hover {
  background: #f3f4f6;
}

.flight-card-warning {
  border-left-color: #f59e0b;
}

.flight-card-danger {
  border-left-color: #ef4444;
  background: #fef2f2;
}

.flight-card-success {
  border-left-color: #10b981;
}

.flight-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.flight-no {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.flight-status {
  display: flex;
  align-items: center;
  gap: 8px;
}

.risk-badge {
  background-color: #fee2e2;
  color: #991b1b;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 500;
  display: flex;
  align-items: center;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.7; }
}

.flight-info {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 8px;
}

.flight-info span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.flight-deicing-info {
  font-size: 13px;
  color: #4b5563;
}

.loading-container,
.empty-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40px 0;
}

.spray-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.spray-card {
  background: #f0f9ff;
  border-radius: 8px;
  padding: 12px;
  border: 1px solid #bae6fd;
}

.spray-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.spray-no {
  font-size: 14px;
  font-weight: 600;
  color: #0369a1;
}

.spray-info {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  font-size: 12px;
  color: #6b7280;
  margin-bottom: 10px;
}

.spray-progress {
  margin-top: 8px;
}

.vehicle-status-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.vehicle-item {
  background: #f9fafb;
  border-radius: 6px;
  padding: 10px;
}

.vehicle-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.vehicle-no {
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
}

.vehicle-info {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #6b7280;
}

.env-check-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.env-check-item {
  background: #f9fafb;
  border-radius: 6px;
  padding: 10px;
  border-left: 3px solid #10b981;
}

.env-check-warning {
  border-left-color: #ef4444;
  background: #fef2f2;
}

.env-check-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.env-check-no {
  font-size: 13px;
  font-weight: 500;
  color: #1f2937;
}

.env-check-info {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #6b7280;
}

.text-danger {
  color: #ef4444;
  font-weight: 500;
}

.recheck-hint {
  margin-top: 6px;
  font-size: 12px;
  color: #d97706;
  display: flex;
  align-items: center;
  gap: 4px;
}

@media (max-width: 1200px) {
  .dashboard-content {
    grid-template-columns: 1fr;
  }
}
</style>
