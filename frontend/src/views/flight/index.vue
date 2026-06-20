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
  EyeOutlined
} from '@vicons/antd'
import {
  getFlightPage,
  addFlight,
  updateFlight,
  deleteFlight,
  checkFlightDeicingStatus
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
    title: '安全检查',
    key: 'risk',
    width: 100,
    render: (row) => {
      const hasRisk = row.deicingRequired && !row.deicingCompleted && row.flightStatus === 'DEPARTED'
      return h('n-tag', { type: hasRisk ? 'error' : 'success', size: 'small' }, () => hasRisk ? '有风险' : '正常')
    }
  },
  {
    title: '操作',
    key: 'actions',
    width: 180,
    render: (row) => {
      return h('n-space', { size: 'small' }, () => [
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
