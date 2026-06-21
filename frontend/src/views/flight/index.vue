<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">航班管理</h2>
    </div>

    <div class="table-toolbar">
      <div class="table-toolbar-left">
        <n-input
          v-model:value="searchForm.flightNo"
          placeholder="搜索航班号"
          clearable
          style="width: 200px;"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <n-icon>
              <SearchOutlined />
            </n-icon>
          </template>
        </n-input>
        <n-select
          v-model:value="searchForm.flightStatus"
          placeholder="航班状态"
          clearable
          style="width: 140px;"
          :options="flightStatusOptions"
        />
        <n-button type="primary" @click="handleSearch">
          <template #icon>
            <n-icon>
              <SearchOutlined />
            </n-icon>
          </template>
          查询
        </n-button>
        <n-button @click="handleReset">重置</n-button>
      </div>
      <div class="table-toolbar-right">
        <n-button type="primary" @click="handleAdd">
          <template #icon>
            <n-icon>
              <PlusOutlined />
            </n-icon>
          </template>
          新增航班
        </n-button>
      </div>
    </div>

    <n-data-table
      :columns="columns"
      :data="tableData"
      :loading="loading"
      :pagination="pagination"
      :bordered="false"
      striped
      @update:page="handlePageChange"
    />

    <n-modal
      v-model:show="riskModalVisible"
      preset="card"
      title="航班风险备注"
      style="width: 560px"
      :mask-closable="true"
    >
      <div v-if="currentRiskFlight" class="risk-remark-panel">
        <div class="risk-panel-header">
          <span class="risk-panel-flight">{{ currentRiskFlight.flightNo }}</span>
          <n-tag :type="getFlightStatusType(currentRiskFlight)" size="small">
            {{ getFlightStatusText(currentRiskFlight) }}
          </n-tag>
        </div>

        <div class="risk-panel-section">
          <div class="risk-panel-section-title">当前风险状态</div>
          <div v-if="currentRiskFlight.hasRiskRemark" class="risk-status risk-status-active">
            <n-icon size="16" color="#ef4444">
              <WarningFilled />
            </n-icon>
            <span>存在风险备注</span>
          </div>
          <div v-else class="risk-status risk-status-normal">
            <n-icon size="16" color="#10b981">
              <CheckCircleFilled />
            </n-icon>
            <span>无风险备注</span>
          </div>
        </div>

        <div v-if="currentRiskFlight.hasRiskRemark" class="risk-panel-section">
          <div class="risk-panel-section-title">风险原因</div>
          <div class="risk-reason-detail">
            <p>{{ currentRiskFlight.riskReason }}</p>
            <div class="risk-meta">
              <span>标记人：{{ currentRiskFlight.riskMarkedBy || '未知' }}</span>
              <span>标记时间：{{ formatDateTime(currentRiskFlight.riskMarkTime) }}</span>
            </div>
          </div>
        </div>

        <div v-if="!currentRiskFlight.deicingCompleted && currentRiskFlight.flightStatus !== 'DEPARTED'" class="risk-panel-section">
          <div class="risk-panel-section-title">操作</div>
          <div class="risk-actions">
            <n-input
              v-model:value="riskReasonInput"
              type="textarea"
              :rows="3"
              placeholder="请输入风险原因..."
              maxlength="500"
              show-count
            />
            <div class="risk-action-buttons">
              <n-button
                v-if="!currentRiskFlight.hasRiskRemark"
                type="warning"
                :loading="riskSubmitting"
                @click="handleMarkRisk"
                :disabled="!riskReasonInput.trim()"
              >
                标记风险
              </n-button>
              <n-button
                v-else
                type="success"
                :loading="riskSubmitting"
                @click="handleClearRisk"
              >
                清除风险备注
              </n-button>
            </div>
          </div>
        </div>

        <div class="risk-panel-section">
          <div class="risk-panel-section-title">历史记录</div>
          <div v-if="riskHistoryList.length === 0" class="empty-history">
            暂无历史记录
          </div>
          <div v-else class="risk-history-list">
            <div
              v-for="(item, index) in riskHistoryList"
              :key="item.id"
              class="risk-history-item"
            >
              <div class="risk-history-dot"></div>
              <div class="risk-history-content">
                <div class="risk-history-reason">{{ item.riskReason }}</div>
                <div class="risk-history-meta">
                  <span>标记人：{{ item.markedBy || '未知' }}</span>
                  <span>标记时间：{{ formatDateTime(item.markTime) }}</span>
                </div>
                <div v-if="item.clearTime" class="risk-history-cleared">
                  <n-tag size="small" type="success">已清除</n-tag>
                  <span class="clear-info">
                    {{ item.clearedBy || '未知' }} · 
                    {{ getClearTypeText(item.clearType) }} · 
                    {{ formatDateTime(item.clearTime) }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </n-modal>

    <n-modal
      v-model:show="showModal"
      preset="dialog"
      :title="modalTitle"
      :mask-closable="false"
      positive-text="确定"
      negative-text="取消"
      @positive-click="handleSubmit"
      @negative-click="showModal = false"
    >
      <n-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-placement="left"
        label-width="100px"
      >
        <n-form-item label="航班号" path="flightNo">
          <n-input v-model:value="formData.flightNo" placeholder="请输入航班号" />
        </n-form-item>
        <n-form-item label="航空公司" path="airline">
          <n-input v-model:value="formData.airline" placeholder="请输入航空公司" />
        </n-form-item>
        <n-form-item label="机型" path="aircraftType">
          <n-input v-model:value="formData.aircraftType" placeholder="请输入机型" />
        </n-form-item>
        <n-form-item label="出发机场" path="departureAirport">
          <n-input v-model:value="formData.departureAirport" placeholder="请输入出发机场" />
        </n-form-item>
        <n-form-item label="到达机场" path="arrivalAirport">
          <n-input v-model:value="formData.arrivalAirport" placeholder="请输入到达机场" />
        </n-form-item>
        <n-form-item label="计划起飞时间" path="scheduledDepartureTime">
          <n-date-picker
            v-model:value="formData.scheduledDepartureTime"
            type="datetime"
            placeholder="请选择计划起飞时间"
            style="width: 100%;"
          />
        </n-form-item>
        <n-form-item label="停机位" path="standNo">
          <n-input v-model:value="formData.standNo" placeholder="请输入停机位" />
        </n-form-item>
        <n-form-item label="需要除冰" path="deicingRequired">
          <n-switch v-model:value="formData.deicingRequired" />
        </n-form-item>
        <n-form-item label="备注" path="remark">
          <n-input v-model:value="formData.remark" type="textarea" placeholder="请输入备注" />
        </n-form-item>
      </n-form>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useMessage, useDialog } from 'naive-ui'
import {
  SearchOutlined,
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  EyeOutlined,
  WarningFilled,
  CheckCircleFilled
} from '@vicons/antd'
import {
  getFlightPage,
  addFlight,
  updateFlight,
  deleteFlight,
  checkFlightDeicingStatus,
  markRiskRemark,
  clearRiskRemark,
  getRiskRemarkHistory
} from '@/api/flight'
import dayjs from 'dayjs'

const message = useMessage()
const dialog = useDialog()

const loading = ref(false)
const tableData = ref([])
const showModal = ref(false)
const isEdit = ref(false)
const modalTitle = ref('新增航班')
const formRef = ref(null)

const riskModalVisible = ref(false)
const currentRiskFlight = ref(null)
const riskReasonInput = ref('')
const riskHistoryList = ref([])
const riskSubmitting = ref(false)

const searchForm = reactive({
  flightNo: '',
  flightStatus: null,
  airline: ''
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50, 100],
  onChange: (page) => {
    pagination.page = page
    fetchData()
  },
  onUpdatePageSize: (pageSize) => {
    pagination.pageSize = pageSize
    pagination.page = 1
    fetchData()
  }
})

const flightStatusOptions = [
  { label: '计划中', value: 'SCHEDULED' },
  { label: '除冰中', value: 'DEICING' },
  { label: '已除冰', value: 'DEICED' },
  { label: '已起飞', value: 'DEPARTED' },
  { label: '已取消', value: 'CANCELLED' }
]

const formData = reactive({
  id: null,
  flightNo: '',
  airline: '',
  aircraftType: '',
  departureAirport: '',
  arrivalAirport: '',
  scheduledDepartureTime: null,
  standNo: '',
  deicingRequired: true,
  remark: ''
})

const formRules = {
  flightNo: {
    required: true,
    message: '请输入航班号',
    trigger: 'blur'
  }
}

const columns = [
  { title: '航班号', key: 'flightNo', width: 120 },
  { title: '航空公司', key: 'airline', width: 120 },
  { title: '机型', key: 'aircraftType', width: 100 },
  {
    title: '航线',
    key: 'route',
    width: 150,
    render: (row) => {
      return `${row.departureAirport || '--'} - ${row.arrivalAirport || '--'}`
    }
  },
  {
    title: '计划起飞',
    key: 'scheduledDepartureTime',
    width: 160,
    render: (row) => {
      return row.scheduledDepartureTime ? dayjs(row.scheduledDepartureTime).format('YYYY-MM-DD HH:mm') : '--'
    }
  },
  { title: '停机位', key: 'standNo', width: 100 },
  {
    title: '航班状态',
    key: 'flightStatus',
    width: 100,
    render: (row) => {
      const typeMap = {
        SCHEDULED: 'default',
        DEICING: 'info',
        DEICED: 'success',
        DEPARTED: 'success',
        CANCELLED: 'error'
      }
      const textMap = {
        SCHEDULED: '计划中',
        DEICING: '除冰中',
        DEICED: '已除冰',
        DEPARTED: '已起飞',
        CANCELLED: '已取消'
      }
      return h('n-tag', { type: typeMap[row.flightStatus] || 'default', size: 'small' }, () => textMap[row.flightStatus] || '未知')
    }
  },
  {
    title: '除冰状态',
    key: 'deicingStatus',
    width: 120,
    render: (row) => {
      if (!row.deicingRequired) {
        return h('n-tag', { type: 'default', size: 'small' }, () => '无需除冰')
      }
      return h('n-tag', { type: row.deicingCompleted ? 'success' : 'warning', size: 'small' }, () => row.deicingCompleted ? '已完成' : '待除冰')
    }
  },
  {
    title: '风险备注',
    key: 'riskRemark',
    width: 120,
    render: (row) => {
      if (row.hasRiskRemark) {
        return h('n-tag', { type: 'error', size: 'small' }, () => '有风险')
      }
      const hasRisk = row.deicingRequired && !row.deicingCompleted && row.flightStatus === 'DEPARTED'
      if (hasRisk) {
        return h('n-tag', { type: 'warning', size: 'small' }, () => '待检查')
      }
      return h('n-tag', { type: 'success', size: 'small' }, () => '正常')
    }
  },
  {
    title: '操作',
    key: 'actions',
    width: 240,
    render: (row) => {
      return h('n-space', { size: 'small' }, () => [
        h('n-button', {
          size: 'small',
          type: 'warning',
          onClick: () => handleRiskRemark(row)
        }, () => row.hasRiskRemark ? '查看风险' : '标记风险'),
        h('n-button', {
          size: 'small',
          type: 'info',
          onClick: () => handleCheckStatus(row)
        }, () => '检查状态'),
        h('n-button', {
          size: 'small',
          type: 'primary',
          onClick: () => handleEdit(row)
        }, () => '编辑'),
        h('n-button', {
          size: 'small',
          type: 'error',
          onClick: () => handleDelete(row)
        }, () => '删除')
      ])
    }
  }
]

function fetchData() {
  loading.value = true
  getFlightPage({
    pageNum: pagination.page,
    pageSize: pagination.pageSize,
    flightNo: searchForm.flightNo || undefined,
    flightStatus: searchForm.flightStatus || undefined,
    airline: searchForm.airline || undefined
  }).then(data => {
    tableData.value = data.records
    pagination.itemCount = data.total
  }).catch(err => {
    message.error(err.message || '获取数据失败')
  }).finally(() => {
    loading.value = false
  })
}

function handleSearch() {
  pagination.page = 1
  fetchData()
}

function handleReset() {
  searchForm.flightNo = ''
  searchForm.flightStatus = null
  searchForm.airline = ''
  pagination.page = 1
  fetchData()
}

function handlePageChange(page) {
  pagination.page = page
  fetchData()
}

function handleAdd() {
  isEdit.value = false
  modalTitle.value = '新增航班'
  resetForm()
  showModal.value = true
}

function handleEdit(row) {
  isEdit.value = true
  modalTitle.value = '编辑航班'
  Object.assign(formData, row)
  if (row.scheduledDepartureTime) {
    formData.scheduledDepartureTime = dayjs(row.scheduledDepartureTime).valueOf()
  }
  showModal.value = true
}

function handleDelete(row) {
  dialog.warning({
    title: '删除确认',
    content: `确定要删除航班 ${row.flightNo} 吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: () => {
      deleteFlight(row.id).then(() => {
        message.success('删除成功')
        fetchData()
      }).catch(err => {
        message.error(err.message || '删除失败')
      })
    }
  })
}

function handleCheckStatus(row) {
  checkFlightDeicingStatus(row.id).then(data => {
    if (data.hasRisk) {
      message.warning(data.riskMessage)
    } else {
      message.success('航班除冰状态正常')
    }
  }).catch(err => {
    message.error(err.message || '检查失败')
  })
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

function formatDateTime(time) {
  if (!time) return '--'
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

function getClearTypeText(clearType) {
  const map = {
    MANUAL: '手动清除',
    DEICING_COMPLETED: '除冰完成自动清除'
  }
  return map[clearType] || '未知'
}

function handleRiskRemark(row) {
  currentRiskFlight.value = { ...row }
  riskReasonInput.value = ''
  riskHistoryList.value = []
  riskModalVisible.value = true
  loadRiskHistory(row.id)
}

async function loadRiskHistory(flightId) {
  try {
    const data = await getRiskRemarkHistory(flightId)
    riskHistoryList.value = data || []
  } catch (error) {
    console.error('获取风险备注历史失败:', error)
  }
}

async function handleMarkRisk() {
  if (!currentRiskFlight.value || !riskReasonInput.value.trim()) return
  
  riskSubmitting.value = true
  try {
    await markRiskRemark(currentRiskFlight.value.id, {
      riskReason: riskReasonInput.value.trim(),
      operator: '机务调度员'
    })
    message.success('风险备注标记成功')
    currentRiskFlight.value.hasRiskRemark = true
    currentRiskFlight.value.riskReason = riskReasonInput.value.trim()
    currentRiskFlight.value.riskMarkedBy = '机务调度员'
    currentRiskFlight.value.riskMarkTime = new Date().toISOString()
    riskReasonInput.value = ''
    loadRiskHistory(currentRiskFlight.value.id)
    fetchData()
  } catch (error) {
    message.error('标记风险备注失败：' + (error.message || '未知错误'))
  } finally {
    riskSubmitting.value = false
  }
}

async function handleClearRisk() {
  if (!currentRiskFlight.value) return
  
  riskSubmitting.value = true
  try {
    await clearRiskRemark(currentRiskFlight.value.id, {
      operator: '机务调度员',
      clearType: 'MANUAL'
    })
    message.success('风险备注已清除')
    currentRiskFlight.value.hasRiskRemark = false
    currentRiskFlight.value.riskClearBy = '机务调度员'
    currentRiskFlight.value.riskClearTime = new Date().toISOString()
    loadRiskHistory(currentRiskFlight.value.id)
    fetchData()
  } catch (error) {
    message.error('清除风险备注失败：' + (error.message || '未知错误'))
  } finally {
    riskSubmitting.value = false
  }
}

function handleSubmit() {
  formRef.value?.validate((errors) => {
    if (!errors) {
      const submitData = { ...formData }
      if (submitData.scheduledDepartureTime) {
        submitData.scheduledDepartureTime = dayjs(submitData.scheduledDepartureTime).toISOString()
      }

      const promise = isEdit.value ? updateFlight(submitData) : addFlight(submitData)
      promise.then(() => {
        message.success(isEdit.value ? '更新成功' : '新增成功')
        showModal.value = false
        fetchData()
      }).catch(err => {
        message.error(err.message || '操作失败')
      })
    }
  })
}

function resetForm() {
  formData.id = null
  formData.flightNo = ''
  formData.airline = ''
  formData.aircraftType = ''
  formData.departureAirport = ''
  formData.arrivalAirport = ''
  formData.scheduledDepartureTime = null
  formData.standNo = ''
  formData.deicingRequired = true
  formData.remark = ''
}

onMounted(() => {
  fetchData()
})

import { h } from 'vue'
import { NTag } from 'naive-ui'
</script>

<style scoped>
.risk-remark-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.risk-panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 12px;
  border-bottom: 1px solid #e5e7eb;
}

.risk-panel-flight {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.risk-panel-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.risk-panel-section-title {
  font-size: 14px;
  font-weight: 600;
  color: #374151;
}

.risk-status {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  border-radius: 6px;
  font-size: 14px;
}

.risk-status-active {
  background: #fef2f2;
  color: #991b1b;
}

.risk-status-normal {
  background: #ecfdf5;
  color: #065f46;
}

.risk-reason-detail {
  padding: 12px;
  background: #f9fafb;
  border-radius: 6px;
  border-left: 3px solid #ef4444;
}

.risk-reason-detail p {
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #1f2937;
  line-height: 1.5;
}

.risk-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #6b7280;
}

.risk-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.risk-action-buttons {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.empty-history {
  padding: 20px;
  text-align: center;
  color: #9ca3af;
  font-size: 13px;
}

.risk-history-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 300px;
  overflow-y: auto;
}

.risk-history-item {
  display: flex;
  gap: 12px;
  position: relative;
}

.risk-history-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #f59e0b;
  flex-shrink: 0;
  margin-top: 4px;
}

.risk-history-item::after {
  content: '';
  position: absolute;
  left: 4px;
  top: 16px;
  width: 2px;
  height: calc(100% - 8px);
  background: #e5e7eb;
}

.risk-history-item:last-child::after {
  display: none;
}

.risk-history-content {
  flex: 1;
  padding-bottom: 12px;
}

.risk-history-reason {
  font-size: 13px;
  color: #1f2937;
  margin-bottom: 4px;
  line-height: 1.4;
}

.risk-history-meta {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #6b7280;
  margin-bottom: 6px;
}

.risk-history-cleared {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #059669;
}

.clear-info {
  color: #6b7280;
}
</style>
