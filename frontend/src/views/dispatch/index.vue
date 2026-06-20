<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">调度管理</h2>
    </div>

    <div class="table-toolbar">
      <div class="table-toolbar-left">
        <n-input
        v-model:value="searchForm.dispatchNo" placeholder="搜索调度单号" clearable style="width: 200px;"
        @keyup.enter="handleSearch"
      >
        <template #prefix><n-icon><SearchOutlined /></n-icon></template>
      </n-input>
      <n-select v-model:value="searchForm.dispatchStatus" placeholder="调度状态" clearable style="width: 140px;" :options="statusOptions" />
      <n-button type="primary" @click="handleSearch">
        <template #icon><n-icon><SearchOutlined /></n-icon></template>
        查询
      </n-button>
      <n-button @click="handleReset">重置</n-button>
    </div>
    <div class="table-toolbar-right">
      <n-button type="primary" @click="handleAdd">
        <template #icon><n-icon><PlusOutlined /></n-icon></template>
        创建调度
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
    style="width: 600px;"
    @positive-click="handleSubmit"
    @negative-click="showModal = false"
  >
    <n-form ref="formRef" :model="formData" :rules="formRules" label-placement="left" label-width="120px">
      <n-form-item label="航班" path="flightId">
        <n-select
          v-model:value="formData.flightId" placeholder="请选择航班"
          :options="flightOptions" filterable
        />
      </n-form-item>
      <n-form-item label="除冰车" path="vehicleId">
        <n-select
          v-model:value="formData.vehicleId" placeholder="请选择车辆"
          :options="vehicleOptions" filterable
        />
      </n-form-item>
      <n-form-item label="液体批次" path="batchId">
        <n-select
          v-model:value="formData.batchId" placeholder="请选择液体批次"
          :options="batchOptions" filterable
        />
      </n-form-item>
      <n-form-item label="驾驶员" path="driverName">
        <n-input v-model:value="formData.driverName" placeholder="请输入驾驶员姓名" />
      </n-form-item>
      <n-form-item label="预计喷洒量(L)" path="estimatedSprayVolume">
        <n-input-number
          v-model:value="formData.estimatedSprayVolume" placeholder="请输入预计喷洒量"
          :min="0" style="width: 100%;"
        />
      </n-form-item>
      <n-form-item label="调度备注" path="remark">
        <n-input v-model:value="formData.remark" type="textarea" placeholder="请输入备注" />
      </n-form-item>
    </n-form>
  </n-modal>
</div>
</template>

<script setup>
import { ref, reactive, onMounted, h } from 'vue'
import { useMessage, useDialog, NTag } from 'naive-ui'
import { SearchOutlined, PlusOutlined } from '@vicons/antd'
import {
  getDispatchPage,
  addDispatch,
  updateDispatch,
  deleteDispatch,
  startDispatch,
  completeDispatch,
  cancelDispatch
} from '@/api/dispatch'
import { getFlightList } from '@/api/flight'
import { getVehicleList } from '@/api/vehicle'
import { getBatchList } from '@/api/batch'
import dayjs from 'dayjs'

const message = useMessage()
const dialog = useDialog()

const loading = ref(false)
const tableData = ref([])
const showModal = ref(false)
const isEdit = ref(false)
const modalTitle = ref('创建调度')
const formRef = ref(null)

const searchForm = reactive({
  dispatchNo: '',
  dispatchStatus: null,
  flightId: null
})

const pagination = reactive({
  page: 1, pageSize: 10, itemCount: 0,
  showSizePicker: true, pageSizes: [10, 20, 50],
  onChange: (page) => { pagination.page = page; fetchData() },
  onUpdatePageSize: (pageSize) => { pagination.pageSize = pageSize; pagination.page = 1; fetchData() }
})

const statusOptions = [
  { label: '待执行', value: 'PENDING' },
  { label: '进行中', value: 'IN_PROGRESS' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已取消', value: 'CANCELLED' }
]

const flightOptions = ref([])
const vehicleOptions = ref([])
const batchOptions = ref([])

const formData = reactive({
  id: null, flightId: null, vehicleId: null, batchId: null,
  driverName: '', estimatedSprayVolume: 500, remark: ''
})

const formRules = {
  flightId: { required: true, message: '请选择航班', trigger: 'change' },
  vehicleId: { required: true, message: '请选择车辆', trigger: 'change' },
  batchId: { required: true, message: '请选择液体批次', trigger: 'change' }
}

const columns = [
  { title: '调度单号', key: 'dispatchNo', width: 140 },
  { title: '航班号', key: 'flightNo', width: 100 },
  { title: '车辆编号', key: 'vehicleNo', width: 110 },
  { title: '批次号', key: 'batchNo', width: 130 },
  { title: '驾驶员', key: 'driverName', width: 100 },
  { title: '预计喷洒量(L)', key: 'estimatedSprayVolume', width: 120 },
  {
    title: '调度状态', key: 'dispatchStatus', width: 100,
    render: (row) => {
      const typeMap = { PENDING: 'warning', IN_PROGRESS: 'info', COMPLETED: 'success', CANCELLED: 'default' }
      const textMap = { PENDING: '待执行', IN_PROGRESS: '进行中', COMPLETED: '已完成', CANCELLED: '已取消' }
      return h(NTag, { type: typeMap[row.dispatchStatus] || 'default', size: 'small' }, () => textMap[row.dispatchStatus] || '未知')
    }
  },
  {
    title: '调度时间', key: 'dispatchTime', width: 160,
    render: (row) => row.dispatchTime ? dayjs(row.dispatchTime).format('YYYY-MM-DD HH:mm') : '--'
  },
  {
    title: '操作', key: 'actions', width: 220,
    render: (row) => {
      const btns = []
      if (row.dispatchStatus === 'PENDING') {
        btns.push(h('n-button', { size: 'small', type: 'success', onClick: () => handleStart(row) }, () => '开始'))
        btns.push(h('n-button', { size: 'small', type: 'error', onClick: () => handleCancel(row) }, () => '取消'))
      }
      if (row.dispatchStatus === 'IN_PROGRESS') {
        btns.push(h('n-button', { size: 'small', type: 'primary', onClick: () => handleComplete(row) }, () => '完成'))
      }
      btns.push(h('n-button', { size: 'small', onClick: () => handleEdit(row) }, () => '编辑'))
      btns.push(h('n-button', { size: 'small', type: 'error', onClick: () => handleDelete(row) }, () => '删除'))
      return h('n-space', { size: 'small' }, () => btns)
    }
  }
]

function fetchData() {
  loading.value = true
  getDispatchPage({
    pageNum: pagination.page, pageSize: pagination.pageSize,
    dispatchNo: searchForm.dispatchNo || undefined,
    dispatchStatus: searchForm.dispatchStatus || undefined,
    flightId: searchForm.flightId || undefined
  }).then(data => {
    tableData.value = data.records
    pagination.itemCount = data.total
  }).catch(err => message.error(err.message || '获取数据失败'))
    .finally(() => { loading.value = false })
}

function loadOptions() {
  getFlightList().then(data => {
    flightOptions.value = data.map(f => ({ label: f.flightNo, value: f.id }))
  })
  getVehicleList().then(data => {
    vehicleOptions.value = data.filter(v => v.vehicleStatus === 'IDLE').map(v => ({ label: v.vehicleNo, value: v.id }))
  })
  getBatchList().then(data => {
    batchOptions.value = data.filter(b => b.batchStatus === 'AVAILABLE').map(b => ({ label: b.batchNo, value: b.id }))
  })
}

function handleSearch() { pagination.page = 1; fetchData() }
function handleReset() { searchForm.dispatchNo = ''; searchForm.dispatchStatus = null; searchForm.flightId = null; pagination.page = 1; fetchData() }
function handlePageChange(page) { pagination.page = page; fetchData() }

function handleAdd() {
  isEdit.value = false
  modalTitle.value = '创建调度'
  resetForm()
  loadOptions()
  showModal.value = true
}

function handleEdit(row) {
  isEdit.value = true
  modalTitle.value = '编辑调度'
  loadOptions()
  Object.assign(formData, row)
  showModal.value = true
}

function handleDelete(row) {
  dialog.warning({
    title: '删除确认', content: `确定要删除调度 ${row.dispatchNo} 吗？`,
    positiveText: '确定', negativeText: '取消',
    onPositiveClick: () => {
      deleteDispatch(row.id).then(() => { message.success('删除成功'); fetchData() })
        .catch(err => message.error(err.message || '删除失败'))
    }
  })
}

function handleStart(row) {
  dialog.info({
    title: '开始确认', content: `确定要开始调度 ${row.dispatchNo} 吗？`,
    positiveText: '确定', negativeText: '取消',
    onPositiveClick: () => {
      startDispatch(row.id).then(() => { message.success('调度已开始'); fetchData() })
        .catch(err => message.error(err.message || '操作失败'))
    }
  })
}

function handleComplete(row) {
  dialog.success({
    title: '完成确认', content: `确定要完成调度 ${row.dispatchNo} 吗？`,
    positiveText: '确定', negativeText: '取消',
    onPositiveClick: () => {
      completeDispatch(row.id).then(() => { message.success('调度已完成'); fetchData() })
        .catch(err => message.error(err.message || '操作失败'))
    }
  })
}

function handleCancel(row) {
  dialog.warning({
    title: '取消确认', content: `确定要取消调度 ${row.dispatchNo} 吗？`,
    positiveText: '确定', negativeText: '取消',
    onPositiveClick: () => {
      cancelDispatch(row.id).then(() => { message.success('调度已取消'); fetchData() })
        .catch(err => message.error(err.message || '操作失败'))
    }
  })
}

function handleSubmit() {
  formRef.value?.validate((errors) => {
    if (!errors) {
      const promise = isEdit.value ? updateDispatch(formData) : addDispatch(formData)
      promise.then(() => {
        message.success(isEdit.value ? '更新成功' : '创建成功')
        showModal.value = false
        fetchData()
      }).catch(err => message.error(err.message || '操作失败'))
    }
  })
}

function resetForm() {
  Object.assign(formData, {
    id: null, flightId: null, vehicleId: null, batchId: null,
    driverName: '', estimatedSprayVolume: 500, remark: ''
  })
}

onMounted(() => { fetchData() })
</script>
